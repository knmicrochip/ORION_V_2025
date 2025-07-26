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
                "rotate": [self.rotate / 100.0],  # Scaled to [-1, 1]
            }
        }
