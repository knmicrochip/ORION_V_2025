# Core Principles

* Test-Driven Development: Generate comprehensive tests for every class
* Defensive programming by design: The application should expect unexpectable, therefore
it must be hardened against network downtimes, misconfigured payloads and various Java-related
problems like NullPointerExceptions, threading issues, etc
* Java 21 Features: Utilize modern Java features (records, pattern matching, virtual threads)
* SpringBoot Best Practices: Follow dependency injection, configuration management, and auto-configuration patterns
* MQTT Integration: Implement robust MQTT communication with proper error handling and connection management
* Include SL4J logging
* Use Health indicator (Spring Actuator)

# Testing

For every method, generate:
* Unit Tests: Mock external dependencies, test business logic
* Integration Tests: Use Testcontainers for MQTT broker (HiveMQ testcontainers)
* Edge Case Tests: Null inputs, empty collections, timeout scenarios
* Error Handling Tests: Network failures, authentication errors, malformed data

# Project structure tree

Follow the proposed structure tree for the implementation, and mirror it in corresponding `test/` directory:

```
src/main
├── java
│   └── pl
│       └── orion
│           └── rover_controller_service
│               ├── chassis
│               │   ├── controller
│               │   ├── model
│               │   └── service
│               ├── config
│               │   └── MqttConfig.java
│               ├── manipulator
│               ├── RoverControllerServiceApplication.java
│               ├── science
│               └── utils
└── resources
    └── application.yml
```

# Code Generation Commands

When generating code, always:
* Start with the data model (Java records)
* Create configuration classes with validation
* Implement service classes with dependency injection
* Generate comprehensive tests for each method
* Add integration tests with Testcontainers
* Include proper logging and monitoring