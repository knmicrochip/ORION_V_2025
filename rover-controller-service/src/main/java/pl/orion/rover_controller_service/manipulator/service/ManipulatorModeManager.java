package pl.orion.rover_controller_service.manipulator.service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import pl.orion.rover_controller_service.config.MqttClient;
import pl.orion.rover_controller_service.manipulator.config.ManipulatorProperties;
import pl.orion.rover_controller_service.manipulator.model.ManipulatorInboundPayload;

@Service
public class ManipulatorModeManager {

    private static final String DEFAULT_MODE = "PWM";
    private final MqttClient mqttClient;
    private final ManipulatorProperties manipulatorProperties;
    private final Map<String, ManipulatorModeStrategy> strategies;

    public ManipulatorModeManager(MqttClient mqttClient, 
                                    ManipulatorProperties manipulatorProperties,
                                    List<ManipulatorModeStrategy> strategies) {
        this.mqttClient = mqttClient;
        this.manipulatorProperties = manipulatorProperties;
        this.strategies = strategies.stream()
                .collect(Collectors.toMap(ManipulatorModeStrategy::getMode, Function.identity()));
    }

    public void handle(ManipulatorInboundPayload payload) {
        String mode = Optional.ofNullable(payload.mode()).orElse(DEFAULT_MODE);
        Optional.ofNullable(strategies.get(mode))
                .map(strategy -> strategy.handle(payload))
                .filter(Objects::nonNull)
                .ifPresent(this::send);
    }

    private void send(byte[] payload) {
        mqttClient.getMqttClient().publishWith()
                .topic(manipulatorProperties.downstream().inbound())
                .payload(payload)
                .send();
    }
}
