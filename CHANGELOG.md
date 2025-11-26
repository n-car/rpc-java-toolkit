# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.0] - 2025-11-26

### Added
- Initial release of rpc-java-toolkit
- **rpc-core** module with JSON-RPC 2.0 types
- **rpc-client** module with HTTP client (OkHttp)
- **rpc-android** module with Kotlin extensions
- Safe Mode serialization (S:, D:, n prefixes)
- Kotlin Coroutines support
- LiveData integration for Android
- Flow API for reactive programming
- Retrofit integration
- Thread-safe client implementation
- Configurable timeouts
- Authentication support (Bearer tokens)
- Comprehensive unit tests
- Full documentation with examples
- Cross-platform compatibility with RPC Toolkit ecosystem

### Features

#### rpc-core
- RpcRequest, RpcResponse, RpcError types
- RpcSerializer with Gson
- Safe Mode type serialization
- BigInteger and Instant support
- Standard error codes
- Custom exceptions

#### rpc-client
- OkHttp-based HTTP client
- Configurable timeouts (connect, read, write)
- Custom headers support
- Authentication token management
- Notification support (fire-and-forget)
- Automatic retry logic
- Thread-safe operations

#### rpc-android
- RpcClientKt with suspend functions
- RpcViewModel with LiveData
- RpcFlow for reactive streams
- RetrofitRpcClient for Retrofit integration
- Type-safe result types
- RpcResult sealed class (Loading/Success/Error)
- Extension functions for convenience
- ProGuard rules included
- Android Manifest with permissions

### Compatibility
- Java 11+
- Kotlin 1.9+
- Android API 21+ (Android 5.0 Lollipop)
- Compatible with rpc-express-toolkit, rpc-php-toolkit, rpc-dotnet-toolkit, rpc-arduino-toolkit, node-red-contrib-rpc-toolkit
