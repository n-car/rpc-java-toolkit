package com.carpanese.rpc.server;

import com.google.gson.JsonPrimitive;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for RpcSafeEndpoint
 */
class RpcSafeEndpointTest {
    
    @Test
    void testSafeEndpointInitializesSuccessfully() {
        RpcSafeEndpoint endpoint = new RpcSafeEndpoint();
        assertNotNull(endpoint);
    }
    
    @Test
    void testSafeEndpointWithContext() {
        Object context = new Object();
        RpcSafeEndpoint endpoint = new RpcSafeEndpoint(context);
        assertNotNull(endpoint);
    }
    
    @Test
    void testSafeEndpointAllowsOverridingOptions() {
        RpcOptions options = new RpcOptions()
            .setEnableBatch(false)
            .setEnableLogging(false);
        
        RpcSafeEndpoint endpoint = new RpcSafeEndpoint(null, options);
        assertNotNull(endpoint);
    }
    
    @Test
    void testSafeEndpointHandlesRpcCall() throws Exception {
        RpcSafeEndpoint endpoint = new RpcSafeEndpoint();
        
        endpoint.addMethod("add", (params, ctx) -> {
            var p = params.getAsJsonObject();
            int a = p.get("a").getAsInt();
            int b = p.get("b").getAsInt();
            return new JsonPrimitive(a + b);
        });
        
        String request = "{"
            + "\"jsonrpc\":\"2.0\","
            + "\"method\":\"add\","
            + "\"params\":{\"a\":5,\"b\":3},"
            + "\"id\":1"
            + "}";
        
        String response = endpoint.handleRequest(request);
        
        assertTrue(response.contains("\"result\":8"));
        assertTrue(response.contains("\"id\":1"));
    }
    
    @Test
    void testSafeEndpointInheritsFromRpcEndpoint() {
        RpcSafeEndpoint endpoint = new RpcSafeEndpoint();
        assertTrue(endpoint instanceof RpcEndpoint);
    }
    
    @Test
    void testSafeEndpointCanRegisterMethods() {
        RpcSafeEndpoint endpoint = new RpcSafeEndpoint();
        
        endpoint.addMethod("test", (params, ctx) -> new JsonPrimitive("ok"));
        
        assertNotNull(endpoint.getMethod("test"));
    }
}
