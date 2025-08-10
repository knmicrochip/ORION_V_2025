package pl.orion.rover_controller_service.chassis.service;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import pl.orion.rover_controller_service.chassis.model.ChassisInboundPayload;
import pl.orion.rover_controller_service.chassis.model.ChassisPwmOutboundPayload;

@Service
public class PwmModeStrategy implements DriveModeStrategy {
    private static final Logger logger = LoggerFactory.getLogger(PwmModeStrategy.class);
    
    private static final int MAX_PWM = 255;
    private static final double REGULAR_DRIVE_DEADZONE_THRESHOLD = 0.05;
    private static final double STICK_THRESHOLD = 0.1;
    private static final double ROTATE_THRESHOLD = 0.25;
    
    @Override
    public Object process(ChassisInboundPayload payload) {
        logger.debug("Processing payload in PWM mode: {}", payload);
        
        // Extract joystick values
        double stickX = payload.payload().stick()[0];
        double stickY = payload.payload().stick()[1];
        double rotateZ = payload.payload().rotate()[0];
        
        // Determine if we should use rotate or stick input
        boolean useRotate = (Math.abs(stickX) < STICK_THRESHOLD && 
                            Math.abs(stickY) < STICK_THRESHOLD && 
                            Math.abs(rotateZ) > ROTATE_THRESHOLD);

        // Use case for the UI input, in which X,Y are always zero if once decides to use rotation
        useRotate = useRotate || (
            BigDecimal.ZERO.compareTo(BigDecimal.valueOf(Math.abs(stickX))) == 0 &&
            BigDecimal.ZERO.compareTo(BigDecimal.valueOf(Math.abs(stickY))) == 0);

        
        short leftPwm, rightPwm;
        
        if (useRotate) {
            // In-place rotation, negative PWM means rotating counter-clockwise
            // For left rotation (rotateZ < 0): left wheels backward, right wheels forward
            // For right rotation (rotateZ > 0): left wheels forward, right wheels backward
            leftPwm = (short) (rotateZ * MAX_PWM);
            rightPwm = (short) (-rotateZ * MAX_PWM);
        } else {
            // Differential drive based on stick input, negative PWM means rotating counter-clockwise
            // Map stickY (forward/backward) to both wheels
            // Map stickX (left/right) to differential steering
            int[] pwmValues = advancedJoystickToPWM(stickX, stickY);
            leftPwm = (short) pwmValues[0];
            rightPwm = (short) pwmValues[1];
        }
        
        // Create outbound payload with same PWM value for all wheels in a pair
        ChassisPwmOutboundPayload.ChassisPwmPayload pwmPayload = 
            new ChassisPwmOutboundPayload.ChassisPwmPayload(leftPwm, rightPwm, leftPwm, rightPwm);
        
        return new ChassisPwmOutboundPayload("chassis", "pwm", pwmPayload);
    }

    public static int[] advancedJoystickToPWM(double stickX, double stickY) {
        // Dead zone handling
        if (Math.abs(stickX) < REGULAR_DRIVE_DEADZONE_THRESHOLD 
            && Math.abs(stickY) < REGULAR_DRIVE_DEADZONE_THRESHOLD) {
            return new int[]{0, 0};
        }
        
        // Pure rotation when no forward/backward input
        if (Math.abs(stickY) < REGULAR_DRIVE_DEADZONE_THRESHOLD) {
            if (stickX < 0) {
                return new int[]{0, (int)(MAX_PWM * Math.abs(stickX))};
            } else if (stickX > 0) {
                return new int[]{(int)(MAX_PWM * Math.abs(stickX)), 0};
            } else {
                return new int[]{0, 0};
            }
        }
        
        // Use more descriptive variable names for clarity
        double turn = stickX;      // -1.0 (left) to 1.0 (right)
        double throttle = -stickY; // Inverted so 1.0 is full forward
        double leftSpeed = throttle + turn;

        double rightSpeed = throttle - turn;

        double maxMagnitude = Math.max(Math.abs(leftSpeed), Math.abs(rightSpeed));
        if (maxMagnitude > 1.0) {
            leftSpeed /= maxMagnitude;
            rightSpeed /= maxMagnitude;
        }

        
        int rightPwm = (int)(rightSpeed * MAX_PWM);
        int leftPwm = (int)(-leftSpeed * MAX_PWM);
        leftPwm = Math.max(-MAX_PWM, Math.min(MAX_PWM, leftPwm));
        rightPwm = Math.max(-MAX_PWM, Math.min(MAX_PWM, rightPwm));

        return new int[]{leftPwm, rightPwm};
 
    }
    
    @Override
    public String getModeName() {
        return "pwm";
    }
}
