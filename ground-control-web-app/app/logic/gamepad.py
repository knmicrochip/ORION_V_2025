import logging
from app.state import ChassisState, ManipulatorState
from app.logic.mqtt_client import MqttClient
from app.config import MQTT_TOPICS

logger = logging.getLogger(__name__)

def process_chassis_gamepad(gamepad, state: ChassisState, mqtt_client: MqttClient):
    """Processes gamepad data for the chassis."""
    state.left_stick = [float(gamepad['axes'][0]), float(gamepad['axes'][1])]
    state.rotate = float(gamepad['axes'][2])
    state.button_x = gamepad['buttons'][0]
    state.button_y = gamepad['buttons'][1]
    state.button_a = gamepad['buttons'][2]
    state.button_b = gamepad['buttons'][3]

    mqtt_client.publish(MQTT_TOPICS['chassis_input'], state.get_payload())

def process_manipulator_gamepad(gamepad, state: ManipulatorState, mqtt_client: MqttClient):
    """Processes gamepad data for the manipulator."""
    state.rotate_gripper = float(gamepad['axes'][0])
    state.flex_gripper = float(gamepad['axes'][1])
    state.rotate_turret = -float(gamepad['values'][6]) if not gamepad['buttons'][7] else float(gamepad['values'][7])
    state.flex_arm = float(gamepad['axes'][2])
    state.flex_forearm = float(gamepad['axes'][3])
    state.grip = -float(gamepad['values'][4]) if gamepad['buttons'][4] else float(gamepad['values'][5])
    state.button_x = gamepad['buttons'][2]
    state.button_y = gamepad['buttons'][3]
    state.button_a = gamepad['buttons'][0]
    state.button_b = gamepad['buttons'][1]

    mqtt_client.publish(MQTT_TOPICS['manipulator_input'], state.get_payload())

def process_gamepad_data(event, chassis_state: ChassisState, manipulator_state: ManipulatorState, mqtt_client: MqttClient):
    try:
        gamepad_data_js = event.args

        if not gamepad_data_js:
            chassis_state.gamepad_active = False
            manipulator_state.gamepad_active = False
            return

        chassis_state.gamepad_active = False
        manipulator_state.gamepad_active = False

        for gamepad in gamepad_data_js:
            gamepad_id = gamepad.get('id', '').lower()
            if 'logitech extreme 3d' in gamepad_id:
                chassis_state.gamepad_active = True
                process_chassis_gamepad(gamepad, chassis_state, mqtt_client)
            elif 'xbox' in gamepad_id or 'microsoft' in gamepad_id or 'rumblepad':
                manipulator_state.gamepad_active = True
                process_manipulator_gamepad(gamepad, manipulator_state, mqtt_client)

    except Exception as e:
        logger.error(f"Error in process_gamepad_data: {e}", exc_info=True)

def setup_gamepad_listener(chassis_state: ChassisState, manipulator_state: ManipulatorState, mqtt_client: MqttClient):
    pass
