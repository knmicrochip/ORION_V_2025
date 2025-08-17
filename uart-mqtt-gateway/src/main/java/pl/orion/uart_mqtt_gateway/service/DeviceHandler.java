package pl.orion.uart_mqtt_gateway.service;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortEvent;
import com.fazecast.jSerialComm.SerialPortMessageListener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.orion.uart_mqtt_gateway.config.UartMqttGatewayProperties;

@RequiredArgsConstructor
@Slf4j
public class DeviceHandler implements MqttMessageHandler, SerialPortMessageListener {
    private final SerialPort serialPort;
    private final UartMqttGatewayProperties properties;
    private final MqttService mqttService;

    private AtomicBoolean connected = new AtomicBoolean(false);
    private CompletableFuture<String> eventType = new CompletableFuture<>();
    private UartMqttGatewayProperties.UartMqttMapping.MqttMapping mqttTopics = null;

    public void start() {
        serialPort.setBaudRate(properties.getSerial().getBaudRate()); // Example baud rate, can be configured
        serialPort.setNumDataBits(properties.getSerial().getDataBits());
        serialPort.setNumStopBits(properties.getSerial().getStopBits());
        serialPort.setParity(properties.getSerial().getParityBit());
        serialPort.openPort();
        serialPort.addDataListener(this);
        connected.set(true);
    }

    public void stop() {
        mqttService.unsubscribe(mqttTopics.getInbound());
        serialPort.removeDataListener();
        serialPort.closePort();
        connected.set(false);
    }

    @Override
    public void handleMessage(String topic, String payload) {
        log.debug("Received MQTT message on topic {}: {}", topic, payload);
        serialPort.writeBytes(payload.getBytes(), payload.length());
    }

    @Override
    public int getListeningEvents() {
        return SerialPort.LISTENING_EVENT_DATA_RECEIVED
                | SerialPort.LISTENING_EVENT_PORT_DISCONNECTED;
    }

    public boolean isConnected() {
        return connected.get();
    }

    public CompletableFuture<String> getEventType() {
        return eventType;
    }

    @Override
    public boolean delimiterIndicatesEndOfMessage() {
        return true;
    }

    @Override
    public byte[] getMessageDelimiter() {
        return properties.getSerial().getDelimiter().getBytes();
    }

    @Override
    public void serialEvent(SerialPortEvent event) {
        switch (event.getEventType()) {
            case SerialPort.LISTENING_EVENT_DATA_RECEIVED:
                processSerialData(event.getReceivedData());
                break;
            case SerialPort.LISTENING_EVENT_PORT_DISCONNECTED:
                log.info("Serial port {} disconnected", serialPort.getSystemPortPath());
                stop();
                break;
            default:
                // Handle other event types if necessary
                break;
        }
    }

    private void processSerialData(byte[] data) {
        if (data == null || data.length == 0) {
            log.warn("Received empty data from serial port");
            return;
        }
        if (!identifyEventType(data)) {
            log.warn("Failed to identify event type for data. Payload missing or malformed");
            return;
        }

        if (mqttTopics != null) {
            mqttService.publish(mqttTopics.getOutbound(), new String(data));
        }
    }

    private boolean identifyEventType(byte[] data) {
        if (!eventType.isDone()) {
            try {
                JsonNode jsonNode = new ObjectMapper().readTree(data);
                if (jsonNode.has("eventType")) {
                    final String eventTypeString = jsonNode.get("eventType").asText();
                    mqttTopics = properties.getUartMqttMapping().stream()
                        .filter(mapping -> mapping.getEventType().equals(eventTypeString))
                        .findFirst()
                        .map(UartMqttGatewayProperties.UartMqttMapping::getMqtt)
                        .orElseThrow(() -> new IllegalArgumentException("No mapping found for event type: " + eventTypeString));
                    eventType.complete(eventTypeString);
                    mqttService.subscribe(mqttTopics.getInbound(), this);
                    connected.set(true);
                    log.info("Event type detected: {} under port: {}", eventTypeString, serialPort.getSystemPortName());
                }
            } catch (IOException e) {
                log.warn("Failed to parse serial data as JSON", e);
                return false;
            }
        }
        return true;
    }
}
