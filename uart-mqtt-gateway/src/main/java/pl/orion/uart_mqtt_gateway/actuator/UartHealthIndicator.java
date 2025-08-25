package pl.orion.uart_mqtt_gateway.actuator;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.orion.uart_mqtt_gateway.config.UartMqttGatewayProperties;
import pl.orion.uart_mqtt_gateway.model.ErrorPayload;
import pl.orion.uart_mqtt_gateway.service.MqttService;


@Slf4j
@Component
@RequiredArgsConstructor
public class UartHealthIndicator implements HealthIndicator {
    @Value("${spring.application.name}")
    private String appName;
    private final UartMqttGatewayProperties properties;
    private final MqttService mqttService;

    private final Map<String, Integer> failureCounter = new ConcurrentHashMap<>();

    @Override
    public Health health() {
        final var failedDevices = failureCounter.entrySet().stream()
            .filter(entry -> entry.getValue() > 5)
            .map(entry -> String.format("{port=%s, failures=%d}", entry.getKey(), entry.getValue()))
            .collect(Collectors.joining(", "));
        
        if (failedDevices != null && !failedDevices.isEmpty()) {
            log.error("Detected failures on the following devices [{}]", failedDevices);
            sendErrorOntoMqttTopic(failedDevices);
            return Health.down().withDetail("failedDevices", failedDevices).build();
        }
        return Health.up().build();
    }

    public void registerUartDetectionFailure(String portPath) {
        int failures = failureCounter.getOrDefault(portPath, 0);
        failures++;
        failureCounter.put(portPath, failures);
    }

    public void clearUartDetectionFailure(String portPath) {
        failureCounter.remove(portPath);
    }

    private void sendErrorOntoMqttTopic(final String errorMessage) {
        ErrorPayload errorPayload = new ErrorPayload(appName, "deviceConnectionError", errorMessage);
        ObjectMapper mapper = new ObjectMapper();
        try {
            mqttService.publish(properties.getErrorTopic(), mapper.writeValueAsString(errorPayload));
        }
        catch (JsonProcessingException e) {
            log.error("Cannot serialize error payload={} due to={}", errorPayload, e.toString());
        }
    }
}
