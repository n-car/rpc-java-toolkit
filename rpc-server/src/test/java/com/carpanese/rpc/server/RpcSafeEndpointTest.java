package com.carpanese.rpc.server;

import com.carpanese.rpc.core.RpcError;
import com.carpanese.rpc.core.RpcException;
import com.google.gson.JsonObject;
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
    void testSafeEndpointEncodesAndDecodesJsonElementValues() {
        RpcOptions options = new RpcOptions()
            .setEnableLogging(false)
            .setEnableMiddleware(false)
            .setEnableValidation(false);
        RpcSafeEndpoint endpoint = new RpcSafeEndpoint(null, options);

        endpoint.addMethod("ping", (params, ctx) -> new JsonPrimitive("pong"));
        endpoint.addMethod("echo", (params, ctx) -> params);

        String ping = endpoint.handleRequest("{\"jsonrpc\":\"2.0\",\"method\":\"ping\",\"id\":1}");
        assertTrue(ping.contains("\"result\":\"S:pong\""));

        String echoRequest = "{"
            + "\"jsonrpc\":\"2.0\","
            + "\"method\":\"echo\","
            + "\"params\":{"
            + "\"plain\":\"S:hello\","
            + "\"safePrefix\":\"S:S:literal\","
            + "\"datePrefix\":\"S:D:literal\","
            + "\"bigintLikeString\":\"S:9007199254740993n\""
            + "},"
            + "\"id\":2"
            + "}";
        String echo = endpoint.handleRequest(echoRequest);

        assertTrue(echo.contains("\"plain\":\"S:hello\""));
        assertTrue(echo.contains("\"safePrefix\":\"S:S:literal\""));
        assertTrue(echo.contains("\"datePrefix\":\"S:D:literal\""));
        assertTrue(echo.contains("\"bigintLikeString\":\"S:9007199254740993n\""));
    }

    @Test
    void testSafeEndpointEncodesErrorData() {
        RpcOptions options = new RpcOptions()
            .setEnableLogging(false)
            .setEnableMiddleware(false)
            .setEnableValidation(false)
            .setSanitizeErrors(false);
        RpcSafeEndpoint endpoint = new RpcSafeEndpoint(null, options);

        endpoint.addMethod("domainError", (params, ctx) -> {
            JsonObject data = new JsonObject();
            data.addProperty("markerString", "S:error-data-literal");
            data.addProperty("plain", "hello");
            throw new RpcException(new RpcError(-32042, "Domain failure", data));
        });

        String response = endpoint.handleRequest("{\"jsonrpc\":\"2.0\",\"method\":\"domainError\",\"id\":1}");

        assertTrue(response.contains("\"code\":-32042"));
        assertTrue(response.contains("\"message\":\"S:Domain failure\""));
        assertTrue(response.contains("\"markerString\":\"S:S:error-data-literal\""));
        assertTrue(response.contains("\"plain\":\"S:hello\""));
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
