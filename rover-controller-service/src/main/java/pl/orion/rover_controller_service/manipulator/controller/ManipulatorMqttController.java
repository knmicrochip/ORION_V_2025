package pl.orion.rover_controller_service.manipulator.controller;

import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import pl.orion.rover_controller_service.config.MqttClient;
import pl.orion.rover_controller_service.manipulator.model.ManipulatorInboundPayload;
import pl.orion.rover_controller_service.manipulator.service.ManipulatorModeManager;

@Controller
public class ManipulatorMqttController {

    private static final Logger logger = LoggerFactory.getLogger(ManipulatorMqttController.class);

    private final Mqtt5AsyncClient mqttClient;
    private final ManipulatorModeManager manipulatorModeManager;
    private final ObjectMapper objectMapper;

    @Value("${manipulator.upstream.inbound}")
    private String manipulatorInboundTopic;

    public ManipulatorMqttController(MqttClient mqttClient, ManipulatorModeManager manipulatorModeManager, @Qualifier("manipulatorObjectMapper") ObjectMapper objectMapper) {
        this.mqttClient = mqttClient.getMqttClient();
        this.manipulatorModeManager = manipulatorModeManager;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void subscribeToTopics() {
        logger.info("Subscribing to manipulator inbound topic: {}", manipulatorInboundTopic);

        mqttClient.subscribeWith()
                .topicFilter(manipulatorInboundTopic)
                .qos(MqttQos.AT_LEAST_ONCE)
                .callback(this::handleInboundMessage)
                .send()
                .whenComplete((subAck, throwable) -> {
                    if (throwable != null) {
                        logger.error("Failed to subscribe to manipulator inbound topic: {}", throwable.getMessage(), throwable);
                    } else {
                        logger.info("Successfully subscribed to manipulator inbound topic: {}", manipulatorInboundTopic);
                    }
                });
    }

    @PreDestroy
    public void unsubscribeFromTopics() {
        logger.info("Unsubscribing from manipulator inbound topic: {}", manipulatorInboundTopic);

        mqttClient.unsubscribeWith()
                .topicFilter(manipulatorInboundTopic)
                .send()
                .whenComplete((unsubAck, throwable) -> {
                    if (throwable != null) {
                        logger.error("Failed to unsubscribe from manipulator inbound topic: {}", throwable.getMessage(), throwable);
                    } else {
                        logger.info("Successfully unsubscribed from manipulator inbound topic: {}", manipulatorInboundTopic);
                    }
                });
    }

    private void handleInboundMessage(Mqtt5Publish publish) {
        try {
            String payload = new String(publish.getPayloadAsBytes(), StandardCharsets.UTF_8);
            logger.trace("Received message on topic {}: {}", publish.getTopic(), payload);

            ManipulatorInboundPayload manipulatorPayload = objectMapper.readValue(payload, ManipulatorInboundPayload.class);

            manipulatorModeManager.handle(manipulatorPayload);
        } catch (Exception e) {
            logger.error("Error processing inbound manipulator message: {}", e.getMessage(), e);
        }
    }
}
