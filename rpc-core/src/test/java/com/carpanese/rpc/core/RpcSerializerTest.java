package com.carpanese.rpc.core;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class RpcSerializerTest {
    
    @Test
    void testBasicSerialization() {
        RpcSerializer serializer = new RpcSerializer(false);
        
        RpcRequest request = new RpcRequest("test", new JsonPrimitive("param"), 1);
        String json = serializer.toJson(request);
        
        assertTrue(json.contains("\"method\":\"test\""));
        assertTrue(json.contains("\"jsonrpc\":\"2.0\""));
    }
    
    @Test
    void testSafeModeStrings() {
        RpcSerializer serializer = new RpcSerializer(true);
        
        JsonObject obj = new JsonObject();
        obj.addProperty("message", "hello");
        
        String json = serializer.toJson(obj);
        assertTrue(json.contains("S:hello"), "Should add S: prefix in safe mode");
    }
    
    @Test
    void testSafeModeBigInteger() {
        RpcSerializer serializer = new RpcSerializer(true);
        
        BigInteger big = new BigInteger("123456789012345678901234567890");
        String json = serializer.toJson(big);
        
        assertTrue(json.contains("n\""), "Should add n suffix in safe mode");
    }
    
    @Test
    void testSafeModeDate() {
        RpcSerializer serializer = new RpcSerializer(true);
        
        Instant now = Instant.parse("2025-11-26T10:30:00Z");
        String json = serializer.toJson(now);
        
        assertTrue(json.contains("D:2025-11-26T10:30:00Z"), "Should add D: prefix in safe mode");
    }
    
    @Test
    void testRequestSerialization() {
        RpcSerializer serializer = new RpcSerializer();
        
        JsonObject params = new JsonObject();
        params.addProperty("name", "test");
        
        RpcRequest request = new RpcRequest("myMethod", params, 123);
        String json = serializer.toJson(request);
        
        RpcRequest deserialized = serializer.fromJson(json, RpcRequest.class);
        assertEquals("myMethod", deserialized.getMethod());
        assertEquals(123.0, deserialized.getId()); // Gson deserializes numbers as Double
    }
    
    @Test
    void testNotification() {
        RpcRequest notification = RpcRequest.notification("notify", new JsonPrimitive("test"));
        assertTrue(notification.isNotification());
        assertNull(notification.getId());
    }
    
    @Test
    void testErrorResponse() {
        RpcError error = RpcError.methodNotFound("unknownMethod");
        RpcResponse response = new RpcResponse(error, 1);
        
        assertTrue(response.isError());
        assertFalse(response.isSuccess());
        assertEquals(RpcError.METHOD_NOT_FOUND, response.getError().getCode());
    }
    
    @Test
    void testSuccessResponse() {
        JsonObject result = new JsonObject();
        result.addProperty("status", "ok");
        
        RpcResponse response = new RpcResponse(result, 1);
        
        assertTrue(response.isSuccess());
        assertFalse(response.isError());
        assertNotNull(response.getResult());
    }
}
