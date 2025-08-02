package pl.orion.rover_controller_service.chassis.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "chassis")
public record ChassisProperties(String eventType, Topics downstream, Topics upstream) {

    public record Topics(String inbound, String outbound) {
    }
}
