#!/usr/bin/env sh

MQTT_USERNAME=$MQTT_USERNAME
MQTT_PASSWORD=$MQTT_PASSWORD
docker_mode=false

# Parse command-line arguments
for arg in "$@"; do
    case $arg in
        --docker)
            docker_mode=true
            shift # Remove --docker from processing
            ;;
        *)
            # Unknown option
            ;;
    esac
done

if [ "$docker_mode" = true ]; then
    echo "Creating default user with Docker..."
    docker run --rm -u $(id -u):$(id -g) -v"$PWD":"$PWD" -w"$PWD" eclipse-mosquitto mosquitto_passwd -c -b passwordfile $MQTT_USERNAME $MQTT_PASSWORD
    if [ $? -eq 0 ]; then
        echo "Default user 'user' created successfully in passwordfile using Docker."
    else
        echo "Failed to create user with Docker."
        exit 1
    fi
else
    echo "Creating default user directly (requires mosquitto_passwd installed locally)..."
    echo $PWD
    echo "Username: $MQTT_USERNAME"
    echo "Password: $MQTT_PASSWORD"
    mosquitto_passwd -c -b /mosquitto/config/passwordfile $MQTT_USERNAME $MQTT_PASSWORD
fi
