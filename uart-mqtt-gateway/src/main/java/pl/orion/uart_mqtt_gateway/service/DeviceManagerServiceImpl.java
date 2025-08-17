package pl.orion.uart_mqtt_gateway.service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.fazecast.jSerialComm.SerialPort;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.orion.uart_mqtt_gateway.config.UartMqttGatewayProperties;

@Service
@Primary
@RequiredArgsConstructor
@Slf4j
public class DeviceManagerServiceImpl implements DeviceManagerService {

    private static final int SERIAL_EVENT_TYPE_CONNECTION_INTERVAL_MS = 2000;
    private static final int MAX_DETECTION_ATTEMPTS = 5;

    private final UartMqttGatewayProperties properties;
    private final MqttService mqttService;
    private final Map<String, DeviceHandler> managedDevices = new ConcurrentHashMap<>();
    private final Map<String, DeviceHandler> connectionPendingDevices = new ConcurrentHashMap<>();
    private final Map<String, Integer> blacklistedDevices = new ConcurrentHashMap<>();

    @Override
    public void stop() {
        managedDevices.values().forEach(DeviceHandler::stop);
    }

    @Override
    @Scheduled(fixedRateString = "${uart-mqtt-gateway.serial.scan-interval-ms}", initialDelayString = "${uart-mqtt-gateway.serial.scan-interval-ms}")
    public void scan() {
        List<SerialPort> availablePorts = getAllAvailablePorts();
        final var unconnectedPorts = availablePorts.stream()
            .filter(port -> !managedDevices.containsKey(port.getSystemPortName()))
            .toList();

        log.info("Scanning for UART devices. Available ports={}, ports not yet connected={}", availablePorts, unconnectedPorts);
        for (SerialPort port : unconnectedPorts) {
            detectAndProcessDevice(port);
        }

        removeDisconnectedDevices(availablePorts);
    }

    private List<SerialPort> getAllAvailablePorts() {
        List<SerialPort> availablePorts = Arrays.asList(SerialPort.getCommPorts()).stream()
            .filter(port -> 
                    properties.getSerial().getAllowedPortNamePrefixes().stream()
                        .anyMatch(prefix -> port.getSystemPortPath().startsWith(prefix))
            )
            .filter(port -> !isPortBlacklisted(port))
            .collect(Collectors.toList());
        return availablePorts;
    }

    private void detectAndProcessDevice(SerialPort port) {
        DeviceHandler handler;
        if (connectionPendingDevices.containsKey(port.getSystemPortName())) {
            handler = connectionPendingDevices.get(port.getSystemPortName());
        }
        else {
            handler = new DeviceHandler(port, properties, mqttService);
            handler.start();
            connectionPendingDevices.put(port.getSystemPortName(), handler);
        }

        try {
            String eventType = handler.getEventType().get(SERIAL_EVENT_TYPE_CONNECTION_INTERVAL_MS, TimeUnit.MILLISECONDS);
            managedDevices.put(port.getSystemPortName(), handler);
            connectionPendingDevices.remove(port.getSystemPortName());
            log.info("Started handling device: {} with event type: {}", port.getSystemPortName(), eventType);
        } catch (Exception e) {
            log.warn("Failed to get event type for device: {}", port.getSystemPortName());
            int detectionAttempt = blacklistedDevices.getOrDefault(port.getSystemPortName(), 0);
            detectionAttempt++;
            blacklistedDevices.put(port.getSystemPortName(), detectionAttempt);
            if (isPortBlacklisted(port)) {
                log.info("Port {} has been blacklisted - no further connection attempts", port.getSystemPortName());
            }
        }
    }

    private boolean isPortBlacklisted(SerialPort port) {
        return !(blacklistedDevices.getOrDefault(port.getSystemPortName(), 0) < MAX_DETECTION_ATTEMPTS);
    }

    private void removeDisconnectedDevices(List<SerialPort> availablePorts) {
        managedDevices.entrySet().stream()
                .filter(kv -> !kv.getValue().isConnected())
                .forEach(kv -> {
                    DeviceHandler handler = managedDevices.remove(kv.getKey());

                    if (blacklistedDevices.containsKey(kv.getKey())) {
                        blacklistedDevices.remove(kv.getKey());
                    }
                    handler.stop();
                    log.info("Stopped handling device and evicted it from memory: {}", kv.getKey());
                });
    }

}
