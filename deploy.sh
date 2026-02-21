#!/bin/bash

set -e

DEPLOY_DIR="/root/DeployDonWeb/carpool-backend"
GPG_FILE="$DEPLOY_DIR/src/main/resources/application.properties.gpg"
APP_PROPS="$DEPLOY_DIR/src/main/resources/application.properties"
LOG_FILE="/root/deploy.log"

echo "========================================" | tee -a "$LOG_FILE"
echo "Iniciando deploy carpool-backend: $(date)" | tee -a "$LOG_FILE"
echo "========================================" | tee -a "$LOG_FILE"

cd "$DEPLOY_DIR"

echo "Rama actual:" | tee -a "$LOG_FILE"
git branch | tee -a "$LOG_FILE"

echo "Iniciando pull de la rama..." | tee -a "$LOG_FILE"
git pull | tee -a "$LOG_FILE"

echo "Desencriptando application.properties.gpg..." | tee -a "$LOG_FILE"
if [ -z "${GPG_PASSPHRASE:-}" ]; then
  echo "Error: GPG_PASSPHRASE no está definida." | tee -a "$LOG_FILE"
  exit 1
fi
gpg --batch --yes --passphrase-file /root/.gpg_passphrase --pinentry-mode loopback --decrypt -o "$APP_PROPS" "$GPG_FILE"
echo "application.properties generado." | tee -a "$LOG_FILE"

echo "Bajando contenedores..." | tee -a "$LOG_FILE"
docker-compose down | tee -a "$LOG_FILE"

echo "Compilando proyecto..." | tee -a "$LOG_FILE"
mvn clean package -DskipTests | tee -a "$LOG_FILE"

echo "Buildando imagen Docker..." | tee -a "$LOG_FILE"
docker-compose build | tee -a "$LOG_FILE"

echo "Levantando contenedores en background..." | tee -a "$LOG_FILE"
nohup docker-compose up > /root/carpool-up.log 2>&1 &

echo "Deploy finalizado: $(date)" | tee -a "$LOG_FILE"
