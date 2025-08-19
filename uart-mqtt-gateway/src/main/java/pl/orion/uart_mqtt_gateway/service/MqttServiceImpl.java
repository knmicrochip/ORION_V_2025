package pl.orion.uart_mqtt_gateway.service;

import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.orion.uart_mqtt_gateway.config.UartMqttGatewayProperties;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class MqttServiceImpl implements MqttService {

    private final UartMqttGatewayProperties properties;
    private Mqtt5AsyncClient client;

    @Override
    public void connect() {
        log.info("Attempting to connect to MQTT broker under URL={}@{}:{}", properties.getMqtt().getBrokerUsername(), properties.getMqtt().getBrokerUrl(), properties.getMqtt().getBrokerPort());

        client = MqttClient.builder()
                .useMqttVersion5()
                .automaticReconnect()
                    .maxDelay(properties.getMqtt().getConnectionReconnectDelayMs(), TimeUnit.MILLISECONDS)
                    .applyAutomaticReconnect()
                .simpleAuth()
                    .username(properties.getMqtt().getBrokerUsername())
                    .password(properties.getMqtt().getBrokerPassword().getBytes())
                    .applySimpleAuth()
                .identifier(properties.getMqtt().getClientId() + "-" + UUID.randomUUID())
                .serverHost(properties.getMqtt().getBrokerUrl())
                .serverPort(properties.getMqtt().getBrokerPort())
                .buildAsync();

        client.connectWith()
                .keepAlive((int) TimeUnit.MILLISECONDS.toSeconds(properties.getMqtt().getConnectionKeepaliveMs()))
                .send()
                .whenComplete((connAck, throwable) -> {
                    if (throwable != null) {
                        log.error("Failed to connect to MQTT broker", throwable);
                    } else {
                        log.info("Connected to MQTT broker");
                    }
                }).join();
    }

    @Override
    public void disconnect() {
        client.disconnect();
    }

    @Override
    public void publish(String topic, String payload) {
        client.publishWith()
                .topic(topic)
                .qos(MqttQos.AT_LEAST_ONCE)
                .payload(payload.getBytes())
                .send();
    }

    @Override
    public void subscribe(String topic, MqttMessageHandler handler) {
        client.subscribeWith()
                .topicFilter(topic)
                .qos(MqttQos.AT_LEAST_ONCE)
                .callback(publish -> handler.handleMessage(topic, new String(publish.getPayloadAsBytes())))
                .send();
    }

    @Override
    public void unsubscribe(String topic) {
        client.unsubscribeWith()
            .topicFilter(topic)
            .send();
    }
}