#!/usr/bin/env bash

pushd config
    source create_default_user.sh --docker
popd

docker run -it --rm --name mosquitto-broker \
  -u $(id -u):$(id -g) \
  -p 1883:1883 \
  -p 9001:9001 \
  -v ./config:/mosquitto/config \
  eclipse-mosquitto