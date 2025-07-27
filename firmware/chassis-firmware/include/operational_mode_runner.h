#pragma once
#include "crtp_operational_mode.h"
#include "pwm_operational_mode.h"
#include "cfl_operational_mode.h"
#include "ros_operational_mode.h"

enum class OpMode {
    PWM,
    CFL,
    ROS
};

class OperationalModeRunner {
    static PwmOperationalMode pwmMode;
    static CflOperationalMode cflMode;
    static RosOperationalMode rosMode;
    constexpr static size_t CAPACITY = OperationalModeCrtp<PwmOperationalMode>::CAPACITY;

    void (*executor)();
    void (*processor)(const StaticJsonDocument<CAPACITY>& doc);
    StaticJsonDocument<CAPACITY> (*feedbackGenerator)();

public:
    OperationalModeRunner() : executor(nullptr), processor(nullptr), feedbackGenerator(nullptr) {}

    void selectMode(OpMode mode) {
        switch (mode) {
            case OpMode::PWM:
                executor = [] { pwmMode.execute(); };
                processor = [](const StaticJsonDocument<CAPACITY>& doc) {
                    pwmMode.process(doc);
                };
                feedbackGenerator = []() { return pwmMode.feedback(); };
                break;
            case OpMode::CFL:
                executor = [] { cflMode.execute(); };
                processor = [](const StaticJsonDocument<CAPACITY>& doc) {
                    cflMode.process(doc);
                };
                feedbackGenerator = []() { return cflMode.feedback(); };
                break;
            case OpMode::ROS:
                executor = [] { rosMode.execute(); };
                processor = [](const StaticJsonDocument<CAPACITY>& doc) {
                    rosMode.process(doc);
                };
                feedbackGenerator = []() { return rosMode.feedback(); };
                break;
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