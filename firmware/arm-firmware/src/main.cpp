#include <Arduino.h>
#include <ArduinoJson.h>
#include "motor_runner.h"



StaticJsonDocument<256> json;

unsigned long lastExecutionTime = 0;

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
        const char *mode = json["mode"];
        
        
      }
    }
   

    const auto currentTime = millis();
    if (currentTime - lastExecutionTime > 1000) {
      
      //serializeJson(feedback, Serial);
      

      lastExecutionTime = currentTime;
    }

    json.clear();
}