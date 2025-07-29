package pl.orion.rover_controller_service.chassis.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pl.orion.rover_controller_service.chassis.model.ChassisInboundPayload;
import pl.orion.rover_controller_service.chassis.model.ChassisRosOutboundPayload;

@Service
public class RosModeStrategy implements DriveModeStrategy {
    private static final Logger logger = LoggerFactory.getLogger(RosModeStrategy.class);
    
    @Override
    public Object process(ChassisInboundPayload payload) {
        logger.debug("Processing payload in ROS mode: {}", payload);
        
        // TODO: Implement ROS mode logic
        // This is a placeholder implementation
        ChassisRosOutboundPayload.ChassisRosPayload rosPayload = 
            new ChassisRosOutboundPayload.ChassisRosPayload(new double[]{0.0, 0.0, 0.0}, new double[]{0.0, 0.0, 0.0});
        
        return new ChassisRosOutboundPayload("chassis", "ros", rosPayload);
    }
    
    @Override
    public String getModeName() {
        return "ros";
    }
}
