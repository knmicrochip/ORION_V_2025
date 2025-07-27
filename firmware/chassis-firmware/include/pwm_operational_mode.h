#pragma once
#include "crtp_operational_mode.h"

class PwmOperationalMode : public OperationalModeCrtp<PwmOperationalMode> {
public:
    void processImpl(const StaticJsonDocument<CAPACITY>& doc) {
        // Serial.println("Processing PWM operational mode");
    }

    void executeImpl() {
        // Serial.println("Executing PWM operational mode");
    }

    StaticJsonDocument<CAPACITY> feedbackImpl() {
        StaticJsonDocument<CAPACITY> doc;
        doc["eventType"] = "chassis";
        doc["mode"] = "PWM";
        auto payload = doc.createNestedObject("payload");

        return doc;
    }
};