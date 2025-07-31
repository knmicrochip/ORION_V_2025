# Stage 1: Build the application
FROM amazoncorretto:21-alpine AS builder

WORKDIR /app

# Copy the project files
COPY build.gradle settings.gradle gradlew ./
COPY gradle ./gradle
COPY src ./src

# Make gradlew executable
RUN chmod +x ./gradlew

# Build the application (compiles, tests, and creates the JAR)
RUN ./gradlew build --no-daemon -x test

# Stage 2: Create the final, minimal image
FROM amazoncorretto:21-alpine

WORKDIR /app

# Copy the JAR file from the builder stage
COPY --from=builder /app/build/libs/*.jar app.jar
ENV MQTT_USERNAME=user
ENV MQTT_PASSWORD=user

# Expose the application port defined in the configuration
EXPOSE 8088

# Set the entrypoint to run the application
# Note: To access serial ports, the container must be run with appropriate privileges,
# for example: docker run -d --privileged -v /dev:/dev uart-mqtt-gateway
ENTRYPOINT ["java", "-jar", "app.jar"]
