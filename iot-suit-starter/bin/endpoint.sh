#!/bin/bash

CURRENT_DIR=`dirname $0`
API_HOME=`cd "$CURRENT_DIR/.." >/dev/null; pwd`
run_jar="iot-suit-starter-1.0.0-SNAPSHOT.jar"
cd $API_HOME
Jar="$API_HOME/lib/${run_jar}"
RETVAL="0"
LOG="api_stdout.log"

nohup /usr/bin/redis-server /etc/redis.conf &
cd /usr/bin
chmod 777 /usr/bin/nginx
nginx
cd $API_HOME

java \
-Dconnector.ak=$1 \
-Dconnector.sk=$2 \
-Dproject.code=$3 \
-jar $Jar >> $API_HOME/logs/$LOG 2>&1


