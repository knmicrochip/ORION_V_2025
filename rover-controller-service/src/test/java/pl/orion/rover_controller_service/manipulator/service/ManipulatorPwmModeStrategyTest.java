package pl.orion.rover_controller_service.manipulator.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.fasterxml.jackson.databind.ObjectMapper;

import pl.orion.rover_controller_service.manipulator.model.ManipulatorInboundPayload;
import pl.orion.rover_controller_service.manipulator.model.ManipulatorPwmOutboundPayload;

@ActiveProfiles("test")
@SpringBootTest
class ManipulatorPwmModeStrategyTest {

    @Autowired
    private ManipulatorPwmModeStrategy manipulatorPwmModeStrategy;

    @Autowired
    @Qualifier("manipulatorObjectMapper")
    private ObjectMapper objectMapper;


    @Test
    void handle_shouldSendPwmPayload() throws Exception {
        // Given
        ManipulatorInboundPayload inboundPayload = new ManipulatorInboundPayload(
                "manipulator",
                "PWM",
                new ManipulatorInboundPayload.Payload(0.5, 0.6, 0.7, 0.8, 0.9, 1.0, false, false, false, false)
        );

        // When
        final var receivedMessage = manipulatorPwmModeStrategy.handle(inboundPayload);

        // Then
        assertNotNull(receivedMessage);
        ManipulatorPwmOutboundPayload receivedPayload = objectMapper.readValue(receivedMessage, ManipulatorPwmOutboundPayload.class);
        assertEquals("manipulator", receivedPayload.eventType());
        assertEquals("PWM", receivedPayload.mode());
        assertEquals(50, receivedPayload.payload().rotate_turret());
        assertEquals(60, receivedPayload.payload().flex_arm());
        assertEquals(70, receivedPayload.payload().flex_forearm());
        assertEquals(80, receivedPayload.payload().flex_gripper());
        assertEquals(90, receivedPayload.payload().rotate_gripper());
        assertEquals(100, receivedPayload.payload().end_effector());
    }
}