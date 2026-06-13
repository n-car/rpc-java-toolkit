package it.carpanese.rpc.client;

import it.carpanese.rpc.core.RpcException;
import it.carpanese.rpc.core.RpcRequest;
import it.carpanese.rpc.core.RpcResponse;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RpcClientTest {

    private MockWebServer server;
    private RpcClient client;

    @BeforeEach
    void setUp() throws IOException {
        server = new MockWebServer();
        server.start();

        String url = server.url("/rpc").toString();
        client = new RpcClient(url);
    }

    @AfterEach
    void tearDown() throws IOException {
        client.close();
        server.shutdown();
    }

    @Test
    void testSuccessfulCall() throws Exception {
        // Mock server response
        String responseJson = "{\"jsonrpc\":\"2.0\",\"result\":\"pong\",\"id\":1}";
        server.enqueue(new MockResponse()
                .setBody(responseJson)
                .setHeader("Content-Type", "application/json"));

        // Make call
        var result = client.call("ping", null, 1);

        // Verify
        assertEquals("pong", result.getAsString());

        // Check request
        RecordedRequest request = server.takeRequest();
        assertEquals("/rpc", request.getPath());
        assertEquals("POST", request.getMethod());
        String body = request.getBody().readUtf8();
        assertTrue(body.contains("\"method\":\"ping\""));
        assertFalse(body.contains("\"params\""));
    }

    @Test
    void testErrorResponse() {
        // Mock error response
        String errorJson = "{\"jsonrpc\":\"2.0\",\"error\":{\"code\":-32601,\"message\":\"Method not found\"},\"id\":1}";
        server.enqueue(new MockResponse()
                .setBody(errorJson)
                .setHeader("Content-Type", "application/json"));

        // Make call and expect exception
        RpcException exception = assertThrows(RpcException.class, () -> {
            client.call("unknownMethod", null, 1);
        });

        assertEquals(-32601, exception.getErrorCode());
        assertTrue(exception.getMessage().contains("Method not found"));
    }

    @Test
    void testNotification() throws Exception {
        // Mock server response (notification doesn't need response)
        server.enqueue(new MockResponse().setResponseCode(200));

        // Send notification
        JsonObject params = new JsonObject();
        params.addProperty("event", "test");
        client.notify("logEvent", params);

        // Check request
        RecordedRequest request = server.takeRequest();
        String body = request.getBody().readUtf8();
        assertTrue(body.contains("\"method\":\"logEvent\""));
        assertFalse(body.contains("\"id\""), "Notifications must omit id");
    }

    @Test
    void testSafeMode() throws Exception {
        // Create client with safe mode
        RpcClientConfig config = new RpcClientConfig().setSafeMode(true);
        try (RpcClient safeClient = new RpcClient(server.url("/rpc").toString(), config)) {

            // Mock response with safe mode header
            String responseJson = "{\"jsonrpc\":\"2.0\",\"result\":\"S:hello\",\"id\":1}";
            server.enqueue(new MockResponse()
                    .setBody(responseJson)
                    .setHeader("Content-Type", "application/json")
                    .setHeader("X-RPC-Safe-Enabled", "true"));

            // Make call
            var result = safeClient.call("test", new JsonPrimitive("param"), 1);

            // Verify safe mode header was sent
            RecordedRequest request = server.takeRequest();
            String body = request.getBody().readUtf8();
            assertEquals("hello", result.getAsString());
            assertEquals("true", request.getHeader("X-RPC-Safe-Enabled"));
            assertTrue(body.contains("\"params\":\"S:param\""));
        }
    }

    @Test
    void testSafeModeBatch() throws Exception {
        RpcClientConfig config = new RpcClientConfig().setSafeMode(true);
        try (RpcClient safeClient = new RpcClient(server.url("/rpc").toString(), config)) {
            String responseJson = "["
                    + "{\"jsonrpc\":\"2.0\",\"result\":\"S:pong\",\"id\":1},"
                    + "{\"jsonrpc\":\"2.0\",\"error\":{\"code\":-32601,\"message\":\"S:Method not found\"},\"id\":2}"
                    + "]";
            server.enqueue(new MockResponse()
                    .setBody(responseJson)
                    .setHeader("Content-Type", "application/json")
                    .setHeader("X-RPC-Safe-Enabled", "true"));

            JsonObject params = new JsonObject();
            params.addProperty("plain", "hello");

            List<RpcResponse> responses = safeClient.batch(List.of(
                    new RpcRequest("ping", null, 1),
                    new RpcRequest("missing.method", null, 2),
                    RpcRequest.notification("notify.record", params)
            ));

            RecordedRequest request = server.takeRequest();
            String body = request.getBody().readUtf8();

            assertEquals(2, responses.size());
            assertEquals("pong", responses.get(0).getResult().getAsString());
            assertEquals(-32601, responses.get(1).getError().getCode());
            assertEquals("Method not found", responses.get(1).getError().getMessage());
            assertEquals("true", request.getHeader("X-RPC-Safe-Enabled"));
            assertTrue(body.startsWith("["));
            assertTrue(body.contains("\"method\":\"ping\""));
            assertTrue(body.contains("\"method\":\"notify.record\""));
            assertTrue(body.contains("\"plain\":\"S:hello\""));
        }
    }

    @Test
    void testSafeModeBatchNotificationOnlyNoContent() throws Exception {
        RpcClientConfig config = new RpcClientConfig().setSafeMode(true);
        try (RpcClient safeClient = new RpcClient(server.url("/rpc").toString(), config)) {
            server.enqueue(new MockResponse().setResponseCode(204));

            JsonObject params = new JsonObject();
            params.addProperty("eventName", "notify-only");

            List<RpcResponse> responses = safeClient.batch(List.of(
                    RpcRequest.notification("notify.record", params)
            ));

            RecordedRequest request = server.takeRequest();
            String body = request.getBody().readUtf8();

            assertTrue(responses.isEmpty());
            assertEquals("true", request.getHeader("X-RPC-Safe-Enabled"));
            assertTrue(body.startsWith("["));
            assertTrue(body.contains("\"method\":\"notify.record\""));
            assertFalse(body.contains("\"id\""));
        }
    }

    @Test
    void testHttpError() {
        // Mock HTTP error
        server.enqueue(new MockResponse().setResponseCode(500));

        // Make call and expect exception
        assertThrows(RpcException.class, () -> {
            client.call("test", null, 1);
        });
    }
}
