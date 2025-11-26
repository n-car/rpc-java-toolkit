package com.carpanese.rpc.server;

import com.google.gson.JsonElement;
import com.carpanese.rpc.core.RpcException;

/**
 * Functional interface for RPC method handlers on server side
 */
@FunctionalInterface
public interface RpcMethodHandler {
    
    /**
     * Handle an RPC method call
     * 
     * @param params Method parameters as JsonElement (can be null)
     * @param context Server context object (can be null)
     * @return Result as JsonElement
     * @throws RpcException If an RPC error occurs
     * @throws Exception For other errors (will be converted to internal error)
     */
    JsonElement handle(JsonElement params, Object context) throws Exception;
}
