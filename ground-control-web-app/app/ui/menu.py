from nicegui import ui
from typing import Callable

def menu(switch_pane_handler: Callable[[str], None], active_pane: str):
    dark_mode_state, on_dark_mode_change = ui.state(False)

    with ui.column():
        dark_mode = ui.dark_mode()
        dark_mode.value = dark_mode_state
        ui.switch('Dark mode', value=dark_mode_state,on_change=lambda x: toggle_state(dark_mode, on_dark_mode_change))
        ui.button("Chassis", on_click=lambda: switch_pane_handler('chassis')).classes(f'{"bg-orange-700" if active_pane == "chassis" else "bg-gray-300"} text-white')
        ui.button("Manipulator", on_click=lambda: switch_pane_handler('manipulator')).classes(f'{"bg-orange-700" if active_pane == "manipulator" else "bg-gray-300"} text-white')
        ui.button("Science", on_click=lambda: switch_pane_handler('science')).classes(f'{"bg-orange-700" if active_pane == "science" else "bg-gray-300"} text-white')

    def toggle_state(dark_mode, on_dark_mode_change):
        dark_mode.toggle()
        on_dark_mode_change(dark_mode.value)