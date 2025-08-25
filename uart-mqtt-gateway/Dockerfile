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
RUN ./gradlew build --no-daemon

# Stage 2: Create the final, minimal image
FROM amazoncorretto:21-alpine

RUN apk add --update \
    curl \
    && rm -rf /var/cache/apk/*
WORKDIR /app

# Copy the JAR file from the builder stage
COPY --from=builder /app/build/libs/*.jar app.jar

# Expose the application port defined in the configuration
EXPOSE 8088

HEALTHCHECK --interval=1s --timeout=2s --start-period=10s \
   CMD [ "$(wget --server-response --spider --quiet localhost:8088/actuator/health 2>&1 | awk 'NR==1{print $2}')" = "200" ]

ENTRYPOINT ["java", "-XX:+UseZGC", "-Xmx256m", "-jar", "app.jar"]