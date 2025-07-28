package pl.orion.rover_controller_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class RoverControllerServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(RoverControllerServiceApplication.class, args);
	}

}
