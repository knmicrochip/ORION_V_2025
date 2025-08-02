package pl.orion.rover_controller_service.chassis.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;

import pl.orion.rover_controller_service.chassis.config.ChassisConfig;
import pl.orion.rover_controller_service.chassis.config.ChassisProperties;
import pl.orion.rover_controller_service.chassis.model.ChassisInboundPayload;
import pl.orion.rover_controller_service.chassis.model.ChassisPwmOutboundPayload;
import pl.orion.rover_controller_service.config.MqttClient;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@PropertySource("classpath:application-test.properties")
@SpringBootTest
class ChassisMqttControllerIntegrationTest {
    
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MqttClient mqttClient;

    @Autowired
    private ChassisMqttController chassisMqttController;

    @Autowired
    private ChassisProperties chassisProperties;

    private static final BlockingQueue<ChassisPwmOutboundPayload> receivedMessages = 
        new LinkedBlockingQueue<>();

    @BeforeEach
    void setUp() {
        final Mqtt5AsyncClient client = this.mqttClient.getMqttClient();
        client.subscribeWith()
            .topicFilter(chassisProperties.downstream().inbound())
            .callback(this::subscriptionCallabck)
            .send().join();
    }

    @AfterEach
    void tearDown() {
        receivedMessages.clear(); 

        final Mqtt5AsyncClient client = this.mqttClient.getMqttClient();
        client.unsubscribeWith()
            .topicFilter(chassisProperties.downstream().inbound())
            .send();
        }


    @Test
    void contextLoads() throws Exception {
        // GIVEN the upstream sends the records to the controller

        final Mqtt5AsyncClient client = this.mqttClient.getMqttClient();
        double[] stick = {0.0, 0.0};
        double[] rotate = {-1.0};   // Simulating a left rotation
        final var payload = 
            new ChassisInboundPayload.ChassisPayload(stick, false, false, false, false, rotate);
        final var inboundPayload = new ChassisInboundPayload("chassis", payload);

        client.publishWith()
            .topic(chassisProperties.upstream().inbound())
            .payload(objectMapper.writeValueAsBytes(inboundPayload))
            .send().join();

        
        // WHEN the controller recieves and processes the message
        // it sends the message downstream, so the integration test receives in this::subscriptionCallabck
        ChassisPwmOutboundPayload receivedPayload = receivedMessages.poll(5, TimeUnit.SECONDS);

        // THEN the downstream should receive the expected message
        assertNotNull("Received payload should not be null", receivedPayload);
        assertEquals("Should receive only one message, no other message left in the queue", 0, receivedMessages.size());
        
        final var outPayload = receivedPayload.payload();
        assertEquals("Front left motor speed should be -255", -255, outPayload.fl());
        assertEquals("Rear left motor speed should be -255", -255, outPayload.rl());
        assertEquals("Front right motor speed should be 255", 255, outPayload.fr());
        assertEquals("Rear right motor speed should be 255", 255, outPayload.rr());
    }


    private void subscriptionCallabck(Mqtt5Publish mqtt5Publish) {
        try {
            var payload = objectMapper.readValue(
                mqtt5Publish.getPayloadAsBytes(), ChassisPwmOutboundPayload.class);
            receivedMessages.offer(payload);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize payload", e);
        }
    }
}
