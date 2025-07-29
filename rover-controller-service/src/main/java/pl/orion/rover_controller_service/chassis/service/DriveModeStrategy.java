package pl.orion.rover_controller_service.chassis.service;

import pl.orion.rover_controller_service.chassis.model.ChassisInboundPayload;

public interface DriveModeStrategy {
    /**
     * Process the inbound payload and return the appropriate outbound payload
     * based on the specific drive mode implementation.
     *
     * @param payload The inbound payload containing joystick and button data
     * @return The outbound payload specific to this drive mode
     */
    Object process(ChassisInboundPayload payload);
    
    /**
     * Get the name of this drive mode.
     *
     * @return The mode name (e.g., "pwm", "cfl", "ros")
     */
    String getModeName();
}
