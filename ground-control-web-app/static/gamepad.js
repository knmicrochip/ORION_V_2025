let gamepads = {};
let intervalId = null;

function gamepadHandler(event, connecting) {
  const gamepad = event.gamepad;
  console.log(`Gamepad ${connecting ? 'connected' : 'disconnected'}: ${gamepad.id}`);
  console.log("Gamepad object:", gamepad);
  if (connecting) {
    gamepads[gamepad.index] = gamepad;
    console.log("Gamepads connected:", Object.keys(gamepads).length);
    if (!intervalId) {
      console.log("Starting polling interval...");
      intervalId = setInterval(pollGamepads, 20);
      console.log("Started gamepad polling. Interval ID:", intervalId);
      // Test if the interval is actually set
      setTimeout(() => {
        console.log("Interval test: 100ms passed");
      }, 100);
    } else {
      console.log("Polling interval already running.");
    }
  } else {
    delete gamepads[gamepad.index];
    console.log("Gamepads connected:", Object.keys(gamepads).length);
    if (Object.keys(gamepads).length === 0) {
      console.log("Clearing polling interval...");
      clearInterval(intervalId);
      intervalId = null;
      console.log("Stopped gamepad polling.");
    }
  }
}

let pollCount = 0;

function pollGamepads() {
  pollCount++;
  console.log(`Polling gamepads... (poll #${pollCount})`);
  try {
    const active_gamepads = navigator.getGamepads();
    console.log("Active gamepads:", active_gamepads);
    let gamepad_data = [];
    for (const gamepad of active_gamepads) {
      if (gamepad) {
        console.log("Processing gamepad:", gamepad.id, "index:", gamepad.index);
        let data = {
          index: gamepad.index,
          axes: gamepad.axes.map(a => a.toFixed(4)),
          buttons: gamepad.buttons.map(b => b.pressed),
        };
        gamepad_data.push(data);
      }
    }
    console.log("Collected gamepad data:", gamepad_data);
    if (gamepad_data.length > 0) {
      console.log("Gamepad data collected:", gamepad_data);
      console.log("Checking if window._receive_gamepad_data is defined:", typeof window._receive_gamepad_data);
      if (typeof window._receive_gamepad_data === 'function') {
        console.log("Calling Python function with gamepad data:", gamepad_data);
        try {
          window._receive_gamepad_data(gamepad_data);
          console.log("Successfully called Python function");
        } catch (e) {
          console.error("Error calling Python function:", e);
        }
      } else {
        console.error("window._receive_gamepad_data is not a function:", typeof window._receive_gamepad_data);
        console.log("Available window properties:", Object.keys(window).filter(key => key.includes('receive') || key.includes('gamepad')));
      }
    } else {
      console.log("No active gamepads found during poll.");
    }
  } catch (e) {
    console.error("Error polling gamepads:", e);
  }
}

window.addEventListener("gamepadconnected", (e) => { gamepadHandler(e, true); }, false);
window.addEventListener("gamepaddisconnected", (e) => { gamepadHandler(e, false); }, false);
