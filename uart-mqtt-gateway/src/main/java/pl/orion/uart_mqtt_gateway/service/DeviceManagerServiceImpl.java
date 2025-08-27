package pl.orion.uart_mqtt_gateway.service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.fazecast.jSerialComm.SerialPort;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.orion.uart_mqtt_gateway.actuator.UartHealthIndicator;
import pl.orion.uart_mqtt_gateway.config.UartMqttGatewayProperties;

@Service
@Primary
@RequiredArgsConstructor
@Slf4j
public class DeviceManagerServiceImpl implements DeviceManagerService {

    private static final int SERIAL_EVENT_TYPE_CONNECTION_INTERVAL_MS = 2000;
    private static final int LIVENESS_TIMEOUT_THRESHOLD_MS = 15000;

    private final UartHealthIndicator uartHealthIndicator;
    private final UartMqttGatewayProperties properties;
    private final MqttService mqttService;

    private Map<String, DeviceHandler> managedDevices = new ConcurrentHashMap<>(5);

    @PreDestroy
    @Override
    public void stop() {
        managedDevices.values().forEach(DeviceHandler::stop);
    }

    @Override
    @Scheduled(fixedRateString = "${uart-mqtt-gateway.serial.scan-interval-ms}", initialDelayString = "${uart-mqtt-gateway.serial.scan-interval-ms}")
    public void scan() {
        List<SerialPort> availablePorts = getAllAvailablePorts();
        log.debug("Port candidates for processing={}", availablePorts.stream().map(SerialPort::getSystemPortPath).collect(Collectors.toList()));

        final var unconnectedPorts = availablePorts.stream()
            .filter(port -> !managedDevices.containsKey(port.getSystemPortPath()))
            .toList();

        final var unidentifiedUnconnectedDevices = unconnectedPorts.stream()
            .map(this::startDeviceIdentification);
        final var identificationPendingDevices = Stream.concat(
                unidentifiedUnconnectedDevices,
                managedDevices.values().stream()
                    .filter(device -> device.getState() == DeviceConnState.IDENTIFYING)
            )
            .toList();

        log.info("Scanning for UART devices... Pending connection={}, pending_identification={}, connected={}",
            unconnectedPorts.stream().map(SerialPort::getSystemPortPath).toList(),
            identificationPendingDevices.stream().map(DeviceHandler::getSystemPortPath).toList(),
            managedDevices.keySet());

        for (DeviceHandler device : identificationPendingDevices) {
            managedDevices.put(device.getSystemPortPath(), device);

            try {
                final String eventType = device.getEventType().get(SERIAL_EVENT_TYPE_CONNECTION_INTERVAL_MS, TimeUnit.MILLISECONDS);
                log.info("Device [{}] has been identified as [{}]", device.getSystemPortPath(), eventType);
                uartHealthIndicator.clearUartDetectionFailure(device.getSystemPortPath());
            }
            catch (Exception e) {
                log.error("Device [{}]. Cannot identify device under port. Reason: {}", device.getSystemPortPath(), e.toString());
                uartHealthIndicator.registerUartDetectionFailure(device.getSystemPortPath());
            }
        }
    }

    @Scheduled(fixedRateString = "${uart-mqtt-gateway.serial.remove-disconnected-interval-ms}", initialDelayString = "${uart-mqtt-gateway.serial.remove-disconnected-interval-ms}")
    public void removeDisconnectedDevices() {
        log.trace("Running garbage collection on unused and/or dead devices");
        for (Entry<String, DeviceHandler> entry : managedDevices.entrySet()) {
            final var device = entry.getValue();
            if (device.getState() == DeviceConnState.DISCONNECTED
                    || !isDeviceAlive(device)) {
                        device.stop();
                        managedDevices.remove(entry.getKey());
                        log.info("[Device={}, eventType={}] has been removed as no longer active", entry.getKey(), device.getEventType().getNow("unknown"));
                    }
        }
    }

    public void removeDevice(String portPath) {
        final var removedDevice = managedDevices.remove(portPath);
        log.info("[Device={}, eventType={}] has been removed on disconnect.", portPath, removedDevice.getEventType().getNow("unknown"));
    }

    private DeviceHandler startDeviceIdentification(SerialPort port) {
        DeviceHandler handler = new DeviceHandler(port, properties, mqttService, this::removeDevice);
        handler.start();
        return handler;
    }

    private boolean isDeviceAlive(DeviceHandler device) {
        final var now = TimeService.getCurrentTimeMillis();
        final var lastSerialMsgReceivedTimestamp = device.getLastSerialMsgReceivedTimestamp();
        final var diff = now - lastSerialMsgReceivedTimestamp;

        log.trace("[Device={}] Last serial message received timestamp [{}], now [{}], diff [{}] return result={}", device.getSystemPortPath(), lastSerialMsgReceivedTimestamp, now, diff, diff < LIVENESS_TIMEOUT_THRESHOLD_MS);
        return diff < LIVENESS_TIMEOUT_THRESHOLD_MS;
    }

    private List<SerialPort> getAllAvailablePorts() {
        List<SerialPort> availablePorts = Arrays.asList(SerialPort.getCommPorts());

        log.debug("Detected all port paths: {}", availablePorts.stream()
            .map(SerialPort::getSystemPortPath)
            .toList()
        );

        return Stream.concat(
            availablePorts.stream()
                .filter(port -> properties.getSerial().getAllowedPortPrefixPattern().stream()
                    .peek(prefix -> log.trace("[PREFIX] PORT=[{}], prefix=[{}]", port.getSystemPortPath(), prefix))
                    .anyMatch(prefix -> port.getSystemPortPath().startsWith(prefix))),
            availablePorts.stream()
                .filter(port -> properties.getSerial().getAllowedPortRegexPattern().stream()
                    .peek(regex -> log.trace("[REGEX] PORT=[{}], regex=[{}]", port.getSystemPortPath(), regex))
                    .anyMatch(regex -> port.getSystemPortPath().matches(regex)))
            )
            .collect(Collectors.toList());
    }
}
