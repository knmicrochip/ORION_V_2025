from nicegui import ui, app
from app.ui.chassis_pane import chassis_pane
from app.ui.manipulator_pane import manipulator_pane
from app.ui.science_pane import science_pane
from app.ui.menu import menu
from app.state import ChassisState, ManipulatorState
from app.logic.gamepad import process_gamepad_data
from app.logic.mqtt_client import MqttClient
from app.config import setup_logging, MQTT_TOPICS

setup_logging()

app.add_static_files('/static', 'static') # Explicitly add static files directory

ui.add_head_html('<link rel="stylesheet" href="/static/theme.css">')

# Shared states
chassis_state = ChassisState()
manipulator_state = ManipulatorState()
mqtt_client = MqttClient()

# Global content area reference
content_area = None

@ui.refreshable
def menu_content(active_pane: str):
    menu(switch_pane, active_pane)

@ui.refreshable
def telemetry_content(mode: str, payload: dict = None):
    ui.label(f"Mode: {mode}").classes('text-lg font-semibold')
    if payload:
        for key, value in payload.items():
            ui.label(f"{key}: {value}")
    else:
        ui.label(f"{mode} functionality is not implemented yet").classes('text-md')

def switch_pane(pane_name: str):
    """Clears the content area and loads the selected pane."""
    global content_area
    with content_area:
        content_area.clear()
        # Disconnect from previous topic if any
        if chassis_state.active_topic:
            mqtt_client.unsubscribe(chassis_state.active_topic, chassis_state.telemetry_callback)
            chassis_state.active_topic = None
            chassis_state.telemetry_callback = None
        if manipulator_state.active_topic:
            mqtt_client.unsubscribe(manipulator_state.active_topic, manipulator_state.telemetry_callback)
            manipulator_state.active_topic = None
            manipulator_state.telemetry_callback = None

        if pane_name == 'chassis':
            chassis_pane(chassis_state, mqtt_client)
            chassis_state.active_topic = MQTT_TOPICS['chassis_output']
            chassis_state.telemetry_callback = lambda topic, payload: telemetry_content.refresh('Chassis', payload)
            mqtt_client.subscribe(chassis_state.active_topic, chassis_state.telemetry_callback)
        elif pane_name == 'manipulator':
            manipulator_pane(manipulator_state, mqtt_client)
            manipulator_state.active_topic = MQTT_TOPICS['manipulator_output']
            manipulator_state.telemetry_callback = lambda topic, payload: telemetry_content.refresh('Manipulator', payload)
            mqtt_client.subscribe(manipulator_state.active_topic, manipulator_state.telemetry_callback)
        elif pane_name == 'science':
            science_pane()
            telemetry_content.refresh('Science')
    menu_content.refresh(pane_name) # Refresh the menu to highlight the active pane

# Main UI Layout
@ui.page('/')
def main_page():
    global content_area

    # Left side: Collapsible Menu (direct child of page)
    with ui.left_drawer().classes('p-4') as left_drawer:
        menu_content('chassis') # Initial active pane

    # Main content area (fills remaining space)
    with ui.column().classes('w-full h-screen no-wrap'): # Use h-screen to fill vertical space
        # Button to toggle the left drawer (always visible, positioned absolutely)
        ui.button(icon='menu', on_click=left_drawer.toggle).props('flat fab-mini absolute-top-left')

        # Top section: Playground (fills remaining space in this row)
        with ui.row().classes('w-full flex-grow no-wrap'): # flex-grow to take available height
            # Playground pane (fills remaining space in this row)
            content_area = ui.column().classes('flex-grow h-full') # flex-grow to take remaining width, full height of this row

        # Bottom section: Telemetry Pane (full width at the very bottom)
        with ui.column().classes('w-full h-1/5 p-2'): # This is the container for telemetry_content
            telemetry_content('Chassis') # Initial display for telemetry

        # Initial content
    switch_pane('chassis')

    ui.on('gamepad_data_event', lambda e: process_gamepad_data(e, chassis_state, manipulator_state, mqtt_client))

    # Inject Gamepad JS
    ui.add_body_html('<script src="/static/gamepad_logic.js"></script>')
    

import asyncio

@app.on_startup
async def connect_mqtt():
    mqtt_client.set_event_loop(asyncio.get_running_loop())
    mqtt_client.connect()

@app.on_shutdown
async def disconnect_mqtt():
    mqtt_client.disconnect()

ui.run()
