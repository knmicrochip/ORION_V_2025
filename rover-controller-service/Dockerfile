# Stage 1: Build the application
FROM amazoncorretto:21-alpine AS builder

WORKDIR /app

COPY build.gradle settings.gradle gradlew ./
COPY gradle ./gradle
COPY src ./src

RUN chmod +x ./gradlew

RUN ./gradlew bootJar

# Stage 2: Create the final, minimal image
FROM amazoncorretto:21-alpine

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar
ENV MQTT_USERNAME=user
ENV MQTT_PASSWORD=user

EXPOSE 8880

ENTRYPOINT ["java", "-XX:+UseZGC", "-Xmx256m", "-jar", "app.jar"]
