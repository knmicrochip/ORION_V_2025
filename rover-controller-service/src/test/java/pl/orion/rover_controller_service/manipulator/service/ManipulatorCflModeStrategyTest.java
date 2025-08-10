package pl.orion.rover_controller_service.manipulator.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import pl.orion.rover_controller_service.manipulator.model.ManipulatorCflOutboundPayload;
import pl.orion.rover_controller_service.manipulator.model.ManipulatorInboundPayload;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ActiveProfiles("test")
@SpringBootTest
class ManipulatorCflModeStrategyTest {

    @Autowired
    private ManipulatorCflModeStrategy cflModeStrategy;

    @Autowired
    @Qualifier("manipulatorObjectMapper")
    private ObjectMapper objectMapper;

    @Test
    void handle_shouldSendCflPayload() throws Exception {
        // Given
        ManipulatorInboundPayload inboundPayload = new ManipulatorInboundPayload(
                "manipulator",
                "CFL",
                new ManipulatorInboundPayload.Payload(0.5, 0.6, 0.7, 0.8, 0.9, 1.0, false, false, false, false)
        );

        // When
        final var receivedMessage = cflModeStrategy.handle(inboundPayload);

        // Then
        assertNotNull(receivedMessage);
        ManipulatorCflOutboundPayload receivedPayload = objectMapper.readValue(receivedMessage, ManipulatorCflOutboundPayload.class);
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