# Core Principles

## Flow State First

* Keep functions under 20 lines for mental clarity
* Use descriptive variable names: sensor_data not sd
* Follow PEP8 religiously - consistency breeds confidence
* Create a skeleton first by creating methods/classes and associations between them.
  Then, gradually fill in the blanks and provide the code

## MQTT Mastery

### Clean topic structure
```
TOPICS = {
    'chassis_output': 'orion/topic/chassis/outbound'.
    'chassis_input': 'orion/topic/chassis/controller/inbound'
}
```
## NiceGUI Zen

* One component per function
* Reactive updates with ui.timer() for real-time MQTT data

( Separate UI logic from business logic

## Testing Rituals
### Test Pyramid

* Unit: Mock MQTT clients, test pure functions
* Integration: Test MQTT message handling
* E2E: Playwright for UI interactions

```
@pytest.fixture
def mock_mqtt_client():
    return MagicMock()
```

## Quick Feedback Loop

* pytest-watch for continuous testing
* Pre-commit hooks for PEP8 validation
* Type hints everywhere: def handle_message(topic: str, payload: dict) -> None:

## Vibecoding Mantras
* Fail Fast: Validate MQTT payloads immediately
* Log Wisely: Use structured logging for debugging IoT chaos
* Stay Async: Leverage asyncio for non-blocking MQTT operations
* Component Isolation: Each UI element should be self-contained
* Implement only one request at a time. Ask immediately for a human review. Propose next another step