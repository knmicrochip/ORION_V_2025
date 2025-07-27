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

void setup() {
    Serial.begin(115200);
    while(!Serial);
}

void loop() {
    runner.run();
    delay(1000);

    auto error = deserializeJson(json, Serial);
    if (error == DeserializationError::Ok) {
      const char *mode = json["mode"];
      runner.selectMode(mode);
      runner.parse(json);
    }

    auto feedback = runner.generateFeedback();
    serializeJson(feedback, Serial);
    Serial.print("\n\n");                       // globally agreed delimiter for UART messages

    json.clear();
}