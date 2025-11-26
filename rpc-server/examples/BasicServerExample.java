package com.carpanese.rpc.server;

import com.carpanese.rpc.server.logging.RpcLogFormat;
import com.carpanese.rpc.server.logging.RpcLogLevel;
import com.carpanese.rpc.server.logging.RpcLoggerOptions;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * Basic example demonstrating RPC Server usage
 */
public class BasicServerExample {
    
    public static void main(String[] args) {
        // Create context object
        Context context = new Context();
        context.database = new java.util.HashMap<>();
        
        // Configure options
        RpcOptions options = new RpcOptions()
            .setEnableBatch(true)
            .setMaxBatchSize(50)
            .setEnableLogging(true)
            .setEnableIntrospection(true)
            .setLoggerOptions(new RpcLoggerOptions()
                .setLevel(RpcLogLevel.INFO)
                .setFormat(RpcLogFormat.TEXT)
                .setIncludeTimestamp(true));
        
        // Create endpoint
        RpcEndpoint endpoint = new RpcEndpoint(context, options);
        
        // Register Calculator methods with schema
        endpoint.addMethod("calculator.add", (params, ctx) -> {
            JsonObject p = params.getAsJsonObject();
            int a = p.get("a").getAsInt();
            int b = p.get("b").getAsInt();
            return new JsonPrimitive(a + b);
        }, new MethodConfig()
            .withDescription("Add two numbers")
            .withExposeSchema(true));
        
        endpoint.addMethod("calculator.subtract", (params, ctx) -> {
            JsonObject p = params.getAsJsonObject();
            int a = p.get("a").getAsInt();
            int b = p.get("b").getAsInt();
            return new JsonPrimitive(a - b);
        }, new MethodConfig()
            .withDescription("Subtract two numbers")
            .withExposeSchema(true));
        
        // Register User methods
        endpoint.addMethod("user.get", (params, ctx) -> {
            Context c = (Context) ctx;
            JsonObject p = params.getAsJsonObject();
            int id = p.get("id").getAsInt();
            
            String user = c.database.get("user_" + id);
            if (user == null) {
                throw new com.carpanese.rpc.core.RpcException(
                    com.carpanese.rpc.core.RpcError.INVALID_PARAMS,
                    "User " + id + " not found"
                );
            }
            
            JsonObject result = new JsonObject();
            result.addProperty("id", id);
            result.addProperty("name", user);
            return result;
        }, new MethodConfig()
            .withDescription("Get user by ID")
            .withExposeSchema(true));
        
        endpoint.addMethod("user.create", (params, ctx) -> {
            Context c = (Context) ctx;
            JsonObject p = params.getAsJsonObject();
            int id = p.get("id").getAsInt();
            String name = p.get("name").getAsString();
            
            c.database.put("user_" + id, name);
            
            JsonObject result = new JsonObject();
            result.addProperty("id", id);
            result.addProperty("name", name);
            return result;
        }, new MethodConfig()
            .withDescription("Create a new user")
            .withExposeSchema(true));
        
        // System methods
        endpoint.addMethod("system.ping", (params, ctx) -> {
            return new JsonPrimitive("pong");
        });
        
        endpoint.addMethod("system.time", (params, ctx) -> {
            return new JsonPrimitive(System.currentTimeMillis());
        });
        
        // Test single request
        System.out.println("=== Single Request Example ===");
        String request1 = "{\"jsonrpc\":\"2.0\",\"method\":\"calculator.add\",\"params\":{\"a\":5,\"b\":3},\"id\":1}";
        String response1 = endpoint.handleRequest(request1);
        System.out.println("Request: " + request1);
        System.out.println("Response: " + response1);
        System.out.println();
        
        // Test batch request
        System.out.println("=== Batch Request Example ===");
        String batchRequest = "[" +
            "{\"jsonrpc\":\"2.0\",\"method\":\"calculator.add\",\"params\":{\"a\":10,\"b\":5},\"id\":1}," +
            "{\"jsonrpc\":\"2.0\",\"method\":\"calculator.subtract\",\"params\":{\"a\":10,\"b\":3},\"id\":2}," +
            "{\"jsonrpc\":\"2.0\",\"method\":\"system.ping\",\"params\":{},\"id\":3}" +
            "]";
        String batchResponse = endpoint.handleRequest(batchRequest);
        System.out.println("Request: " + batchRequest);
        System.out.println("Response: " + batchResponse);
        System.out.println();
        
        // Test introspection
        System.out.println("=== Introspection Example ===");
        String introspectionRequest = "{\"jsonrpc\":\"2.0\",\"method\":\"__rpc.listMethods\",\"params\":{},\"id\":4}";
        String introspectionResponse = endpoint.handleRequest(introspectionRequest);
        System.out.println("Request: " + introspectionRequest);
        System.out.println("Response: " + introspectionResponse);
        System.out.println();
        
        System.out.println("=== Capabilities Example ===");
        String capabilitiesRequest = "{\"jsonrpc\":\"2.0\",\"method\":\"__rpc.capabilities\",\"params\":{},\"id\":5}";
        String capabilitiesResponse = endpoint.handleRequest(capabilitiesRequest);
        System.out.println("Request: " + capabilitiesRequest);
        System.out.println("Response: " + capabilitiesResponse);
    }
    
    static class Context {
        java.util.Map<String, String> database;
    }
}
