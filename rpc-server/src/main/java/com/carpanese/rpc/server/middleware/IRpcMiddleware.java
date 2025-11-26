package com.carpanese.rpc.server.middleware;

import com.carpanese.rpc.core.RpcRequest;

/**
 * Interface for RPC middleware components
 */
public interface IRpcMiddleware {
    
    /**
     * Execute before the RPC method is called
     * 
     * @param request The RPC request
     * @param context Server context object
     * @throws Exception If validation or processing fails
     */
    void beforeAsync(RpcRequest request, Object context) throws Exception;
    
    /**
     * Execute after the RPC method is called
     * 
     * @param request The RPC request
     * @param result The method result
     * @param context Server context object
     * @throws Exception If post-processing fails
     */
    void afterAsync(RpcRequest request, Object result, Object context) throws Exception;
}
