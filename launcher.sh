#!/usr/bin/env bash

# --- Configuration ---
JOURNAL_FILE="journal_docker_compose.log"

# --- Environment Setup ---
export IP_ADDRESS_OF_HOST_PC_IN_LOCAL_NETWORK=$(hostname -I | awk '{print $1}')
export MQTT_USERNAME=user
export MQTT_PASSWORD=user

export UART_MQTT_GATEWAY_SERIAL_ALLOWED_PORT_NAME_PREFIXES="/dev/ttyUSB,regex:/dev/ttyACM[3-9]"


# --- Functions ---
print_manual() {
    echo "Usage: ./launcher.sh [COMMAND]"
    echo ""
    echo "Commands:"
    echo "  up (eclipse|nanomq) [--force]   Start the services"
    echo "  down                            Stop the services"
    echo "  --help, -h                      Show this help message"
    echo ""
    echo "Examples:"
    echo "  ./launcher.sh up eclipse          # Start the eclipse services"
    echo "  ./launcher.sh up nanomq --force   # Start the nanomq services with a forced rebuild"
    echo "  ./launcher.sh down                # Stop the running services"
}

# --- Argument Parsing ---
COMMAND=$1

if [[ "$COMMAND" == "--help" || "$COMMAND" == "-h" ]]; then
    print_manual
    exit 0
fi

if [[ "$COMMAND" != "up" && "$COMMAND" != "down" ]]; then
    print_manual
    exit 1
fi

if [[ "$COMMAND" == "up" ]]; then
    BROKER=$2
    FORCE_FLAG=$3
    if [[ "$BROKER" != "eclipse" && "$BROKER" != "nanomq" ]]; then
        print_manual
        exit 1
    fi
fi

# --- Main Logic ---
if [[ "$COMMAND" == "up" ]]; then
    DOCKER_COMPOSE_FILE="docker-compose-${BROKER}.yaml"
    echo "$DOCKER_COMPOSE_FILE" > $JOURNAL_FILE

    if [[ "$FORCE_FLAG" == "--force" ]]; then
        echo "Forcing rebuild..."
        UID=$(id -u) GID=$(id -g) docker compose -f $DOCKER_COMPOSE_FILE up -d --build --no-deps --force-recreate
    else
        UID=$(id -u) GID=$(id -g) docker compose -f $DOCKER_COMPOSE_FILE up -d
    fi
elif [[ "$COMMAND" == "down" ]]; then
    if [ ! -f "$JOURNAL_FILE" ]; then
        echo "Journal file not found. Cannot determine which docker-compose file to use."
        exit 1
    fi
    DOCKER_COMPOSE_FILE=$(cat $JOURNAL_FILE)
    UID=$(id -u) GID=$(id -g) docker compose -f $DOCKER_COMPOSE_FILE down
    rm $JOURNAL_FILE
fi
