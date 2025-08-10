package pl.orion.rover_controller_service.manipulator.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import pl.orion.rover_controller_service.manipulator.config.ManipulatorProperties;
import pl.orion.rover_controller_service.manipulator.model.ManipulatorCflOutboundPayload;
import pl.orion.rover_controller_service.manipulator.model.ManipulatorInboundPayload;

@Service
public class ManipulatorCflModeStrategy implements ManipulatorModeStrategy {

    private static final Logger logger = LoggerFactory.getLogger(ManipulatorCflModeStrategy.class);

    private final ManipulatorProperties manipulatorProperties;
    private final ObjectMapper objectMapper;

    public ManipulatorCflModeStrategy(ManipulatorProperties manipulatorProperties, @Qualifier("manipulatorObjectMapper") ObjectMapper objectMapper) {
        this.manipulatorProperties = manipulatorProperties;
        this.objectMapper = objectMapper;
    }

    @Override
    public byte[] handle(ManipulatorInboundPayload payload) {
        try {
            ManipulatorCflOutboundPayload outboundPayload = toOutboundPayload(payload);
            return objectMapper.writeValueAsBytes(outboundPayload);
        } catch (JsonProcessingException e) {
            logger.error("Error serializing CFL outbound payload", e);
        }
        
        return null;
    }

    @Override
    public String getMode() {
        return "CFL";
    }

    private ManipulatorCflOutboundPayload toOutboundPayload(ManipulatorInboundPayload inboundPayload) {
        ManipulatorInboundPayload.Payload p = inboundPayload.payload();
        ManipulatorProperties.JointsProperties joints = manipulatorProperties.joints();
        ManipulatorCflOutboundPayload.Payload payload = new ManipulatorCflOutboundPayload.Payload(
                p.rotate_turret() * joints.turret_rotation().max_ang_v(),
                p.flex_forearm() * joints.forearm_flex().max_ang_v(),
                p.flex_arm() * joints.arm_flex().max_ang_v(),
                p.flex_gripper() * joints.gripper_flex().max_ang_v(),
                p.rotate_gripper() * joints.gripper_rotation().max_ang_v(),
                p.grip() * joints.end_effector_flex().max_ang_v()
        );
        return new ManipulatorCflOutboundPayload(
                manipulatorProperties.eventType(),
                getMode(),
                payload
        );
    }
}