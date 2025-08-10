package pl.orion.rover_controller_service.manipulator.model;

public record ManipulatorTelemetryInboundPayload(
        String eventType,
        String mode,
        Payload payload
) {
    public record Payload(
            byte amps_rotate_turret,
            byte amps_flex_forearm,
            byte amps_flex_arm,
            byte amps_flex_gripper,
            byte amps_rotate_gripper,
            byte amps_end_effector,
            double ang_rotate_turret,
            double ang_flex_forearm,
            double ang_flex_arm,
            double ang_flex_gripper,
            double ang_rotate_gripper,
            double ang_end_effector
    ) {}
}
