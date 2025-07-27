#pragma once
#include "crtp_operational_mode.h"

class CflOperationalMode : public OperationalModeCrtp<CflOperationalMode> {
public:
    void processImpl(const StaticJsonDocument<CAPACITY>& doc) {
        // Serial.println("Processing CRTP operational mode");
    }

    void executeImpl() {
        // Serial.println("Executing CRTP operational mode");
    }

    StaticJsonDocument<CAPACITY> feedbackImpl() {
        StaticJsonDocument<CAPACITY> doc;
        doc["eventType"] = "chassis";
        doc["mode"] = "CFL";
        auto payload = doc.createNestedObject("payload");

        return doc;
    }
};