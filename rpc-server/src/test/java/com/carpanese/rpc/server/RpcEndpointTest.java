package com.carpanese.rpc.server;

import com.carpanese.rpc.core.RpcError;
import com.carpanese.rpc.core.RpcException;
import com.google.gson.JsonPrimitive;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for RpcEndpoint
 */
class RpcEndpointTest {
    
    private RpcEndpoint endpoint;
    
    @BeforeEach
    void setUp() {
        RpcOptions options = new RpcOptions()
            .setEnableLogging(false)
            .setEnableBatch(true)
            .setEnableIntrospection(true);
        
        endpoint = new RpcEndpoint(null, options);
        
        // Register test methods
        endpoint.addMethod("add", (params, ctx) -> {
            var p = params.getAsJsonObject();
            int a = p.get("a").getAsInt();
            int b = p.get("b").getAsInt();
            return new JsonPrimitive(a + b);
        });
        
        endpoint.addMethod("echo", (params, ctx) -> params);
        
        endpoint.addMethod("error", (params, ctx) -> {
            throw new RpcException(RpcError.INVALID_PARAMS, "Test error");
        });
    }
    
    @Test
    void testAddMethod() {
        endpoint.addMethod("testMethod", (params, ctx) -> new JsonPrimitive("ok"));
        assertNotNull(endpoint.getMethod("testMethod"));
    }
    
    @Test
    void testAddMethodWithConfig() {
        MethodConfig config = new MethodConfig()
            .withDescription("Test method")
            .withExposeSchema(true);
        
        endpoint.addMethod("configMethod", (params, ctx) -> new JsonPrimitive("ok"), config);
        
        MethodConfig retrieved = endpoint.getMethod("configMethod");
        assertNotNull(retrieved);
        assertEquals("Test method", retrieved.getDescription());
        assertTrue(retrieved.isExposeSchema());
    }
    
    @Test
    void testAddMethodDuplicate() {
        assertThrows(IllegalArgumentException.class, () -> {
            endpoint.addMethod("add", (params, ctx) -> new JsonPrimitive(0));
        });
    }
    
    @Test
    void testAddIntrospectionMethod() {
        assertThrows(IllegalArgumentException.class, () -> {
            endpoint.addMethod("__rpc.custom", (params, ctx) -> new JsonPrimitive("ok"));
        });
    }
    
    @Test
    void testRemoveMethod() {
        endpoint.removeMethod("add");
        assertNull(endpoint.getMethod("add"));
    }
    
    @Test
    void testListMethods() {
        String[] methods = endpoint.listMethods();
        assertTrue(methods.length >= 3); // add, echo, error + introspection methods
        assertTrue(java.util.Arrays.asList(methods).contains("add"));
    }
    
    @Test
    void testHandleSingleRequest() {
        String request = "{\"jsonrpc\":\"2.0\",\"method\":\"add\",\"params\":{\"a\":5,\"b\":3},\"id\":1}";
        String response = endpoint.handleRequest(request);
        
        assertNotNull(response);
        assertTrue(response.contains("\"result\":8"));
        assertTrue(response.contains("\"id\":1"));
    }
    
    @Test
    void testHandleNotification() {
        String request = "{\"jsonrpc\":\"2.0\",\"method\":\"echo\",\"params\":{\"test\":true}}";
        String response = endpoint.handleRequest(request);
        
        assertEquals("", response); // Notifications return empty string
    }
    
    @Test
    void testHandleBatchRequest() {
        String request = "[" +
            "{\"jsonrpc\":\"2.0\",\"method\":\"add\",\"params\":{\"a\":1,\"b\":2},\"id\":1}," +
            "{\"jsonrpc\":\"2.0\",\"method\":\"add\",\"params\":{\"a\":3,\"b\":4},\"id\":2}" +
            "]";
        String response = endpoint.handleRequest(request);
        
        assertNotNull(response);
        assertTrue(response.contains("\"result\":3"));
        assertTrue(response.contains("\"result\":7"));
    }
    
    @Test
    void testHandleMethodNotFound() {
        String request = "{\"jsonrpc\":\"2.0\",\"method\":\"unknown\",\"params\":{},\"id\":1}";
        String response = endpoint.handleRequest(request);
        
        assertNotNull(response);
        assertTrue(response.contains("\"error\""));
        assertTrue(response.contains("" + RpcError.METHOD_NOT_FOUND));
    }
    
    @Test
    void testHandleRpcError() {
        String request = "{\"jsonrpc\":\"2.0\",\"method\":\"error\",\"params\":{},\"id\":1}";
        String response = endpoint.handleRequest(request);
        
        assertNotNull(response);
        assertTrue(response.contains("\"error\""));
        assertTrue(response.contains("Test error"));
    }
    
    @Test
    void testHandleInvalidJson() {
        String request = "{invalid json}";
        String response = endpoint.handleRequest(request);
        
        assertNotNull(response);
        assertTrue(response.contains("\"error\""));
        assertTrue(response.contains("" + RpcError.PARSE_ERROR));
    }
    
    @Test
    void testHandleInvalidRequest() {
        // Test with empty method name instead of wrong jsonrpc version
        // (jsonrpc field is final in RpcRequest so wrong version can't be deserialized)
        String request = "{\"jsonrpc\":\"2.0\",\"method\":\"\",\"id\":1}";
        String response = endpoint.handleRequest(request);
        
        assertNotNull(response);
        assertTrue(response.contains("\"error\""));
        assertTrue(response.contains("" + RpcError.INVALID_REQUEST));
    }
    
    @Test
    void testIntrospectionListMethods() {
        String request = "{\"jsonrpc\":\"2.0\",\"method\":\"__rpc.listMethods\",\"params\":{},\"id\":1}";
        String response = endpoint.handleRequest(request);
        
        assertNotNull(response);
        assertTrue(response.contains("\"result\""));
        assertTrue(response.contains("\"add\""));
    }
    
    @Test
    void testIntrospectionCapabilities() {
        String request = "{\"jsonrpc\":\"2.0\",\"method\":\"__rpc.capabilities\",\"params\":{},\"id\":1}";
        String response = endpoint.handleRequest(request);
        
        assertNotNull(response);
        assertTrue(response.contains("\"batch\":true"));
        assertTrue(response.contains("\"introspection\":true"));
        assertTrue(response.contains("\"methodCount\""));
    }
    
    @Test
    void testIntrospectionVersion() {
        String request = "{\"jsonrpc\":\"2.0\",\"method\":\"__rpc.version\",\"params\":{},\"id\":1}";
        String response = endpoint.handleRequest(request);
        
        assertNotNull(response);
        assertTrue(response.contains("\"toolkit\":\"rpc-java-toolkit\""));
        assertTrue(response.contains("\"javaVersion\""));
    }
}
