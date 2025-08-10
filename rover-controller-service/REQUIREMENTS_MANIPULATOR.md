# [Functional Requirements] Manipulator integration requirements

> The key words "MUST", "MUST NOT", "REQUIRED", "SHALL", "SHALL NOT", "SHOULD", 
> "SHOULD NOT", "RECOMMENDED", "MAY", and "OPTIONAL" in this document are to be 
> interpreted as described in [RFC 2119](https://tools.ietf.org/html/rfc2119).

The initial functional and non-functional requirements have been defined in the 
[Requirements](./REQUIREMENTS.md) file. These shall be applied on top of the specification
defined below.

## Hardware design
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
    joints:
        turret_rotation:
          max_ang_v: PI/10       // max angular velocity in radians
          min_pos: 0             // min absolute position in radians
          max_pos: 5PI/3         // max absolute position in radians
        forearm_flex:
          max_ang_v: PI/10       // max angular velocity in radians
          min_pos: 0             // min absolute position in radians
          max_pos: 2PI/3         // max absolute position in radians
        arm_flex:
          max_ang_v: PI/10       // max angular velocity in radians
          min_pos: 0             // min absolute position in radians
          max_pos: 2PI/3         // max absolute position in radians
        gripper_flex:
          max_ang_v: PI/10       // max angular velocity in radians
          min_pos: 0             // min absolute position in radians
          max_pos: 2PI/3         // max absolute position in radians
        gripper_rotation:
          max_ang_v: PI/10       // max angular velocity in radians
          min_pos: 0             // min absolute position in radians
          max_pos: 2PI           // max absolute position in radians
        end_effector_flex:
          max_ang_v: PI/10       // max angular velocity in radians
          min_pos: 0             // min absolute position in radians
          max_pos: PI/4          // max absolute position in radians
```

## Payloads
### Inbound payload
* The application shall receive the following payload from the topic configured under  
`manipulator.upstream.inbound` published by the upstream application [ground-control-web-app](../ground-control-web-app/README.md), namely **upstream inbound**:


```
{
  "eventType": "manipulator",
  "mode": "PWM|CFL|POS|INV_K",    // operational mode: PWM|CFL|POS|INV_K, PWM by default if `mode` is not present
  "payload": {
    "rotate_turret": <<double>>,  // [-1.0, 1.0] - rotate [left, right]
    "flex_arm": <<double>>,       // [-1.0, 1.0] - flex [up, down]
    "flex_forearm": <<double>>,   // [-1.0, 1.0] - flex [up, down]
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
    "amps_flex_arm": <<int8_t>>,       // [0, 100] - flex [up, down] in percent
    "amps_flex_forearm": <<int8_t>>,   // [0, 100] - flex [up, down] in percent
    "amps_flex_gripper": <<int8_t>>,   // [0, 100] - flex [up, down] in percent
    "amps_rotate_gripper": <<int8_t>>, // [0, 100] - rotate [left, right] in percent
    "amps_end_effector": <<int8_t>>,   // [0, 100] - end effector (i.e., gripper, a shovel) [open, close] in percent
    "ang_rotate_turret": <<double>>,  // [0, 5PI/3] - angle, absolute position, expressed in radians
    "ang_flex_arm": <<double>>,       // [0, 2PI/3] - angle, absolute position, expressed in radians 
    "ang_flex_forearm": <<double>>,   // [0, 2PI/3] - angle, absolute position, expressed in radians
    "ang_flex_gripper": <<double>>,   // [0, 2PI/3] - angle, absolute position, expressed in radians
    "ang_rotate_gripper": <<double>>, // [0, 2PI]   - angle, absolute position, expressed in radians
    "ang_end_effector": <<double>>,   // [0, PI/4]  - angle, absolute position, expressed in radians 
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
    a the absolute position of each manipulator joint, and sends them downstream.
    * **Manipulator INV_K Mode** (Inverse Kinematics): Receives a command from the upstream
     (a translation and rotation quaternions of an end effector), plans and computes movement of each
     manipulator component, and sends the POS commands downstream.

* An operational mode shall be selectable in run time, supported by the 
[strategy pattern](https://en.wikipedia.org/wiki/Strategy_pattern).
* Each operational mode strategy implementation shall access telemetry messages published by the
microcontroller under `manipulator.downstream.outbound` topic.

* The application should support *manipulator automation* and executions of predefened actions,
 including folding the manipulator, setting it to a manipulator-ready state, etc.
    * The autonomous command execution shall be run independently from PWM/CFL/POS modes.
    * The application shall map `button_(x|y|a|b)` and other button mappings to trigger
    one of the predefined manipulator positions
    * The autonomous manipulator behavior shall be described in section [Manipulator automation](#manipulator-automation)


### Manipulator operational mode: PWM Mode

PWM mode configuration is meant to provide the most basic form of controlling 
manipulator effectors remotely. This is the quickest way to implement and test the hardware
yet it proved to be good enough to control the manipulator in real life scenarios. Each
manipulator effector uses high gear ratio, which minimizes the risks of inconsistent
joint rotation.

* The PWM mode shall convert upstream input into PWM values expressed in percentage 
* The PWM mode shall rely on on open feedback loop to control the manipulator joints
* The operator shall manually control the PWM that is applied to each joint, individually
* The payload shall be sent onto `manipulator.downstream.inbound` topic
* The negative values shall rotate the joints counter-clock wise and positive
values shall rotate joints in clockwise direction, assuming the plane as described in 
[Hardware design](#hardware-design)
* The exact input-to-output mapping rules are presented in the table below

| Input field from `manipulator.upstream.inbound` | Output field to `manipulator.downstream.inbound` | Input value | Mapped output value | Description | 
| :-- | :-- | :-- | :-- | :-- |
| `payload.rotate_turret` | `payload.rotate_turret` | [-1.0, 1.0] | [-100, 100] | Rotate about Y-axis |
| `payload.flex_arm` | `payload.flex_arm` | [-1.0, 1.0] | [-100, 100] | Flex about Z-axis |
| `payload.flex_forearm` | `payload.flex_forearm` | [-1.0, 1.0] | [-100, 100] | Flex about Z-axis |
| `payload.flex_gripper` | `payload.flex_gripper` | [-1.0, 1.0] | [-100, 100] | Flex about Z-axis |
| `payload.rotate_gripper` | `payload.rotate_gripper` | [-1.0, 1.0] | [-100, 100] | Rotate about X-axis |
| `payload.end_effector` | `payload.end_effector` | [-1.0, 1.0] | [-100, 100] | Grip/open-close a lid in the shovel;, values <0 expand the grip; values >1 close the grip |



#### Manipulator PWM mode: Outbound traffic

* The payload shall not exceed 400 bytes
* The payload should be formatted in a compact JSON form

PWM mode payload:

```
{
  "eventType": "manipulator",
  "mode": "PWM",                  // PWM mode indicator
  "payload": {
    "rotate_turret": <<int8_t>>,  // [-100, 100] - rotate [left, right], units: PWM in percentage
    "flex_arm": <<int8_t>>,       // [-100, 100] - flex [up, down], units: PWM in percentage
    "flex_forearm": <<int8_t>>,   // [-100, 100] - flex [up, down], units: PWM in percentage
    "flex_gripper": <<int8_t>>,   // [-100, 100] - flex [up, down], units: PWM in percentage
    "rotate_gripper": <<int8_t>>, // [-100, 100] - rotate [left, right], units: PWM in percentage
    "end_effector": <<int8_t>>,   // [-100, 100] - end effector (i.e., gripper, a shovel) [open, close], units: PWM in percentage
  }
}
```

### Manipulator operational mode: CFL Mode

The CFL mode is meant to control each effector individually with a constant speed, regardless
of the load that each effector might experience. The manipulator firmware shall apply PID
regulator to precisely control angular velocity of each joint. This mode is a step forward
towards full *POS mode*.


* The CFL mode shall convert upstream input into angular velocity values for each joint 
expressed in `rad/s` unit
* The CFL mode shall rely on closed feedback loop to control the speed of each manipulator joint
* The operator shall manually control the angular velocity that is applied to each joint, individually
* The payload shall be sent onto `manipulator.downstream.inbound` topic
* The negative values shall rotate the joints counter-clock wise and positive
values shall rotate joints in clockwise direction, assuming the plane as described in 
[Hardware design](#hardware-design)
* The exact input-to-output mapping rules are presented in the table below

| Input field from `manipulator.upstream.inbound` | Output field to `manipulator.downstream.inbound` | Input value | MAX_ANG_V:double constant | Mapped output value | Description | 
| :-- | :-- | :-- | :-- | :-- | :-- |
| `payload.rotate_turret` | `payload.rotate_turret` | `input`=[-1.0, 1.0] | `MAX_TURRET_ROT_ANG_V` | `MAX_TURRET_ROT_ANG_V*input` | Rotate about Y-axis |
| `payload.flex_arm` | `payload.flex_arm` | `input`=[-1.0, 1.0] | `MAX_ARM_FLEX_ANG_V` | `MAX_ARM_FLEX_ANG_V*input` | Flex about Z-axis |
| `payload.flex_forearm` | `payload.flex_forearm` | `input`=[-1.0, 1.0] | `MAX_FOREARM_FLEX_ANG_V` | `MAX_FOREARM_FLEX_ANG_V*input` | Flex about Z-axis |
| `payload.flex_gripper` | `payload.flex_gripper` | `input`=[-1.0, 1.0] | `MAX_GRIPPER_FLEX_ANG_V` | `MAX_GRIPPER_FLEX_ANG_V*input` | Flex about Z-axis |
| `payload.rotate_gripper` | `payload.rotate_gripper` | `input`=[-1.0, 1.0] | `MAX_GRIPPER_ROT_ANG_V` | `MAX_GRIPPER_ROT_ANG_V*input` | Rotate about X-axis |
| `payload.end_effector` | `payload.end_effector` | `input`=[-1.0, 1.0] | `MAX_END_EFFECTOR_ANG_V` | `MAX_END_EFFECTOR_ANG*input` | Grip/open-close a lid in the shovel;, values <0 expand the grip; values >1 close the grip |

#### Manipulator CFL mode: Outbound traffic

* The payload shall not exceed 400 bytes
* The payload should be formatted in a compact JSON form

CFL mode payload:
```
{
  "eventType": "manipulator",
  "mode": "CFL",
  "payload": {
    "rotate_turret": <<double>>,  // [-1.0, 1.0] - rotate [left, right], units: rad/s
    "flex_arm": <<double>>,       // [-1.0, 1.0] - flex [up, down], units: rad/s
    "flex_forearm": <<double>>,   // [-1.0, 1.0] - flex [up, down], units: rad/s
    "flex_gripper": <<double>>,   // [-1.0, 1.0] - flex [up, down], units: rad/s
    "rotate_gripper": <<double>>, // [-1.0, 1.0] - rotate [left, right], units: rad/s
    "end_effector": <<double>>,           // [-1.0, 1.0] - grip [open, close], units: rad/s
    
  }
}
```

### Manipulator operational mode: POS Mode

TBD

### Manipulator operational mode: INV_K Mode

TBD

### Manipulator automation

* The manipulator automation shall rely on `POS` Mode to apply a desired state
* The manipulator automation shall prevent executing another action while the current
automatic action is in progress
  * The software shall subscribe to inbound telemetry messages from `manipulator.upstream.outbound` topic
  * The telemetry messages shall help tracking the execution progress with a 1% accuracy, i.e,. for a range of *PI/2 rad*, it gives an error range of `+/-PI/200 rad`. 
  Say, a joint must be set to `PI/4`, therefore the correct set shall be `PI/4 +/-PI/200 rad` or `[49PI/200, 51PI/200] rad`

* The manipulator automation shall be immediately interrupted if *upstream.inbound* traffic
is registered. The human command is always a priority
* Each present shall be assigned to a separate command issued by upstream application:
  * `button_x` shall trigger a folding action
  * `button_y` shall trigger a setting to a manipulator-ready state
  * etc.