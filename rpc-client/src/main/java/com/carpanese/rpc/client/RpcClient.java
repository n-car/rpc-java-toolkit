package com.carpanese.rpc.client;

import com.carpanese.rpc.core.*;
import com.google.gson.JsonElement;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * JSON-RPC 2.0 HTTP Client
 * 
 * Thread-safe client for making RPC calls to remote servers.
 * Compatible with Express, PHP, .NET, Arduino, and Node-RED RPC servers.
 * 
 * Example usage:
 * <pre>
 * RpcClient client = new RpcClient("http://localhost:3000/rpc");
 * JsonElement result = client.call("myMethod", params);
 * </pre>
 */
public class RpcClient implements AutoCloseable {
    
    private static final Logger log = LoggerFactory.getLogger(RpcClient.class);
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    
    private final String url;
    private final OkHttpClient httpClient;
    private final RpcSerializer serializer;
    private final Map<String, String> defaultHeaders;
    private final boolean safeMode;
    
    /**
     * Creates a new RPC client with custom configuration
     * 
     * @param url Server URL (e.g., "http://localhost:3000/rpc")
     * @param config Client configuration
     */
    public RpcClient(String url, RpcClientConfig config) {
        this.url = url;
        this.safeMode = config.isSafeMode();
        this.serializer = new RpcSerializer(safeMode);
        this.defaultHeaders = new HashMap<>(config.getHeaders());
        
        // Add safe mode header if enabled
        if (safeMode) {
            this.defaultHeaders.put("X-RPC-Safe-Enabled", "true");
        }
        
        // Build HTTP client
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(config.getConnectTimeout())
                .readTimeout(config.getReadTimeout())
                .writeTimeout(config.getWriteTimeout())
                .build();
        
        log.info("RPC Client initialized: url={}, safeMode={}", url, safeMode);
    }
    
    /**
     * Creates a new RPC client with default configuration
     */
    public RpcClient(String url) {
        this(url, new RpcClientConfig());
    }
    
    /**
     * Call a remote method
     * 
     * @param method Method name
     * @param params Method parameters (can be null)
     * @return Result as JsonElement
     * @throws RpcException If the call fails or returns an error
     * @throws IOException If a network error occurs
     */
    public JsonElement call(String method, JsonElement params) throws RpcException, IOException {
        return call(method, params, System.currentTimeMillis());
    }
    
    /**
     * Call a remote method with custom request ID
     * 
     * @param method Method name
     * @param params Method parameters
     * @param id Request ID
     * @return Result as JsonElement
     * @throws RpcException If the call fails or returns an error
     * @throws IOException If a network error occurs
     */
    public JsonElement call(String method, JsonElement params, Object id) 
            throws RpcException, IOException {
        
        RpcRequest request = new RpcRequest(method, params, id);
        RpcResponse response = execute(request);
        
        if (response.isError()) {
            throw new RpcException(response.getError());
        }
        
        return response.getResult();
    }
    
    /**
     * Send a notification (no response expected)
     * 
     * @param method Method name
     * @param params Method parameters
     * @throws IOException If a network error occurs
     */
    public void notify(String method, JsonElement params) throws IOException {
        RpcRequest request = RpcRequest.notification(method, params);
        
        String requestJson = serializer.toJson(request);
        log.debug("Sending notification: {}", requestJson);
        
        RequestBody body = RequestBody.create(requestJson, JSON);
        Request httpRequest = buildHttpRequest(body, new HashMap<>());
        
        try (Response httpResponse = httpClient.newCall(httpRequest).execute()) {
            if (!httpResponse.isSuccessful()) {
                log.warn("Notification failed: {} {}", httpResponse.code(), httpResponse.message());
            }
        }
    }
    
    /**
     * Execute an RPC request and return the response
     * 
     * @param request RPC request
     * @return RPC response
     * @throws RpcException If the response is invalid
     * @throws IOException If a network error occurs
     */
    private RpcResponse execute(RpcRequest request) throws RpcException, IOException {
        String requestJson = serializer.toJson(request);
        log.debug("Sending request: {}", requestJson);
        
        RequestBody body = RequestBody.create(requestJson, JSON);
        Request httpRequest = buildHttpRequest(body, new HashMap<>());
        
        try (Response httpResponse = httpClient.newCall(httpRequest).execute()) {
            if (!httpResponse.isSuccessful()) {
                throw new RpcException(
                    RpcError.INTERNAL_ERROR,
                    "HTTP error: " + httpResponse.code() + " " + httpResponse.message()
                );
            }
            
            ResponseBody responseBody = httpResponse.body();
            if (responseBody == null) {
                throw new RpcException(RpcError.INTERNAL_ERROR, "Empty response body");
            }
            
            String responseJson = responseBody.string();
            log.debug("Received response: {}", responseJson);
            
            // Check server safe mode compatibility
            if (safeMode) {
                String serverSafeHeader = httpResponse.header("X-RPC-Safe-Enabled");
                if (serverSafeHeader == null || !serverSafeHeader.equals("true")) {
                    throw new RpcException(
                        RpcError.INTERNAL_ERROR,
                        "Client has safe mode enabled but server does not support it"
                    );
                }
            }
            
            RpcResponse response = serializer.fromJson(responseJson, RpcResponse.class);
            return response;
        }
    }
    
    /**
     * Build HTTP request with headers
     */
    private Request buildHttpRequest(RequestBody body, Map<String, String> extraHeaders) {
        Request.Builder builder = new Request.Builder()
                .url(url)
                .post(body);
        
        // Add default headers
        for (Map.Entry<String, String> header : defaultHeaders.entrySet()) {
            builder.addHeader(header.getKey(), header.getValue());
        }
        
        // Add extra headers
        for (Map.Entry<String, String> header : extraHeaders.entrySet()) {
            builder.addHeader(header.getKey(), header.getValue());
        }
        
        return builder.build();
    }
    
    /**
     * Set authentication token
     */
    public void setAuthToken(String token) {
        defaultHeaders.put("Authorization", "Bearer " + token);
    }
    
    /**
     * Clear authentication token
     */
    public void clearAuth() {
        defaultHeaders.remove("Authorization");
    }
    
    @Override
    public void close() {
        httpClient.dispatcher().executorService().shutdown();
        httpClient.connectionPool().evictAll();
        log.info("RPC Client closed");
    }
    
    /**
     * Get the server URL
     */
    public String getUrl() {
        return url;
    }
    
    /**
     * Check if safe mode is enabled
     */
    public boolean isSafeMode() {
        return safeMode;
    }
}
