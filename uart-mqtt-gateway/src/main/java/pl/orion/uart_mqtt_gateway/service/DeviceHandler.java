package pl.orion.uart_mqtt_gateway.service;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

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
    private final Consumer<String> disconnectedHook;

    private final AtomicLong lastSerialMsgReceivedTimestamp = new AtomicLong(TimeService.getCurrentTimeMillis());
    private DeviceConnState state = DeviceConnState.UNKNOWN;
    private CompletableFuture<String> eventType = new CompletableFuture<>();
    private UartMqttGatewayProperties.UartMqttMapping.MqttMapping mqttTopics = null;


    public void start() {
        serialPort.setBaudRate(properties.getSerial().getBaudRate()); // Example baud rate, can be configured
        serialPort.setNumDataBits(properties.getSerial().getDataBits());
        serialPort.setNumStopBits(properties.getSerial().getStopBits());
        serialPort.setParity(properties.getSerial().getParityBit());
        serialPort.openPort();
        serialPort.addDataListener(this);
        this.state = DeviceConnState.IDENTIFYING;
        this.lastSerialMsgReceivedTimestamp.set(TimeService.getCurrentTimeMillis());
    }

    public void stop() {
        final var portpath = serialPort.getSystemPortPath();

        if (this.state != DeviceConnState.DISCONNECTED) {
            if (mqttTopics != null) {
                mqttService.unsubscribe(mqttTopics.getInbound());
            }
            serialPort.removeDataListener();
            serialPort.closePort();
            this.state = DeviceConnState.DISCONNECTED;
            disconnectedHook.accept(portpath);
        }
    }

    @Override
    public void handleMessage(String topic, String payload) {
        log.trace("Received MQTT message on topic {}: {}", topic, payload);
        serialPort.writeBytes(payload.getBytes(), payload.length());
    }

    @Override
    public int getListeningEvents() {
        return SerialPort.LISTENING_EVENT_DATA_RECEIVED
                | SerialPort.LISTENING_EVENT_PORT_DISCONNECTED;
    }

    public CompletableFuture<String> getEventType() {
        return eventType;
    }

    public String getSystemPortPath() {
        return serialPort.getSystemPortPath();
    }

    public Long getLastSerialMsgReceivedTimestamp() {
        return lastSerialMsgReceivedTimestamp.get();
    }

    public DeviceConnState getState() {
        return state;
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
        this.lastSerialMsgReceivedTimestamp.set(TimeService.getCurrentTimeMillis());

        switch (event.getEventType()) {
            case SerialPort.LISTENING_EVENT_DATA_RECEIVED:
                processSerialData(event.getReceivedData());
                break;
            case SerialPort.LISTENING_EVENT_PORT_DISCONNECTED:
                log.info("[Device={}, eventType={}] Device disconnected", serialPort.getSystemPortPath(), eventType.getNow("unknown"));
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
                    this.state = DeviceConnState.CONNECTED;

                    mqttService.subscribe(mqttTopics.getInbound(), this);
                    log.info("[Device={}] Detected eventType=[{}], applying MQTT topic mapping: {}", getSystemPortPath(), eventTypeString, mqttTopics); 
                }
            } catch (IOException e) {
                log.trace("[Device={}] Failed to parse incoming JSON payload: {}", getSystemPortPath(), e.toString());
                return false;
            }
        }
        return true;
    }
}
