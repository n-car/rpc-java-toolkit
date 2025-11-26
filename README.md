# rpc-java-toolkit

[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Java](https://img.shields.io/badge/Java-11+-orange.svg)](https://openjdk.org/)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9+-purple.svg)](https://kotlinlang.org/)
[![Android](https://img.shields.io/badge/Android-API%2021+-green.svg)](https://developer.android.com/)

Complete JSON-RPC 2.0 toolkit for Java and Android. Multi-module architecture supporting backend servers, desktop clients, and mobile Android apps.

## 📦 Modules

### rpc-core
Core types and serialization for JSON-RPC 2.0.
- `RpcRequest`, `RpcResponse`, `RpcError`
- `RpcSerializer` with Safe Mode support
- Cross-platform compatibility

**Maven:**
```xml
<dependency>
    <groupId>com.carpanese.rpc</groupId>
    <artifactId>rpc-core</artifactId>
    <version>1.0.0</version>
</dependency>
```

**Gradle:**
```gradle
implementation 'com.carpanese.rpc:rpc-core:1.0.0'
```

### rpc-server ⭐ NEW
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
    <groupId>com.carpanese.rpc</groupId>
    <artifactId>rpc-server</artifactId>
    <version>1.0.0</version>
</dependency>
```

**Gradle:**
```gradle
implementation 'com.carpanese.rpc:rpc-server:1.0.0'
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
    <groupId>com.carpanese.rpc</groupId>
    <artifactId>rpc-client</artifactId>
    <version>1.0.0</version>
</dependency>
```

**Gradle:**
```gradle
implementation 'com.carpanese.rpc:rpc-client:1.0.0'
```

### rpc-android
Android-specific extensions with Kotlin Coroutines, LiveData, Flow, and Retrofit support.
- `RpcClientKt` - Coroutine-based client
- `RpcViewModel` - ViewModel with LiveData
- `RpcFlow` - Reactive Flow API
- `RetrofitRpcClient` - Retrofit integration

**Gradle:**
```gradle
implementation 'com.carpanese.rpc:rpc-android:1.0.0'
```

## 🚀 Quick Start

### Java Backend / Desktop

```java
import com.carpanese.rpc.client.RpcClient;
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
import com.carpanese.rpc.android.RpcClientKt
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

### Android - ViewModel + LiveData

```kotlin
import com.carpanese.rpc.android.RpcViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class MyViewModel : RpcViewModel("http://api.example.com/rpc") {
    
    private val _data = MutableLiveData<User>()
    val data: LiveData<User> = _data
    
    fun loadUser(userId: Int) {
        val params = JsonObject().apply {
            addProperty("userId", userId)
        }
        
        callRpcAs<User>("getUser", params) { user ->
            _data.value = user
        }
    }
}

// In Activity/Fragment
viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
    progressBar.isVisible = isLoading
}

viewModel.error.observe(viewLifecycleOwner) { error ->
    error?.let { showError(it.message) }
}

viewModel.data.observe(viewLifecycleOwner) { user ->
    updateUI(user)
}
```

### Android - Flow API

```kotlin
import com.carpanese.rpc.android.*
import kotlinx.coroutines.flow.*

lifecycleScope.launch {
    rpcResultFlowAs<List<User>>("http://api.example.com/rpc") {
        call("getUsers")
    }.collect { result ->
        when (result) {
            is RpcResult.Loading -> showProgress()
            is RpcResult.Success -> updateUI(result.data)
            is RpcResult.Error -> showError(result.exception.message)
        }
    }
}
```

### Android - Retrofit Integration

```kotlin
import com.carpanese.rpc.android.RpcService
import com.carpanese.rpc.android.RetrofitRpcClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val retrofit = Retrofit.Builder()
    .baseUrl("http://api.example.com/")
    .addConverterFactory(GsonConverterFactory.create())
    .build()

val rpcService = retrofit.create(RpcService::class.java)
val client = RetrofitRpcClient(rpcService, "rpc")

lifecycleScope.launch {
    val result = client.call("myMethod", params)
    // Handle result
}
```

## 🎨 Features

### Core Features
- ✅ **JSON-RPC 2.0 Compliance** - Full specification support
- ✅ **Safe Mode** - Type-safe serialization (S:, D:, n)
- ✅ **Multi-Module** - Use only what you need
- ✅ **Cross-Platform** - Works with entire RPC Toolkit ecosystem
- ✅ **Thread-Safe** - Concurrent requests supported
- ✅ **Timeout Control** - Configurable timeouts
- ✅ **Authentication** - Bearer token support

### Android Features
- ✅ **Kotlin Coroutines** - Suspend functions
- ✅ **LiveData** - Reactive UI updates
- ✅ **Flow** - Modern reactive streams
- ✅ **ViewModel** - Architecture components
- ✅ **Retrofit** - Advanced HTTP features
- ✅ **Type-Safe** - Generic result types

## 📖 Documentation

### Java Client Configuration

```java
import com.carpanese.rpc.client.RpcClientConfig;
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

## 🔗 Cross-Platform Compatibility

Works seamlessly with:
- ✅ **[rpc-express-toolkit](https://github.com/n-car/rpc-express-toolkit)** - Node.js/Express
- ✅ **[rpc-php-toolkit](https://github.com/n-car/rpc-php-toolkit)** - PHP
- ✅ **[rpc-dotnet-toolkit](https://github.com/n-car/rpc-dotnet-toolkit)** - .NET
- ✅ **[rpc-arduino-toolkit](https://github.com/n-car/rpc-arduino-toolkit)** - Arduino/ESP32
- ✅ **[node-red-contrib-rpc-toolkit](https://github.com/n-car/node-red-contrib-rpc-toolkit)** - Node-RED

## 📱 Android Examples

### Example 1: Simple API Call

```kotlin
class WeatherActivity : AppCompatActivity() {
    private val client = RpcClientKt("http://api.weather.com/rpc")
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        lifecycleScope.launch {
            val params = JsonObject().apply {
                addProperty("city", "Rome")
            }
            
            try {
                val weather = client.callAs<Weather>("getWeather", params)
                temperatureText.text = "${weather.temp}°C"
            } catch (e: RpcException) {
                Toast.makeText(this@WeatherActivity, e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
```

### Example 2: IoT Device Control

```kotlin
class DeviceControlViewModel : RpcViewModel("http://192.168.1.100:8080") {
    
    fun toggleLed(ledId: Int, state: Boolean) {
        val params = JsonObject().apply {
            addProperty("ledId", ledId)
            addProperty("state", state)
        }
        
        callRpc("setLed", params) { result ->
            // LED toggled successfully
        }
    }
    
    fun readSensors() {
        callRpcAs<SensorData>("readSensors") { data ->
            // Update UI with sensor data
        }
    }
}
```

### Example 3: Real-time Updates with Flow

```kotlin
class DashboardFragment : Fragment() {
    
    private val client = RpcClientKt("http://api.example.com/rpc")
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Poll sensor data every 5 seconds
        viewLifecycleOwner.lifecycleScope.launch {
            while (isActive) {
                rpcResultFlowAs<SensorData>(client.url) {
                    call("getSensors")
                }.collect { result ->
                    when (result) {
                        is RpcResult.Success -> updateSensors(result.data)
                        is RpcResult.Error -> showError(result.exception.message)
                    }
                }
                delay(5000)
            }
        }
    }
}
```

## 🛠️ Building

### Requirements
- JDK 11 or higher
- Gradle 8.0+
- Android SDK 34 (for Android module)

### Build All Modules

```bash
./gradlew build
```

### Build Specific Module

```bash
./gradlew :rpc-core:build
./gradlew :rpc-client:build
./gradlew :rpc-android:build
```

### Run Tests

```bash
./gradlew test
```

### Publish to Maven Local

```bash
./gradlew publishToMavenLocal
```

## 🔗 Related Projects

- [rpc-express-toolkit](https://github.com/n-car/rpc-express-toolkit) - Node.js/Express implementation
- [rpc-php-toolkit](https://github.com/n-car/rpc-php-toolkit) - PHP implementation
- [rpc-dotnet-toolkit](https://github.com/n-car/rpc-dotnet-toolkit) - .NET implementation
- [rpc-arduino-toolkit](https://github.com/n-car/rpc-arduino-toolkit) - Arduino/ESP32 implementation
- [node-red-contrib-rpc-toolkit](https://github.com/n-car/node-red-contrib-rpc-toolkit) - Node-RED visual programming

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🤝 Contributing

Contributions are welcome! Please read [CONTRIBUTING.md](CONTRIBUTING.md) for details.

## 🙏 Acknowledgments

- Part of the RPC Toolkit ecosystem
- OkHttp for HTTP client
- Gson for JSON serialization
- Kotlin Coroutines for async operations

---

**rpc-java-toolkit** - JSON-RPC 2.0 for Java and Android
