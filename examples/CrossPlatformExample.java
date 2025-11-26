package com.carpanese.rpc.examples;

import com.carpanese.rpc.client.RpcClient;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Example demonstrating cross-platform RPC calls
 * Works with Express, PHP, .NET, Arduino, Node-RED servers
 */
public class CrossPlatformExample {
    
    public static void main(String[] args) {
        System.out.println("=== Cross-Platform RPC Examples ===\n");
        
        // Call Node.js Express server
        callExpressServer();
        
        // Call PHP server
        callPhpServer();
        
        // Call .NET server
        callDotNetServer();
        
        // Call Arduino/ESP32 device
        callArduinoDevice();
        
        // Call Node-RED flow
        callNodeRed();
    }
    
    private static void callExpressServer() {
        System.out.println("--- Node.js Express Server ---");
        try (RpcClient client = new RpcClient("http://localhost:3000/rpc")) {
            
            JsonObject params = new JsonObject();
            params.addProperty("name", "Java Client");
            
            JsonElement result = client.call("getServerInfo", params);
            System.out.println("Server info: " + result);
            
        } catch (Exception e) {
            System.err.println("Express server error: " + e.getMessage());
        }
        System.out.println();
    }
    
    private static void callPhpServer() {
        System.out.println("--- PHP Server ---");
        try (RpcClient client = new RpcClient("http://localhost:8000/api/rpc")) {
            
            JsonElement time = client.call("getTime", null);
            System.out.println("Server time: " + time);
            
        } catch (Exception e) {
            System.err.println("PHP server error: " + e.getMessage());
        }
        System.out.println();
    }
    
    private static void callDotNetServer() {
        System.out.println("--- .NET Server ---");
        try (RpcClient client = new RpcClient("http://localhost:5000/api/rpc")) {
            
            JsonObject params = new JsonObject();
            params.addProperty("userId", 123);
            
            JsonElement user = client.call("getUser", params);
            System.out.println("User data: " + user);
            
        } catch (Exception e) {
            System.err.println(".NET server error: " + e.getMessage());
        }
        System.out.println();
    }
    
    private static void callArduinoDevice() {
        System.out.println("--- Arduino/ESP32 Device ---");
        try (RpcClient client = new RpcClient("http://192.168.1.100:8080")) {
            
            // Read temperature sensor
            JsonElement temp = client.call("readTemp", null);
            System.out.println("Temperature: " + temp + "°C");
            
            // Control LED
            JsonObject ledParams = new JsonObject();
            ledParams.addProperty("ledId", 1);
            ledParams.addProperty("state", true);
            client.call("setLed", ledParams);
            System.out.println("LED turned on");
            
        } catch (Exception e) {
            System.err.println("Arduino error: " + e.getMessage());
        }
        System.out.println();
    }
    
    private static void callNodeRed() {
        System.out.println("--- Node-RED Flow ---");
        try (RpcClient client = new RpcClient("http://localhost:1880/rpc")) {
            
            JsonObject params = new JsonObject();
            params.addProperty("room", "bedroom");
            
            JsonElement sensors = client.call("getAllSensors", params);
            System.out.println("Sensors: " + sensors);
            
        } catch (Exception e) {
            System.err.println("Node-RED error: " + e.getMessage());
        }
        System.out.println();
    }
}
