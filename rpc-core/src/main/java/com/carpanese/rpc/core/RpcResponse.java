package com.carpanese.rpc.core;

import com.google.gson.JsonElement;

import javax.annotation.Nullable;

/**
 * JSON-RPC 2.0 Response
 * 
 * Represents the response to a remote procedure call.
 */
public class RpcResponse {
    
    private final String jsonrpc = "2.0";
    private final JsonElement result;
    private final RpcError error;
    private final Object id;
    
    /**
     * Creates a successful response
     */
    public RpcResponse(JsonElement result, Object id) {
        this.result = result;
        this.error = null;
        this.id = id;
    }
    
    /**
     * Creates an error response
     */
    public RpcResponse(RpcError error, Object id) {
        this.result = null;
        this.error = error;
        this.id = id;
    }
    
    public String getJsonrpc() {
        return jsonrpc;
    }
    
    @Nullable
    public JsonElement getResult() {
        return result;
    }
    
    @Nullable
    public RpcError getError() {
        return error;
    }
    
    @Nullable
    public Object getId() {
        return id;
    }
    
    /**
     * Check if this response is an error
     */
    public boolean isError() {
        return error != null;
    }
    
    /**
     * Check if this response is successful
     */
    public boolean isSuccess() {
        return error == null;
    }
    
    @Override
    public String toString() {
        if (isError()) {
            return String.format("RpcResponse{error=%s, id=%s}", error, id);
        } else {
            return String.format("RpcResponse{result=%s, id=%s}", result, id);
        }
    }
}
