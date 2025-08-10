package pl.orion.rover_controller_service.manipulator.model;

public record ManipulatorCflOutboundPayload(
        String eventType,
        String mode,
        Payload payload
) {
    public record Payload(
            double rotate_turret,
            double flex_forearm,
            double flex_arm,
            double flex_gripper,
            double rotate_gripper,
            double end_effector
    ) {}
}
