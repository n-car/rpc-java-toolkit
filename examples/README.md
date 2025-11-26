# Java Examples

This directory contains example code demonstrating how to use rpc-java-toolkit.

## Examples

### SimpleExample.java
Basic usage of the RPC client:
- Creating a client
- Making RPC calls
- Sending notifications
- Custom configuration
- Error handling

**Run:**
```bash
# Compile
javac -cp "rpc-core.jar:rpc-client.jar:gson.jar:okhttp.jar" SimpleExample.java

# Run
java -cp ".:rpc-core.jar:rpc-client.jar:gson.jar:okhttp.jar" SimpleExample
```

### CrossPlatformExample.java
Demonstrates calling different RPC servers:
- Node.js Express server
- PHP server
- .NET server
- Arduino/ESP32 devices
- Node-RED flows

**Run:**
```bash
javac -cp "rpc-core.jar:rpc-client.jar:gson.jar:okhttp.jar" CrossPlatformExample.java
java -cp ".:rpc-core.jar:rpc-client.jar:gson.jar:okhttp.jar" CrossPlatformExample
```

## Android Examples

See `rpc-android` module for Android-specific examples with:
- Kotlin Coroutines
- LiveData
- Flow API
- ViewModel integration

## Building Examples

### With Gradle
```bash
cd rpc-java-toolkit
./gradlew :examples:build
```

### Manual Compilation
1. Build the project: `./gradlew build`
2. Copy JARs from `build/libs/` to `examples/libs/`
3. Compile examples as shown above

## Server Setup

To run these examples, you need at least one RPC server running:

### Node.js Express
```bash
cd rpc-express-toolkit
npm install
node examples/server-example.js
```

### PHP
```bash
cd rpc-php-toolkit
php -S localhost:8000 examples/server.php
```

### .NET
```bash
cd rpc-dotnet-toolkit
dotnet run
```

### Arduino/ESP32
Upload BasicServer or WiFiServer example to your device.

### Node-RED
Import example flows and deploy.

## Testing Without Server

If you don't have a server running, the examples will catch connection errors gracefully:

```java
try {
    JsonElement result = client.call("method", params);
} catch (RpcException e) {
    // RPC-level error (method not found, etc.)
    System.err.println("RPC Error: " + e.getMessage());
} catch (IOException e) {
    // Network error (connection refused, timeout, etc.)
    System.err.println("Network Error: " + e.getMessage());
}
```
