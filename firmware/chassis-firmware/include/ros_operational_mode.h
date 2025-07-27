#pragma once
#include "crtp_operational_mode.h"

class RosOperationalMode : public OperationalModeCrtp<RosOperationalMode> {
    double linear_x = 0.0;
    double linear_y = 0.0;
    double linear_z = 0.0;
    double angular_x = 0.0;
    double angular_y = 0.0;
    double angular_z = 0.0;

public:
    void processImpl(const StaticJsonDocument<CAPACITY>& doc) {
        linear_x = doc["payload"]["linear"][0];
        linear_y = doc["payload"]["linear"][1];
        linear_z = doc["payload"]["linear"][2];
        angular_x = doc["payload"]["angular"][0];
        angular_y = doc["payload"]["angular"][1];
        angular_z = doc["payload"]["angular"][2];
    }

    void executeImpl() {
        // This is where the linear and angular velocities would be used to
        // control the rover. Since there's no hardware abstraction layer yet,
        // we'll leave this empty.
    }

    StaticJsonDocument<CAPACITY> feedbackImpl() {
        StaticJsonDocument<CAPACITY> doc;
        doc["eventType"] = "chassis";
        doc["mode"] = "ROS";
        auto payload = doc.createNestedObject("payload");

        auto linearArray = payload["linearV"].createNestedArray();
        linearArray.add(linear_x);
        linearArray.add(linear_y);
        linearArray.add(linear_z);

        auto angularArray = payload["angularV"].createNestedArray();
        angularArray.add(angular_x);
        angularArray.add(angular_y);
        angularArray.add(angular_z);

        return doc;
    }
};