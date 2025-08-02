# ORION V Science Module Firmware 2025 edition
This directory contains embedded firmware for the ORION V science module. Its primary role is to control the science module, which includes collecting sample data from sensors and sending
it to the main controller. It is also responsible for allowing manual control over the drill, elevator and conveyor belt.
The software is written using Arduino IDE, and is only compatible with Arduino Due board.

## Features
* **Communication with the main controller**:
Communicates with the main controller via UART and mqtt for receiving commands and sending telemetry and sample data.
* **Sample data collection**: Conducts an automated research sequence, moving the sample between different sensors and collecting data.
When the sequence is complete, compiled data is sent in a single frame to the main controller.
* **Telemetry data transmission**: Sends current consumption data from drill and elevator motors to facilitate system status monitoring.

## Getting started
### Prerequisites
[Arduino IDE](https://www.arduino.cc/en/software/) or [PlatformIO](https://platformio.org/) are required.
You will also need following libraries :
* **[ArduinoJson v6](https://arduinojson.org/v6/)**
* **[Stepper](https://docs.arduino.cc/libraries/stepper/)**
* **[Wire](https://docs.arduino.cc/language-reference/en/functions/communication/wire/)**
* **[SparkFun AS7265X Arduino Library](https://github.com/sparkfun/SparkFun_AS7265X_Arduino_Library)**
* **[HX711](https://github.com/RobTillaart/HX711)**

You may also need to install Arduino SAM Boards software in Arduino IDE Boards Manager.

### Deployment instructions (Arduino IDE):
1. Make sure all sensors and wires are properly connected to the science board.
2. Connect the Arduino Due board to your computer. Make sure you plug in the cable to the programming port of Due, not the native one.
3. Open science.ino in Arduino IDE.
4. Make sure all libraries mentioned above are installed.
5. Select " Arduino Due (Programming Port)" in board selection menu.
6. Compile and upload.

## Usage
When reset by powering on or by flashing firmware, the system will calibrate drum position, which may take a few seconds.
When the drum stops moving the system is able to receive commands. Accepted commands are described in REQUIREMENTS.md file.
**Note**: When research sequence is started, the device is unable to receive any commands until sequence is over.
