import logging
import os

def setup_logging():
    log_level_str = os.environ.get("LOG_LEVEL", "INFO").upper()
    log_level = getattr(logging, log_level_str, logging.INFO)

    logging.basicConfig(
        level=log_level,
        format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
        handlers=[
            logging.StreamHandler()
        ]
    )
    logging.getLogger("nicegui").setLevel(logging.WARNING) # Suppress verbose NiceGUI logs


# MQTT Configuration
MQTT_BROKER_URL = os.environ.get("MQTT_BROKER_URL", "localhost")
MQTT_BROKER_PORT = int(os.environ.get("MQTT_BROKER_PORT", 9001))
MQTT_PROTOCOL_VERSION = 5
MQTT_RECONNECT_INTERVAL = 5 # seconds
MQTT_CLIENT_ID_PREFIX = "ground-control-web-app-"
MQTT_USERNAME = os.environ.get("MQTT_USERNAME", "user")
MQTT_PASSWORD = os.environ.get("MQTT_PASSWORD", "user")

MQTT_TOPICS = {
    'chassis_output': 'orion/topic/chassis/outbound',
    'chassis_input': 'orion/topic/chassis/controller/inbound',
    'manipulator_output': 'orion/topic/manipulator/outbound',
    'manipulator_input': 'orion/topic/manipulator/controller/inbound'
}
