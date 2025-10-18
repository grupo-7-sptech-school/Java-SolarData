#!/bin/bash

git clone https://github.com/grupo-7-sptech-school/Java-SolarData.git
cd Java-SolarData

while true
do
    java -jar out/artifacts/looca_api_jar/looca-api.jar
    sleep 30
done