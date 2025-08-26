package pl.orion.uart_mqtt_gateway.config;

import lombok.Data;
import lombok.Getter;
import lombok.AccessLevel;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.Name;

import java.util.ArrayList;
import java.util.List;


@ConfigurationProperties(prefix = "uart-mqtt-gateway")
@Data
public class UartMqttGatewayProperties {
    private String errorTopic;

    private Mqtt mqtt;
    private Serial serial;
    private List<UartMqttMapping> uartMqttMapping;

    @Data
    public static class Mqtt {
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
    }

    @Data
    public static class Serial {
        private int scanIntervalMs;
        private List<String> allowedPortNamePrefixes;
        private int baudRate;
        private int dataBits;
        private int stopBits;
        private int parityBit;
        private int readTimeoutMs;
        private int writeTimeoutMs;
        private int bufferSize;
        private String delimiter;

        private static final String REGEX_PREFIX = "regex:";

        @Getter(AccessLevel.NONE)
        private static final List<String> ALLOWED_PORT_PREFIX_PATTERNS = new ArrayList<>();

        @Getter(AccessLevel.NONE)
        private static final List<String> ALLOWED_PORT_REGEX_PATTERNS = new ArrayList<>();

        public List<String> getAllowedPortPrefixPattern() {
            if (ALLOWED_PORT_PREFIX_PATTERNS.isEmpty()) {
                allowedPortNamePrefixes.stream()
                    .filter(name -> !name.startsWith(REGEX_PREFIX))
                    .forEach(ALLOWED_PORT_PREFIX_PATTERNS::add);
            }
            return ALLOWED_PORT_PREFIX_PATTERNS;
        }

        public List<String> getAllowedPortRegexPattern() {
            if (ALLOWED_PORT_REGEX_PATTERNS.isEmpty()) {
                allowedPortNamePrefixes.stream()
                    .filter(name -> name.startsWith(REGEX_PREFIX))
                    .map(name -> name.substring(REGEX_PREFIX.length()))
                    .forEach(ALLOWED_PORT_REGEX_PATTERNS::add);
            }
            return ALLOWED_PORT_REGEX_PATTERNS;
        }
    }

    @Data
    public static class UartMqttMapping {
        private String label;
        private String eventType;
        private MqttMapping mqtt;

        @Data
        public static class MqttMapping {
            private String inbound;
            private String outbound;
        }
    }
}