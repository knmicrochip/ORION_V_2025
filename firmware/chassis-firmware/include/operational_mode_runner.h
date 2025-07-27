#pragma once
#include <string.h>
#include "crtp_operational_mode.h"
#include "pwm_operational_mode.h"
#include "cfl_operational_mode.h"
#include "ros_operational_mode.h"

class OperationalModeRunner {
    static PwmOperationalMode pwmMode;
    static CflOperationalMode cflMode;
    static RosOperationalMode rosMode;
    constexpr static size_t CAPACITY = OperationalModeCrtp<PwmOperationalMode>::CAPACITY;

    void (*executor)();
    void (*processor)(const StaticJsonDocument<CAPACITY>& doc);
    StaticJsonDocument<CAPACITY> (*feedbackGenerator)();

public:
    OperationalModeRunner() : executor(nullptr), processor(nullptr), feedbackGenerator(nullptr) {
        selectMode("pwm");
    }

    void selectMode(const char* mode) {
        if (mode == nullptr) {
            return;
        }

        if (strcmp(mode, "pwm") == 0 || strcmp(mode, "PWM") == 0) {
            executor = [] { pwmMode.execute(); };
            processor = [](const StaticJsonDocument<CAPACITY>& doc) {
                pwmMode.process(doc);
            };
            feedbackGenerator = []() { return pwmMode.feedback(); };
        } else if (strcmp(mode, "cfl") == 0 || strcmp(mode, "CFL") == 0) {
            executor = [] { cflMode.execute(); };
            processor = [](const StaticJsonDocument<CAPACITY>& doc) {
                cflMode.process(doc);
            };
            feedbackGenerator = []() { return cflMode.feedback(); };
        } else if (strcmp(mode, "ros") == 0 || strcmp(mode, "ROS") == 0) {
            executor = [] { rosMode.execute(); };
            processor = [](const StaticJsonDocument<CAPACITY>& doc) {
                rosMode.process(doc);
            };
            feedbackGenerator = []() { return rosMode.feedback(); };
        }
    }

    void parse(const StaticJsonDocument<CAPACITY>& doc) {
        if (processor) {
            processor(doc);
        }
    }

    void run() {
        if (executor) {
            executor();
        }
    }

    StaticJsonDocument<CAPACITY> generateFeedback() {
        if (feedbackGenerator) {
            return feedbackGenerator();
        }
        return StaticJsonDocument<CAPACITY>();
    }

};