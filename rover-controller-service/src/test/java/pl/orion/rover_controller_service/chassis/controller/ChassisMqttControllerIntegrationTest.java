package pl.orion.rover_controller_service.chassis.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(ChassisMqttController.class)
class ChassisMqttControllerIntegrationTest {
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Test
    void contextLoads() {
        // This test just verifies that the context loads successfully
        // and that the ChassisMqttController can be instantiated
    }
}
