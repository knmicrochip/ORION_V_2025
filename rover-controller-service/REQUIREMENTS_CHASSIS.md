# [Functional Requirements] Chassis integration requirements

> The key words "MUST", "MUST NOT", "REQUIRED", "SHALL", "SHALL NOT", "SHOULD", 
> "SHOULD NOT", "RECOMMENDED", "MAY", and "OPTIONAL" in this document are to be 
> interpreted as described in [RFC 2119](https://tools.ietf.org/html/rfc2119).

The initial functional and non-functional requirements have been defined in the 
[Requirements](./REQUIREMENTS.md) file. These shall be applied on top of the specification
defined below.

## Hardware design

Hardware design overview, essentials for the software development:

* The hardware chassis shall be equipped with 4 fixed wheels, with each pair of wheels installed on the same axle.
* The hardware chassis shall support only a [differential drive controller](https://en.wikipedia.org/wiki/Differential_wheeled_robot) for locomotion.
* The chassis must be equipped with encoders to read the angular velocity of each wheel.

## `application.yml` configuration
* The chassis integration shall use the following MQTT topic configuration for 
inbound and outbound traffic, see
[`application.yml` configuration template](./REQUIREMENTS.md#applicationyml-configuration-template) for more details:

```
chassis:
    eventType: chassis
    downstream:
        inbound: orion/topic/chassis/inbound
        outbound: orion/topic/chassis/outbound
    upstream:
        inbound: orion/topic/chassis/controller/inbound
```        

## Payloads

### Inbound payload

* The application shall receive the following payload from the topic configured under  
`chassis.upstream.inbound` published by the upstream application [ground-control-web-app](../ground-control-web-app/README.md):

```
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

| field | type | value range | description |
| --- | --- | --- | --- |
| payload.stick[0] | double | [-1.0, 1.0] | Turn [left, right]. A value of '-1' sets the maximum turn to the right, and '1' sets the maximum turn to the left. |
| payload.stick[1] | double | [-1.0, 1.0] | Drive [forward, backward]. A value of '-1' sets the maximum drive forward, and '1' sets the maximum drive backward. |
| payload.rotate | double | [-1.0, 1.0] | Turn in-place [left, right]. A value of '-1' sets the maximum turn to the left, and '1' sets the maximum turn to the right. |
| payload.button_(x\|y\|a\|b) | boolean | true|false | Not implemented yet | 

## Outbound payloads

* Outbound payloads shall be strictly linked with the [operational modes](#operational-modes)
configured for the robot.


### Operational modes
* The application shall three distinct operational modes:

    * **PWM Mode**: Receives a command from the upstream, remaps it onto PWM values for each
wheel, and sends it downstream. 
    * **CFL Mode** (Closed Feedback Loop): Receives a command from the upstream, remaps it into 
angular velocity values for each wheel, and sends them downstream.
    * **ROS Mode**: Receives a command from the upstream, remaps it into a ROS-compatible 
[Twist](https://docs.ros.org/en/foxy/api/geometry_msgs/html/msg/Twist.html) messages 
messages with linear and angular velocity for precise rover control,
including slip compensation and terrain adaptation

* The application shall support more modes in future
* Operational modes shall be swappable in real-time, supported by the 
[strategy pattern](https://en.wikipedia.org/wiki/Strategy_pattern).
* (!) The outbound payload value (the value this application computes) is based on the assumption that a negative value indicates a counter-clockwise wheel rotation, while a positive value indicates clockwise rotation. This convention applies when standing next to the robot and observing its proximal wheels.


### [Operational Mode] Chassis: PWM Mode


* The application shall map the inbound payload to the outbound payload by applying the
control principles outlined in the table below:

    * Joystick X,Y-axis commands shall be translated to the following values:

| payload.stick[0] | payload.stick[1] | Left PWM | Right PWM | Behavior |
| :-- | :-- | :-- | :-- | :-- |
| 0 | -1 | -255 | 255 | Full speed forward |
| 0 | 1 | 255 | -255 | Full speed backward |
| 0 | 0 | 0 | 0 | No movement |
| -0.3 | -0.7 | 179 | 255 | Forward with gentle left curve |
| 0.3 | -0.7 | 255 | 178 | Forward with gentle right curve |
| -1 | -0.3 | -77 | 255 | Hard left turn, while driving forward |
| 1 | -0.3 | -255 | 77 | Hard right turn, while driving forward |
| -1 | 0.3 | 77 | -255 | Hard left turn, while driving backward |
| 1 | 0.3 | -255 | 77 | Hard right turn, while driving backward |
| -1 | 0 | 0 | 255 | Sharp left turn (left wheels stopped) |
| 1 | 0 | 255 | 0 | Sharp right turn (right wheels stopped) |

* The application shall prioritize `payload.stick[]` input over `payload.rotate`:
* The application shall implement an in-place rover rotation:
    * All `abs(payload.stick[])` values must be lower than 0.1, and `abs(payload.rotate)` must be
    greater than 0.25 to activate in-place robot rotation.
    * The Z-axis joystick shall be translated to control in-place robot rotation:

| payload.rotate | Left PWM | Right PWM | Behavior |
| :-- | :-- | :-- | :-- |
| -1 | -255 | 255 | In-place left rotation |
|1 | 255 | -255 | In-place right rotation |


#### PWM Mode: outbound payload
```
{
 "eventType": "chassis",    // Unique firmware identifier for chassis telemetry
 "mode": "pwm",             // operational mode: PWM
 "payload": {
    "fl": "<<int16>>",      // front-left wheel PWM in range [-255, 255]
    "fr": "<<int16>>",      // front-right wheel PWM in range [-255, 255]
    "rl": "<<int16>>",      // rear-left wheel PWM in range [-255, 255]
    "rr": "<<int16>>"       // rear-right wheel PWM in range [-255, 255]
 }
}
```

### [Operational Mode] Chassis: CFL Mode

CFL Mode requirements: TBD 
#### CFL Mode: Outbound payload

```
{
 "eventType": "chassis",    // Unique firmware identifier for chassis telemetry
 "mode": "CFL",             // operational mode: CFL
 "payload": {
    "fl": "<<double>>",    // Front-left wheel angular velocity in rad/s
    "fr": "<<double>>",    // Front-right wheel angular velocity in rad/s
    "rl": "<<double>>",    // Rear-left wheel angular velocity in rad/s
    "rr": "<<double>>"     // Rear-right wheel angular velocity in rad/s
 }
}
```

### [Operational Mode] Chassis: ROS Mode

CFL Mode requirements: TBD 

#### ROS Mode: Outbound payload
```
{
 "eventType": "chassis",    // Unique firmware identifier for chassis telemetry
 "mode": "ROS",             // operational mode: ROS
 "payload": {
    "linear": [<<double>>, <<double>>, <<double>>],     // Vector3 (x, y, z) expressed in m/s
    "angular": [<<double>>, <<double>>, <<double>>]     // Vector3 (x, y, z) expressed in rad/s
 }
}
```

