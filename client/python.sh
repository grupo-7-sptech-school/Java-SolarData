#!/bin/bash

git clone https://github.com/grupo-7-sptech-school/Python-SolarData.git
cd Python-SolarData

python3 -m venv venv
source venv/bin/activate
pip3 install -r requirements.txt
python3 aplicacaoDeCaptura.py