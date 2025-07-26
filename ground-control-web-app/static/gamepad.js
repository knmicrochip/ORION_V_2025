let gamepads = {};
let intervalId = null;

function gamepadHandler(event, connecting) {
  const gamepad = event.gamepad;
  console.log(`Gamepad ${connecting ? 'connected' : 'disconnected'}: ${gamepad.id}`);
  if (connecting) {
    gamepads[gamepad.index] = gamepad;
    if (!intervalId) {
      intervalId = setInterval(pollGamepads, 20);
      console.log("Started gamepad polling.");
    }
  } else {
    delete gamepads[gamepad.index];
    if (Object.keys(gamepads).length === 0) {
      clearInterval(intervalId);
      intervalId = null;
      console.log("Stopped gamepad polling.");
    }
  }
}

function pollGamepads() {
  try {
    const active_gamepads = navigator.getGamepads();
    let gamepad_data = [];
    for (const gamepad of active_gamepads) {
      if (gamepad) {
        let data = {
          index: gamepad.index,
          axes: gamepad.axes.map(a => a.toFixed(4)),
          buttons: gamepad.buttons.map(b => b.pressed),
        };
        gamepad_data.push(data);
      }
    }
    if (gamepad_data.length > 0) {
      // Use the globally set element ID
      const element = getElement(window.gamepadElementId);
      if (element) {
        element.emit('gamepad_update', gamepad_data);
        // console.log("Emitted gamepad_update:", gamepad_data);
      }
    } else {
      // console.log("No active gamepads found during poll.");
    }
  } catch (e) {
    console.error("Error polling gamepads:", e);
  }
}

window.addEventListener("gamepadconnected", (e) => { gamepadHandler(e, true); }, false);
window.addEventListener("gamepaddisconnected", (e) => { gamepadHandler(e, false); }, false);