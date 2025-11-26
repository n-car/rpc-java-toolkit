package com.carpanese.rpc.core;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;

import javax.annotation.Nullable;

/**
 * JSON-RPC 2.0 Error Object
 * 
 * Standard error codes:
 * -32700: Parse error
 * -32600: Invalid Request
 * -32601: Method not found
 * -32602: Invalid params
 * -32603: Internal error
 * -32000 to -32099: Server error (reserved for implementation-defined server-errors)
 */
public class RpcError {
    
    // Standard error codes
    public static final int PARSE_ERROR = -32700;
    public static final int INVALID_REQUEST = -32600;
    public static final int METHOD_NOT_FOUND = -32601;
    public static final int INVALID_PARAMS = -32602;
    public static final int INTERNAL_ERROR = -32603;
    public static final int SERVER_ERROR = -32000;
    
    private final int code;
    private final String message;
    private final JsonElement data;
    
    /**
     * Creates a new RPC error
     * 
     * @param code Error code
     * @param message Error message
     * @param data Additional error data (optional)
     */
    public RpcError(int code, String message, @Nullable JsonElement data) {
        this.code = code;
        this.message = message;
        this.data = data != null ? data : JsonNull.INSTANCE;
    }
    
    /**
     * Creates a new RPC error without additional data
     */
    public RpcError(int code, String message) {
        this(code, message, null);
    }
    
    // Standard error factory methods
    
    public static RpcError parseError() {
        return new RpcError(PARSE_ERROR, "Parse error");
    }
    
    public static RpcError invalidRequest() {
        return new RpcError(INVALID_REQUEST, "Invalid Request");
    }
    
    public static RpcError methodNotFound(String method) {
        return new RpcError(METHOD_NOT_FOUND, "Method not found: " + method);
    }
    
    public static RpcError invalidParams(String message) {
        return new RpcError(INVALID_PARAMS, "Invalid params: " + message);
    }
    
    public static RpcError internalError(String message) {
        return new RpcError(INTERNAL_ERROR, "Internal error: " + message);
    }
    
    public static RpcError serverError(String message) {
        return new RpcError(SERVER_ERROR, "Server error: " + message);
    }
    
    public int getCode() {
        return code;
    }
    
    public String getMessage() {
        return message;
    }
    
    public JsonElement getData() {
        return data;
    }
    
    @Override
    public String toString() {
        return String.format("RpcError{code=%d, message='%s'}", code, message);
    }
}
