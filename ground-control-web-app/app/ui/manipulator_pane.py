from nicegui import ui
import logging
from app.state import ManipulatorState
from app.logic.mqtt_client import MqttClient
from app.config import MQTT_TOPICS

logger = logging.getLogger(__name__)

def manipulator_pane(state: ManipulatorState, mqtt_client: MqttClient):
    
    def send_manipulator_data():
        """Send current manipulator state via MQTT."""
        if not state.gamepad_active:
            logger.info(f"Sending manipulator data: {state.get_payload()}")
            mqtt_client.publish(MQTT_TOPICS['manipulator_input'], state.get_payload())

    def handle_gamepad_input(gamepad_data):
        """Handle manipulator control logic from gamepad data."""
        # This is a stub for you to fill in.
        # Example: state.rotate_turret = gamepad_data['axes'][0]
        logger.info(f"Manipulator gamepad data: {gamepad_data}")

    with ui.dialog() as joystick_dialog, ui.card().classes('w-80 h-80 flex flex-col items-center justify-center'):
        joystick_container = ui.column().classes('items-center')

    def create_joystick_in_dialog(func_name: str):
        """Creates a joystick inside the dialog for the given function."""
        with joystick_container:
            ui.label(f"Controlling: {func_name.replace('_', ' ').title()}").classes('self-center')
            
            def on_move(e):
                if not state.gamepad_active:
                    if func_name in ['rotate_turret', 'rotate_gripper']:
                        value = e.x
                    elif func_name in ['flex_forearm', 'flex_arm', 'flex_gripper', 'grip']:
                        value = -1 * e.y
                    else:
                        value = 0
                    
                    setattr(state, func_name, max(-1.0, min(1.0, value)))
                    send_manipulator_data()

            def on_end(e):
                if not state.gamepad_active:
                    setattr(state, func_name, 0.0)
                    send_manipulator_data()

            ui.joystick(on_move=on_move, on_end=on_end).classes('w-32 h-32 bg-[#f7a623] opacity-80 rounded-full border-2 border-black')

    def open_joystick_dialog(func_name: str):
        """Clear previous joystick and open the dialog with a new one."""
        joystick_container.clear()
        create_joystick_in_dialog(func_name)
        joystick_dialog.open()

    with ui.column().classes('w-full items-center no-shadow border-[1px] border-gray-200 p-4'):
        ui.label("Manipulator Controls").classes('text-xl font-semibold mb-4')
        
        with ui.element('div').classes('relative w-full max-w-2xl mx-auto'):
            ui.image('/static/manipulator_ui.drawio.svg').classes('w-full')

            svg_width = 660
            svg_height = 550

            def create_joystick_area(func_name, x, y, w, h, color):
                style = f'left: {x/svg_width*100}%; top: {y/svg_height*100}%; width: {w/svg_width*100}%; height: {h/svg_height*100}%;'
                element = ui.element('div').classes(f'absolute cursor-pointer {color} opacity-0').style(style)
                element.on('click', lambda: open_joystick_dialog(func_name))
                return element

            create_joystick_area('flex_arm (up-down)', 44, 280, 44, 65, 'bg-[#f7a623]')
            create_joystick_area('flex_forearm (up-down)', 223, 3, 44, 65, 'bg-green-500')
            create_joystick_area('flex_gripper (up-down)', 470, 3, 44, 65, 'bg-yellow-500')
            create_joystick_area('grip (up-down)', 550, 125, 44, 65, 'bg-purple-500')
            create_joystick_area('rotate_turret (left-right)', 15, 440, 100, 80, 'bg-pink-500')
            create_joystick_area('rotate_gripper (left-right)', 365, 244, 100, 80, 'bg-blue-500')

        ui.separator().classes('my-4')
        
        with ui.column().classes('w-full items-center'):
            ui.label('Actions').classes('text-lg')
            button_grid = ui.grid(columns=2).classes('gap-2')
            with button_grid:
                y_button = ui.button('Y').props('color=yellow text-black fab-mini')
                x_button = ui.button('X').props('color=blue fab-mini')
                b_button = ui.button('B').props('color=red fab-mini')
                a_button = ui.button('A').props('color=green fab-mini')
            
            def press_button(button_name):
                if not state.gamepad_active:
                    setattr(state, button_name, True)
                    send_manipulator_data()

            def release_button(button_name):
                if not state.gamepad_active:
                    setattr(state, button_name, False)
                    send_manipulator_data()

            for btn, name in [(y_button, 'button_y'), (x_button, 'button_x'), (b_button, 'button_b'), (a_button, 'button_a')]:
                btn.on('mousedown', lambda b=name: press_button(b))
                btn.on('mouseup', lambda b=name: release_button(b))
                btn.on('mouseleave', lambda b=name: release_button(b))
                btn.on('touchstart', lambda b=name: press_button(b), throttle=0.1)
                btn.on('touchend', lambda b=name: release_button(b), throttle=0.1)
        
        def update_ui_enable_state():
            enabled = not state.gamepad_active
            y_button.enabled = enabled
            x_button.enabled = enabled
            b_button.enabled = enabled
            a_button.enabled = enabled
            # The joystick areas don't need to be disabled as the dialog won't open if gamepad is active
            # and the on_move/on_end handlers already check the flag.

        ui.timer(0.1, update_ui_enable_state, active=True)
