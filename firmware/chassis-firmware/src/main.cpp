#include <Arduino.h>
#include <ArduinoJson.h>
#include "operational_mode_runner.h"
#include "motor_runner.h"
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

unsigned long lastExecutionTime = 0;

void setup() {
    Serial.begin(115200);
    setupPWM();
    while(!Serial);
    
    
}

void loop() {
    runner.run();
    //Serial.print("TIC ");
    //Serial.print(millis());
    if(Serial.available()) 
    {
      auto error = deserializeJson(json, Serial); //bitch giving delay, need to think of something
      //Serial.print(" TAC ");
      //Serial.print(millis());
      if (error == DeserializationError::Ok) 
      {
        const char *mode = json["mode"];
        runner.selectMode(mode);
        runner.parse(json);
      }
    }
    //Serial.print(" TOE ");
    //Serial.print(millis());

    const auto currentTime = millis();
    if (currentTime - lastExecutionTime > 1000) {
      auto feedback = runner.generateFeedback();
      serializeJson(feedback, Serial);
      //Serial.print("\n\n");                       // globally agreed delimiter for UART messages

      lastExecutionTime = currentTime;
    }
    //Serial.print(" SHIT ");
    //Serial.print(millis());
    json.clear();
}