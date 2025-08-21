# ORION_V_2025

This repository contains the complete software stack for the ORION_V_2025 Mars rover project.
It encompasses firmware for various robotic modules,
a ground control web application, a rover controller service, and a UART-to-MQTT gateway, 
all designed to work in unison to deliver a robust and reliable robotic experience.

The project is a part of [European Rover Challenge 2025](http://roverchallenge.eu/) efforts to design, build and successfully operate a Mars Rover analog. 
The European Rover Challenge is Europe's premier robotics competition focused on Mars 
exploration missions, where international teams compete in realistic 
Martian terrain simulations to test their rover designs and capabilities. 
This project represents the innovative work being conducted at 
Lublin University of Technology.

## Key Features

*   **Modular Architecture**: Designed with a C4 model approach, separating concerns into system, container, and component levels for clarity and maintainability.
*   **Real-time Communication**: Leverages MQTT for efficient and reliable communication between all software components and hardware modules.
*   **Ground Control Web Application**: A web-based interface for controlling the rover, featuring a modular UI, real-time telemetry, and support for various input methods (UI controls, USB gamepads/joysticks).
*   **Rover Controller Service**: A Java-based service that links real-time event processing with the remote control of the robotic platform, aggregating various hardware modules (chassis, manipulator, science) and implementing their behaviors.
*   **UART-MQTT Gateway**: A service that translates UART full-duplex communication into MQTT-driven events, enabling any onboard device to be connected to the TCP/IP stack and controlled remotely.
*   **Embedded Firmware**: Dedicated firmware for chassis, manipulator and science modules, handling low-level control, sensor data acquisition, and communication.

## Architecture Overview

The system is structured around a central MQTT broker, facilitating communication between the ground control application, the rover controller service, the UART-MQTT gateway, and the various firmware modules. Detailed architecture can be found in [Architecture](./ARCHITECTURE.md) page.

**Service Interactions:**

*   **MQTT Broker**: Acts as the central communication hub. All services publish messages to and subscribe from topics on this broker using the MQTT protocol.
*   **UART-MQTT Gateway**: This service bridges the communication gap between the microcontrollers and the MQTT network. It receives data from microcontrollers via UART and publishes it to the MQTT broker. Conversely, it subscribes to MQTT topics and sends commands to the microcontrollers via UART.
*   **Rover Controller Service**: This Java-based service is the brain of the rover's control logic. It subscribes to MQTT topics for commands originating from the Ground Control Web App. After processing these commands (e.g., applying control algorithms, remapping values), it publishes control signals back to the MQTT broker, which are then routed to the appropriate microcontrollers via the UART-MQTT Gateway. It also subscribes to telemetry data from the microcontrollers (via the UART-MQTT Gateway) to monitor the rover's status.
*   **Ground Control Web App**: This Python-based web application serves as the primary interface for human operators. It publishes control commands (e.g., joystick movements, button presses) to the MQTT broker, which are then consumed by the Rover Controller Service. It also subscribes to telemetry data published by the Rover Controller Service (which originates from the microcontrollers via the UART-MQTT Gateway) to display real-time status to the user.
*   **Embedded Firmware**: Running on microcontrollers, these firmware modules communicate with the UART-MQTT Gateway via UART. They send telemetry data (e.g., sensor readings, motor feedback) and receive control commands.

The interactions between components are represented with the following UML
component diagram:

![Container level software component diagram](uml/c4model/02_container_level/010_container_rover_software_component_diagram.svg)

## Prerequisites

To set up and run the ORION_V_2025 project, you will need the following:

*   **Linux distribution** - the application is aimed to work on Linux machines, although
it requires very little effort to port it to Windows 
*   **Docker**: For containerized deployment of services (MQTT broker, ground control, rover controller, UART-MQTT gateway).
*   **Docker Compose**: For bulk deployment of several containerized applications
*   **Serial Port Access**: Ensure your user has the necessary permissions to access serial ports (e.g., `sudo usermod -a -G dialout $USER` on Linux).

### Local development
*   **Java 21 Development Kit (JDK)**: Required for building and running the `rover-controller-service` and `uart-mqtt-gateway` 
*   **Python 3**: Required for the `ground-control-web-app`.
*   **uv (Python package manager)**: Recommended for managing Python dependencies in the `ground-control-web-app`.
*   **Arduino IDE or PlatformIO**: For compiling and flashing firmware to the microcontrollers.

## Getting Started: Launching the Application

The `launcher.sh` script is designed to simplify the process of starting and stopping the MQTT broker. Before launching, ensure you have met all the prerequisites. Refer to the script manual for more launching details.

**1. Launching the MQTT Broker**

The `launcher.sh` script can start either the NanoMQ or Eclipse Mosquitto broker using Docker Compose. MQTT credentials are automatically handled by the script.

To start the NanoMQ broker:

```bash
./launcher.sh up nanomq
```

To start the Eclipse Mosquitto broker:

```bash
./launcher.sh up eclipse
```

To stop the running MQTT broker:

```bash
./launcher.sh down
```

Your web application (Ground Control Web App) shall be accessible under `http://localhost` (default port: `80`).

**2. Launching Other Services**

The other services (Ground Control Web App, Rover Controller Service, UART-MQTT Gateway) are launched independently. Please refer to their respective `README.md` files for detailed instructions on how to build and run them.

For example:

*   **Ground Control Web App**: Navigate to `ground-control-web-app/` and follow the instructions in its [`README.md`](./ground-control-web-app/README.md).
*   **Rover Controller Service**: Navigate to `rover-controller-service/` and follow the instructions in its [`README.md`](./rover-controller-service/README.md).
*   **UART-MQTT Gateway**: Navigate to `uart-mqtt-gateway/` and follow the instructions in its [`README.md`](./uart-mqtt-gateway/README.md).


## Project Structure

```
.
├── firmware/                 # Embedded firmware for various modules (chassis, manipulator, science)
├── ground-control-web-app/   # Python-based web application for rover control
├── mqtt-server/              # Docker configurations for MQTT brokers (NanoMQ, Eclipse Mosquitto)
├── rover-controller-service/ # Java Spring Boot service for rover control logic
├── uart-mqtt-gateway/        # Java Spring Boot service for UART-MQTT bridging
└── uml/                      # C4 model diagrams and other UML assets
```

## Author

This project is authored by:
* [Krzysztof Kruk](https://github.com/krkruk)
* [MasterOfJunk](https://github.com/MasterOfJunk)
* and other KN Microchip students at Lublin University of Technology

## License

This project is licensed under MIT license. Please refer to [LICENSE](./LICENSE) file.
