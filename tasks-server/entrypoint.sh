#!/usr/bin/env bash
echo $HTTP_SERVER_PORT
echo $MONGO_SERVER_PORT
echo $MONGO_SERVER_HOST

java -Dhttp.server.port=$HTTP_SERVER_PORT -Dmongo.server.port=$MONGO_SERVER_PORT -Dmongo.server.host=$MONGO_SERVER_HOST -jar tasks-server.jar
