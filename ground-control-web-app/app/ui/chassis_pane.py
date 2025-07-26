from nicegui import ui
import asyncio
import logging
from app.state import ChassisState

logger = logging.getLogger(__name__)

from app.logic.mqtt_client import MqttClient
from app.config import MQTT_TOPICS

def chassis_pane(state: ChassisState, mqtt_client: MqttClient):
    def send_joystick_data(e):
        if not state.gamepad_active:
            state.left_stick = [e.x, -e.y]
            logger.info(f"Joystick moved: X={e.x:.2f}, Y={-e.y:.2f}")
            mqtt_client.publish(MQTT_TOPICS['chassis_input'], state.get_payload())

    def press_button(button_name):
        if not state.gamepad_active:
            setattr(state, button_name, True)
            logger.info(f"Button {button_name.upper()} pressed")
            mqtt_client.publish(MQTT_TOPICS['chassis_input'], state.get_payload())

    def release_button(button_name):
        if not state.gamepad_active:
            setattr(state, button_name, False)
            logger.info(f"Button {button_name.upper()} released")
            mqtt_client.publish(MQTT_TOPICS['chassis_input'], state.get_payload())

    with ui.card().classes('w-full items-center no-shadow border-[1px] border-gray-200 p-4'):
        ui.label("Chassis Controls").classes('text-xl font-semibold mb-4')

        with ui.row().classes('w-full justify-around items-center'):
            # Joystick
            with ui.column().classes('items-center'):
                ui.label('Movement').classes('text-lg')
                joystick = ui.joystick(
                    on_move=send_joystick_data,
                    on_end=lambda e: (setattr(state, 'left_stick', [0.0, 0.0]), logger.debug("Joystick reset"), mqtt_client.publish(MQTT_TOPICS['chassis_input'], state.get_payload()))
                )

            # Action Buttons
            with ui.column().classes('items-center'):
                ui.label('Actions').classes('text-lg')
                button_grid = ui.grid(columns=2).classes('gap-2')
                with button_grid:
                    y_button = ui.button('Y').props('color=yellow text-black fab-mini')
                    x_button = ui.button('X').props('color=blue fab-mini')
                    b_button = ui.button('B').props('color=red fab-mini')
                    a_button = ui.button('A').props('color=green fab-mini')

                # Button event handlers
                for btn, name in [(y_button, 'button_y'), (x_button, 'button_x'), (b_button, 'button_b'), (a_button, 'button_a')]:
                    btn.on('mousedown', lambda b=name: press_button(b))
                    btn.on('mouseup', lambda b=name: release_button(b))
                    btn.on('mouseleave', lambda b=name: release_button(b))  # Handle mouse leaving button area
                    btn.on('touchstart', lambda b=name: press_button(b), throttle=0.1)
                    btn.on('touchend', lambda b=name: release_button(b), throttle=0.1)

        ui.separator().classes('my-4')

        # Rotation Joystick
        with ui.column().classes('w-full items-center'):
            ui.label('Rotation').classes('text-lg')
            _rotation_slider = ui.joystick()

            # Bind the joystick's value to the state for visual feedback (optional, as joystick is input)
            # _rotation_slider.bind_value(state, 'rotate') # Not directly bindable like slider

            def handle_rotation_joystick_move(e):
                if not state.gamepad_active:
                    state.rotate = e.x
                    logger.info(f"Rotation joystick moved: {e.x:.2f}")
                    mqtt_client.publish(MQTT_TOPICS['chassis_input'], state.get_payload())

            def handle_rotation_joystick_end(e):
                if not state.gamepad_active:
                    state.rotate = 0
                    logger.info("Rotation joystick reset to 0")
                    mqtt_client.publish(MQTT_TOPICS['chassis_input'], state.get_payload())

            _rotation_slider.on_move(handle_rotation_joystick_move)
            _rotation_slider.on_end(handle_rotation_joystick_end)

        # Disable UI elements if gamepad is active
        def update_ui_enable_state():
            joystick.enabled = not state.gamepad_active
            _rotation_slider.enabled = not state.gamepad_active
            y_button.enabled = not state.gamepad_active
            x_button.enabled = not state.gamepad_active
            b_button.enabled = not state.gamepad_active
            a_button.enabled = not state.gamepad_active

        ui.timer(0.1, update_ui_enable_state, active=True)