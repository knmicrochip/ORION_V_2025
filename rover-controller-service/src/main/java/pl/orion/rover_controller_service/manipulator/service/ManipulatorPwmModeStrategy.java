package pl.orion.rover_controller_service.manipulator.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import pl.orion.rover_controller_service.manipulator.config.ManipulatorProperties;
import pl.orion.rover_controller_service.manipulator.model.ManipulatorInboundPayload;
import pl.orion.rover_controller_service.manipulator.model.ManipulatorPwmOutboundPayload;

@Service
public class ManipulatorPwmModeStrategy implements ManipulatorModeStrategy {

    private static final Logger logger = LoggerFactory.getLogger(ManipulatorPwmModeStrategy.class);

    private final ManipulatorProperties manipulatorProperties;
    private final ObjectMapper objectMapper;

    public ManipulatorPwmModeStrategy(ManipulatorProperties manipulatorProperties, @Qualifier("manipulatorObjectMapper") ObjectMapper objectMapper) {
        this.manipulatorProperties = manipulatorProperties;
        this.objectMapper = objectMapper;
    }

    @Override
    public byte[] handle(ManipulatorInboundPayload payload) {
        try {
            ManipulatorPwmOutboundPayload outboundPayload = toOutboundPayload(payload);
            return objectMapper.writeValueAsBytes(outboundPayload);
        } catch (JsonProcessingException e) {
            logger.error("Error serializing PWM outbound payload", e);
        }

        return null;
    }

    @Override
    public String getMode() {
        return "PWM";
    }

    private ManipulatorPwmOutboundPayload toOutboundPayload(ManipulatorInboundPayload inboundPayload) {
        ManipulatorInboundPayload.Payload p = inboundPayload.payload();
        ManipulatorPwmOutboundPayload.Payload payload = new ManipulatorPwmOutboundPayload.Payload(
                (byte) (p.rotate_turret() * 100),
                (byte) (p.flex_forearm() * 100),
                (byte) (p.flex_arm() * 100),
                (byte) (p.flex_gripper() * 100),
                (byte) (p.rotate_gripper() * 100),
                (byte) (p.grip() * 100)
        );
        return new ManipulatorPwmOutboundPayload(
                manipulatorProperties.eventType(),
                getMode(),
                payload
        );
    }
}