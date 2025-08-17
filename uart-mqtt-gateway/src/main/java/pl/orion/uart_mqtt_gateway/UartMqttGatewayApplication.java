package pl.orion.uart_mqtt_gateway;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

import pl.orion.uart_mqtt_gateway.config.UartMqttGatewayProperties;
import pl.orion.uart_mqtt_gateway.service.DeviceManagerService;
import pl.orion.uart_mqtt_gateway.service.DeviceManagerServiceImpl;
import pl.orion.uart_mqtt_gateway.service.MqttService;

@EnableScheduling
@SpringBootApplication
@EnableConfigurationProperties(UartMqttGatewayProperties.class)
@RequiredArgsConstructor
public class UartMqttGatewayApplication implements ApplicationRunner {

	private final MqttService mqttService;
	private final DeviceManagerService deviceManagerService;


	public static void main(String[] args) {
		SpringApplication.run(UartMqttGatewayApplication.class, args);
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		mqttService.connect();
		((DeviceManagerServiceImpl) deviceManagerService).init();
	}
}