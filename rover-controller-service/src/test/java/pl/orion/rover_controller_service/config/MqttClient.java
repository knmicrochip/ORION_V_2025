package pl.orion.rover_controller_service.config;

import org.junit.Rule;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.context.annotation.Scope;
import org.testcontainers.hivemq.HiveMQContainer;
import org.testcontainers.utility.DockerImageName;

import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@TestComponent
@Scope("prototype") 
public class MqttClient implements DisposableBean {

    @Rule
    private final HiveMQContainer hiveTestContainer = new HiveMQContainer(DockerImageName.parse("hivemq/hivemq-ce:latest"));

    private final Mqtt5AsyncClient mqtt5AsyncClient;

    public MqttClient(MqttConfig mqttConfig) {

        hiveTestContainer.start();
        this.mqtt5AsyncClient = com.hivemq.client.mqtt.MqttClient.builder()
                .serverHost(hiveTestContainer.getHost())
                .serverPort(hiveTestContainer.getFirstMappedPort())
                .useMqttVersion5()
                .identifier("test-client")
                .buildAsync();
    }

    public synchronized Mqtt5AsyncClient getMqttClient() {
        return this.mqtt5AsyncClient;
    }

    @PostConstruct
    public void connect() {
        this.mqtt5AsyncClient.connect().join();
    }

    @Override
    public void destroy() {
        mqtt5AsyncClient.disconnect().join();
    }

    
}
