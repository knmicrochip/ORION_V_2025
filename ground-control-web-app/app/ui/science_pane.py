from nicegui import ui
import asyncio
import logging
from app.state import ScienceState

logger = logging.getLogger(__name__)

from app.logic.mqtt_client import MqttClient
from app.config import MQTT_TOPICS

def science_pane(state: ScienceState, mqtt_client: MqttClient):
    def press_button(button_name):
        if not state.gamepad_active:
            setattr(state, button_name, True)
            logger.info(f"Button {button_name.upper()} pressed")
            mqtt_client.publish(MQTT_TOPICS['science_input'], state.get_payload())

    def release_button(button_name):
        if not state.gamepad_active:
            setattr(state, button_name, False)
            logger.info(f"Button {button_name.upper()} released")
            mqtt_client.publish(MQTT_TOPICS['science_input'], state.get_payload())

    with ui.card().classes('w-full items-center no-shadow border-[1px] border-gray-200 p-4'):
        ui.label("Science Controls").classes('text-xl font-semibold mb-4')

        with ui.row().classes('w-full justify-around items-center'):
          
            # Action Buttons
            with ui.column().classes('items-center'):
                ui.label('Actions').classes('text-lg')
                button_grid = ui.grid(columns=2).classes('gap-2')
                with button_grid:
                    button_0 = ui.button('0').props('color=blue text-black fab-mini')
                    button_1 = ui.button('1').props('color=blue fab-mini')
                    button_2 = ui.button('2').props('color=blue fab-mini')
                    button_3 = ui.button('3').props('color=blue fab-mini')
                    button_4 = ui.button('4').props('color=blue fab-mini')
                    button_5 = ui.button('5').props('color=blue fab-mini')

                # Button event handlers
                for btn, name in [(button_0, 'button_0'), (button_1, 'button_1'), (button_2, 'button_2'), (button_3, 'button_3'),(button_4, 'button_4'),(button_5, 'button_5')]:
                    btn.on('mousedown', lambda b=name: press_button(b))
                    btn.on('mouseup', lambda b=name: release_button(b))
                  

        # Disable UI elements if gamepad is active
        def update_ui_enable_state():
            button_0.enabled = not state.gamepad_active
            button_1.enabled = not state.gamepad_active
            button_2.enabled = not state.gamepad_active
            button_3.enabled = not state.gamepad_active
            button_4.enabled = not state.gamepad_active
            button_5.enabled = not state.gamepad_active

        ui.timer(0.1, update_ui_enable_state, active=True)
