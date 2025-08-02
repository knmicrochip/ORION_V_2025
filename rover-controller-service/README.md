# Rover Controller Service

## Overview

This project, `rover-controller-service`, is designed to link real-time event processing with the remote control of a robotic platform. It aggregates various hardware modules, including chassis, manipulator, and science, implementing their corresponding behaviors.

## Tech Stack

The application is built using the following technologies:

*   **Java 21**: The primary programming language.
*   **Spring Boot**: A comprehensive application framework for building robust, production-ready applications.
*   **Spring Actuator**: Provides production-ready features to monitor and manage the application.
*   **HiveMQ MQTT Client**: An asynchronous MQTT client library for Java, used for robust MQTT communication.
*   **SLF4J**: A simple logging facade for Java, providing a unified logging interface.
*   **Mockito**: A popular mocking framework for unit testing Java applications.
*   **Testcontainers**: Used for providing lightweight, disposable containers for integration tests, particularly for the MQTT broker.

## Features

### MQTT Integration

The service heavily relies on MQTT for communication with the robotic platform. Key aspects include:

*   **Globally Accessible MQTT Client**: Configured as a globally accessible bean.
*   **Configurable Broker**: MQTT broker details (URL, port, username, password) are configurable via `application.yml` and environment variables.
*   **Liveness and Readiness Probes**: Exposes endpoints to monitor application health, ensuring MQTT connectivity is maintained.

### Chassis Control

The chassis module supports differential drive control and features distinct operational modes:

*   **PWM Mode**: Translates joystick commands into PWM values for each wheel.
*   **CFL Mode** (Closed Feedback Loop): Remaps commands into angular velocity values for each wheel.
*   **ROS Mode**: Converts commands into ROS-compatible `Twist` messages for precise control.

These modes are swappable in real-time using a strategy pattern.

#### Chassis Configuration Example (`application.yml`)

```yaml
chassis:
    eventType: chassis
    downstream:
        inbound: orion/topic/chassis/inbound
        outbound: orion/topic/chassis/outbound
    upstream:
        inbound: orion/topic/chassis/controller/inbound
```

### Manipulator and Science Modules

While detailed functional requirements are to be defined, the project structure includes placeholders for `manipulator` and `science` modules, indicating future expansion for these robotic components.

## Configuration

The application's configuration is primarily managed via `src/main/resources/application.yml` and environment variables.

### MQTT Configuration

MQTT broker credentials (`MQTT_USERNAME`, `MQTT_PASSWORD`) are expected as environment variables. The `application.yml` defines the MQTT client and connection parameters:

```yaml
mqtt:
    clientId: rover-controller-service
    broker:
        url: ${MQTT_BROKER_URL}
        port: 1883
        username: ${MQTT_USERNAME}
        password: ${MQTT_PASSWORD}
    connection:
        timeout.ms: 5000
        keepalive.ms: 60000
        reconnect.delay.ms: 5000
```

## Building and Running

### Prerequisites

*   Java 21 Development Kit (JDK)
*   Docker (for containerized deployment)

### Build with Gradle

To build the application JAR:

```bash
./gradlew build 
```

This command compiles the project and creates an executable JAR in `build/libs/`.

### Run Locally

After building, you can run the JAR directly:

```bash
java -XX:+UseZGC -Xmx256m -jar build/libs/rover-controller-service-*.jar
```

Remember to set the required environment variables:

```bash
export MQTT_USERNAME=your_username
export MQTT_PASSWORD=your_password
export MQTT_BROKER_URL=your_mqtt_broker_url
java -XX:+UseZGC -Xmx256m -jar build/libs/rover-controller-service-*.jar
```

### Build and Run with Docker

The project includes a `Dockerfile` for containerizing the application.

1.  **Build the Docker image:**

    ```bash
    docker build -t rover-controller-service .
    ```

2.  **Run the Docker container:**

    ```bash
    docker run -d \
      -p 8088:8088 \
      -e MQTT_USERNAME=user \
      -e MQTT_PASSWORD=user \
      -e MQTT_BROKER_URL=your_mqtt_broker_url \
      --name rover-controller-service-app \
      rover-controller-service
    ```

    *   `-p 8088:8088`: Maps the container's port 8088 to the host's port 8088.
    *   `-e MQTT_USERNAME=user -e MQTT_PASSWORD=user`: Sets the MQTT credentials. **Change these for production!**
    *   `-e MQTT_BROKER_URL=your_mqtt_broker_url`: Set your MQTT broker URL here.
    *   `--privileged -v /dev:/dev`: (Optional) If serial port access is required, run with these flags.

## Testing

The project adheres to a comprehensive testing strategy:

*   **Unit Tests**: Focus on individual components, mocking external dependencies.
*   **Integration Tests**: Utilize Testcontainers to spin up a mock MQTT broker (HiveMQ Testcontainers) to verify end-to-end communication flows.
*   **Edge Case Tests**: Cover null inputs, empty collections, and timeout scenarios.
*   **Error Handling Tests**: Validate behavior under network failures, authentication errors, and malformed data.

To run all tests:

```bash
./gradlew test
```

## Project Structure

The project follows a well-defined structure, as detailed in `GEMINI.md`.

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