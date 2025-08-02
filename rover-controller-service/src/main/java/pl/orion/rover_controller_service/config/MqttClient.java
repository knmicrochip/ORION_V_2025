package pl.orion.rover_controller_service.config;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Component
@Scope("singleton") // Ensure a new instance is created for each injection
@Slf4j
public class MqttClient implements DisposableBean{

    private final MqttConfig mqttConfig;
    private final Mqtt5AsyncClient mqtt5AsyncClient;

    public MqttClient(MqttConfig mqttConfig) {
    
        log.info(">>> Starting MQTT client with config: {}", mqttConfig);
        var client = com.hivemq.client.mqtt.MqttClient.builder()
                .useMqttVersion5()
                .automaticReconnect()
                    .maxDelay(mqttConfig.getConnectionReconnectDelayMs(), TimeUnit.MILLISECONDS)
                    .applyAutomaticReconnect()
                .simpleAuth()
                    .username(mqttConfig.getBrokerUser())
                    .password(mqttConfig.getBrokerPassword().getBytes(StandardCharsets.US_ASCII))
                    .applySimpleAuth()
                .serverHost(mqttConfig.getBrokerUrl())
                .serverPort(mqttConfig.getBrokerPort())
                .identifier(UUID.randomUUID().toString())
                .buildAsync();

        this.mqttConfig = mqttConfig;
        this.mqtt5AsyncClient = client;
    }

    public synchronized Mqtt5AsyncClient getMqttClient() {
        return mqtt5AsyncClient;
    }

    @PostConstruct
    public void connect() {

        mqtt5AsyncClient.connectWith()
            .cleanStart(true)
            .keepAlive((int) mqttConfig.getConnectionKeepaliveMs()/1000)
            .send()
            .whenComplete((connAck, throwable) -> {
                if (throwable != null) {
                    log.error("Failed to connect to MQTT broker: {}", throwable.getMessage(), throwable);
                    System.exit(1);
                } else {
                    log.info("Connected to MQTT broker at {}:{}", mqttConfig.getBrokerUrl(), mqttConfig.getBrokerPort());
                }
            });
    }

    @Override
    public void destroy() {
        log.info("Disconnecting from MQTT broker");
        mqtt5AsyncClient.disconnectWith()
            .send()
            .whenComplete((disconnect, throwable) -> {
                if (throwable != null) {
                    log.error("Failed to disconnect from MQTT broker: {}", throwable.getMessage(), throwable);
                } else {
                    log.info("Disconnected from MQTT broker");
                }
            });
    }
}
