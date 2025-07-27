#include <Arduino.h>
#include <ArduinoJson.h>
#include "operational_mode_runner.h"

// static member initialization
PwmOperationalMode OperationalModeRunner::pwmMode;
CflOperationalMode OperationalModeRunner::cflMode;
RosOperationalMode OperationalModeRunner::rosMode;

// Define global variables
OperationalModeRunner runner;
StaticJsonDocument<256> json;

OpMode modeParser(const char* mode);

void setup() {
    runner.selectMode(OpMode::PWM); 
    Serial.begin(115200);
    while(!Serial);
}

void loop() {
    runner.run();
    delay(1000);

    auto error = deserializeJson(json, Serial);
    if (error == DeserializationError::Ok) {
      const char *mode = json["mode"];
      runner.selectMode(modeParser(mode));
      runner.parse(json);
    }

    auto feedback = runner.generateFeedback();
    serializeJson(feedback, Serial);
    Serial.println();
    Serial.println();

    json.clear();
}

OpMode modeParser(const char* mode) {
    if (strcmp(mode, "PWM") == 0) {
        return OpMode::PWM;
    } else if (strcmp(mode, "CFL") == 0) {
        return OpMode::CFL;
    } else if (strcmp(mode, "ROS") == 0) {
        return OpMode::ROS;
    }
    return OpMode::PWM; // default mode
}