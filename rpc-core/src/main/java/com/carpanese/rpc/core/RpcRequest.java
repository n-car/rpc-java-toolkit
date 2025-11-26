package com.carpanese.rpc.core;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;

import javax.annotation.Nullable;

/**
 * JSON-RPC 2.0 Request
 * 
 * Represents a remote procedure call request according to the JSON-RPC 2.0 specification.
 * 
 * @see <a href="https://www.jsonrpc.org/specification">JSON-RPC 2.0 Specification</a>
 */
public class RpcRequest {
    
    private final String jsonrpc = "2.0";
    private final String method;
    private final JsonElement params;
    private final Object id;
    
    /**
     * Creates a new RPC request
     * 
     * @param method The method name to call
     * @param params The method parameters (can be null)
     * @param id The request ID (null for notifications)
     */
    public RpcRequest(String method, @Nullable JsonElement params, @Nullable Object id) {
        if (method == null || method.isEmpty()) {
            throw new IllegalArgumentException("Method name cannot be null or empty");
        }
        this.method = method;
        this.params = params != null ? params : JsonNull.INSTANCE;
        this.id = id;
    }
    
    /**
     * Creates a new RPC request with auto-generated ID
     */
    public RpcRequest(String method, @Nullable JsonElement params) {
        this(method, params, System.currentTimeMillis());
    }
    
    /**
     * Creates a notification (no response expected)
     */
    public static RpcRequest notification(String method, @Nullable JsonElement params) {
        return new RpcRequest(method, params, null);
    }
    
    public String getJsonrpc() {
        return jsonrpc;
    }
    
    public String getMethod() {
        return method;
    }
    
    public JsonElement getParams() {
        return params;
    }
    
    @Nullable
    public Object getId() {
        return id;
    }
    
    /**
     * Check if this is a notification (no response expected)
     */
    public boolean isNotification() {
        return id == null;
    }
    
    @Override
    public String toString() {
        return String.format("RpcRequest{method='%s', id=%s}", method, id);
    }
}
