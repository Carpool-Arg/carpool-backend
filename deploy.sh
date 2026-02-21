#!/bin/bash

set -e

DEPLOY_DIR="/root/DeployDonWeb/carpool-backend"
LOG_FILE="/root/deploy.log"

echo "========================================" | tee -a "$LOG_FILE"
echo "Iniciando deploy carpool-backend: $(date)" | tee -a "$LOG_FILE"
echo "========================================" | tee -a "$LOG_FILE"

cd "$DEPLOY_DIR"

echo "Rama actual:" | tee -a "$LOG_FILE"
git branch | tee -a "$LOG_FILE"

echo "Iniciando pull de la rama..." | tee -a "$LOG_FILE"
git pull | tee -a "$LOG_FILE"

echo "Bajando contenedores..." | tee -a "$LOG_FILE"
docker-compose down | tee -a "$LOG_FILE"

echo "Compilando proyecto..." | tee -a "$LOG_FILE"
mvn clean package -DskipTests | tee -a "$LOG_FILE"

echo "Buildando imagen Docker..." | tee -a "$LOG_FILE"
docker-compose build | tee -a "$LOG_FILE"

echo "Levantando contenedores en background..." | tee -a "$LOG_FILE"
nohup docker-compose up > /root/carpool-up.log 2>&1 &

echo "Deploy finalizado: $(date)" | tee -a "$LOG_FILE"
