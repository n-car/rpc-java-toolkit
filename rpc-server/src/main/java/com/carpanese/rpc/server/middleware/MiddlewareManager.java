package com.carpanese.rpc.server.middleware;

import com.carpanese.rpc.core.RpcRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * Manager for middleware execution pipeline
 */
public class MiddlewareManager {
    private final List<IRpcMiddleware> beforeMiddleware = new ArrayList<>();
    private final List<IRpcMiddleware> afterMiddleware = new ArrayList<>();

    /**
     * Add middleware to the pipeline
     * 
     * @param middleware The middleware to add
     * @param phase "before" or "after"
     */
    public void add(IRpcMiddleware middleware, String phase) {
        if ("before".equalsIgnoreCase(phase)) {
            beforeMiddleware.add(middleware);
        } else if ("after".equalsIgnoreCase(phase)) {
            afterMiddleware.add(middleware);
        } else {
            throw new IllegalArgumentException("Phase must be 'before' or 'after'");
        }
    }

    /**
     * Execute before middleware
     */
    public void executeBefore(RpcRequest request, Object context) throws Exception {
        for (IRpcMiddleware middleware : beforeMiddleware) {
            middleware.beforeAsync(request, context);
        }
    }

    /**
     * Execute after middleware
     */
    public void executeAfter(RpcRequest request, Object result, Object context) throws Exception {
        for (IRpcMiddleware middleware : afterMiddleware) {
            middleware.afterAsync(request, result, context);
        }
    }
}
