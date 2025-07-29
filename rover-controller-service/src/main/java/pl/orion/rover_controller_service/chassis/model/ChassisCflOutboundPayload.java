package pl.orion.rover_controller_service.chassis.model;

public record ChassisCflOutboundPayload(
    String eventType,
    String mode,
    ChassisCflPayload payload
) {
    public record ChassisCflPayload(
        double fl,  // front-left wheel angular velocity in rad/s
        double fr,  // front-right wheel angular velocity in rad/s
        double rl,  // rear-left wheel angular velocity in rad/s
        double rr   // rear-right wheel angular velocity in rad/s
    ) {
    }
}
