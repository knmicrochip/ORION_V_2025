import paho.mqtt.client as mqtt
import json
import time
import logging
import asyncio
from urllib.parse import urlparse

from app.config import (
    MQTT_BROKER_URL,
    MQTT_BROKER_PORT,
    MQTT_PROTOCOL_VERSION,
    MQTT_RECONNECT_INTERVAL,
    MQTT_CLIENT_ID_PREFIX,
    MQTT_USERNAME,
    MQTT_PASSWORD,
    MQTT_TOPICS
)

logger = logging.getLogger(__name__)

class MqttClient:
    _QUALITY_OF_SERVICE = 1 # At least once
    
    def __init__(self):
        self.client_id = f"{MQTT_CLIENT_ID_PREFIX}{int(time.time())}"
        self.client = mqtt.Client(client_id=self.client_id, protocol=mqtt.MQTTv5, transport='websockets')
        self.client.username_pw_set(MQTT_USERNAME, MQTT_PASSWORD)
        self.client.on_connect = self._on_connect
        self.client.on_disconnect = self._on_disconnect
        self.client.on_message = self._on_message
        self.client.reconnect_delay_set(min_delay=1, max_delay=MQTT_RECONNECT_INTERVAL)
        self.message_callbacks = {}

        self.publish_queue = asyncio.Queue()
        self.publish_task = None
        self.last_publish_time = 0.0
        self.publish_interval = 1/50 # 50Hz
        self.loop = None # To store the NiceGUI event loop

    def set_event_loop(self, loop):
        self.loop = loop

    def _on_connect(self, client, userdata, flags, rc, properties=None):
        if rc == 0:
            logger.info("Connected to MQTT Broker!")
            # Re-subscribe to topics after successful reconnection
            for topic in self.message_callbacks.keys():
                # Only subscribe if there are active callbacks for the topic
                if self.message_callbacks[topic]:
                    self.client.subscribe(topic)
            if not self.publish_task or self.publish_task.done():
                if self.loop:
                    self.publish_task = self.loop.call_soon_threadsafe(asyncio.create_task, self._publisher_task())
                else:
                    logger.error("Event loop not set for MQTT client. Cannot start publisher task.")
        else:
            logger.error(f"Failed to connect, return code {rc}")

    def _on_disconnect(self, client, userdata, rc, properties=None):
        if rc != 0:
            logger.warning(f"Disconnected from MQTT Broker with code {rc}. Attempting to reconnect...")
        if self.publish_task:
            self.publish_task.cancel()
            self.publish_task = None

    def _on_message(self, client, userdata, msg):
        try:
            payload = json.loads(msg.payload.decode())
            topic = msg.topic
            if topic in self.message_callbacks:
                for callback in self.message_callbacks[topic]:
                    callback(topic, payload)
        except json.JSONDecodeError:
            logger.error(f"Failed to decode JSON from message on topic {msg.topic}: {msg.payload}")
        except Exception as e:
            logger.error(f"Error processing MQTT message on topic {msg.topic}: {e}")

    async def _publisher_task(self):
        while True:
            try:
                topic, payload = await self.publish_queue.get()
                current_time = time.time()
                time_to_wait = self.publish_interval - (current_time - self.last_publish_time)
                if time_to_wait > 0:
                    await asyncio.sleep(time_to_wait)

                self.client.publish(topic, json.dumps(payload), qos=self._QUALITY_OF_SERVICE)
                self.last_publish_time = time.time()
                self.publish_queue.task_done()
            except asyncio.CancelledError:
                logger.info("MQTT publisher task cancelled.")
                break
            except Exception as e:
                logger.error(f"Error in MQTT publisher task: {e}")

    def connect(self):
        try:
            broker_host = MQTT_BROKER_URL
            broker_port = MQTT_BROKER_PORT

            logger.info(f"Attempting to connect to MQTT Broker at {broker_host}:{broker_port} via websockets")
            self.client.connect(broker_host, broker_port, 60)
            self.client.loop_start() # Starts a new thread for network loop

        except Exception as e:
            logger.error(f"Could not connect to MQTT Broker: {e}")

    def disconnect(self):
        logger.info("Disconnecting from MQTT Broker.")
        if self.publish_task:
            self.publish_task.cancel()
            self.publish_task = None
        self.client.disconnect()
        self.client.loop_stop()

    def subscribe(self, topic: str, callback):
        if topic not in self.message_callbacks:
            self.message_callbacks[topic] = []
        self.message_callbacks[topic].append(callback)
        self.client.subscribe(topic, options=mqtt.SubscribeOptions(qos=self._QUALITY_OF_SERVICE))
        logger.info(f"Subscribed to topic: {topic}")

    def unsubscribe(self, topic: str, callback):
        if topic in self.message_callbacks:
            if callback in self.message_callbacks[topic]:
                self.message_callbacks[topic].remove(callback)
            if not self.message_callbacks[topic]: # If no more callbacks for this topic
                self.client.unsubscribe(topic)
                del self.message_callbacks[topic]
                logger.info(f"Unsubscribed from topic: {topic}")

    def publish(self, topic: str, payload: dict):
        # Put the message into the queue, the publisher task will handle rate limiting
        try:
            self.publish_queue.put_nowait((topic, payload))
        except asyncio.QueueFull:
            logger.warning("Publish queue is full, dropping message.")
