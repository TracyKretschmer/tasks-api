#!/usr/bin/env bash
echo $API_HOST_PATH
echo $API_HOST_PORT

java -Dapi.hostPath=$API_HOST_PATH -Dapi.hostPort=$API_HOST_PORT -jar tasks-client.jar
