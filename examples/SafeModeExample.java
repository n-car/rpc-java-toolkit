package examples;

import com.carpanese.rpc.server.RpcSafeEndpoint;
import com.carpanese.rpc.client.RpcSafeClient;
import com.google.gson.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Example: Using RpcSafeEndpoint and RpcSafeClient
 * 
 * This example demonstrates how to use the Safe mode classes
 * for better type preservation and automatic safe mode handling.
 */
public class SafeModeExample {
    
    public static void main(String[] args) {
        serverExample();
        clientExample();
    }
    
    static void serverExample() {
        System.out.println("===== SERVER SIDE =====\n");
        
        // Create a safe endpoint (Safe Mode enabled by default)
        Map<String, Object> context = new HashMap<>();
        context.put("server", "example");
        RpcSafeEndpoint rpc = new RpcSafeEndpoint(context);
        
        // Register methods that handle special types
        rpc.addMethod("getCurrentTime", (params, ctx) -> {
            JsonObject result = new JsonObject();
            result.addProperty("timestamp", Instant.now().getEpochSecond());
            result.addProperty("iso8601", Instant.now().toString());
            result.addProperty("timezone", "UTC");
            return result;
        });
        
        rpc.addMethod("mathOperations", (params, ctx) -> {
            JsonObject p = params.getAsJsonObject();
            double a = p.get("a").getAsDouble();
            double b = p.get("b").getAsDouble();
            
            JsonObject result = new JsonObject();
            result.addProperty("infinity", Double.POSITIVE_INFINITY);
            result.addProperty("negativeInfinity", Double.NEGATIVE_INFINITY);
            result.addProperty("notANumber", Double.NaN);
            result.addProperty("result", a + b);
            return result;
        });
        
        rpc.addMethod("echo", (params, ctx) -> params);
        
        // Simulate handling a request
        String request = "{"
            + "\"jsonrpc\":\"2.0\","
            + "\"method\":\"mathOperations\","
            + "\"params\":{\"a\":10,\"b\":5},"
            + "\"id\":1"
            + "}";
        
        try {
            String response = rpc.handleRequest(request);
            System.out.println("Server Response:");
            System.out.println(response);
            System.out.println();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    static void clientExample() {
        System.out.println("===== CLIENT SIDE =====\n");
        
        // Create a safe client (Safe Mode enabled by default)
        try (RpcSafeClient client = new RpcSafeClient("http://localhost:8080/rpc")) {
            
            // Call methods
            JsonElement time = client.call("getCurrentTime", null);
            System.out.println("Current time: " + time);
            
            JsonObject mathParams = new JsonObject();
            mathParams.addProperty("a", 10);
            mathParams.addProperty("b", 5);
            JsonElement math = client.call("mathOperations", mathParams);
            System.out.println("Math operations: " + math);
            
            JsonObject echoParams = new JsonObject();
            echoParams.addProperty("test", "value");
            echoParams.addProperty("number", 42);
            JsonElement echo = client.call("echo", echoParams);
            System.out.println("Echo: " + echo);
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
        
        System.out.println();
        System.out.println("=== Safe Mode Classes Demo ===");
        System.out.println("RpcSafeEndpoint: Automatically enables Safe Mode for the server");
        System.out.println("RpcSafeClient: Automatically enables Safe Mode for the client");
        System.out.println("Both classes provide a cleaner API compared to manual option setting");
    }
}
