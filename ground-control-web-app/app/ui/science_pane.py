from nicegui import ui
import asyncio
import logging
from app.state import ScienceState

logger = logging.getLogger(__name__)

from app.logic.mqtt_client import MqttClient
from app.config import MQTT_TOPICS

def science_pane(science_state: ScienceState, mqtt_client: MqttClient):
    def send_joystick_data(e):
        if not science_state.gamepad_active:
            science_state.stick = -e.y
            logger.info(f"Joystick moved: Y={-e.y:.2f}")
            mqtt_client.publish(MQTT_TOPICS['science_input'], science_state.get_payload())

    def toggle_changed():
        if not science_state.gamepad_active:
            setattr(science_state,"conveyor",toggle_conv.value)
            logger.info(f"Conveyor toggle changed: value={toggle_conv.value}")
            mqtt_client.publish(MQTT_TOPICS['science_input'], science_state.get_payload())

    def slider_changed():
        if not science_state.gamepad_active:
            setattr(science_state,"drill",slider_drill.value)
            logger.info(f"Drill slider changed: value={slider_drill.value}")
            mqtt_client.publish(MQTT_TOPICS['science_input'], science_state.get_payload())

    def press_button(button_name):
        if not science_state.gamepad_active:
            setattr(science_state, button_name, True)
            logger.info(f"Button {button_name.upper()} pressed")
            mqtt_client.publish(MQTT_TOPICS['science_input'], science_state.get_payload())

    def release_button(button_name):
        if not science_state.gamepad_active:
            setattr(science_state, button_name, False)
            logger.info(f"Button {button_name.upper()} released")
            mqtt_client.publish(MQTT_TOPICS['science_input'], science_state.get_payload())

    def reset_slider():
        slider_drill.set_value(0)
        setattr(science_state, "drill", 0)
        slider_drill.update()

    with ui.card().classes('w-full items-center no-shadow border-[1px] border-gray-200 p-4'):
        ui.label("Science Controls").classes('text-xl font-semibold mb-4')

        with ui.grid(columns=2).classes('w-full justify-around items-center'):
          
            # Start Button
            with ui.column().classes('items-center'):
                ui.label('Drum').classes('text-lg')
                button_start = ui.button('Start Sequence').props('color=blue fab-mini')

                # Button event handlers
                for btn, name in [(button_start, 'start'), ]:
                    btn.on('mousedown', lambda b=name: press_button(b))
                    btn.on('mouseup', lambda b=name: release_button(b))

            # Conveyor Slider
            with ui.column().classes('items-center'):
                ui.label('Conveyor').classes('text-lg')
                toggle_conv = ui.toggle({1: 'Reverse', 2: 'Stop', 3: 'Forward'},
                    value=2,
                    on_change=toggle_changed
                ).props('fab-mini')

            # Elevator Joystick
            with ui.column().classes('items-center'):
                ui.label('Elevator').classes('text-lg')
                joystick_elev = ui.joystick(
                    on_move=send_joystick_data,
                    on_end=lambda e: (setattr(science_state, 'stick', 0.0),logger.debug("Joystick reset"),mqtt_client.publish(MQTT_TOPICS['science_input'],science_state.get_payload()))
                ).classes('w-32 h-32 bg-[#f7a623] opacity-80 rounded-full border-2 border-black')

            #Drill Slider
            with ui.column().classes('items-center'):
                ui.label('Drill').classes('text-lg')
                slider_drill = ui.slider(min=-255,max = 255, value = 0,
                    on_change=slider_changed)
                ui.label().bind_text_from(slider_drill, 'value')
                button_reset = ui.button('stop',on_click=reset_slider)
                  

        # Disable UI elements if gamepad is active
        #def update_ui_enable_state():
        #    button_start.enabled = not science_state.gamepad_active
        #    toggle_conv.enabled = not science_state.gamepad_active
        #    joystick_elev.enabled = not science_state.gamepad_active
        #    slider_drill.enabled = not science_state.gamepad_active
        #    button_reset.enabled = not science_state.gamepad_active



        #ui.timer(0.1, update_ui_enable_state, active=True)