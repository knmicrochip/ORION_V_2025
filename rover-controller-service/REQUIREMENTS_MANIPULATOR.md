# [Functional Requirements] Manipulator integration requirements

> The key words "MUST", "MUST NOT", "REQUIRED", "SHALL", "SHALL NOT", "SHOULD", 
> "SHOULD NOT", "RECOMMENDED", "MAY", and "OPTIONAL" in this document are to be 
> interpreted as described in [RFC 2119](https://tools.ietf.org/html/rfc2119).

The initial functional and non-functional requirements have been defined in the 
[Requirements](./REQUIREMENTS.md) file. These shall be applied on top of the specification
defined below.

Hardware design overview, essentials for the software development:

* The manipulator shall be a 6-Degree-of-Freedom device installed directly on
a chassis platform
* The manipulator shall comprise the following hardware components.
A right-handed Cartesian coordinate system is used. The rover is observed 
in the X–Y plane (median plane) as viewed from its right side (with the robot’s front on the right):
    * X-axis is front-back (sagittal axis), Y-axis is up-down (vertical axis), and Z-axis is left-right (horizontal axis).
    * The turret shall rotate about the Y-axis. All remaining components shall 
    be installed directly on top of it. Therefore, the turret is the base of the coordinate system.
    * The arm shall flex about the Z-axis (motion in the X–Y plane). 
    It is joined with the turret at its proximal end and with the forearm at its distal joint.
    * The forearm shall flex about the Z-axis (motion in the X–Y plane). 
    It is joined with the arm at its proximal end and with the gripper at its distal joint.
    * The gripper/wrist shall rotate about its X-axis (rotation in the Y–Z plane).
    * The gripper/wrist shall rotate about its Z-axis (rotation in the X–Y plane). 
    It is joined with the forearm at its proximal end and with the end-effector at its distal joint.
    * The end-effector shall flex about the Z-axis (motion in the X–Y plane). 
    It is joined with the gripper at its proximal end. The end effector is a swappable component with
    a 2-finger grasper and a closable shovel

The manipulator can be represented with the following diagram:

![Manipulator hardware design](../ground-control-web-app/static/manipulator_ui.drawio.svg)




## `application.yml` configuration

* The manipulator integration shall use the following MQTT topic configuration 
for inbound and outbound traffic, see 
[`application.yml` configuration template](./REQUIREMENTS.md#applicationyml-configuration-template) for more details:

```
manipulator:
    eventType: manipulator
    downstream:
        inbound: orion/topic/manipulator/inbound
        outbound: orion/topic/manipulator/outbound
    upstream:
        inbound: orion/topic/manipulator/controller/inbound
```

## Payloads
### Inbound payload
* The application shall receive the following payload from the topic configured under  
`manipulator.upstream.inbound` published by the upstream application [ground-control-web-app](../ground-control-web-app/README.md), namely **upstream inbound**:


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

* The application should receive the following payload from the topic configured under
`manipulator.downstream.outbound` published by the downstream application including 
[uart-mqtt-gateway](../uart-mqtt-gateway/README.md) and [manipulator-firmware](../firmware/manipulator-firmware). The payload shall be utilized to implement semi-autonomous behavior
as described in section [Manipulator automation](#manipulator-automation)

```
(payload under 400 bytes)
{
  "eventType": "manipulator",
  "mode": "PWM",
  "payload": {
    "amps_rotate_turret": <<int8_t>>,  // [0, 100] - rotate [left, right] in percent
    "amps_flex_forearm": <<int8_t>>,   // [0, 100] - flex [up, down] in percent
    "amps_flex_arm": <<int8_t>>,       // [0, 100] - flex [up, down] in percent
    "amps_flex_gripper": <<int8_t>>,   // [0, 100] - flex [up, down] in percent
    "amps_rotate_gripper": <<int8_t>>, // [0, 100] - rotate [left, right] in percent
    "amps_end_effector": <<int8_t>>,   // [0, 100] - end effector (i.e., gripper, a shovel) [open, close] in percent
    "ang_rotate_turret": <<double>>,  // [0, 5PI/3] - angle, Cartesian coordinates [rotated maximally to right, rotated maximally to left]
    "ang_flex_forearm": <<double>>,   // [0, 100] - flex [up, down] in percent
    "ang_flex_arm": <<double>>,       // [0, 100] - flex [up, down] in percent
    "ang_flex_gripper": <<double>>,   // [0, 100] - flex [up, down] in percent
    "ang_rotate_gripper": <<double>>, // [0, 100] - rotate [left, right] in percent
    "ang_end_effector": <<double>>,   // [0, 100] - end effector (i.e., gripper, a shovel) [open, close] in percent
  }
}
```

## Outbound payloads

* Outbound payloads shall be strictly linked with the [operational modes](#operational-modes)
configured for the robot.

### Operational mode

* The application shall three distinct manipulator operational modes:

    * **Manipulator PWM Mode**: (Open Feedback Loop) Receives a command from the upstream, remaps it onto PWM values for each manipulator joint, and sends it downstream. 
    * **Manipulator CFL Mode** (Closed Feedback Loop): Receives a command from the upstream, remaps it into angular velocity values for each manipulator joint, and sends them downstream.
    * **Manipulator POS Mode** (Position Control): Receives a command from the upstream, remaps it into
    a tranlation

* The application should support *manipulator automation* and executions of predefened actions,
 including folding the manipulator, setting it to a manipulator-ready state, etc.
    * The autonomous command execution shall be run independently from PWM/CFL/POS modes.
    * The application shall map `button_(x|y|a|b)` and other button mappings to trigger
    one of the predefined manipulator positions
    * The autonomous manipulator behavior shall be described in section [Manipulator automation](#manipulator-automation)


### Manipulator operational mode: PWM Mode
#### Manipulator PWM mode: Outbound traffic

(payload under 400 bytes)
```
{
  "eventType": "manipulator",
  "mode": "PWM",
  "payload": {
    "rotate_turret": <<int8_t>>,  // [-100, 100] - rotate [left, right], units: PWM in percent
    "flex_forearm": <<int8_t>>,   // [-100, 100] - flex [up, down], units: PWM in percent
    "flex_arm": <<int8_t>>,       // [-100, 100] - flex [up, down], units: PWM in percent
    "flex_gripper": <<int8_t>>,   // [-100, 100] - flex [up, down], units: PWM in percent
    "rotate_gripper": <<int8_t>>, // [-100, 100] - rotate [left, right], units: PWM in percent
    "end_effector": <<int8_t>>,   // [-100, 100] - end effector (i.e., gripper, a shovel) [open, close], units: PWM in percent
  }
}
```

### Manipulator operational mode: CFL Mode

```
{
  "eventType": "manipulator",
  "mode": "CFL",
  "payload": {
    "rotate_turret": <<double>>,  // [-1.0, 1.0] - rotate [left, right], units: rad/s
    "flex_forearm": <<double>>,   // [-1.0, 1.0] - flex [up, down], units: rad/s
    "flex_arm": <<double>>,       // [-1.0, 1.0] - flex [up, down], units: rad/s
    "flex_gripper": <<double>>,   // [-1.0, 1.0] - flex [up, down], units: rad/s
    "rotate_gripper": <<double>>, // [-1.0, 1.0] - rotate [left, right], units: rad/s
    "grip": <<double>>,           // [-1.0, 1.0] - grip [open, close], units: rad/s
    
  }
}
```

### Manipulator automation

* The manipulator automation shall rely on `POS` Mode to apply a desired state
* The manipulator automation shall prevent executing another action while the current
automatic action is in progress
* The manipulator automation shall be immediately interrupted if *upstream.inbound* traffic
is registered. The human command is always a priority