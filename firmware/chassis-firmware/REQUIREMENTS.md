# Firmware Requirements

This document outlines all requirements that this firmware is designed to meet.

## Application Stack

The application leverages the following software technology stack:

* **[C++17](https://en.cppreference.com/w/cpp/language/)**: Primary programming language that provides classes and static patterns such as [CRTP](https://en.wikipedia.org/wiki/Curiously_recurring_template_pattern)
* **[PlatformIO](https://github.com/platformio/platformio)**: Embedded development plugin for VSCode
* **[Arduino Framework](https://www.arduino.cc/en/main)**: Embedded platform providing hardware abstraction for rapid development
* **[ArduinoJson v6](https://arduinojson.org/v6/)**: JSON parsing and serialization library for Arduino. Version 6 is used for its `StaticJsonDocument` support.
* **[Arduino PID Library](https://github.com/br3ttb/Arduino-PID-Library)**: PID controller implementation

## Hardware Stack

* **[Arduino Mega2560](https://store.arduino.cc/products/arduino-mega-2560-rev3)**: Development board featuring an ATmega2560 microcontroller
* **UART-to-USB Converter**: Built-in Arduino functionality
* **Serial Communication**: Full-duplex with the following configuration:
  - Baud rate: 115,200
  - Data frame: 8-bit
  - Parity: None
  - Stop bits: 1
  - Flow control: None

On Linux systems, the serial device is accessible at:
* `/dev/tty*` (typically `/dev/ttyUSB*` or `/dev/ttyACM*`)

## Application Architecture

The application comprises the following core components:

* **Message Processing**: Receive and transmit JSON payloads
* **Command Parsing**: Parse incoming commands and validate syntax
* **Control Algorithm Management**: Switch between and apply control algorithms using CRTP strategy pattern
* **Telemetry System**: Collect and transmit status data at regular, predictable intervals

### Communication Protocol

All payloads are terminated with two newline characters: `\n\n`. When the serial implementation detects this delimiter, it parses the entire received buffer as a single JSON message.

Similarly, all outbound data from the application to the onboard computer must be suffixed with `\n\n`.

## Operational Modes

The application supports three distinct operational modes:

* **PWM Mode**: Accepts direct PWM commands for individual motor control
* **CFL Mode** (Closed Feedback Loop): Accepts angular velocity commands for individual motors with feedback control
* **ROS Mode**: Accepts ROS-compatible Twist-like messages with linear and angular velocity for precise rover control,
including slip compensation and terrain adaptation

Each operational mode is implemented using [CRTP](http://en.wikipedia.org/wiki/Curiously_recurring_template_pattern), providing a compile-time implementation of the [strategy design pattern](https://en.wikipedia.org/wiki/Strategy_pattern).

The operational mode can be switched dynamically using the commands described in the [Inbound Messages](#inbound-messages) section.

To select an operational mode, the application shall rely on function pointers to select
an algorithm in runtime.

## JSON Communication Schema

This section defines the communication contract for the chassis firmware. The JSON schema is compatible 
with the one defined by [uart-mqtt-gateway](../../uart-mqtt-gateway/README.md#json-schema):

```
{
 "eventType": "(power|manipulator|chassis|science)",
 "payload": {
   "value1": "",
   [...]
 }
}
```

## Outbound JSON schema

The outbound schema defines messages that the microcontroller **sends to the onboard computer** via UART. 
These messages provide essential telemetry including power consumption, wheel angular velocities, 
dead-reckoning heading, and system status.

Telemetry data helps operators monitor chassis status and locate the rover when other systems 
(especially cameras) are unavailable.

> **Note**: Messages must be formatted in compact mode without extra whitespace to optimize transmission
> speed across all services, including the MQTT integration layer.

**Telemetry Payload Structure:**

```
{
 "eventType": "chassis",        // chassis telemetry unique firmware identifier
 "mode": "(pwm|cfl|ros)",    // current operation mode, either PWM, CFL or ROS
 "payload": {
    "fl_angV": "<<double>>",    // front-left wheel angular velocity in rad/s
    "fr_angV": "<<double>>",    // front-right wheel angular velocity in rad/s
    "rl_angV": "<<double>>",    // rear-left wheel angular velocity in rad/s
    "rr_angV": "<<double>>",    // rear-right wheel angular velocity in rad/s
    "fl_pwm": "<<int16>>",      // front-left wheel PWM in range [-255, 255]
    "fr_pwm": "<<int16>>",      // front-right wheel PWM in range [-255, 255]
    "rl_pwm": "<<int16>>",      // rear-left wheel PWM in range [-255, 255]
    "rr_pwm": "<<int16>>",      // rear-right wheel PWM in range [-255, 255]
    "heading": "<<double>>,     // filtered and processed IMU heading in degrees,
    "linearV": "<<double>>",    // optical-flow sensor linear velocity in m/s
    "angularV": "<<double>>"    // optical-flow sensor angular velocity in rad/s
 }
}
```


## Inbound messages

The chassis firmware accepts multiple command payloads based on the [JSON schema](#json-communication-schema).

#### PWM Operational Mode

In **PWM mode**, the rover chassis is controlled by directly applying PWM values to individual wheels. 
Each wheel is independently controlled by higher-level applications within the system architecture, 
such as [rover-controller-service](../../rover-controller-service/README.md).

**PWM Command Payload:**

```
{
 "eventType": "chassis",    // chassis telemetry unique firmware identifier
 "mode": "pwm",             // operational mode: PWM
 "payload": {
    "fl": "<<int16>>",      // front-left wheel PWM in range [-255, 255]
    "fr": "<<int16>>",      // front-right wheel PWM in range [-255, 255]
    "rl": "<<int16>>",      // rear-left wheel PWM in range [-255, 255]
    "rr": "<<int16>>"       // rear-right wheel PWM in range [-255, 255]
 }
}
```

### **CFL** operational mode

In **CFL mode** (Closed Feedback Loop), the rover chassis is controlled by specifying desired 
angular velocities in `rad/s` for each wheel. The firmware maintains these target velocities 
using feedback control. This mode provides predictability for scripting applications in uniform 
terrain and serves as the foundation for closed-loop control that maintains rover heading.

Each wheel remains individually controlled by higher-level applications such as [
    rover-controller-service](../../rover-controller-service/README.md).

**CFL Command Payload:**
```
{
 "eventType": "chassis",    // chassis telemetry unique firmware identifier
 "mode": "CFL",             // operational mode: CFL
 "payload": {
    "fl": ""<<double>>",    // front-left wheel angular velocity in rad/s
    "fr": ""<<double>>",    // front-right wheel angular velocity in rad/s
    "rl": ""<<double>>",    // rear-left wheel angular velocity in rad/s
    "rr": ""<<double>>"     // rear-right wheel angular velocity in rad/s
 }
}
```

### **ROS** operational mode
In **ROS mode**, the rover chassis is controlled using linear and angular velocity vectors that 
correspond to the ROS [Twist](http://docs.ros.org/en/api/geometry_msgs/html/msg/Twist.html) message format. 
This mode is designed for integration with ROS middleware deployed alongside the MQTT-based stack.

The ROS middleware provides autonomous navigation capabilities by combining multiple systems including cameras, 
sensors, dead reckoning algorithms, [SLAM](https://en.wikipedia.org/wiki/Simultaneous_localization_and_mapping), 
and path planning frameworks like [Nav2](https://github.com/ros2/nav2).

This interface enables seamless integration with off-the-shelf ROS components for complete platform autonomy.

**ROS Command Payload:**
```
{
 "eventType": "chassis",    // chassis telemetry unique firmware identifier
 "mode": "ROS",             // operational mode: ROS
 "payload": {
    "linear": [<<double>>, <<double>>, <<double>>],     //Vector3 (x, y, z) expressed in m/s
    "angular": [<<double>>, <<double>>, <<double>>]     //Vector3 (x, y, z) expressed in rad/s
 }
}
```