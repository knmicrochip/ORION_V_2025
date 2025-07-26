#!/usr/bin/env bash

# detect IP address of this machine in local network
export IP_ADDRESS_OF_HOST_PC_IN_LOCAL_NETWORK=$(hostname -I | awk '{print $1}')

# MQTT details
export MQTT_USERNAME=user
export MQTT_PASSWORD=user

# The application must be launched with user ID and group ID to work 
# and mount configuration files correctly

UID=$(id -u) GID=$(id -g) docker compose up -d