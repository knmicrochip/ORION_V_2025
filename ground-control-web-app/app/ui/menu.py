from nicegui import ui
from typing import Callable

def menu(switch_pane_handler: Callable[[str], None], active_pane: str):
    
    with ui.column():
        ui.switch('Dark mode').bind_value(ui.dark_mode())
        ui.button("Chassis", on_click=lambda: switch_pane_handler('chassis')).classes(f'{"bg-orange-700" if active_pane == "chassis" else "bg-gray-300"} text-white')
        ui.button("Manipulator", on_click=lambda: switch_pane_handler('manipulator')).classes(f'{"bg-orange-700" if active_pane == "manipulator" else "bg-gray-300"} text-white')
        ui.button("Science", on_click=lambda: switch_pane_handler('science')).classes(f'{"bg-orange-700" if active_pane == "science" else "bg-gray-300"} text-white')
