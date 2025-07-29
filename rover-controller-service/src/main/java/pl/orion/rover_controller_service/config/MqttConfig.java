package pl.orion.rover_controller_service.config;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.Name;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;

@Configuration
@ConfigurationProperties(prefix = "mqtt")
public class MqttConfig {

        private String clientId;

        @Name("broker.url")
        private String brokerUrl;

        @Name("broker.port")
        private int brokerPort;

        @Name("broker.username")
        private String brokerUsername;

        @Name("broker.password")
        private String brokerPassword;

        @Name("connection.timeout.ms")
        private long connectionTimeoutMs;

        @Name("connection.keepalive.ms")
        private long connectionKeepaliveMs;

        @Name("connection.reconnect.delay.ms")
        private long connectionReconnectDelayMs;

        @Bean
        public Mqtt5AsyncClient mqttClient() {
            return MqttClient.builder()
                .useMqttVersion5()
                .automaticReconnect()
                    .maxDelay(connectionReconnectDelayMs, TimeUnit.MILLISECONDS)
                    .applyAutomaticReconnect()
                .simpleAuth()
                    .username(brokerUsername)
                    .password(brokerPassword.getBytes())
                    .applySimpleAuth()
                .identifier(clientId+ "-" + UUID.randomUUID())
                .serverHost(brokerUrl)
                .serverPort(brokerPort)
                .buildAsync();
        }
}
