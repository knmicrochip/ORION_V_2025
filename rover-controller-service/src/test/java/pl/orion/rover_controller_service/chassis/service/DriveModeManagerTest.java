package pl.orion.rover_controller_service.chassis.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import pl.orion.rover_controller_service.chassis.model.ChassisInboundPayload;

class DriveModeManagerTest {
    
    @Mock
    private DriveModeStrategy pwmStrategy;
    
    @Mock
    private DriveModeStrategy cflStrategy;
    
    @Mock
    private DriveModeStrategy rosStrategy;
    
    private DriveModeManager driveModeManager;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Configure mock strategies
        when(pwmStrategy.getModeName()).thenReturn("pwm");
        when(cflStrategy.getModeName()).thenReturn("cfl");
        when(rosStrategy.getModeName()).thenReturn("ros");
        
        // Create list of strategies
        List<DriveModeStrategy> strategies = Arrays.asList(pwmStrategy, cflStrategy, rosStrategy);
        
        // Create DriveModeManager with mocked strategies
        driveModeManager = new DriveModeManager(strategies);
    }
    
    @Test
    void testGetCurrentModeName() {
        // By default, PWM mode should be selected
        assertEquals("pwm", driveModeManager.getCurrentModeName());
    }
    
    @Test
    void testGetAvailableModes() {
        String[] availableModes = driveModeManager.getAvailableModes();
        assertEquals(3, availableModes.length);
        assertTrue(Arrays.asList(availableModes).contains("pwm"));
        assertTrue(Arrays.asList(availableModes).contains("cfl"));
        assertTrue(Arrays.asList(availableModes).contains("ros"));
    }
    
    @Test
    void testSwitchMode() {
        // Switch to CFL mode
        assertTrue(driveModeManager.switchMode("cfl"));
        assertEquals("cfl", driveModeManager.getCurrentModeName());
        
        // Switch to ROS mode
        assertTrue(driveModeManager.switchMode("ros"));
        assertEquals("ros", driveModeManager.getCurrentModeName());
        
        // Switch back to PWM mode
        assertTrue(driveModeManager.switchMode("pwm"));
        assertEquals("pwm", driveModeManager.getCurrentModeName());
    }
    
    @Test
    void testSwitchToUnknownMode() {
        // Try to switch to an unknown mode
        assertFalse(driveModeManager.switchMode("unknown"));
        // Current mode should remain unchanged
        assertEquals("pwm", driveModeManager.getCurrentModeName());
    }
    
    @Test
    void testProcessWithCurrentMode() {
        // Create a test payload
        double[] stick = {0.0, 1.0};
        double[] rotate = {0.0};
        ChassisInboundPayload.ChassisPayload payload = 
            new ChassisInboundPayload.ChassisPayload(stick, false, false, false, false, rotate);
        ChassisInboundPayload inboundPayload = new ChassisInboundPayload("chassis", payload);
        
        // Set up mock to return a specific result
        Object expectedResult = new Object();
        when(pwmStrategy.process(inboundPayload)).thenReturn(expectedResult);
        
        // Process the payload
        Object result = driveModeManager.process(inboundPayload);
        
        // Verify that the PWM strategy was called
        verify(pwmStrategy).process(inboundPayload);
        // Verify that the result is what we expected
        assertSame(expectedResult, result);
    }
    
    @Test
    void testProcessWithSwitchedMode() {
        // Create a test payload
        double[] stick = {0.0, 1.0};
        double[] rotate = {0.0};
        ChassisInboundPayload.ChassisPayload payload = 
            new ChassisInboundPayload.ChassisPayload(stick, false, false, false, false, rotate);
        ChassisInboundPayload inboundPayload = new ChassisInboundPayload("chassis", payload);
        
        // Switch to CFL mode
        driveModeManager.switchMode("cfl");
        
        // Set up mock to return a specific result
        Object expectedResult = new Object();
        when(cflStrategy.process(inboundPayload)).thenReturn(expectedResult);
        
        // Process the payload
        Object result = driveModeManager.process(inboundPayload);
        
        // Verify that the CFL strategy was called
        verify(cflStrategy).process(inboundPayload);
        // Verify that the result is what we expected
        assertSame(expectedResult, result);
    }
    
    @Test
    void testProcessWithNoActiveStrategy() {
        // Create DriveModeManager with empty list of strategies
        DriveModeManager emptyManager = new DriveModeManager(Arrays.asList());
        
        // Create a test payload
        double[] stick = {0.0, 1.0};
        double[] rotate = {0.0};
        ChassisInboundPayload.ChassisPayload payload = 
            new ChassisInboundPayload.ChassisPayload(stick, false, false, false, false, rotate);
        ChassisInboundPayload inboundPayload = new ChassisInboundPayload("chassis", payload);
        
        // Processing should throw an exception
        assertThrows(IllegalStateException.class, () -> emptyManager.process(inboundPayload));
    }
}
