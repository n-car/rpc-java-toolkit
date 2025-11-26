package com.carpanese.rpc.core;

/**
 * Exception thrown when an RPC call fails
 */
public class RpcException extends Exception {
    
    private final RpcError error;
    
    /**
     * Creates a new RPC exception from an error object
     */
    public RpcException(RpcError error) {
        super(error.getMessage());
        this.error = error;
    }
    
    /**
     * Creates a new RPC exception with error code and message
     */
    public RpcException(int code, String message) {
        this(new RpcError(code, message));
    }
    
    /**
     * Creates a new RPC exception with error code, message, and cause
     */
    public RpcException(int code, String message, Throwable cause) {
        super(message, cause);
        this.error = new RpcError(code, message);
    }
    
    public RpcError getError() {
        return error;
    }
    
    public int getErrorCode() {
        return error.getCode();
    }
    
    @Override
    public String toString() {
        return String.format("RpcException: %s", error);
    }
}
