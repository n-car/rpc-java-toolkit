package com.carpanese.rpc.server;

import com.carpanese.rpc.core.*;
import com.carpanese.rpc.server.logging.RpcLogger;
import com.carpanese.rpc.server.middleware.MiddlewareManager;
import com.google.gson.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * JSON-RPC 2.0 Server Endpoint
 * 
 * Main class for handling JSON-RPC requests and managing method registration.
 * Thread-safe for concurrent requests.
 */
public class RpcEndpoint {
    private final Map<String, MethodConfig> methods = new ConcurrentHashMap<>();
    private final RpcOptions options;
    private final RpcLogger logger;
    private final MiddlewareManager middleware;
    private final Object context;
    private final Gson gson;
    private final String introspectionPrefix;
    private boolean isInternalRegistration = false;

    /**
     * Create a new RPC endpoint with default options
     */
    public RpcEndpoint() {
        this(null, null);
    }

    /**
     * Create a new RPC endpoint with context
     * 
     * @param context Server context object passed to handlers
     */
    public RpcEndpoint(Object context) {
        this(context, null);
    }

    /**
     * Create a new RPC endpoint with context and options
     * 
     * @param context Server context object
     * @param options Configuration options
     */
    public RpcEndpoint(Object context, RpcOptions options) {
        this.context = context;
        this.options = options != null ? options : new RpcOptions();
        this.introspectionPrefix = this.options.getIntrospectionPrefix();
        
        // Initialize logger
        if (this.options.isEnableLogging()) {
            this.logger = new RpcLogger(this.options.getLoggerOptions());
            this.logger.info("RpcEndpoint initialized");
        } else {
            this.logger = null;
        }

        // Initialize middleware
        if (this.options.isEnableMiddleware()) {
            this.middleware = new MiddlewareManager();
        } else {
            this.middleware = null;
        }

        // Initialize Gson - Safe mode is handled by RpcSerializer
        this.gson = new GsonBuilder().create();

        // Register introspection methods if enabled
        if (this.options.isEnableIntrospection()) {
            registerIntrospectionMethods();
        }
    }

    /**
     * Register an RPC method
     * 
     * @param name Method name
     * @param handler Method handler function
     * @return this for method chaining
     */
    public RpcEndpoint addMethod(String name, RpcMethodHandler handler) {
        return addMethod(name, handler, null);
    }

