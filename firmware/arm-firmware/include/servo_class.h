#include <Arduino.h>
#include "arm_defines.h"
#include <Servo.h>
#define AVERAGE 20

class ServoDriver
{
    int drivePin, positionPin, currentPin, enablePin;
    int position;
    int step;
    int error; //0 - no error, 1 - current error, 2 - position error, 3 - undefined error 
    float current, currentDivider;
    
    Servo thisServo;

    ServoDriver(int initDrivePin, int initPositionPin, int initCurrentPin, int initEnablePin, float initCurrentDivider)
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

    int moveTo(int position, int step)
    {

    }

    int moveBy(int step)
    {

    }

    int update()
    {

        return error;
    }


};