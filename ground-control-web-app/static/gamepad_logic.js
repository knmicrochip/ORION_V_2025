let gamepads = {}; // Stores connected gamepad objects by index
let intervalId = null; // Stores the interval ID for polling
let connectedGamepadsCount = 0; // Tracks the number of currently connected gamepads

function gamepadHandler(event, connecting) {
    const gamepad = event.gamepad;
    console.log(`Gamepad ${connecting ? 'connected' : 'disconnected'}: ${gamepad.id}`);

    if (connecting) {
        gamepads[gamepad.index] = gamepad;
        connectedGamepadsCount++;
        console.log("Gamepads connected count:", connectedGamepadsCount);
    } else {
        delete gamepads[gamepad.index];
        connectedGamepadsCount--;
        console.log("Gamepads connected count:", connectedGamepadsCount);
    }
}

function pollGamepads() {
    if (connectedGamepadsCount > 0) { // Only proceed if there are connected gamepads
        try {
            const active_gamepads = navigator.getGamepads();

            let gamepad_data = [];
            for (const gamepad of active_gamepads) {
                if (gamepad && gamepads[gamepad.index]) { // Ensure it's a connected gamepad we're tracking
                    let data = {
                        index: gamepad.index,
                        axes: gamepad.axes.map(a => a.toFixed(4)),
                        buttons: gamepad.buttons.map(b => b.pressed),
                    };
                    gamepad_data.push(data);
                }
            }

            if (gamepad_data.length > 0) {
                try {
                    emitEvent('gamepad_data_event', gamepad_data);
                } catch (e) {
                    console.error("Error emitting gamepad_data_event:", e);
                }
            }
        } catch (e) {
            console.error("Error polling gamepads:", e);
        }
    }
}

// Event listeners for gamepad connection/disconnection
window.addEventListener("gamepadconnected", (e) => { gamepadHandler(e, true); }, false);
window.addEventListener("gamepaddisconnected", (e) => { gamepadHandler(e, false); }, false);

// Start continuous gamepad polling
console.log("NiceGUI is ready. Starting continuous gamepad polling.");
if (intervalId === null) { // Ensure interval is only set once
    intervalId = setInterval(pollGamepads, 20); // Poll every 20ms
    console.log("Started continuous gamepad polling. Interval ID:", intervalId);
}