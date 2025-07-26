from nicegui import ui
from typing import Callable

def menu(switch_pane_handler: Callable[[str], None]):
    with ui.column():
        ui.button("Chassis", on_click=lambda: switch_pane_handler('chassis'))
        ui.button("Manipulator", on_click=lambda: switch_pane_handler('manipulator'))
        ui.button("Science", on_click=lambda: switch_pane_handler('science'))
