package pl.orion.rover_controller_service.chassis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.orion.rover_controller_service.chassis.model.ChassisInboundPayload;
import pl.orion.rover_controller_service.chassis.model.ChassisPwmOutboundPayload;

import static org.junit.jupiter.api.Assertions.*;

class PwmModeStrategyTest {
    
    private PwmModeStrategy pwmModeStrategy;
    
    @BeforeEach
    void setUp() {
        pwmModeStrategy = new PwmModeStrategy();
    }
    
    @Test
    void testGetModeName() {
        assertEquals("pwm", pwmModeStrategy.getModeName());
    }
    
    @Test
    void testFullSpeedForward() {
        // stick[0] = 0 (no turn), stick[1] = 1 (full forward)
        double[] stick = {0.0, 1.0};
        double[] rotate = {0.0};
        
        ChassisInboundPayload.ChassisPayload payload = 
            new ChassisInboundPayload.ChassisPayload(stick, false, false, false, false, rotate);
        ChassisInboundPayload inboundPayload = new ChassisInboundPayload("chassis", payload);
        
        ChassisPwmOutboundPayload outboundPayload = (ChassisPwmOutboundPayload) pwmModeStrategy.process(inboundPayload);
        
        assertEquals(255, outboundPayload.payload().fl());
        assertEquals(255, outboundPayload.payload().fr());
        assertEquals(255, outboundPayload.payload().rl());
        assertEquals(255, outboundPayload.payload().rr());
    }
    
    @Test
    void testFullSpeedBackward() {
        // stick[0] = 0 (no turn), stick[1] = -1 (full backward)
        double[] stick = {0.0, -1.0};
        double[] rotate = {0.0};
        
        ChassisInboundPayload.ChassisPayload payload = 
            new ChassisInboundPayload.ChassisPayload(stick, false, false, false, false, rotate);
        ChassisInboundPayload inboundPayload = new ChassisInboundPayload("chassis", payload);
        
        ChassisPwmOutboundPayload outboundPayload = (ChassisPwmOutboundPayload) pwmModeStrategy.process(inboundPayload);
        
        assertEquals(-255, outboundPayload.payload().fl());
        assertEquals(-255, outboundPayload.payload().fr());
        assertEquals(-255, outboundPayload.payload().rl());
        assertEquals(-255, outboundPayload.payload().rr());
    }
    
    @Test
    void testNoMovement() {
        // stick[0] = 0 (no turn), stick[1] = 0 (no movement)
        double[] stick = {0.0, 0.0};
        double[] rotate = {0.0};
        
        ChassisInboundPayload.ChassisPayload payload = 
            new ChassisInboundPayload.ChassisPayload(stick, false, false, false, false, rotate);
        ChassisInboundPayload inboundPayload = new ChassisInboundPayload("chassis", payload);
        
        ChassisPwmOutboundPayload outboundPayload = (ChassisPwmOutboundPayload) pwmModeStrategy.process(inboundPayload);
        
        assertEquals(0, outboundPayload.payload().fl());
        assertEquals(0, outboundPayload.payload().fr());
        assertEquals(0, outboundPayload.payload().rl());
        assertEquals(0, outboundPayload.payload().rr());
    }
    
    @Test
    void testForwardWithGentleLeftCurve() {
        // stick[0] = -0.3 (left turn), stick[1] = 0.7 (forward)
        double[] stick = {-0.3, 0.7};
        double[] rotate = {0.0};
        
        ChassisInboundPayload.ChassisPayload payload = 
            new ChassisInboundPayload.ChassisPayload(stick, false, false, false, false, rotate);
        ChassisInboundPayload inboundPayload = new ChassisInboundPayload("chassis", payload);
        
        ChassisPwmOutboundPayload outboundPayload = (ChassisPwmOutboundPayload) pwmModeStrategy.process(inboundPayload);
        
        // Left wheels should have lower PWM than right wheels
        assertTrue(outboundPayload.payload().fl() < outboundPayload.payload().fr());
        assertTrue(outboundPayload.payload().rl() < outboundPayload.payload().rr());
        
        // Both should be positive (forward)
        assertTrue(outboundPayload.payload().fl() > 0);
        assertTrue(outboundPayload.payload().fr() > 0);
        assertTrue(outboundPayload.payload().rl() > 0);
        assertTrue(outboundPayload.payload().rr() > 0);
    }
    
    @Test
    void testForwardWithGentleRightCurve() {
        // stick[0] = 0.3 (right turn), stick[1] = 0.7 (forward)
        double[] stick = {0.3, 0.7};
        double[] rotate = {0.0};
        
        ChassisInboundPayload.ChassisPayload payload = 
            new ChassisInboundPayload.ChassisPayload(stick, false, false, false, false, rotate);
        ChassisInboundPayload inboundPayload = new ChassisInboundPayload("chassis", payload);
        
        ChassisPwmOutboundPayload outboundPayload = (ChassisPwmOutboundPayload) pwmModeStrategy.process(inboundPayload);
        
        // Right wheels should have lower PWM than left wheels
        assertTrue(outboundPayload.payload().fr() < outboundPayload.payload().fl());
        assertTrue(outboundPayload.payload().rr() < outboundPayload.payload().rl());
        
        // Both should be positive (forward)
        assertTrue(outboundPayload.payload().fl() > 0);
        assertTrue(outboundPayload.payload().fr() > 0);
        assertTrue(outboundPayload.payload().rl() > 0);
        assertTrue(outboundPayload.payload().rr() > 0);
    }
    
