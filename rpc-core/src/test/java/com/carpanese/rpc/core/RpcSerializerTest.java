package com.carpanese.rpc.core;

import com.google.gson.JsonArray;
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

        // Test direct String serialization
        String str = "hello";
        String json = serializer.toJson(str);
        assertTrue(json.contains("S:hello"), "Should add S: prefix in safe mode for direct strings");
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
    void testSafeModeEncodesAndDecodesJsonElementParams() {
        RpcSerializer serializer = new RpcSerializer(true);

        JsonObject nested = new JsonObject();
        nested.addProperty("safePrefix", "S:literal");
        nested.addProperty("datePrefix", "D:literal");
        nested.addProperty("bigintLikeString", "9007199254740993n");
        JsonArray values = new JsonArray();
        values.add("hello");
        values.add("S:nested");
        nested.add("values", values);

        RpcRequest request = new RpcRequest("echo", nested, 1);
        String json = serializer.toJson(request);

        assertTrue(json.contains("\"safePrefix\":\"S:S:literal\""));
        assertTrue(json.contains("\"datePrefix\":\"S:D:literal\""));
        assertTrue(json.contains("\"bigintLikeString\":\"S:9007199254740993n\""));

        RpcRequest decoded = serializer.fromJson(json, RpcRequest.class);
        JsonObject decodedParams = decoded.getParams().getAsJsonObject();
        assertEquals("S:literal", decodedParams.get("safePrefix").getAsString());
        assertEquals("D:literal", decodedParams.get("datePrefix").getAsString());
        assertEquals("9007199254740993n", decodedParams.get("bigintLikeString").getAsString());
        assertEquals("S:nested", decodedParams.getAsJsonArray("values").get(1).getAsString());
    }

    @Test
    void testSafeModeOmitsNullParams() {
        RpcSerializer serializer = new RpcSerializer(true);

        RpcRequest request = new RpcRequest("ping", null, 1);
        String json = serializer.toJson(request);

        assertFalse(json.contains("\"params\""));
        assertTrue(json.contains("\"id\":1"));
    }

    @Test
    void testSafeModeEncodesAndDecodesResponseErrorData() {
        RpcSerializer serializer = new RpcSerializer(true);

        JsonObject data = new JsonObject();
        data.addProperty("markerString", "S:error-data-literal");
        data.addProperty("plain", "hello");

        RpcResponse response = new RpcResponse(new RpcError(-32042, "Domain failure", data), 1);
        String json = serializer.toJson(response);

        assertTrue(json.contains("\"message\":\"S:Domain failure\""));
        assertTrue(json.contains("\"markerString\":\"S:S:error-data-literal\""));
        assertTrue(json.contains("\"plain\":\"S:hello\""));

        RpcResponse decoded = serializer.fromJson(json, RpcResponse.class);
        assertEquals("Domain failure", decoded.getError().getMessage());
        JsonObject decodedData = decoded.getError().getData().getAsJsonObject();
        assertEquals("S:error-data-literal", decodedData.get("markerString").getAsString());
        assertEquals("hello", decodedData.get("plain").getAsString());
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
