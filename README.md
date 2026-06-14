# rpc-java-toolkit

[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Status](https://img.shields.io/badge/status-beta-yellow.svg)](https://github.com/n-car/rpc-java-toolkit)
[![Maven Central](https://img.shields.io/maven-central/v/it.carpanese.rpc/rpc-core?label=Maven%20Central)](https://central.sonatype.com/namespace/it.carpanese.rpc)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9+-purple.svg)](https://kotlinlang.org/)
[![Android](https://img.shields.io/badge/Android-API%2021+-green.svg)](https://developer.android.com/)

Beta JSON-RPC 2.0 toolkit for Java and Android. Multi-module architecture supporting backend servers, desktop clients, and mobile Android apps.

## Project Status

- Beta package published on Maven Central as `it.carpanese.rpc`.
- Java 21 is required by the current Gradle build.
- Standard JSON-RPC 2.0 remains the default behavior.
- Optional RPC Toolkit Safe Mode interoperability is implemented in the core serializer/client paths.

## Which Module Should I Use?

| Need | Module |
| --- | --- |
| Core JSON-RPC request, response, error, and serialization types | `rpc-core` |
| Java HTTP client for calling JSON-RPC endpoints | `rpc-client` |
| Java endpoint for handling JSON-RPC payloads | `rpc-server` |
| Android Kotlin, LiveData, Flow, ViewModel, or Retrofit helpers | `rpc-android` |

## Modules

### rpc-core
Core types and serialization for JSON-RPC 2.0.
- `RpcRequest`, `RpcResponse`, `RpcError`
- `RpcSerializer` with Safe Mode support
- Cross-platform compatibility

**Maven:**
```xml
<dependency>
    <groupId>it.carpanese.rpc</groupId>
    <artifactId>rpc-core</artifactId>
    <version>0.1.0</version>
</dependency>
```

**Gradle:**
```gradle
implementation 'it.carpanese.rpc:rpc-core:0.1.0'
```

### rpc-server
Server-side JSON-RPC 2.0 endpoint for Java applications.
- Method registration with handlers
- Introspection methods (`__rpc.*`)
- Batch request support
- Middleware system
- Structured logging (Text/JSON)
- Thread-safe concurrent handling
- Schema and metadata support
- Compatible with Servlet, Spring Boot, Vert.x

**Maven:**
```xml
<dependency>
    <groupId>it.carpanese.rpc</groupId>
    <artifactId>rpc-server</artifactId>
    <version>0.1.0</version>
</dependency>
```

**Gradle:**
```gradle
implementation 'it.carpanese.rpc:rpc-server:0.1.0'
```

### rpc-client
HTTP client for making RPC calls (OkHttp-based).
- Thread-safe client
- Timeout configuration
- Authentication support
- Compatible with Express, PHP, .NET, Arduino, Java servers

**Maven:**
```xml
<dependency>
    <groupId>it.carpanese.rpc</groupId>
    <artifactId>rpc-client</artifactId>
    <version>0.1.0</version>
</dependency>
```

**Gradle:**
```gradle
implementation 'it.carpanese.rpc:rpc-client:0.1.0'
```

### rpc-android
Android-specific extensions with Kotlin Coroutines, LiveData, Flow, and Retrofit support.
- `RpcClientKt` - Coroutine-based client
- `RpcViewModel` - ViewModel with LiveData
- `RpcFlow` - Reactive Flow API
- `RetrofitRpcClient` - Retrofit integration

**Gradle:**
```gradle
implementation 'it.carpanese.rpc:rpc-android:0.1.0'
```

## Quick Start

### Java Backend / Desktop

```java
import it.carpanese.rpc.client.RpcClient;
import com.google.gson.JsonObject;

public class Example {
    public static void main(String[] args) throws Exception {
        // Create client
        try (RpcClient client = new RpcClient("http://localhost:3000/rpc")) {

            // Simple call
            JsonElement result = client.call("ping", null);
            System.out.println("Result: " + result);

            // Call with parameters
            JsonObject params = new JsonObject();
            params.addProperty("name", "John");
            JsonElement user = client.call("getUser", params);

            // Notification (no response)
            client.notify("logEvent", params);
        }
    }
}
```

### Android - Kotlin Coroutines

```kotlin
import it.carpanese.rpc.android.RpcClientKt
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val client = RpcClientKt("http://api.example.com/rpc")

    fun loadData() {
        lifecycleScope.launch {
            try {
                val result = client.call("getData")
                // Handle result
                updateUI(result)
            } catch (e: RpcException) {
                // Handle error
                showError(e.message)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        client.close()
    }
}
```

For ViewModel, Flow, Retrofit, and Android instrumentation details, see [Android Usage](docs/ANDROID.md) and the runnable [`examples/android-client`](examples/android-client) project.

## Features

### Core Features
- **JSON-RPC 2.0 Compliance** - Full specification support
- **Safe Mode** - Type-safe serialization (S:, D:, n)
- **Multi-Module** - Use only what you need
- **Cross-Platform** - Works with entire RPC Toolkit ecosystem
- **Thread-Safe** - Concurrent requests supported
- **Timeout Control** - Configurable timeouts
- **Authentication** - Bearer token support

### Android Features
- **Kotlin Coroutines** - Suspend functions
- **LiveData** - Reactive UI updates
- **Flow** - Modern reactive streams
- **ViewModel** - Architecture components
- **Retrofit** - Advanced HTTP features
- **Type-Safe** - Generic result types

## Documentation

### Java Client Configuration

```java
import it.carpanese.rpc.client.RpcClientConfig;
import java.time.Duration;

RpcClientConfig config = new RpcClientConfig()
    .setSafeMode(true)
    .setConnectTimeout(Duration.ofSeconds(10))
    .setReadTimeout(Duration.ofSeconds(30))
    .setHeader("Authorization", "Bearer token");

RpcClient client = new RpcClient("http://api.example.com/rpc", config);
```

### Safe Mode

Enable type-safe serialization with prefixes:

```java
// Client side
RpcClientConfig config = new RpcClientConfig().setSafeMode(true);
RpcClient client = new RpcClient(url, config);

// Serialization behavior:
// Strings:     "hello" → "S:hello"
// Dates:       ISO 8601 → "D:2025-11-26T10:30:00Z"
// BigInteger:  123456789 → "123456789n"
```

### Error Handling

```java
try {
    JsonElement result = client.call("myMethod", params);
} catch (RpcException e) {
    // RPC error (method not found, invalid params, etc.)
    int errorCode = e.getErrorCode();
    String errorMessage = e.getMessage();

    if (errorCode == RpcError.METHOD_NOT_FOUND) {
        // Handle method not found
    }
} catch (IOException e) {
    // Network error
}
```

### Kotlin Extensions

```kotlin
// Type-safe call
data class User(val name: String, val email: String)
val user: User = client.callAs("getUser", params)

// Flow with automatic error handling
rpcResultFlowAs<List<Item>>(url) {
    call("getItems")
}.catch { error ->
    Log.e("RPC", "Error: $error")
}.collect { result ->
    when (result) {
        is RpcResult.Loading -> showProgress()
        is RpcResult.Success -> updateUI(result.data)
        is RpcResult.Error -> showError(result.exception)
    }
}
```

## Cross-Platform Compatibility

Works seamlessly with:
- **[rpc-express-toolkit](https://github.com/n-car/rpc-express-toolkit)** - Node.js/Express
- **[rpc-php-toolkit](https://github.com/n-car/rpc-php-toolkit)** - PHP
- **[rpc-dotnet-toolkit](https://github.com/n-car/rpc-dotnet-toolkit)** - .NET
- **[rpc-arduino-toolkit](https://github.com/n-car/rpc-arduino-toolkit)** - Arduino/ESP32
- **[node-red-contrib-rpc-toolkit](https://github.com/n-car/node-red-contrib-rpc-toolkit)** - Node-RED

## More Documentation

- [Android Usage](docs/ANDROID.md)
- [Building](docs/BUILDING.md)
- [Java and Android examples](examples/README.md)

## Related Projects

- [rpc-express-toolkit](https://github.com/n-car/rpc-express-toolkit) - Node.js/Express implementation
- [rpc-php-toolkit](https://github.com/n-car/rpc-php-toolkit) - PHP implementation
- [rpc-dotnet-toolkit](https://github.com/n-car/rpc-dotnet-toolkit) - .NET implementation
- [rpc-arduino-toolkit](https://github.com/n-car/rpc-arduino-toolkit) - Arduino/ESP32 implementation
- [node-red-contrib-rpc-toolkit](https://github.com/n-car/node-red-contrib-rpc-toolkit) - Node-RED visual programming

## License

MIT. See [LICENSE](LICENSE).

## Contributing

Contributions are welcome! Please read [CONTRIBUTING.md](CONTRIBUTING.md) for details.

## Acknowledgments

- Part of the RPC Toolkit ecosystem
- OkHttp for HTTP client
- Gson for JSON serialization
- Kotlin Coroutines for async operations

---

**rpc-java-toolkit** - JSON-RPC 2.0 for Java and Android
