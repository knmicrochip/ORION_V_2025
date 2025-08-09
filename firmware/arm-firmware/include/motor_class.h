#include <Arduino.h>
#include "arm_defines.h"
#include <Servo.h>
#define AVERAGE 20

#define DEBUG


class MotorDriver
{
    int dirPin1, dirPin2, enablePin, currentPin;
    int currentSpeed;
    int targetSpeed;
    int targetDir;
    int currentDir;
    int speedStep=10;;
    int position;
    int tempPosition;
    
    int error; //0 - no error, 1 - current error, 2 - position error, 3 - undefined error 
    
    float current, currentDivider;
    
    public: MotorDriver(int initdirPin1, int initdirPin2, int initEnablePin, int initCurrentPin, float initCurrentDivider)
    {
        dirPin1=initdirPin1;
        dirPin2=initdirPin2;
        currentPin=initCurrentPin; 
        enablePin=initEnablePin;
        currentDivider=initCurrentDivider;
    }

    int readCurrent() //need to average because of electrical interference
    {
        for(int i=1; i<=AVERAGE; i++) {delayMicroseconds(10); current=((AVERAGE-1)*current+analogRead(currentPin))/AVERAGE;}
    }

    int setSpeed(int tempSpeed)
    {
        targetDir=tempSpeed>0 ? 1 : -1;
        targetSpeed=map(tempSpeed,0,100,0,255);
        Serial.print("setting speed ");
    }

    int setPosition(int tempPosition, int tempSpeed)
    {
        
    }

    int update()
    {
        if(targetSpeed==0)                                                           //if current pwm is zero stop everything
        {   
            digitalWrite(dirPin1,0);
            digitalWrite(dirPin2,0);
            if(targetDir==1)   digitalWrite(dirPin1,1);                      
            if(targetDir==-1)  digitalWrite(dirPin2,1);
        }
    if(targetDir!=currentDir)                                                   //if needs to change direction decrease current power     
        {
            
            currentSpeed-=speedStep;                                               //decrease current pwm
            if(currentSpeed<=0) {currentSpeed=0; currentDir=targetDir;}                                     //but not below 0 because it's stupid
            
        }
        else                                                                    //else it needs to change speed
        {
            if(currentSpeed>targetSpeed)                                            //if slowing down
            {
                Serial.print(" deccel ");
                if(currentSpeed-targetSpeed<speedStep) currentSpeed=targetSpeed;         //if very close just write the value
                else currentSpeed-=speedStep;                                      //else ramp down
            }
            else                                                                //speeding up
            {
                Serial.print(" accel ");
                if(targetSpeed-currentSpeed<speedStep) currentSpeed=targetSpeed;         //if very close just write the value
                else currentSpeed+=speedStep;                                      //else ramp up
            }

        }
                                                                   
    analogWrite(enablePin, abs(currentSpeed));         //update registers
    if(currentSpeed>0) {digitalWrite(dirPin1,1);digitalWrite(dirPin2,0);}
    if(currentSpeed<0) {digitalWrite(dirPin1,0);digitalWrite(dirPin2,1);}

    #ifdef DEBUG
    char debug[100];
    sprintf(debug, "target: speed %d dir %d current: speed %d dir %d pins %d %d",targetSpeed,targetDir,currentSpeed,currentDir, digitalRead(dirPin1), digitalRead(dirPin2));
    Serial.println(debug);
    #endif

  
        return error;
    }

    float getCurrent()
    {
        return current/currentDivider;
    }
    void enableMotor()
    {
        digitalWrite(enablePin, 1);
    }
    void disableMotor()
    {
        digitalWrite(enablePin, 0);
    }
    int getPosition()
    {
        return position;
    }
    

};