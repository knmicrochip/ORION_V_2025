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

The outbound schema defines messages that the microcontroller sends to the onboard computer via UART. 
These messages provide current feedback from bts motor controllers.

> **Note**: Messages must be formatted in compact mode without extra whitespace to optimize transmission
> speed across all services, including the MQTT integration layer.
**Telemetry Payload Structure:**

```
{
 "eventType": "science",        // science module telemetry unique firmware identifier
 "payload": {
    "FbDrillA":"<<float>>",     //Drill motor current in direction A in Amps
    "FbDrillB":"<<float>>",     //Drill motor current in direction B in Amps
    "FbElevatorA":"<<float>>",  //Elevator motor current in direction A in Amps
    "FbElevatorB":"<<float>>"   //Elevator motor current in direction B in Amps
 }
}
```
When research sequence is completed, the system sends sample data via uart.
**Sample Data Payload Structure:**
```
{
 "eventType": "science",        // science module telemetry unique firmware identifier
 "payload": {
    "number":"<<int>>",         //individual number of a sample in range [1,6]
    "mass":"<<float>>",         //sample mass in grams
    "temp":"<<float>>",         //sample temperature in degrees Celsius
    "gasses":{
        "<<float>>",            //MQ2 gas sensor analog output voltage in Volts
        "<<float>>",            //MQ4 gas sensor analog output voltage in Volts
        "<<float>>",            //MQ5 gas sensor analog output voltage in Volts
        "<<float>>"             //MQ8 gas sensor analog output voltage in Volts
    }
  "lights": {                   //18 count values for different wavelenght each. List of wavelenghts in nanometers in order: [410,435,460,485,510,535,560,585,610,645,680,705,730,760,810,860,900,940]
        "<<float>>",            //counts for 410nm wavelenght
        "<<float>>",            //counts for 435nm wavelenght
        ...
        "<<float>>"             //counts for 940nm wavelenght
  }
 }
}
```

## Inbound messages
Science module commands allow for controlling the drill,conveyor belt and to begin the research sequence when the sample is inserted into the drum.
**Command Payload:**

```
{
 "eventType": "science",    // science telemetry unique firmware identifier
 "payload": {
    "drill": "<<int>>",     // drill PWM in range [-255, 255]
    "elev": "<<int>>",      // elevator PWM in range [-255, 255]
    "conv": "<<int>>",      // three values are possible: 1 - conveyor moves in one direction, -1 - conveyor moves in another direction 0 - conveyor stops
    "res_seq": "<<int>>"    //if the value is 1, start the research sequence
 }
}
```
**Note**: When research sequence start, drill, elevator and belt are shut down.
