# Contributing to rpc-java-toolkit

Thank you for your interest in contributing!

## How to Contribute

### Reporting Bugs

1. Check [Issues](https://github.com/n-car/rpc-java-toolkit/issues)
2. Create a new issue with:
   - Clear title and description
   - Steps to reproduce
   - Expected vs actual behavior
   - Java/Kotlin/Android version
   - Code sample

### Suggesting Features

1. Check existing issues
2. Create a new issue with:
   - Clear description
   - Use cases
   - Proposed implementation

### Pull Requests

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/my-feature`
3. Make your changes
4. Add tests
5. Update documentation
6. Run tests: `./gradlew test`
7. Commit: `git commit -m "Add feature X"`
8. Push: `git push origin feature/my-feature`
9. Create a Pull Request

## Development Setup

### Requirements
- JDK 11+
- Android SDK (for Android module)
- Gradle 8.0+

### Build
```bash
./gradlew build
```

### Run Tests
```bash
./gradlew test
```

### Code Style
- Follow Java conventions
- Use Kotlin idioms for Android code
- Add JavaDoc/KDoc comments
- Keep functions small and focused

## Module Guidelines

### rpc-core
- Pure Java 11
- No external dependencies except Gson
- Well-documented public APIs

### rpc-client
- Java 11
- OkHttp for HTTP
- SLF4J for logging
- Thread-safe operations

### rpc-android
- Kotlin
- Coroutines for async operations
- Follow Android Architecture Components
- Support API 21+

## Testing

- Write unit tests for new features
- Maintain test coverage
- Use JUnit 5 for Java
- Use Mockito for mocking

## Documentation

- Update README.md
- Add JavaDoc/KDoc comments
- Include code examples
- Update CHANGELOG.md

## License

By contributing, you agree to license your work under the MIT License.

Thank you! 🎉
