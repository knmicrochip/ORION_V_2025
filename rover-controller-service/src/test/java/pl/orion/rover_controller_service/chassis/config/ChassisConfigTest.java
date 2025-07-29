package pl.orion.rover_controller_service.chassis.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(ChassisConfig.class)
class ChassisConfigTest {
    
    @Test
    void testChassisObjectMapperBeanCreation() {
        // This test will be run in the context of the full application
        // We're just verifying that the configuration class loads correctly
    }
}
