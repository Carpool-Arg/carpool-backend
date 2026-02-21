#!/bin/bash

set -e

DEPLOY_DIR="/root/DeployDonWeb/carpool-backend"
LOG_FILE="/root/deploy-reset-db.log"

echo "========================================" | tee -a "$LOG_FILE"
echo "Iniciando deploy carpool-backend con RESET de DB: $(date)" | tee -a "$LOG_FILE"
echo "========================================" | tee -a "$LOG_FILE"

cd "$DEPLOY_DIR"

echo "Rama actual:" | tee -a "$LOG_FILE"
git branch | tee -a "$LOG_FILE"

echo "Haciendo pull de la rama..." | tee -a "$LOG_FILE"
git pull | tee -a "$LOG_FILE"

echo "Bajando contenedores..." | tee -a "$LOG_FILE"
docker-compose down | tee -a "$LOG_FILE"

echo "Borrando volumen de la DB..." | tee -a "$LOG_FILE"
docker volume rm carpool-backend_carpool-db-data | tee -a "$LOG_FILE"
echo "Volumen borrado." | tee -a "$LOG_FILE"

# ── Compilar y levantar con Liquibase en FALSE ──────────
echo "Compilando con liquibase=false para crear tablas base..." | tee -a "$LOG_FILE"
sed -i 's/spring\.liquibase\.enabled=true/spring.liquibase.enabled=false/' "$APP_PROPS"

mvn clean package -DskipTests | tee -a "$LOG_FILE"
docker-compose build | tee -a "$LOG_FILE"

echo "Levantando para que Hibernate cree las tablas..." | tee -a "$LOG_FILE"
docker-compose up -d | tee -a "$LOG_FILE"

echo "Esperando 20s para que Hibernate termine..." | tee -a "$LOG_FILE"
sleep 20

echo "Bajando contenedores nuevamente..." | tee -a "$LOG_FILE"
docker-compose down | tee -a "$LOG_FILE"

# ── Recompilar con Liquibase en TRUE ────────────────────
echo "Activando liquibase=true para correr migraciones..." | tee -a "$LOG_FILE"
sed -i 's/spring\.liquibase\.enabled=false/spring.liquibase.enabled=true/' "$APP_PROPS"

cd "$DEPLOY_DIR"
mvn clean package -DskipTests | tee -a "$LOG_FILE"
docker-compose build | tee -a "$LOG_FILE"

echo "Levantando contenedores finales en background..." | tee -a "$LOG_FILE"
nohup docker-compose up > /root/carpool-up-reset-db.log 2>&1 &

echo "Deploy con reset de DB finalizado: $(date)" | tee -a "$LOG_FILE"
