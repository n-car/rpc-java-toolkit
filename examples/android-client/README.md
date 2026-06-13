# Android Client Example

This example is a minimal Android application that calls an RPC Toolkit Safe Mode HTTP endpoint from a real Android runtime.

It demonstrates:

- `RpcClientKt` coroutine client usage;
- Safe Mode HTTP negotiation;
- object params with marker-like strings;
- top-level array params through `sumArray`;
- batch requests with success, error, and notification entries;
- notifications and `error.data`;
- standard JSON-RPC client behavior with `X-RPC-Safe-Enabled: false`.

## Requirements

- Android Studio or Android SDK command-line tools
- JDK 17 or newer
- Android device or emulator
- A JSON-RPC server endpoint compatible with RPC Toolkit Safe Mode

The easiest server to use during development is the `rpc-express-toolkit` Safe Mode example server.

## Run The App

Open this directory in Android Studio, or run it with Gradle from the repository root:

```bash
./gradlew -p examples/android-client :app:installDebug
```

The app defaults to:

```text
http://10.0.2.2:3000/api
```

Use that URL for an Android emulator when the server is running on the host machine. For a physical Android device on the same WiFi network, use the computer LAN address instead, for example:

```text
http://192.168.1.20:3000/api
```

The Android manifest enables cleartext HTTP for local development examples.

## Run Instrumented Checks

Start a compatible Safe Mode endpoint, then run:

```bash
./gradlew -p examples/android-client connectedDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.rpcEndpoint=http://10.0.2.2:3000/api
```

For a physical device on the same WiFi network:

```bash
./gradlew -p examples/android-client connectedDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.rpcEndpoint=http://192.168.1.20:3000/api
```

The instrumented checks are intentionally small and cover the behaviors listed above. They are examples, not a replacement for the cross-runtime validation suite maintained separately by the project.
