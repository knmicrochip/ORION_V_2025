package pl.orion.rover_controller_service.manipulator.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ManipulatorInboundPayload(
        String eventType,
        String mode,
        Payload payload
) {
    public record Payload(
            double rotate_turret,
            double flex_arm,
            double flex_forearm,
            double flex_gripper,
            double rotate_gripper,
            double grip,
            @JsonProperty("button_x")
            boolean buttonX,
            @JsonProperty("button_y")
            boolean buttonY,
            @JsonProperty("button_a")
            boolean buttonA,
            @JsonProperty("button_b")
            boolean buttonB
    ) {}
}
