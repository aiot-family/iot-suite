#!/bin/bash

cd ../

# 打包
mvn clean package -Dmaven.test.skip=true

# 构建镜像
docker build -t tuya-spring-boot-starter-sample:1.0.0-SNAPSHOT .

# 运行镜像 (需要填入对应的AK/SK)
docker run --name iot -d -p 8080:8080 -e AK="m7rpd9wqaingmoounmmn" -e SK="cc4a0ada485b42fd926101a923b84ba3" iot:1.0