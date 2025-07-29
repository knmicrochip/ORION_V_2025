package pl.orion.rover_controller_service.chassis.model;

public record ChassisPwmOutboundPayload(
    String eventType,
    String mode,
    ChassisPwmPayload payload
) {
    public record ChassisPwmPayload(
        short fl,  // front-left wheel PWM in range [-255, 255]
        short fr,  // front-right wheel PWM in range [-255, 255]
        short rl,  // rear-left wheel PWM in range [-255, 255]
        short rr   // rear-right wheel PWM in range [-255, 255]
    ) {
    }
}
