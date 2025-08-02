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
You will also need libraries mentioned [here]()

## Deployment instructions:
1. Open science.ino in Arduino IDE.
3. Select " Arduino Due (Programming Port)" in board selection menu.
4. Compile and upload.
