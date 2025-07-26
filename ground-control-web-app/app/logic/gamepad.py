from nicegui import ui
import logging
from app.state import ChassisState
from app.logic.mqtt_client import MqttClient
from app.config import MQTT_TOPICS

logger = logging.getLogger(__name__)

GAMEPAD_JS = """
let gamepads = {};
let intervalId = null;

function gamepadHandler(event, connecting) {
  const gamepad = event.gamepad;
  if (connecting) {
    gamepads[gamepad.index] = gamepad;
    if (!intervalId) {
      // Poll at 50Hz
      intervalId = setInterval(pollGamepads, 20);
    }
  } else {
    delete gamepads[gamepad.index];
    if (Object.keys(gamepads).length === 0) {
      clearInterval(intervalId);
      intervalId = null;
    }
  }
}

function pollGamepads() {
  try {
    const active_gamepads = navigator.getGamepads();
    let gamepad_data = [];
    for (const gamepad of active_gamepads) {
      if (gamepad) {
        let data = {
          index: gamepad.index,
          axes: gamepad.axes.map(a => a.toFixed(4)),
          buttons: gamepad.buttons.map(b => b.pressed),
        };
        gamepad_data.push(data);
      }
    }
    if (gamepad_data.length > 0) {
      const element = getElement(%%ELEMENT_ID%%);
      if (element) {
        element.emit('gamepad_update', gamepad_data);
      }
    }
  } catch (e) {
    console.error("Error polling gamepads:", e);
  }
}

window.addEventListener("gamepadconnected", (e) => { gamepadHandler(e, true); }, false);
window.addEventListener("gamepaddisconnected", (e) => { gamepadHandler(e, false); }, false);
"""

def setup_gamepad(state: ChassisState, mqtt_client: MqttClient):
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

    # Inject the JavaScript, replacing the placeholder with the actual element ID
    final_js = GAMEPAD_JS.replace('%%ELEMENT_ID%%', str(gamepad_listener.id))
    ui.add_body_html(f'<script>{final_js}</script>')
