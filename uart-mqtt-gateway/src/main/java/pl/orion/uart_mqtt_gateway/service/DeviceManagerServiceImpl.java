package pl.orion.uart_mqtt_gateway.service;

import com.fazecast.jSerialComm.SerialPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pl.orion.uart_mqtt_gateway.config.UartMqttGatewayProperties;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

@Service
@Primary
@RequiredArgsConstructor
@Slf4j
public class DeviceManagerServiceImpl implements DeviceManagerService {

    private final UartMqttGatewayProperties properties;
    private final MqttService mqttService;
    private final Map<String, DeviceHandler> managedDevices = new ConcurrentHashMap<>();

    @Override
    public void stop() {
        managedDevices.values().forEach(DeviceHandler::stop);
    }

    @Override
    @Scheduled(fixedRateString = "${uart-mqtt-gateway.serial.scan-interval-ms}", initialDelayString = "${uart-mqtt-gateway.serial.scan-interval-ms}")
    public void scan() {
        log.debug("Scanning for serial devices...");
        List<SerialPort> availablePorts = Arrays.asList(SerialPort.getCommPorts()).stream()
        .filter(port -> 
                        properties.getSerial().getAllowedPortNamePrefixes().stream()
                            .anyMatch(prefix -> port.getSystemPortPath().startsWith(prefix))
        )
        .collect(Collectors.toList());

        // Add new devices
        for (SerialPort port : availablePorts) {
            if (!managedDevices.containsKey(port.getSystemPortName())) {
                    DeviceHandler handler = new DeviceHandler(port, properties, mqttService);
                    handler.start();
                    String eventType = handler.getEventType().join();
                    log.info("Started handling device: {} with event type: {}", port.getSystemPortName(), eventType);
                    managedDevices.put(port.getSystemPortName(), handler);
                }
        }

        // Remove disconnected devices
        List<String> availablePortNames = availablePorts.stream()
            .collect(Collectors.mapping(SerialPort::getSystemPortName, Collectors.toList()));

        managedDevices.keySet().stream()
                .filter(portName -> !availablePortNames.contains(portName))
                .forEach(portName -> {
                    DeviceHandler handler = managedDevices.remove(portName);
                    handler.stop();
                    log.info("Stopped handling device: {}", portName);
                });
    }
}
