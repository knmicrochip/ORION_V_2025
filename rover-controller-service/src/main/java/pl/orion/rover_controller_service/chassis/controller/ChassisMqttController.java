package pl.orion.rover_controller_service.chassis.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;

import pl.orion.rover_controller_service.chassis.model.ChassisInboundPayload;
import pl.orion.rover_controller_service.chassis.service.DriveModeManager;
import pl.orion.rover_controller_service.config.MqttClient;

import java.nio.charset.StandardCharsets;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

@Controller
public class ChassisMqttController {
    private static final Logger logger = LoggerFactory.getLogger(ChassisMqttController.class);
    
    private final Mqtt5AsyncClient mqttClient;
    private final DriveModeManager driveModeManager;
    private final ObjectMapper objectMapper;
    
    @Value("${chassis.upstream.inbound:orion/topic/chassis/controller/inbound}")
    private String chassisInboundTopic;
    
    @Value("${chassis.downstream.inbound:orion/topic/chassis/inbound}")
    private String chassisOutboundTopic;
    
    public ChassisMqttController(MqttClient mqttClient, DriveModeManager driveModeManager, 
                                @Qualifier("chassisObjectMapper") ObjectMapper objectMapper) {
        this.mqttClient = mqttClient.getMqttClient();
        this.driveModeManager = driveModeManager;
        this.objectMapper = objectMapper;
    }
    
    @PostConstruct
    public void subscribeToTopics() {
        logger.info("Subscribing to chassis inbound topic: {}", chassisInboundTopic);
        
        mqttClient.subscribeWith()
            .topicFilter(chassisInboundTopic)
            .qos(MqttQos.AT_LEAST_ONCE)
            .callback(this::handleInboundMessage)
            .send()
            .whenComplete((subAck, throwable) -> {
                if (throwable != null) {
                    logger.error("Failed to subscribe to chassis inbound topic: {}", throwable.getMessage(), throwable);
                } else {
                    logger.info("Successfully subscribed to chassis inbound topic: {}", chassisInboundTopic);
                }
            });
        logger.info("Subscribed", chassisInboundTopic);
    }
    
    @PreDestroy
    public void unsubscribeFromTopics() {
        logger.info("Unsubscribing from chassis inbound topic: {}", chassisInboundTopic);
        
        mqttClient.unsubscribeWith()
            .topicFilter(chassisInboundTopic)
            .send()
            .whenComplete((unsubAck, throwable) -> {
                if (throwable != null) {
                    logger.error("Failed to unsubscribe from chassis inbound topic: {}", throwable.getMessage(), throwable);
                } else {
                    logger.info("Successfully unsubscribed from chassis inbound topic: {}", chassisInboundTopic);
                }
            });
    }
    
    private void handleInboundMessage(Mqtt5Publish publish) {
        try {
            String payload = new String(publish.getPayloadAsBytes(), StandardCharsets.UTF_8);
            logger.trace("Received message on topic {}: {}", publish.getTopic(), payload);
            
            // Parse the inbound payload
            ChassisInboundPayload chassisPayload = objectMapper.readValue(payload, ChassisInboundPayload.class);
            
            // Process the payload using the current drive mode
            Object outboundPayload = driveModeManager.process(chassisPayload);
            
            // Convert the outbound payload to JSON
            String outboundJson = objectMapper.writeValueAsString(outboundPayload);
            
            // Publish the outbound payload
            publishOutboundMessage(outboundJson);
        } catch (Exception e) {
            logger.error("Error processing inbound chassis message: {}", e.getMessage(), e);
        }
    }
    
    private void publishOutboundMessage(String payload) {
        logger.trace("Publishing outbound message to topic {}: {}", chassisOutboundTopic, payload);
        
        mqttClient.publishWith()
            .topic(chassisOutboundTopic)
            .qos(MqttQos.AT_LEAST_ONCE)
            .payload(payload.getBytes(StandardCharsets.UTF_8))
            .qos(com.hivemq.client.mqtt.datatypes.MqttQos.AT_LEAST_ONCE)
            .send()
            .whenComplete((publishResult, throwable) -> {
                if (throwable != null) {
                    logger.error("Failed to publish outbound chassis message: {}", throwable.getMessage(), throwable);
                } else {
                    logger.debug("Successfully published outbound chassis message");
                }
            });
    }
}
