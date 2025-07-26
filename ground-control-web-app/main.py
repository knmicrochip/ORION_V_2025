from nicegui import ui, app
from app.ui.chassis_pane import chassis_pane
from app.ui.manipulator_pane import manipulator_pane
from app.ui.science_pane import science_pane
from app.ui.menu import menu
from app.state import ChassisState
from app.logic.gamepad import setup_gamepad
from app.logic.mqtt_client import MqttClient
from app.config import setup_logging, MQTT_TOPICS

setup_logging()

# Shared state
state = ChassisState()
mqtt_client = MqttClient()

# Global content area reference
content_area = None
telemetry_area = None

def switch_pane(pane_name: str):
    """Clears the content area and loads the selected pane."""
    global content_area
    with content_area:
        content_area.clear()
        # Disconnect from previous topic if any
        if state.active_topic:
            mqtt_client.unsubscribe(state.active_topic, state.telemetry_callback)
            state.active_topic = None
            state.telemetry_callback = None

        if pane_name == 'chassis':
            chassis_pane(state, mqtt_client)
            state.active_topic = MQTT_TOPICS['chassis_output']
            state.telemetry_callback = lambda topic, payload: telemetry_area.set_text(f"Telemetry: {payload}")
            mqtt_client.subscribe(state.active_topic, state.telemetry_callback)
        elif pane_name == 'manipulator':
            manipulator_pane()
        elif pane_name == 'science':
            science_pane()

# Main UI Layout
@ui.page('/')
def main_page():
    global content_area, telemetry_area

    # Left side: Collapsible Menu (direct child of page)
    with ui.left_drawer().classes('bg-gray-200 p-4') as left_drawer:
        menu(switch_pane)

    # Main content area (fills remaining space)
    with ui.column().classes('w-full h-screen no-wrap'): # Use h-screen to fill vertical space
        # Button to toggle the left drawer (always visible, positioned absolutely)
        ui.button(icon='menu', on_click=left_drawer.toggle).props('flat fab-mini absolute-top-left')

        # Top section: Playground (fills remaining space after button)
        with ui.row().classes('w-full flex-grow no-wrap'): # flex-grow to take available height
            # Playground pane (fills remaining space in this row)
            content_area = ui.column().classes('flex-grow h-full') # flex-grow to take remaining width, full height of this row

        # Bottom section: Telemetry Pane (full width at the very bottom)
        telemetry_area = ui.label("Telemetry Pane").classes('w-full h-1/5 bg-gray-100 p-2') # Fixed height for telemetry

    # Initial content
    switch_pane('chassis')

# Setup gamepad listener
setup_gamepad(state, mqtt_client)

import asyncio

@app.on_startup
async def connect_mqtt():
    mqtt_client.set_event_loop(asyncio.get_running_loop())
    mqtt_client.connect()

@app.on_shutdown
async def disconnect_mqtt():
    mqtt_client.disconnect()

ui.run()
