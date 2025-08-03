class ChassisState:
    def __init__(self):
        self.left_stick = [0.0, 0.0]
        self.rotate = 0
        self.button_a = False
        self.button_b = False
        self.button_x = False
        self.button_y = False
        self.gamepad_active = False
        self.active_topic = None
        self.telemetry_callback = None

    def get_payload(self):
        return {
            "eventType": "chassis",
            "payload": {
                "stick": self.left_stick,
                "button_x": self.button_x,
                "button_y": self.button_y,
                "button_a": self.button_a,
                "button_b": self.button_b,
                "rotate": [self.rotate]
            }
        }


class ManipulatorState:
    def __init__(self):
        self.rotate_turret = 0.0
        self.flex_forearm = 0.0
        self.flex_arm = 0.0
        self.flex_gripper = 0.0
        self.rotate_gripper = 0.0
        self.grip = 0.0
        self.button_a = False
        self.button_b = False
        self.button_x = False
        self.button_y = False
        self.gamepad_active = False
        self.active_topic = None
        self.telemetry_callback = None

    def get_payload(self):
        return {
            "eventType": "manipulator",
            "payload": {
                "rotate_turret": self.rotate_turret,
                "flex_forearm": self.flex_forearm,
                "flex_arm": self.flex_arm,
                "flex_gripper": self.flex_gripper,
                "rotate_gripper": self.rotate_gripper,
                "grip": self.grip,
                "button_x": self.button_x,
                "button_y": self.button_y,
                "button_a": self.button_a,
                "button_b": self.button_b
            }
        }