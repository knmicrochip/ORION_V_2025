package pl.orion.rover_controller_service.chassis.model;

public record ChassisRosOutboundPayload(
    String eventType,
    String mode,
    ChassisRosPayload payload
) {
    public record ChassisRosPayload(
        double[] linear,   // Vector3 (x, y, z) expressed in m/s
        double[] angular   // Vector3 (x, y, z) expressed in rad/s
    ) {
    }
}
