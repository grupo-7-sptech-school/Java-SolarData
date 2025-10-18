#!/bin/bash

sudo apt upgrade
sudo apt update

java --version

if [ $? != 0 ];
        then
          sudo apt install openjdk-21-jre-headless
fi

python3 --version

if [ $? != 0 ];
        then
          sudo apt install python3
fi
venv --version
if [ $? != 0 ];
        then
          sudo apt install python3-venv
fi

chmod +x java.sh
chmod +x python.sh

./java.sh
./python.sh