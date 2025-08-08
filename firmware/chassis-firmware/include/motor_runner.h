#pragma once
#include <Arduino.h>

#define PWM_LFF     2   //oc3b
#define PWM_LFB     3   //oc3c
#define PWM_LRF     11  //oc1a
#define PWM_LRB     5   //oc3a
#define PWM_RFF     6   //oc4A
#define PWM_RFB     7   //oc4b
#define PWM_RRF     8   //oc4c
#define PWM_RRB     12  //oc1b

#define CURR_LFF    A0
#define CURR_LFB    A1
#define CURR_LRF    A2
#define CURR_LRB    A3
#define CURR_RFF    A4
#define CURR_RFB    A5
#define CURR_RRF    A6
#define CURR_RRB    A7

#define PIN_LFF     22
#define PIN_LFB     23
#define PIN_LRF     24
#define PIN_LRB     25
#define PIN_RFF     26
#define PIN_RFB     27
#define PIN_RRF     28
#define PIN_RRB     29

#define PWM_STEP 10

#define PWM_MAXVAL 100

void runBTS(int pwm, int pinForward, int pinBackward, volatile uint16_t* regForward, volatile uint16_t* regBackward)
{
    
    int pwmTarget=abs(pwm);                                                     //absolute power value
    int dirTarget=pwm>0 ? 1 : -1;
    if (pwm==0) dirTarget=0;                                                    //target direction
    int dirCurrent=0;                                                           //current direction
    int pwmCurrent=0;                                                           //current absolute power
    if(digitalRead(pinForward)==1)  {dirCurrent=1;  pwmCurrent=*regForward;}    //if is going forward read forward register
    if(digitalRead(pinBackward)==1) {dirCurrent=-1; pwmCurrent=*regBackward;}   //if is going backward read backward register

    if(pwmCurrent==0)                                                           //if current pwm is zero stop everything
        {
            *regForward=0;                                                      //update register
            *regBackward=0;                                                     //update register
            digitalWrite(pinForward,0);                                         //update dir pin
            digitalWrite(pinBackward,0);                                        //update dir pin
            if(dirTarget==1)   digitalWrite(pinForward,1);                      
            if(dirTarget==-1)  digitalWrite(pinBackward,1);
        }
    if(dirTarget!=dirCurrent)                                                   //if needs to change direction decrease current power     
        {
            pwmCurrent-=PWM_STEP;                                               //decrease current pwm
            if(pwmCurrent<=0) pwmCurrent=0;                                     //but not below 0 because it's stupid
            
        }
        else                                                                    //else it needs to change speed
        {
            if(pwmCurrent>pwmTarget)                                            //if slowing down
            {
                if(pwmCurrent-pwmTarget<PWM_STEP) pwmCurrent=pwmTarget;         //if very close just write the value
                else pwmCurrent-=PWM_STEP;                                      //else ramp down
            }
            else                                                                //speeding up
            {
                if(pwmTarget-pwmCurrent<PWM_STEP) pwmCurrent=pwmTarget;         //if very close just write the value
                else pwmCurrent+=PWM_STEP;                                      //else ramp up
            }

        }
                                                                   
    if(dirCurrent==1) {*regForward=pwmCurrent; *regBackward=0;}         //update registers
    if(dirCurrent==-1){*regForward=0; *regBackward=pwmCurrent;}         //update registers
}

void runMotorsWithPWM(int pwmLF, int pwmLR, int pwmRF, int pwmRR)
{
    runBTS(pwmLF, PIN_LFF, PIN_LFB,  &OCR3B, &OCR3C);
    runBTS(pwmLR, PIN_LRF, PIN_LRB,  &OCR1A, &OCR3A);
    runBTS(pwmRF, PIN_RFF, PIN_RFB,  &OCR4A, &OCR4B);
    runBTS(pwmRR, PIN_RRF, PIN_RRB,  &OCR4C, &OCR1B);
}



void setupPWM()
{
    pinMode(PWM_LFF, OUTPUT);
    pinMode(PWM_LFB, OUTPUT);
    pinMode(PWM_LRF, OUTPUT);
    pinMode(PWM_LRB, OUTPUT);
    pinMode(PWM_RFF, OUTPUT);
    pinMode(PWM_RFB, OUTPUT);
    pinMode(PWM_RRF, OUTPUT);
    pinMode(PWM_RRB, OUTPUT);

    pinMode(PIN_LFF, OUTPUT);
    pinMode(PIN_LFB, OUTPUT);
    pinMode(PIN_LRF, OUTPUT);
    pinMode(PIN_LRB, OUTPUT);
    pinMode(PIN_RFF, OUTPUT);
    pinMode(PIN_RFB, OUTPUT);
    pinMode(PIN_RRF, OUTPUT);
    pinMode(PIN_RRB, OUTPUT);
    
    noInterrupts();
    OCR3B = 0L;
    OCR3C = 0L;
    OCR2A = 0L;
    OCR3A = 0L;  
    OCR4A = 0L;
    OCR4B = 0L;
    OCR4C = 0L;
    OCR2B = 0L; 

    //TCCR1A=(1<<COM1A1)|(1<<COM1B1)|(1<<WGM10)|(1<<WGM11);
    TCCR1A=(1<<COM1A1)|(1<<COM1B1)|(1<<WGM10)|(0<<WGM11);
    //TCCR1B=(0<<CS12)|(0<<CS11)|(1<<CS10);
    TCCR1B=0b00000001;
    //ICR1 = PWM_MAXVAL;

    TCCR3A=(1<<COM3A1)|(1<<COM3B1)|(1<<COM3C1)|(1<<WGM30)|(0<<WGM31);
    //TCCR3B=(0<<CS32)|(0<<CS31)|(1<<CS30);
    TCCR3B=0b00000001;
    //ICR3 = PWM_MAXVAL;

    TCCR4A=(1<<COM4A1)|(1<<COM4B1)|(1<<COM4C1)|(1<<WGM40)|(0<<WGM41);
    //TCCR4B=(0<<CS32)|(0<<CS41)|(1<<CS40);
    TCCR4B=0b00000001;
    //ICR4 = PWM_MAXVAL;

    interrupts();

    /*digitalWrite(PIN_LFF, 1);
    digitalWrite(PIN_LRF, 1);
    digitalWrite(PIN_RFF, 1);
    digitalWrite(PIN_RRF, 1);*/

}