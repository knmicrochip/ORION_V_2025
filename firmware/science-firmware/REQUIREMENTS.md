# Firmware Requirements

This document outlines all requirements that this firmware is designed to meet.

## Application Stack

The application leverages the following software technology stack:

* **[Arduino IDE](https://www.arduino.cc/en/software/)**: Integrated development enviroment for Arduino hardware.
* **[Arduino Framework](https://www.arduino.cc/en/main)**: Embedded platform providing hardware abstraction for rapid development.
* **[ArduinoJson v6](https://arduinojson.org/v6/)**: JSON parsing and serialization library for Arduino. Version 6 is used for not being broken.
* **[Stepper](https://docs.arduino.cc/libraries/stepper/)**: Library for controlling stepper motors.
* **[twi](https://github.com/arduino/ArduinoCore-avr/blob/master/libraries/Wire/src/utility/twi.h)**: Low level twin wire library for Arduino ARM boards. Used to bypass broken i2c repeat start implementation in Wire.
* **[Wire](https://docs.arduino.cc/language-reference/en/functions/communication/wire/)**: Arduino i2c library.
* **[SparkFun AS7265X Arduino Library](https://github.com/sparkfun/SparkFun_AS7265X_Arduino_Library)**: An Arduino library to control the AS7265X Spectral Sensors.
* **[HX711](https://github.com/RobTillaart/HX711)**: Arduino library for HX711 24 bit ADC used for load cells and scales.

## Hardware Stack

* **[Arduino Due](https://docs.arduino.cc/hardware/due/)**: Development board featuring an Atmel SAM3X8E ARM Cortex-M3 CPU.
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
* **Command Parsing**: Parse incoming commands
* **Sample aquisition control**: Control drill, elevator and conveyor belt motors according to received commands
* **Automatic measurement sequence**: Upon receiving a command, conduct an automatic science sequence. Control drum movement, collect data from sensors and transmit it via UART
* **Telemetry System**: Collect and transmit status data
### Communication Protocol

All payloads are terminated with two newline characters: `\n\n`. When the serial implementation detects this delimiter, it parses the entire received buffer as a single JSON message.

Similarly, all outbound data from the application to the onboard computer must be suffixed with `\n\n`.


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
* **[PlatformIO](https://github.com/platformio/platformio)**: Embedded development plugin for VSCode
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
