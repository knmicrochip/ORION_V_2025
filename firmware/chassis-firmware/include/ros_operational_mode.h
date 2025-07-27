#pragma once
#include "crtp_operational_mode.h"

class RosOperationalMode : public OperationalModeCrtp<RosOperationalMode> {
public:
    void processImpl(const StaticJsonDocument<CAPACITY>& doc) {
        // Serial.println("Processing ROS operational mode");
    }

    void executeImpl() {
        // Serial.println("Executing ROS operational mode");
    }

    StaticJsonDocument<CAPACITY> feedbackImpl() {
        StaticJsonDocument<CAPACITY> doc;
        doc["eventType"] = "chassis";
        doc["mode"] = "ROS";
        auto payload = doc.createNestedObject("payload");

        return doc;
    }
};