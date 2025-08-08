#include <Arduino.h>
#include <ArduinoJson.h>
#include "arm_setup.h"
//1-base
//2-arm
//3-elbow
//4-wrist
//5-rotate
//6-gripper
//7-shovel

StaticJsonDocument<256> json;

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
        
        
      }
    }
   

    const auto currentTime = millis();
    if (currentTime - lastExecutionTime > 1000) {
      
      //serializeJson(feedback, Serial);
      

      lastExecutionTime = currentTime;
    }

    json.clear();
}