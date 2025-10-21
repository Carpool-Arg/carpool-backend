#!/usr/bin/env bash
# Linea de comando para detener el script en caso de que ocurra algun error
set -euo pipefail

# Uso:
# Para realizar el encriptado correspondiente se debe ejecutar la siguiente linea:
# ./encrypt.sh <archivo-con-password> <archivo-encriptar> [out-file]
# Ejemplo: ./encrypt.sh APP_CONFIG_ENCRYPTION_KEY application.properties

ITEM="$1"
INFILE="$2"
OUTFILE="${3:-${INFILE}.gpg}"

if [ ! -f "$INFILE" ]; then
  echo "Error: archivo a encriptar no encontradoo: $INFILE" >&2
  exit 2
fi

if [ -z "${BW_SESSION:-}" ]; then
  if ! command -v bw >/dev/null 2>&1; then
    echo "Error: 'bw' CLI no encontrada. Instalá Bitwarden CLI." >&2
    exit 3
  fi
  echo "BW_SESSION no detectado. Ejecutando: bw unlock --raw"
  export BW_SESSION="$(bw unlock --raw)"
fi

# Obtener la clave 
KEY="$(bw get password "$ITEM")" || { echo "No se pudo obtener la clave desde bw"; exit 4; }

# Algoritmo AES256 simétrico. 
printf '%s' "$KEY" | gpg --batch --yes --passphrase-fd 0 --pinentry-mode loopback \
  --symmetric --cipher-algo AES256 -o "$OUTFILE" "$INFILE"

# Limpieza
unset KEY
echo "Encrypted -> $OUTFILE"

