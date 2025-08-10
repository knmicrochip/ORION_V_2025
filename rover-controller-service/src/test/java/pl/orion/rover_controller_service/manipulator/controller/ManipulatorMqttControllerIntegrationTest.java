package pl.orion.rover_controller_service.manipulator.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;

import pl.orion.rover_controller_service.config.MqttClient;
import pl.orion.rover_controller_service.manipulator.config.ManipulatorProperties;
import pl.orion.rover_controller_service.manipulator.model.ManipulatorCflOutboundPayload;
import pl.orion.rover_controller_service.manipulator.model.ManipulatorInboundPayload;
import pl.orion.rover_controller_service.manipulator.model.ManipulatorPwmOutboundPayload;

@ActiveProfiles("test")
@SpringBootTest
class ManipulatorMqttControllerIntegrationTest {

    @Autowired
    @Qualifier("manipulatorObjectMapper")
    private ObjectMapper objectMapper;

    @Autowired
    private MqttClient mqttClient;

    @Autowired
    private ManipulatorMqttController manipulatorMqttController;

    @Autowired
    private ManipulatorProperties manipulatorProperties;

    private static final BlockingQueue<Mqtt5Publish> receivedMessages = new LinkedBlockingQueue<>();

    @BeforeEach
    void setUp() {
        final Mqtt5AsyncClient client = this.mqttClient.getMqttClient();
        client.subscribeWith()
                .topicFilter(manipulatorProperties.downstream().inbound())
                .callback(receivedMessages::offer)
                .send().join();
    }

    @AfterEach
    void tearDown() {
        receivedMessages.clear();
        final Mqtt5AsyncClient client = this.mqttClient.getMqttClient();
        client.unsubscribeWith()
                .topicFilter(manipulatorProperties.downstream().inbound())
                .send().join();
    }

    @Test
    void testPwmMode() throws Exception {
        // Given
        ManipulatorInboundPayload inboundPayload = new ManipulatorInboundPayload(
                "manipulator",
                "PWM",
                new ManipulatorInboundPayload.Payload(0.5, 0.6, 0.7, 0.8, 0.9, 1.0, false, false, false, false)
        );

        // When
        mqttClient.getMqttClient().publishWith()
                .topic(manipulatorProperties.upstream().inbound())
                .payload(objectMapper.writeValueAsBytes(inboundPayload))
                .send().join();

        Mqtt5Publish receivedMessage = receivedMessages.poll(5, TimeUnit.SECONDS);

        // Then
        assertNotNull(receivedMessage);
        ManipulatorPwmOutboundPayload receivedPayload = objectMapper.readValue(receivedMessage.getPayloadAsBytes(), ManipulatorPwmOutboundPayload.class);
        assertEquals("manipulator", receivedPayload.eventType());
        assertEquals("PWM", receivedPayload.mode());
        assertEquals(50, receivedPayload.payload().rotate_turret());
        assertEquals(60, receivedPayload.payload().flex_arm());
        assertEquals(70, receivedPayload.payload().flex_forearm());
        assertEquals(80, receivedPayload.payload().flex_gripper());
        assertEquals(90, receivedPayload.payload().rotate_gripper());
        assertEquals(100, receivedPayload.payload().end_effector());
    }

    @Test
    void testCflMode() throws Exception {
        // Given
        ManipulatorInboundPayload inboundPayload = new ManipulatorInboundPayload(
                "manipulator",
                "CFL",
                new ManipulatorInboundPayload.Payload(0.5, 0.6, 0.7, 0.8, 0.9, 1.0, false, false, false, false)
        );

        // When
        mqttClient.getMqttClient().publishWith()
                .topic(manipulatorProperties.upstream().inbound())
                .payload(objectMapper.writeValueAsBytes(inboundPayload))
                .send().join();

        Mqtt5Publish receivedMessage = receivedMessages.poll(5, TimeUnit.SECONDS);

        // Then
        assertNotNull(receivedMessage);
        ManipulatorCflOutboundPayload receivedPayload = objectMapper.readValue(receivedMessage.getPayloadAsBytes(), ManipulatorCflOutboundPayload.class);
        assertEquals("manipulator", receivedPayload.eventType());
        assertEquals("CFL", receivedPayload.mode());
        assertEquals(0.5 * Math.PI / 10, receivedPayload.payload().rotate_turret(), 0.001);
        assertEquals(0.6 * Math.PI / 10, receivedPayload.payload().flex_arm(), 0.001);
        assertEquals(0.7 * Math.PI / 10, receivedPayload.payload().flex_forearm(), 0.001);
        assertEquals(0.8 * Math.PI / 10, receivedPayload.payload().flex_gripper(), 0.001);
        assertEquals(0.9 * Math.PI / 10, receivedPayload.payload().rotate_gripper(), 0.001);
        assertEquals(1.0 * Math.PI / 10, receivedPayload.payload().end_effector(), 0.001);
    }
}
