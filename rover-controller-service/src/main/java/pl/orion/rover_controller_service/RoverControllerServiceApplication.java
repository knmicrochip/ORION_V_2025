package pl.orion.rover_controller_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.orion.rover_controller_service.config.MqttConfig;

@SpringBootApplication
@EnableConfigurationProperties(MqttConfig.class)
@RequiredArgsConstructor
@Slf4j
public class RoverControllerServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(RoverControllerServiceApplication.class, args);
	}
}
