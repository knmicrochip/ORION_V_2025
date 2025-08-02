package pl.orion.rover_controller_service.config;

import org.junit.Rule;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.stereotype.Component;
import org.testcontainers.hivemq.HiveMQContainer;
import org.testcontainers.utility.DockerImageName;

import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;


@TestConfiguration
@Component
public class MqttClient {

    @Rule
    private final HiveMQContainer hiveTestContainer = new HiveMQContainer(DockerImageName.parse("hivemq/hivemq-ce:latest"));

    private final Mqtt5AsyncClient mqtt5AsyncClient;

    public MqttClient() {
        hiveTestContainer.start();
        this.mqtt5AsyncClient = com.hivemq.client.mqtt.MqttClient.builder()
                .serverHost(hiveTestContainer.getHost())
                .serverPort(hiveTestContainer.getFirstMappedPort())
                .useMqttVersion5()
                .identifier("test-client")
                .buildAsync();
        this.mqtt5AsyncClient.connect().join();
    }

    public synchronized Mqtt5AsyncClient mqttClient() {
        return this.mqtt5AsyncClient;
    }
}
