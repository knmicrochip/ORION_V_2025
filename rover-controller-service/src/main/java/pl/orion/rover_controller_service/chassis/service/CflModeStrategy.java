package pl.orion.rover_controller_service.chassis.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pl.orion.rover_controller_service.chassis.model.ChassisInboundPayload;
import pl.orion.rover_controller_service.chassis.model.ChassisCflOutboundPayload;

@Service
public class CflModeStrategy implements DriveModeStrategy {
    private static final Logger logger = LoggerFactory.getLogger(CflModeStrategy.class);
    
    @Override
    public Object process(ChassisInboundPayload payload) {
        logger.debug("Processing payload in CFL mode: {}", payload);
        
        // TODO: Implement CFL mode logic
        // This is a placeholder implementation
        ChassisCflOutboundPayload.ChassisCflPayload cflPayload = 
            new ChassisCflOutboundPayload.ChassisCflPayload(0.0, 0.0, 0.0, 0.0);
        
        return new ChassisCflOutboundPayload("chassis", "cfl", cflPayload);
    }
    
    @Override
    public String getModeName() {
        return "cfl";
    }
}
