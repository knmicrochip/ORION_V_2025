package pl.orion.rover_controller_service.manipulator.service;

import pl.orion.rover_controller_service.manipulator.model.ManipulatorInboundPayload;

public interface ManipulatorModeStrategy {
    byte[] handle(ManipulatorInboundPayload payload);
    String getMode();
}
