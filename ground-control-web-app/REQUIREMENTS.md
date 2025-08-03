# Requirements

## Deployment

Requirement: Build a docker image that corresponds to the Tech Stack:

*   [Python3](https://www.python.org/) - main programming language
*   [UV python package manager](https://docs.astral.sh/uv/): modern package manager that turns
package management hell into pleasure
*   [NiceGUI](https://github.com/nicegui/nicegui): Modern and easy to use Web Framework for Python
    * With some TypeScript patches to handle extra things such as Gamepad API
*   [Gamepad API](https://developer.mozilla.org/en-US/docs/Web/API/Gamepad_API): For USB controller input
*   [Python MQTT Paho library](https://pypi.org/project/paho-mqtt/): MQTT5 library with WebSocket support
*   [Docker](https://www.docker.com/): Containerization platform

---

The application uses `uv` as the python package manager. Therefore, the image build process shall be staged.
The application is build as a docker image. To build an image run:

```sh
docker build -t ground-control-web-app .
```

To run the image, execute the following

```sh
docker run -d --name ground-control-app --net host ground-control-web-app
```

## UI design

I need you to create a web application that performs the following:

1. Splits vertically the screen in a proportion of 10% for the left pane 
(called a menu), and the right pane. The left pane is collapsible, on a click of a button
in the top left corner

2. The menu is meant to provide 3 buttons: Chassis, Manipulator, Science.
The behavior for each button shall be provide in section "Business behavior" below.
Only one button can be in active state. The button shall be highlighted when active.

3. The right side pane shall be split horizontally in 80% (called a playground pane) to 
20% ratio (called a telemetry pane).

4. The playground pane shall display widgets associated with any of the active buttons.

5. The telemetry pane shall display text and widgets associated with the active state.

6. The UI shall support both the PC screen with minimal resolution: 1366 x 768, 
and a mobile app screens in both vertical and horizontal orientation.

7. The mobile screen shall accept multi touch.

## Controllers

1. The application shall use UI as a source of MQTT events,

2. The application shall use two USB controllers at once:

   2.1. A gamepad (Xbox Wireless Controller) 

   2.2. A joystick (Logitech Extreme 3D Pro). This controller comes with 'Z-axis', unlike the gamepad

3. Both the gamepad and the joystick are hot-swappable and always active whenever connected.

4. The USB controllers are prioritized in input, which means whenever these are connected,
the input provided by the panes are ignored.

5. The USB controllers are mapped in a way to correspond the UI widgets.
Refer to 'Business logic' section for detailed mapping

6. The USB controllers shall be probed at frequency of 50Hz. This value shall 
be easily configurable

## Pane Designs

This section introduces instructions for development of the mentioned 3 playground panes:
Chassis, Manipulator, Science..

### Playground pane: Chassis

1. The Chassis pane is a seperate widget, located in a separte python file.

2. The core of of the widget is a draggable area, known as `ui.joystick` 
in NiceGUI framework. It comprises a section of a solid background arranged 
in a shape of a rectangle.

3. The `ui.joystick` produces events that form a vector. The axis left-right is called 
the X-axis, and up-down is called the Y-axis. Both axes produce events whenever the 
knob is dragged a value of a 2D vector in range [-1, 1].
Therefore, whenever the knob is moved, it generates an event with X and Y values.
Expected mapping:
```
X Axis: max left: -1, max right: 1
Y Axis: max up: -1, max down: 1
```
The event can be subscribed later. The event shall be represented as `"left_stick": [x_value_real, y_value_real]`

3. Under the `ui.joystick` widget, you will place another `ui.joystick` known as rotation slider. The slider reflect
the Z-axis in the joystick such as Logitech Extreme 3D Pro.
The slider shall use values between -100 and 100. Whenever the user drops the slider button, it is automatically reset to 0, exactly in the middle of the slider. The values shall map
to `axes[2]` in Gamepad API, which corresponds to `rotate` field in the 
'Chassis JSON message' below.

4. At the bottom of the widget there are 4 buttons, namely X, Y, A, B.
They will be colored as the one found in the Xbox Controller.
These buttons generate events that correspond to a click (a brief press and release):
```
"button_x": true|false,
"button_y": true|false, 
"button_a": true|false,
"button_b": true|false
```

5. Whenever any state associated with the widget is triggered, 
emit all current values within the payload, no more frequent than 50Hz (this should be 
a configurable value). Therefore there will be only one event to subscribe to.

### Playground pane: Manipulator

1. The application shall read the UI design and embed it as a part of the UI. The design can be found in `static/manipulator_ui.svg`.

![Manipulator UI](static/manipulator_ui.drawio.svg)

2. The SVG file defines the following attributes found under key `manipulator_func`:

| Attribute name | Allowed value range [double] | Description |
|----------------|----------------|-------------|
| `rotate_turret` | [-1.0, 1.0] | Rotates the turret: -1.0 - max angular velocity anti-clockwise, 1.0 - max angular velocity clockwise |
| `flex_forearm` | [-1.0, 1.0] | Flex the manipulator forearm: -1.0 - max angular velocity upwards, 1.0 - max angular velocity downwards |
| `flex_arm` | [-1.0, 1.0] | Flex the manipulator arm: -1.0 - max angular velocity upwards, 1.0 - max angular velocity downwards |
| `flex_gripper` | [-1.0, 1.0] | Flex the gripper: -1.0 - max angular velocity upwards, 1.0 - max angular velocity downwards |
| `rotate_gripper` | [-1.0, 1.0] | Rotates the gripper: -1.0 - max angular velocity anti-clockwise, 1.0 - max angular velocity clockwise |
| `grip` | [-1.0, 1.0] | Closes/opens the gripper: -1.0 - opens at max speed, 1.0 - closes at max speed |

3. The user shall be able to tap elements described with the attribute `manipulator_func`. 
The action shall display a NiceGUI `ui.joystick` widget on top of the tapped element.
To hide the widget, the user will tap at any other place on the screen. Use `ui.dialog` to implement 
such a feature.

1. The events shall be issued whenever a user acts on the `ui.joystick` widget.

1. The application shall display a row of Xbox-colored buttons, with labels 
`X`, `Y`, `A`, `B` and batching colors. These buttons generate events that 
correspond to a click (a brief press and release):
```
"button_x": true|false,
"button_y": true|false, 
"button_a": true|false,
"button_b": true|false
```

1. The events shall be collected into one JSON message and mapped accordingly as described in section [Manipulator active state](#manipulator-active-state).


### Playground pane: Science

Do not implement

## Business Logic

### Chassis active state

The user taps the Chassis button in the menu on the left side pane. 
It opens the playground side pane with the chassis controller widget,
and the telemetry pane with in the bottom section.

The bottom section name shall display a label "Mode: Chassis", and accept inbound traffic
from an MQTT topic `orion/topic/chassis/outbound`. Once the user leaves from the Chassis mode, unsubscribe from the topic.

The application shall accept input from both a physical gamepad and the UI. The application
shall use the following:

* analog stick (the knob): extract analog values X and Y
* 'X' button': pressed value
* send a JSON-serialized message onto the topic `orion/topic/chassis/controller/inbound`

Chassis JSON message:

```json
{
  "eventType": "chassis",
  "payload": {
    "stick": [x_value_real, y_value_real],
    "button_x": true|false,
    "button_y": true|false,
    "button_a": true|false,
    "button_b": true|false,
    "rotate": [z_value_real]
  }
}
```

Joystick mapping (Gamepad API):

* axes[0] and axes[1] correspond to `stick` field
* axes[2] corresponds to `rotate` field, applies retrieved analog value from the joystick
an the slider value as described in 'Playground pane: Chassis' section.
* buttons[0] corresponds to `button_x` field
* buttons[1] corresponds to `button_y` field
* buttons[2] corresponds to `button_a` field
* buttons[3] corresponds to `button_b` field

## Manipulator active state

1. The user shall tap the 'Manipulator button' in the menu on the left side pane..
1. The application shall open the Manipulator pane, load the SVG and start accepting
UI events as described in [Playground pane: Manipulator](#playground-pane-manipulator).
1. The application shall display maniplator telemetry in the telemetry pane.
It will subscribe to `orion/topic/manipulator/outbound` and display incoming raw JSON
messages.
1. The application shall send messages onto a downstream topic `orion/topic/manipulator/controller/inbound` and follow the JSON schema as defined in [Outbound Manipulator JSON schema](#outbound-manipulator-json-schema)


### Outbound Manipulator JSON schema

1. The application shall use the following schema, regardless of the input source. Either
UI or gamepad shall rely on the schema.
1. The JSON schema: 

```
{
  "eventType": "manipulator",
  "payload": {
    "rotate_turret": <<double>>,  // [-1.0, 1.0] - rotate [left, right]
    "flex_forearm": <<double>>,   // [-1.0, 1.0] - flex [up, down]
    "flex_arm": <<double>>,       // [-1.0, 1.0] - flex [up, down]
    "flex_gripper": <<double>>,   // [-1.0, 1.0] - flex [up, down]
    "rotate_gripper": <<double>>, // [-1.0, 1.0] - rotate [left, right]
    "grip": <<double>>,           // [-1.0, 1.0] - grip [open, close]
    "button_x": true|false,       // pre-programmed action #1
    "button_y": true|false,       // pre-programmed action #2
    "button_a": true|false,       // pre-programmed action #3
    "button_b": true|false        // pre-programmed action #4
  }
}
```

### UI Manipulator JSON schema mapping

1. The user shall act on the UI, which triggers `ui.joystick` related events that
will be captured, serialized and send downstream.
1. The UI events shall be mapped to the schema as described in the table below:

| UI event | Field in JSON schema | `ui.joystick` mapping | Description |
|----------|----------------------|------------------ | ----------- | 
| `rotate_turret` | `payload.rotate_turret` | Use event `event.x` for `ui.joystick` mapping | Rotates the turret |
| `flex_forearm` | `payload.flex_forearm` | Use event `-1*event.y` for `ui.joystick` mapping | Flexes the forearm |
| `flex_arm` | `payload.flex_arm` | Use event `-1*event.y` for `ui.joystick` mapping | Flexes the arm |
| `flex_gripper` | `payload.flex_gripper` | Use event `-1*event.x` for `ui.joystick` mapping | Flexes the gripper |
| `rotate_gripper` | `payload.rotate_gripper` | Use event `-1*event.x` for `ui.joystick` mapping | Rotates the gripper |
| `grip` | `payload.grip` | Use event `event.x` for `ui.joystick` mapping | Closes/opens the gripper |
| `button_x` | `payload.button_x` | N/A | Pre-programmed action #1 |
| `button_y` | `payload.button_y` | N/A | Pre-programmed action #2 |
| `button_a` | `payload.button_a` | N/A | Pre-programmed action #3 |
| `button_b` | `payload.button_b` | N/A | Pre-programmed action #4 |



### Gamepad Manipulator JSON schema mapping

1. The gamepad integration shall not be implemented, for now


## Science active state

Display a label in the playground pane: "Science functionality is not implemented yet"

# MQTT configuration

Apply the following MQTT configs:
* Broker address: ws://mqtt5:9001
* Protocol version: 5
* Automatic reconnection attempts: yet, at intervals of 5 seconds
* Client-id: ground-control-web-app-${timestamp}
* User: user
* Password: user
* Serialization format: JSON

The application shall be accessible to any device in a local network. The broker
address shall be therefore configurable