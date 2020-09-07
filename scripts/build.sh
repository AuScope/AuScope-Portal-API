#!/usr/bin/env bash

# Build the war using a maven container, but using the local maven repo.
docker run -it --rm -v "$PWD":/usr/src/mymaven -v "$HOME/.m2":/root/.m2 -w /usr/src/mymaven maven:3-jdk-14 mvn clean install

# Build the docker stack
docker-compose build
