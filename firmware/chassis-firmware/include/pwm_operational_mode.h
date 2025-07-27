#pragma once
#include "crtp_operational_mode.h"

class PwmOperationalMode : public OperationalModeCrtp<PwmOperationalMode> {
    int16_t fl_pwm = 0;
    int16_t fr_pwm = 0;
    int16_t rl_pwm = 0;
    int16_t rr_pwm = 0;

public:
    void processImpl(const StaticJsonDocument<CAPACITY>& doc) {
        fl_pwm = doc["payload"]["fl"];
        fr_pwm = doc["payload"]["fr"];
        rl_pwm = doc["payload"]["rl"];
        rr_pwm = doc["payload"]["rr"];
    }

    void executeImpl() {
        // This is where the PWM values would be applied to the motors.
        // Since there's no hardware abstraction layer yet, we'll leave this
        // empty.
    }

    StaticJsonDocument<CAPACITY> feedbackImpl() {
        StaticJsonDocument<CAPACITY> doc;
        doc["eventType"] = "chassis";
        doc["mode"] = "PWM";
        auto payload = doc.createNestedObject("payload");
        payload["fl_pwm"] = fl_pwm;
        payload["fr_pwm"] = fr_pwm;
        payload["rl_pwm"] = rl_pwm;
        payload["rr_pwm"] = rr_pwm;

        return doc;
    }
};