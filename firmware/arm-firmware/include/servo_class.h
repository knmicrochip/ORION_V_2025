#include <Arduino.h>
#include "arm_defines.h"
#include <Servo.h>
#define AVERAGE 20
//#define DEBUG

class ServoDriver
{
    int drivePin, positionPin, currentPin, enablePin;
    float position;
    float target;
    float step;
    int error; //0 - no error, 1 - current error, 2 - position error, 3 - undefined error 
    float current, currentDivider;
    float minPos, maxPos;
    int invert;
    
    Servo thisServo;

    public : ServoDriver(int initDrivePin, int initPositionPin, int initCurrentPin, int initEnablePin, float initCurrentDivider, int initMinPos, int initMaxPos)
    {
        drivePin=initDrivePin;
        positionPin=initPositionPin;
        currentPin=initCurrentPin; 
        enablePin=initEnablePin;
        minPos=initMinPos;
        maxPos=initMaxPos;

        thisServo.attach(drivePin);
        if(minPos>maxPos) invert=-1; else invert=1;
        position=(abs(minPos-maxPos));
        target=position;
    }

    int readCurrent() //need to average because of electrical interference
    {
        for(int i=1; i<=AVERAGE; i++) {delayMicroseconds(10); current=((AVERAGE-1)*current+analogRead(currentPin))/AVERAGE;}
    }
    void enable()
    {
        //Serial.print(" enable on pin ");
        //Serial.print(enablePin);
        //Serial.print(" ");
        digitalWrite(enablePin, 1);
    }

    void disable()
    {
        digitalWrite(enablePin, 0);
    }
    void moveTo(int tempPosition, int tempStep)
    {
        enable();
        target=map(tempPosition,-100,100,minPos,maxPos);
        step=abs(tempStep);
    }

    void setSpeed(int tempStep)
    {
        enable();
       step=tempStep/50;
       target=-1;
    }

    int update()
    {
         #ifdef DEBUG
        char debug[100];
        sprintf(debug, "servo %d target pos %f current pos %f current %d ",drivePin,target,position,0);
        Serial.println(debug);
        #endif

        if(target>=0)
        {   
            if(position>target) position-=step*invert;
            if(position<target) position+=step*invert;
        }
        else (position=position+step*invert);
        if(position<=minPos) {position=minPos; step=0;}
        if(position>=maxPos) {position=maxPos; step=0;}
        thisServo.write((int)position);
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