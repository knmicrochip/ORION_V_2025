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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.orion.uart_mqtt_gateway.config.UartMqttGatewayProperties;

@Service
@Primary
@RequiredArgsConstructor
@Slf4j
public class DeviceManagerServiceImpl implements DeviceManagerService {

    private static final int SERIAL_EVENT_TYPE_CONNECTION_INTERVAL_MS = 2000;
    private static final int LIVENESS_TIMEOUT_THRESHOLD_MS = 15000;

    private final UartMqttGatewayProperties properties;
    private final MqttService mqttService;

    private Map<String, DeviceHandler> managedDevices = new ConcurrentHashMap<>(5);


    @Override
    public void stop() {
        managedDevices.values().forEach(DeviceHandler::stop);
    }

    @Override
    @Scheduled(fixedRateString = "${uart-mqtt-gateway.serial.scan-interval-ms}", initialDelayString = "${uart-mqtt-gateway.serial.scan-interval-ms}")
    public void scan() {
        List<SerialPort> availablePorts = getAllAvailablePorts();
        final var unconnectedPorts = availablePorts.stream()
            .filter(port -> !managedDevices.containsKey(port.getSystemPortPath()))
            .toList();

        log.info("Scanning for UART devices... Pending connection={}, connected={}", 
            unconnectedPorts.stream().map(SerialPort::getSystemPortPath).toList(), managedDevices.keySet());

        final var unidentifiedUnconnectedDevices = unconnectedPorts.stream()
            .map(this::startDeviceIdentification);
        final var identificationPendingDevices = Stream.concat(
                unidentifiedUnconnectedDevices,
                managedDevices.values().stream()
                    .filter(device -> device.getState() == DeviceConnState.IDENTIFYING)
            )
            .toList();

        for (DeviceHandler device : identificationPendingDevices) {
            managedDevices.put(device.getSystemPortPath(), device);

            try {
                final String eventType = device.getEventType().get(SERIAL_EVENT_TYPE_CONNECTION_INTERVAL_MS, TimeUnit.MILLISECONDS);
                log.info("Device [{}] has been identified as [{}]", device.getSystemPortPath(), eventType);
            }
            catch (Exception e) {
                log.error("Device [{}]. Cannot identify device under port. Reason: {}", device.getSystemPortPath(), e.toString());
            }
        }
    }

    @Scheduled(fixedRateString = "${uart-mqtt-gateway.serial.remove-disconnected-interval-ms}", initialDelayString = "${uart-mqtt-gateway.serial.remove-disconnected-interval-ms}")
    public void removeDisconnectedDevices() {
        for (Entry<String, DeviceHandler> entry : managedDevices.entrySet()) {
            final var device = entry.getValue();
            if (device.getState() == DeviceConnState.DISCONNECTED
                    || !isDeviceAlive(device)) {
                        device.stop();
                        managedDevices.remove(entry.getKey());
                        log.info("[Device={}] has been removed as no longer active", entry.getKey());
                    }
        }
    }

    private boolean isDeviceAlive(DeviceHandler device) {
        final var now = TimeService.getCurrentTimeMillis();
        final var lastSerialMsgReceivedTimestamp = device.getLastSerialMsgReceivedTimestamp();
        final var diff = now - lastSerialMsgReceivedTimestamp;

        log.trace("[Device={}] Last serial message received timestamp [{}], now [{}], diff [{}] return result={}", device.getSystemPortPath(), lastSerialMsgReceivedTimestamp, now, diff, diff < LIVENESS_TIMEOUT_THRESHOLD_MS);
        return diff < LIVENESS_TIMEOUT_THRESHOLD_MS;
    }

    private List<SerialPort> getAllAvailablePorts() {
        List<SerialPort> availablePorts = Arrays.asList(SerialPort.getCommPorts()).stream()
            .filter(port -> 
                    properties.getSerial().getAllowedPortNamePrefixes().stream()
                        .anyMatch(prefix -> port.getSystemPortPath().startsWith(prefix))
            )
            .collect(Collectors.toList());
        return availablePorts;
    }

    private DeviceHandler startDeviceIdentification(SerialPort port) {
        DeviceHandler handler = new DeviceHandler(port, properties, mqttService);
        handler.start();
        return handler;
    }
}
