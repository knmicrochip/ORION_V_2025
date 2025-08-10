package pl.orion.rover_controller_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.beans.factory.annotation.Value;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@ConfigurationProperties
@Slf4j
@ToString
@Getter
public class MqttConfig {

        @Value("${mqtt.client-id:rover-controller-service}")
        private String clientId;

        @Value("${mqtt.broker.url:localhost}")
        private String brokerUrl;

        @Value("${mqtt.broker.port:1883}")
        private int brokerPort;

        @Value("${mqtt.broker.username:UNDEFINED_USER}")
        private String brokerUser;

        @Value("${mqtt.broker.password:UNDEFINED_PASSWORD}")
        private String brokerPassword;

        @Value("${mqtt.connection.timeout.ms:30000}")
        private long connectionTimeoutMs;

        @Value("${mqtt.connection.keepalive.ms:60000}")
        private long connectionKeepaliveMs;

        @Value("${mqtt.connection.reconnect.delay.ms:10000}")
        private long connectionReconnectDelayMs;
}
