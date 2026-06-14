# Building

## Requirements

- JDK 21 for the current Gradle build
- Gradle wrapper from this repository
- Android SDK 34 for the Android module

Set `JAVA_HOME` to a JDK 21 installation before running Gradle locally.

## Build All Modules

```bash
./gradlew build
```

## Build A Specific Module

```bash
./gradlew :rpc-core:build
./gradlew :rpc-client:build
./gradlew :rpc-server:build
./gradlew :rpc-android:build
```

## Run Tests

```bash
./gradlew test
```

## Build The Android Example

```bash
./gradlew -p examples/android-client :app:assembleDebug
```

## Publish To Maven Local

```bash
./gradlew publishToMavenLocal
```
