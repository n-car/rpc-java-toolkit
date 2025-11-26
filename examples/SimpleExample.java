package com.carpanese.rpc.examples;

import com.carpanese.rpc.client.RpcClient;
import com.carpanese.rpc.client.RpcClientConfig;
import com.carpanese.rpc.core.RpcException;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.time.Duration;

/**
 * Simple example demonstrating RPC client usage
 */
public class SimpleExample {
    
    public static void main(String[] args) {
        // Example 1: Basic client
        System.out.println("=== Example 1: Basic RPC Client ===");
        basicExample();
        
        // Example 2: Configuration
        System.out.println("\n=== Example 2: Custom Configuration ===");
        configExample();
        
        // Example 3: Error handling
        System.out.println("\n=== Example 3: Error Handling ===");
        errorExample();
    }
    
    private static void basicExample() {
        // Create client for Express/PHP/Node-RED server
        try (RpcClient client = new RpcClient("http://localhost:3000/rpc")) {
            
            // Simple call
            JsonElement result = client.call("ping", null);
            System.out.println("Ping result: " + result);
            
            // Call with parameters
            JsonObject params = new JsonObject();
            params.addProperty("message", "Hello from Java!");
            JsonElement echo = client.call("echo", params);
            System.out.println("Echo result: " + echo);
            
            // Notification (fire-and-forget)
            JsonObject logParams = new JsonObject();
            logParams.addProperty("level", "info");
            logParams.addProperty("message", "Java client connected");
            client.notify("log", logParams);
            System.out.println("Notification sent");
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
    
    private static void configExample() {
        // Custom configuration
        RpcClientConfig config = new RpcClientConfig()
            .setSafeMode(true)  // Enable type-safe serialization
            .setConnectTimeout(Duration.ofSeconds(10))
            .setReadTimeout(Duration.ofSeconds(30))
            .setHeader("User-Agent", "RPC-Java-Client/1.0");
        
        try (RpcClient client = new RpcClient("http://localhost:3000/rpc", config)) {
            System.out.println("Client configured:");
            System.out.println("  URL: " + client.getUrl());
            System.out.println("  Safe Mode: " + client.isSafeMode());
            
            // Set authentication
            client.setAuthToken("your-jwt-token-here");
            System.out.println("  Auth token set");
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
    
    private static void errorExample() {
        try (RpcClient client = new RpcClient("http://localhost:3000/rpc")) {
            
            // Call non-existent method
            try {
                client.call("unknownMethod", null);
            } catch (RpcException e) {
                System.out.println("RPC Error caught:");
                System.out.println("  Code: " + e.getErrorCode());
                System.out.println("  Message: " + e.getMessage());
            }
            
        } catch (Exception e) {
            System.err.println("Connection error: " + e.getMessage());
        }
    }
}