    /**
     * Register an RPC method with configuration
     * 
     * @param name Method name
     * @param handler Method handler function
     * @param config Method configuration (schema, exposeSchema, description)
     * @return this for method chaining
     */
    public RpcEndpoint addMethod(String name, RpcMethodHandler handler, MethodConfig config) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Method name cannot be empty");
        }

        // Prevent users from registering introspection methods
        if (name.startsWith(introspectionPrefix + ".") && !isInternalRegistration) {
            throw new IllegalArgumentException(
                "Method names starting with '" + introspectionPrefix + ".' are reserved for RPC introspection"
            );
        }

        if (methods.containsKey(name)) {
            throw new IllegalArgumentException("Method '" + name + "' is already registered");
        }

        MethodConfig methodConfig = config != null ? config : new MethodConfig();
        methodConfig.setName(name);
        methodConfig.setHandler(handler);

        methods.put(name, methodConfig);

        if (logger != null) {
            logger.debug("Method registered: " + name);
        }

        return this;
    }

    /**
     * Remove a registered method
     * 
     * @param name Method name
     * @return this for method chaining
     */
    public RpcEndpoint removeMethod(String name) {
        methods.remove(name);
        
        if (logger != null) {
            logger.debug("Method removed: " + name);
        }

        return this;
    }

    /**
     * Get all registered method names
     * 
     * @return Array of method names
     */
    public String[] listMethods() {
        return methods.keySet().toArray(new String[0]);
    }

    /**
     * Get method configuration
     * 
     * @param name Method name
     * @return Method configuration or null if not found
     */
    public MethodConfig getMethod(String name) {
        return methods.get(name);
    }

    /**
     * Get middleware manager
     * 
     * @return Middleware manager or null if disabled
     */
    public MiddlewareManager getMiddleware() {
        return middleware;
    }

    /**
     * Get logger instance
     * 
     * @return Logger or null if disabled
     */
    public RpcLogger getLogger() {
        return logger;
    }

    /**
     * Handle a JSON-RPC request
     * 
     * @param jsonRequest JSON-RPC request as string
     * @return JSON-RPC response as string
     */
    public String handleRequest(String jsonRequest) {
        try {
            // Detect batch vs single request
            JsonElement element = JsonParser.parseString(jsonRequest);
            
            if (element.isJsonArray()) {
                if (!options.isEnableBatch()) {
                    RpcResponse error = createErrorResponse(null, 
                        new RpcException(RpcError.INVALID_REQUEST, "Batch requests are not enabled"));
                    return gson.toJson(error);
                }

                JsonArray requests = element.getAsJsonArray();
                
                if (requests.size() == 0) {
                    RpcResponse error = createErrorResponse(null,
                        new RpcException(RpcError.INVALID_REQUEST, "Invalid batch request"));
                    return gson.toJson(error);
                }

                if (options.getMaxBatchSize() > 0 && requests.size() > options.getMaxBatchSize()) {
                    RpcResponse error = createErrorResponse(null,
                        new RpcException(RpcError.INVALID_REQUEST, 
                            "Batch size exceeds maximum of " + options.getMaxBatchSize()));
                    return gson.toJson(error);
                }

                return handleBatchRequest(requests);
            } else {
                RpcRequest request = gson.fromJson(element, RpcRequest.class);
                RpcResponse response = handleSingleRequest(request);
                
                // Notifications return no response
                if (request.getId() == null) {
                    return "";
                }

                return gson.toJson(response);
            }
        } catch (JsonSyntaxException e) {
            if (logger != null) {
                logger.error("Parse error", null, e);
            }
            RpcResponse error = createErrorResponse(null,
                new RpcException(RpcError.PARSE_ERROR, "Invalid JSON", e));
            return gson.toJson(error);
        } catch (Exception e) {
            if (logger != null) {
                logger.error("Unexpected error", null, e);
            }
            RpcResponse error = createErrorResponse(null,
                new RpcException(RpcError.INTERNAL_ERROR, "Internal error", e));
            return gson.toJson(error);
        }
    }

    /**
     * Handle a single RPC request
     */
    private RpcResponse handleSingleRequest(RpcRequest request) {
        try {
            // Validate request
            validateRequest(request);

            // Execute middleware before
            if (middleware != null) {
                middleware.executeBefore(request, context);
            }

            // Find method
            MethodConfig methodConfig = methods.get(request.getMethod());
            if (methodConfig == null) {
                throw new RpcException(RpcError.METHOD_NOT_FOUND, 
                    "Method not found: " + request.getMethod());
            }

            // Execute method
            JsonElement result = methodConfig.getHandler().handle(request.getParams(), context);

            // Execute middleware after
            if (middleware != null) {
                middleware.executeAfter(request, result, context);
            }

            if (logger != null) {
                logger.debug("Method executed successfully: " + request.getMethod());
            }

            // Create response
            return new RpcResponse(result, request.getId());

        } catch (RpcException e) {
            if (logger != null) {
                logger.warn("RPC error: " + e.getMessage());
            }
            return new RpcResponse(e.getError(), request.getId());
        } catch (Exception e) {
            if (logger != null) {
                logger.error("Internal error processing request", null, e);
            }
            
            String message = options.isSanitizeErrors() ? "Internal error" : e.getMessage();
            RpcError error = new RpcError(RpcError.INTERNAL_ERROR, message);
            return new RpcResponse(error, request.getId());
        }
    }

    /**
     * Handle batch request
     */
    private String handleBatchRequest(JsonArray requests) {
        JsonArray responses = new JsonArray();

        if (logger != null) {
            logger.info("Processing batch request with " + requests.size() + " items");
        }

        for (JsonElement element : requests) {
            try {
                RpcRequest request = gson.fromJson(element, RpcRequest.class);
                RpcResponse response = handleSingleRequest(request);
                
                // Don't include notification responses
                if (request.getId() != null) {
                    responses.add(gson.toJsonTree(response));
                }
            } catch (Exception e) {
                if (logger != null) {
                    logger.error("Error in batch request", null, e);
                }
                RpcResponse error = createErrorResponse(null,
                    new RpcException(RpcError.INVALID_REQUEST, "Invalid request in batch"));
                responses.add(gson.toJsonTree(error));
            }
        }

        return gson.toJson(responses);
    }

    /**
     * Validate RPC request
     */
    private void validateRequest(RpcRequest request) throws RpcException {
        if (!"2.0".equals(request.getJsonrpc())) {
            throw new RpcException(RpcError.INVALID_REQUEST, 
                "Invalid JSON-RPC version. Must be '2.0'");
        }

        if (request.getMethod() == null || request.getMethod().trim().isEmpty()) {
            throw new RpcException(RpcError.INVALID_REQUEST, "Method name is required");
        }
    }

    /**
     * Create error response
     */
    private RpcResponse createErrorResponse(Object id, RpcException exception) {
        return new RpcResponse(exception.getError(), id);
    }

    /**
     * Register introspection methods (__rpc.*)
     */
    private void registerIntrospectionMethods() {
        isInternalRegistration = true;

        // __rpc.listMethods - List all user methods
        addMethod(introspectionPrefix + ".listMethods", (params, ctx) -> {
            JsonArray methods = new JsonArray();
            for (String name : this.methods.keySet()) {
                if (!name.startsWith(introspectionPrefix + ".")) {
                    methods.add(name);
                }
            }
            return methods;
        }, new MethodConfig()
            .withDescription("List all available RPC methods")
            .withExposeSchema(true));

        // __rpc.describe - Get schema and description of specific method
        addMethod(introspectionPrefix + ".describe", (params, ctx) -> {
            if (params == null || !params.isJsonObject()) {
                throw new RpcException(RpcError.INVALID_PARAMS, "Method name required");
            }

            JsonObject paramsObj = params.getAsJsonObject();
            if (!paramsObj.has("method")) {
                throw new RpcException(RpcError.INVALID_PARAMS, "Method name required");
            }

            String methodName = paramsObj.get("method").getAsString();
            MethodConfig config = methods.get(methodName);
            
            if (config == null) {
                throw new RpcException(RpcError.METHOD_NOT_FOUND, "Method not found: " + methodName);
            }

            JsonObject result = new JsonObject();
            result.addProperty("name", config.getName());
            result.add("schema", config.getSchema());
            result.addProperty("description", config.getDescription() != null ? config.getDescription() : "");
            return result;
        }, new MethodConfig()
            .withDescription("Get schema and description of a specific method")
            .withExposeSchema(true));

        // __rpc.describeAll - Get all methods with public schemas
        addMethod(introspectionPrefix + ".describeAll", (params, ctx) -> {
            JsonArray result = new JsonArray();
            
            for (MethodConfig config : methods.values()) {
                if (!config.getName().startsWith(introspectionPrefix + ".") && config.isExposeSchema()) {
                    JsonObject methodInfo = new JsonObject();
                    methodInfo.addProperty("name", config.getName());
                    methodInfo.add("schema", config.getSchema());
                    methodInfo.addProperty("description", 
                        config.getDescription() != null ? config.getDescription() : "");
                    result.add(methodInfo);
                }
            }
            
            return result;
        }, new MethodConfig()
            .withDescription("List all methods with public schemas")
            .withExposeSchema(true));

        // __rpc.version - Get toolkit version
        addMethod(introspectionPrefix + ".version", (params, ctx) -> {
            JsonObject result = new JsonObject();
            result.addProperty("toolkit", "rpc-java-toolkit");
            result.addProperty("version", "1.0.0");
            result.addProperty("javaVersion", System.getProperty("java.version"));
            return result;
        }, new MethodConfig()
            .withDescription("Get RPC toolkit version information")
            .withExposeSchema(true));

        // __rpc.capabilities - Get server capabilities
        addMethod(introspectionPrefix + ".capabilities", (params, ctx) -> {
            long methodCount = methods.keySet().stream()
                .filter(name -> !name.startsWith(introspectionPrefix + "."))
                .count();

            JsonObject result = new JsonObject();
            result.addProperty("batch", options.isEnableBatch());
            result.addProperty("introspection", true);
            result.addProperty("validation", options.isEnableValidation());
            result.addProperty("middleware", options.isEnableMiddleware());
            result.addProperty("safeMode", options.isSafeEnabled());
            result.addProperty("methodCount", methodCount);
            return result;
        }, new MethodConfig()
            .withDescription("Get server capabilities and configuration")
            .withExposeSchema(true));

        isInternalRegistration = false;
    }
}
