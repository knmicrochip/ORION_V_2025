#pragma once
#include <ArduinoJson.h>

// CRTP base class
template<typename Derived>
class OperationalModeCrtp {
public:
    constexpr static size_t CAPACITY = 256; 

    void process(const StaticJsonDocument<CAPACITY>& doc) {
        static_cast<Derived*>(this)->processImpl(doc);
    }

    void execute() {
        static_cast<Derived*>(this)->executeImpl();
    }

    StaticJsonDocument<CAPACITY> feedback() {
        return static_cast<Derived*>(this)->feedbackImpl();
    }
};