package pl.orion.rover_controller_service.manipulator.model;

public record ManipulatorPwmOutboundPayload(
        String eventType,
        String mode,
        Payload payload
) {
    public record Payload(
            byte rotate_turret,
            byte flex_forearm,
            byte flex_arm,
            byte flex_gripper,
            byte rotate_gripper,
            byte end_effector
    ) {}
}
