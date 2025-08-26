#include <Arduino.h>
#include <ArduinoJson.h>
#include "operational_mode_runner.h"
#include "motor_runner.h"
#include <arduino-timer.h>

/*
{"eventType":"chassis","mode":"PWM"}
{"eventType":"chassis","mode":"CFL"}
{"eventType":"chassis","mode":"ROS"}
{"eventType":"chassis","mode":"LOL"}
{"eventType": "chassis", "mode": "pwm", "payload": {"fl": "100","fr": "100", "rl":"100","rr": "100"}}
{"eventType": "chassis", "mode": "pwm", "payload": {"fl": "-100","fr": "-100", "rl":"-100","rr": "-100"}}
*/
// static member initialization
PwmOperationalMode OperationalModeRunner::pwmMode;
CflOperationalMode OperationalModeRunner::cflMode;
RosOperationalMode OperationalModeRunner::rosMode;

// Define global variables
OperationalModeRunner runner;
StaticJsonDocument<256> json;
auto timer = timer_create_default();
#define TIMEOUT 15
int timeoutTimer=0;

int state=0;  //0-just power on (yellow on) 1-recieved something 2-stopped after timeout
#define RED    49
#define GREEN  47
#define BLUE   45

#define D 0
#define R 1
#define G 2
#define B 3
#define C 4
#define M 5
#define Y 6

unsigned long lastExecutionTime = 0;
void setColor(int color)
{
  switch (color)
  {
    
    case D:{digitalWrite(RED, 1); digitalWrite(GREEN, 1); digitalWrite(BLUE, 1); } break;
    case R:{digitalWrite(RED, 0); digitalWrite(GREEN, 1); digitalWrite(BLUE, 1); } break;
    case G:{digitalWrite(RED, 1); digitalWrite(GREEN, 0); digitalWrite(BLUE, 1); } break;
    case B:{digitalWrite(RED, 1); digitalWrite(GREEN, 1); digitalWrite(BLUE, 0); } break;
    case C:{digitalWrite(RED, 1); digitalWrite(GREEN, 0); digitalWrite(BLUE, 0); } break;
    case M:{digitalWrite(RED, 0); digitalWrite(GREEN, 1); digitalWrite(BLUE, 0); } break;
    case Y:{digitalWrite(RED, 0); digitalWrite(GREEN, 0); digitalWrite(BLUE, 1); } break;
    default: {digitalWrite(RED, 1); digitalWrite(GREEN, 1); digitalWrite(BLUE, 1); } break;

  }

}

void setup() {
    pinMode(RED, OUTPUT);
    pinMode(GREEN, OUTPUT);
    pinMode(BLUE, OUTPUT);
    digitalWrite(RED, 1);
    digitalWrite(GREEN, 1);
    digitalWrite(BLUE, 1);
    Serial.begin(115200);
    setupPWM();
    while(!Serial);
    
    timer.every(100, [](void *) {
      runner.run();
      return true;
    });
}

void loop() {
    timer.tick(); 
    if(state==0) setColor(B); else if(timeoutTimer>TIMEOUT) state=2; 
    if(state==1) setColor(R);
    if(state==2) setColor(G);
    if(Serial.available()) 
    {
      auto error = deserializeJson(json, Serial); //bitch giving delay, need to think of something
      //Serial.print(" TAC ");
      //Serial.print(millis());
      if (error == DeserializationError::Ok) 
      { 
        
        timeoutTimer=0;
        state = 1;
        const char *mode = json["mode"];
        runner.selectMode(mode);
        runner.parse(json);
        setColor(R);
      }
    }
    //Serial.print(" TOE ");
    //Serial.print(millis());

    const auto currentTime = millis();
    if (currentTime - lastExecutionTime > 500) {
      auto feedback = runner.generateFeedback();
      serializeJson(feedback, Serial);
      Serial.print("\n\n");                       // globally agreed delimiter for UART messages
      timeoutTimer++;
      //Serial.print("timeout ");
      //Serial.print(timeoutTimer);
      //Serial.print("state ");
      //Serial.println(state);
      lastExecutionTime = currentTime;
    }
    //Serial.print(" SHIT ");
    //Serial.print(millis());
    json.clear();
}

