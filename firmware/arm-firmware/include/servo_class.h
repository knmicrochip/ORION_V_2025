#include <Arduino.h>
#include "arm_defines.h"
#include <Servo.h>
#define AVERAGE 20

class ServoDriver
{
    int drivePin, positionPin, currentPin, enablePin;
    int position;
    int target;
    int step;
    int error; //0 - no error, 1 - current error, 2 - position error, 3 - undefined error 
    float current, currentDivider;
    
    Servo thisServo;

    public : ServoDriver(int initDrivePin, int initPositionPin, int initCurrentPin, int initEnablePin, float initCurrentDivider)
    {
        drivePin=initDrivePin;
        positionPin=initPositionPin;
        currentPin=initCurrentPin; 
        enablePin=initEnablePin;

        thisServo.attach(drivePin);
    }

    int readCurrent() //need to average because of electrical interference
    {
        for(int i=1; i<=AVERAGE; i++) {delayMicroseconds(10); current=((AVERAGE-1)*current+analogRead(currentPin))/AVERAGE;}
    }
    void enable()
    {
        digitalWrite(enablePin, 1);
    }

    void disable()
    {
        digitalWrite(enablePin, 0);
    }
    void moveTo(int tempPosition, int tempStep)
    {
        if (tempPosition>180||tempPosition<0) error=2;
        target=tempPosition;
        step=abs(tempStep);
    }

    void setSpeed(int tempStep)
    {
       step=tempStep;
       target=-1;
    }

    int update()
    {
        if(target>=0)
        {   
            if(position<0) {position=0; step=0;}
            if(position>180) {position=180; step=0;}
            if(position>target) position-=step;
            if(position<target) position+=step;
            
        }
        thisServo.write(position);
        return error;
    }

    float getCurrent()
    {
        return current/currentDivider;
    }
    
    int getPosition()
    {
        return position;
    }

};