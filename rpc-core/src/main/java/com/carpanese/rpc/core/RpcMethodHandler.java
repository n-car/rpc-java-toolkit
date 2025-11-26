package com.carpanese.rpc.core;

import com.google.gson.JsonElement;

/**
 * Functional interface for RPC method handlers
 */
@FunctionalInterface
public interface RpcMethodHandler {
    
    /**
     * Handle an RPC method call
     * 
     * @param params Method parameters as JsonElement
     * @param context Additional context (can be null)
     * @return Result as JsonElement
     * @throws RpcException If an error occurs during method execution
     */
    JsonElement handle(JsonElement params, Object context) throws RpcException;
}