    @Test
    void testSharpLeftTurn() {
        // stick[0] = -0.7 (left turn), stick[1] = 0.7 (forward)
        double[] stick = {-0.7, 0.7};
        double[] rotate = {0.0};
        
        ChassisInboundPayload.ChassisPayload payload = 
            new ChassisInboundPayload.ChassisPayload(stick, false, false, false, false, rotate);
        ChassisInboundPayload inboundPayload = new ChassisInboundPayload("chassis", payload);
        
        ChassisPwmOutboundPayload outboundPayload = (ChassisPwmOutboundPayload) pwmModeStrategy.process(inboundPayload);
        
        // Left wheels should be stopped (0 PWM)
        assertEquals(0, outboundPayload.payload().fl());
        assertEquals(0, outboundPayload.payload().rl());
        
        // Right wheels should have full PWM
        assertEquals(255, outboundPayload.payload().fr());
        assertEquals(255, outboundPayload.payload().rr());
    }
    
    @Test
    void testSharpRightTurn() {
        // stick[0] = 0.7 (right turn), stick[1] = 0.7 (forward)
        double[] stick = {0.7, 0.7};
        double[] rotate = {0.0};
        
        ChassisInboundPayload.ChassisPayload payload = 
            new ChassisInboundPayload.ChassisPayload(stick, false, false, false, false, rotate);
        ChassisInboundPayload inboundPayload = new ChassisInboundPayload("chassis", payload);
        
        ChassisPwmOutboundPayload outboundPayload = (ChassisPwmOutboundPayload) pwmModeStrategy.process(inboundPayload);
        
        // Right wheels should be stopped (0 PWM)
        assertEquals(0, outboundPayload.payload().fr());
        assertEquals(0, outboundPayload.payload().rr());
        
        // Left wheels should have full PWM
        assertEquals(255, outboundPayload.payload().fl());
        assertEquals(255, outboundPayload.payload().rl());
    }
    
    @Test
    void testInPlaceLeftRotation() {
        // stick values below threshold, rotate value above threshold
        double[] stick = {0.05, 0.05}; // Below threshold of 0.1
        double[] rotate = {-1.0}; // Full left rotation
        
        ChassisInboundPayload.ChassisPayload payload = 
            new ChassisInboundPayload.ChassisPayload(stick, false, false, false, false, rotate);
        ChassisInboundPayload inboundPayload = new ChassisInboundPayload("chassis", payload);
        
        ChassisPwmOutboundPayload outboundPayload = (ChassisPwmOutboundPayload) pwmModeStrategy.process(inboundPayload);
        
        // Left wheels should have negative PWM (backward)
        assertTrue(outboundPayload.payload().fl() < 0);
        assertTrue(outboundPayload.payload().rl() < 0);
        
        // Right wheels should have positive PWM (forward)
        assertTrue(outboundPayload.payload().fr() > 0);
        assertTrue(outboundPayload.payload().rr() > 0);
        
        // Should be at maximum values
        assertEquals(-255, outboundPayload.payload().fl());
        assertEquals(255, outboundPayload.payload().fr());
        assertEquals(-255, outboundPayload.payload().rl());
        assertEquals(255, outboundPayload.payload().rr());
    }
    
    @Test
    void testInPlaceRightRotation() {
        // stick values below threshold, rotate value above threshold
        double[] stick = {0.05, 0.05}; // Below threshold of 0.1
        double[] rotate = {1.0}; // Full right rotation
        
        ChassisInboundPayload.ChassisPayload payload = 
            new ChassisInboundPayload.ChassisPayload(stick, false, false, false, false, rotate);
        ChassisInboundPayload inboundPayload = new ChassisInboundPayload("chassis", payload);
        
        ChassisPwmOutboundPayload outboundPayload = (ChassisPwmOutboundPayload) pwmModeStrategy.process(inboundPayload);
        
        // Left wheels should have positive PWM (forward)
        assertTrue(outboundPayload.payload().fl() > 0);
        assertTrue(outboundPayload.payload().rl() > 0);
        
        // Right wheels should have negative PWM (backward)
        assertTrue(outboundPayload.payload().fr() < 0);
        assertTrue(outboundPayload.payload().rr() < 0);
        
        // Should be at maximum values
        assertEquals(255, outboundPayload.payload().fl());
        assertEquals(-255, outboundPayload.payload().fr());
        assertEquals(255, outboundPayload.payload().rl());
        assertEquals(-255, outboundPayload.payload().rr());
    }
}
