#include <Arduino.h>
#include <ArduinoJson.h>
#include "arm_executor.h"
//{"eventType": "manipulator", "mode": "pwm", "payload": {"rotate_turret": "100","flex_forearm": "100", "flex_arm":"100","flex_gripper": "100", "rotate_gripper": "100","end_effector":"100"}}
//{"eventType": "manipulator", "mode": "pwm", "payload": {"rotate_turret": "-100","flex_forearm": "0", "flex_arm":"0","flex_gripper": "180", "rotate_gripper": "180","end_effector":"180"}}
//{"eventType": "manipulator", "mode": "pwm", "payload": {"rotate_turret": "-100","flex_forearm": "-100", "flex_arm":"-100","flex_gripper": "-100", "rotate_gripper": "-100","end_effector":"-100"}}
//{"eventType": "manipulator", "mode": "pwm", "payload": {"rotate_turret": "0","flex_forearm": "0", "flex_arm":"0","flex_gripper": "-100", "rotate_gripper": "100","end_effector":"0"}}
//{"eventType": "manipulator", "mode": "pwm", "payload": {"rotate_turret": "0","flex_forearm": "0", "flex_arm":"0","flex_gripper": "0", "rotate_gripper": "0","end_effector":"0"}}
StaticJsonDocument<400> json;

unsigned long lastExecutionTime1 = 0;
unsigned long lastExecutionTime2 = 0;
const char *mode;

#define PH_SENSOR A11


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
            //Serial.println("pwm mode message recieved");
            executePWM(json);
            }
        
      }
    }
   

    const auto motorTime = millis();
  const auto feedbackTime = millis();
    if (motorTime - lastExecutionTime1 > 100) {
      
      runMotors();
      
      lastExecutionTime1 = motorTime;
    }
  if (feedbackTime - lastExecutionTime2 > 500) {
      int ph = analogRead(PH_SENSOR);
      //Serial.println("{\"eventType\": \"manipulator\", \"mode\": \"pwm\", \"payload\": {\"key\":\"ass\"}}");
      Serial.print("{\"eventType\": \"manipulator\", \"mode\": \"pwm\", \"payload\": {\"key\":\"");
      Serial.print(ph);
      Serial.println("\"}}");
      Serial.print("\n\n");
      
      lastExecutionTime2 = feedbackTime;
    }

   

    json.clear();
}