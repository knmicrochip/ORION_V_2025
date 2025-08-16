#include <Arduino.h>
#include <ArduinoJson.h>
#include "arm_executor.h"
//{"eventType": "manipulator", "mode": "pwm", "payload": {"rotate_turret": "100","flex_forearm": "100", "flex_arm":"100","flex_gripper": "100", "rotate_gripper": "100","end_effector":"100"}}
//{"eventType": "manipulator", "mode": "pwm", "payload": {"rotate_turret": "0","flex_forearm": "0", "flex_arm":"0","flex_gripper": "180", "rotate_gripper": "180","end_effector":"180"}}
//{"eventType": "manipulator", "mode": "pwm", "payload": {"rotate_turret": "-100","flex_forearm": "-100", "flex_arm":"-100","flex_gripper": "-100", "rotate_gripper": "-100","end_effector":"-100"}}
//{"eventType": "manipulator", "mode": "pwm", "payload": {"rotate_turret": "0","flex_forearm": "0", "flex_arm":"0","flex_gripper": "0", "rotate_gripper": "0","end_effector":"0"}}
StaticJsonDocument<400> json;

unsigned long lastExecutionTime = 0;
const char *mode;



void setup() {
    Serial.begin(115200);
    pinStart();
    while(!Serial);
       
    
}

void loop() {
    if(Serial.available()) 
    {
      auto error = deserializeJson(json, Serial); 
      
      if (error == DeserializationError::Ok) 
      {
        mode = json["mode"];
        if (strcmp(mode, "pwm") == 0 || strcmp(mode, "PWM") == 0) {
            Serial.println("pwm mode message recieved");
            executePWM(json);
            }
        
      }
    }
   

    const auto currentTime = millis();
    if (currentTime - lastExecutionTime > 100) {
      
      runMotors();
      Serial.println("running motors");
      lastExecutionTime = currentTime;
    }

    json.clear();
}