import logging
from app.state import ChassisState
from app.logic.mqtt_client import MqttClient
from app.config import MQTT_TOPICS

logger = logging.getLogger(__name__)

def process_gamepad_data(gamepad_data_js, state, mqtt_client):
    try:
        logging.debug(f"Python: process_gamepad_data called with data: {gamepad_data_js}")
        
        gamepad_data = gamepad_data_js

        state.gamepad_active = True

        if not gamepad_data:
            state.gamepad_active = False
            return

        gamepad = gamepad_data[0]
        logging.debug(f"Processing gamepad: {gamepad}")

        state.left_stick = [float(gamepad['axes'][0]), float(gamepad['axes'][1])]
        state.rotate = float(gamepad['axes'][2]) * 100  # Scale to [-100, 100]
        state.button_x = gamepad['buttons'][0]
        state.button_y = gamepad['buttons'][1]
        state.button_a = gamepad['buttons'][2]
        state.button_b = gamepad['buttons'][3]

        logger.debug(f"Gamepad payload: {state.get_payload()}")
        mqtt_client.publish(MQTT_TOPICS['chassis_input'], state.get_payload())
        logging.debug("Python: process_gamepad_data finished successfully!")
    except Exception as e:
        print(f"Error in process_gamepad_data: {e}")
        logger.error(f"Error in process_gamepad_data: {e}")
        import traceback
        traceback.print_exc()

def setup_gamepad_listener(state: ChassisState, mqtt_client: MqttClient):
    # This function will be called from JavaScript via ui.run_javascript
    # No need to return a function or create a ui.element here
    pass
