from nicegui import ui
import logging
from app.state import ChassisState
from app.logic.mqtt_client import MqttClient
from app.config import MQTT_TOPICS

logger = logging.getLogger(__name__)

def setup_gamepad_listener(state: ChassisState, mqtt_client: MqttClient):
    def handle_gamepad_update(e):
        gamepad_data = e.args
        state.gamepad_active = True

        if not gamepad_data:
            state.gamepad_active = False
            return

        # Prioritize the first connected gamepad
        gamepad = gamepad_data[0]

        # Update state based on gamepad mapping from requirements
        state.left_stick = [float(gamepad['axes'][0]), float(gamepad['axes'][1])]
        state.rotate = float(gamepad['axes'][2]) * 100  # Scale to [-100, 100]
        state.button_x = gamepad['buttons'][0]
        state.button_y = gamepad['buttons'][1]
        state.button_a = gamepad['buttons'][2]
        state.button_b = gamepad['buttons'][3]

        logger.debug(f"Gamepad payload: {state.get_payload()}")
        mqtt_client.publish(MQTT_TOPICS['chassis_input'], state.get_payload())

    # Create a hidden element to receive gamepad events
    gamepad_listener = ui.element('div').on('gamepad_update', handle_gamepad_update)
    return gamepad_listener
