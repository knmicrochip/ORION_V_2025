package pl.orion.rover_controller_service.manipulator.service;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;

import pl.orion.rover_controller_service.config.MqttClient;
import pl.orion.rover_controller_service.manipulator.config.ManipulatorProperties;
import pl.orion.rover_controller_service.manipulator.model.ManipulatorInboundPayload;


class ManipulatorModeManagerTest {

    @Mock
    private ManipulatorPwmModeStrategy manipulatorPwmModeStrategy;

    @Mock
    private ManipulatorCflModeStrategy manipulatorCflModeStrategy;

    @Autowired
    private MqttClient mqttClient;

    @Autowired
    private ManipulatorProperties manipulatorProperties;

    private ManipulatorModeManager manipulatorModeManager;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(manipulatorPwmModeStrategy.getMode()).thenReturn("PWM");
        when(manipulatorCflModeStrategy.getMode()).thenReturn("CFL");
        manipulatorModeManager = new ManipulatorModeManager(mqttClient,
                                                             manipulatorProperties,
                                                             List.of(manipulatorPwmModeStrategy, manipulatorCflModeStrategy));
    }

    @Test
    void handle_shouldCallPwmStrategy_whenModeIsPwm() {
        // Given
        ManipulatorInboundPayload payload = new ManipulatorInboundPayload("manipulator", "PWM", null);

        // When
        manipulatorModeManager.handle(payload);

        // Then
        verify(manipulatorPwmModeStrategy).handle(payload);
        verify(manipulatorCflModeStrategy, never()).handle(payload);
    }

    @Test
    void handle_shouldCallCflStrategy_whenModeIsCfl() {
        // Given
        ManipulatorInboundPayload payload = new ManipulatorInboundPayload("manipulator", "CFL", null);

        // When
        manipulatorModeManager.handle(payload);

        // Then
        verify(manipulatorCflModeStrategy).handle(payload);
        verify(manipulatorPwmModeStrategy, never()).handle(payload);
    }

    @Test
    void handle_shouldCallPwmStrategy_whenModeIsNull() {
        // Given
        ManipulatorInboundPayload payload = new ManipulatorInboundPayload("manipulator", null, null);

        // When
        manipulatorModeManager.handle(payload);

        // Then
        verify(manipulatorPwmModeStrategy).handle(payload);
        verify(manipulatorCflModeStrategy, never()).handle(payload);
    }

    @Test
    void handle_shouldDoNothing_whenModeIsUnknown() {
        // Given
        ManipulatorInboundPayload payload = new ManipulatorInboundPayload("manipulator", "UNKNOWN", null);

        // When
        manipulatorModeManager.handle(payload);

        // Then
        verify(manipulatorPwmModeStrategy, never()).handle(payload);
        verify(manipulatorCflModeStrategy, never()).handle(payload);
    }
}
