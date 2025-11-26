# rpc-server

Server-side JSON-RPC 2.0 endpoint for Java applications.

## Features

- ✅ **JSON-RPC 2.0 Compliance** - Full specification support
- ✅ **Method Registration** - Simple API for registering handlers
- ✅ **Introspection** - Built-in `__rpc.*` methods for discovery
- ✅ **Batch Support** - Handle multiple requests in one call
- ✅ **Middleware System** - Before/after request processing hooks
- ✅ **Structured Logging** - Multiple levels and formats (Text/JSON)
- ✅ **Thread-Safe** - Concurrent request handling
- ✅ **Schema Support** - Method metadata and documentation
- ✅ **Context Passing** - Share data across handlers

## Installation

### Gradle

```gradle
dependencies {
    implementation project(':rpc-server')
}
```

### Maven

```xml
<dependency>
    <groupId>com.carpanese.rpc</groupId>
    <artifactId>rpc-server</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Quick Start

```java
import com.carpanese.rpc.server.*;
import com.google.gson.JsonPrimitive;

// Create endpoint
RpcEndpoint endpoint = new RpcEndpoint();

// Register methods
endpoint.addMethod("add", (params, ctx) -> {
    var p = params.getAsJsonObject();
    int a = p.get("a").getAsInt();
    int b = p.get("b").getAsInt();
    return new JsonPrimitive(a + b);
});

// Handle request
String request = "{\"jsonrpc\":\"2.0\",\"method\":\"add\",\"params\":{\"a\":5,\"b\":3},\"id\":1}";
String response = endpoint.handleRequest(request);
// {"jsonrpc":"2.0","result":8,"id":1}
```

## Configuration

```java
RpcOptions options = new RpcOptions()
    .setEnableBatch(true)
    .setMaxBatchSize(50)
    .setEnableLogging(true)
    .setEnableIntrospection(true)
    .setLoggerOptions(new RpcLoggerOptions()
        .setLevel(RpcLogLevel.INFO)
        .setFormat(RpcLogFormat.TEXT));

RpcEndpoint endpoint = new RpcEndpoint(context, options);
```

## Method Registration with Schema

```java
MethodConfig config = new MethodConfig()
    .withDescription("Add two numbers")
    .withExposeSchema(true)
    .withSchema(schemaObject);

endpoint.addMethod("calculator.add", (params, ctx) -> {
    // ... handler implementation
}, config);
```

## Context Usage

```java
class AppContext {
    Database db;
    Config config;
}

AppContext context = new AppContext();
RpcEndpoint endpoint = new RpcEndpoint(context, options);

endpoint.addMethod("getUser", (params, ctx) -> {
    AppContext app = (AppContext) ctx;
    return app.db.findUser(params.get("id").getAsInt());
});
```

## Middleware

```java
MiddlewareManager middleware = endpoint.getMiddleware();

// Add timing middleware
middleware.add(new IRpcMiddleware() {
    @Override
    public void beforeAsync(RpcRequest request, Object context) {
        System.out.println("Before: " + request.getMethod());
    }
    
    @Override
    public void afterAsync(RpcRequest request, Object result, Object context) {
        System.out.println("After: " + request.getMethod());
    }
}, "before");
```

## Introspection Methods

When `enableIntrospection` is true, these methods are automatically registered:

- `__rpc.listMethods` - List all available methods
- `__rpc.describe` - Get method schema and description
- `__rpc.describeAll` - Get all methods with public schemas
- `__rpc.version` - Get toolkit version
- `__rpc.capabilities` - Get server capabilities

## Error Handling

```java
endpoint.addMethod("divide", (params, ctx) -> {
    var p = params.getAsJsonObject();
    int a = p.get("a").getAsInt();
    int b = p.get("b").getAsInt();
    
    if (b == 0) {
        throw new RpcException(
            RpcError.INVALID_PARAMS,
            "Division by zero"
        );
    }
    
    return new JsonPrimitive(a / b);
});
```

## Batch Requests

```java
String batch = "[" +
    "{\"jsonrpc\":\"2.0\",\"method\":\"add\",\"params\":{\"a\":1,\"b\":2},\"id\":1}," +
    "{\"jsonrpc\":\"2.0\",\"method\":\"multiply\",\"params\":{\"a\":3,\"b\":4},\"id\":2}" +
    "]";

String response = endpoint.handleRequest(batch);
// Returns array of responses
```

## Integration Examples

### Servlet Integration

```java
@WebServlet("/rpc")
public class RpcServlet extends HttpServlet {
    private final RpcEndpoint endpoint = new RpcEndpoint();
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) 
            throws IOException {
        // Read request body
        String requestBody = req.getReader().lines()
            .collect(Collectors.joining());
        
        // Handle RPC request
        String response = endpoint.handleRequest(requestBody);
        
        // Send response
        resp.setContentType("application/json");
        resp.getWriter().write(response);
    }
}
```

### Spring Boot Integration

```java
@RestController
public class RpcController {
    private final RpcEndpoint endpoint;
    
    public RpcController() {
        this.endpoint = new RpcEndpoint();
        registerMethods();
    }
    
    @PostMapping("/rpc")
    public String handleRpc(@RequestBody String request) {
        return endpoint.handleRequest(request);
    }
    
    private void registerMethods() {
        endpoint.addMethod("ping", (params, ctx) -> 
            new JsonPrimitive("pong"));
    }
}
```

## Thread Safety

RpcEndpoint is thread-safe and can handle concurrent requests:

```java
RpcEndpoint endpoint = new RpcEndpoint();
endpoint.addMethod("process", handler);

// Can be called from multiple threads
ExecutorService executor = Executors.newFixedThreadPool(10);
for (int i = 0; i < 100; i++) {
    executor.submit(() -> {
        String response = endpoint.handleRequest(request);
    });
}
```

## Examples

See `examples/BasicServerExample.java` for a complete working example.

## Testing

Run tests:
```bash
./gradlew :rpc-server:test
```

## License

MIT License - see LICENSE file for details.
