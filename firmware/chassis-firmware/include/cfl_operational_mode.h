#pragma once
#include "crtp_operational_mode.h"

class CflOperationalMode : public OperationalModeCrtp<CflOperationalMode> {
    double fl_ang_v = 0.0;
    double fr_ang_v = 0.0;
    double rl_ang_v = 0.0;
    double rr_ang_v = 0.0;

public:
    void processImpl(const StaticJsonDocument<CAPACITY>& doc) {
        fl_ang_v = doc["payload"]["fl"];
        fr_ang_v = doc["payload"]["fr"];
        rl_ang_v = doc["payload"]["rl"];
        rr_ang_v = doc["payload"]["rr"];
    }

    void executeImpl() {
        // This is where the PID controllers would be used to achieve the target
        // angular velocities. Since there's no hardware abstraction layer yet,
        // we'll leave this empty.
    }

    StaticJsonDocument<CAPACITY> feedbackImpl() {
        StaticJsonDocument<CAPACITY> doc;
        doc["eventType"] = "chassis";
        doc["mode"] = "CFL";
        auto payload = doc.createNestedObject("payload");
        payload["fl_angV"] = fl_ang_v;
        payload["fr_angV"] = fr_ang_v;
        payload["rl_angV"] = rl_ang_v;
        payload["rr_angV"] = rr_ang_v;

        return doc;
    }
};