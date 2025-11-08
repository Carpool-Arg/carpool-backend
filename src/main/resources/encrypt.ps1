<#
.SYNOPSIS
Script para encriptar archivos usando GPG y Bitwarden.

.USAGE
.\encrypt.ps1 <Item-Bitwarden> <Archivo-a-encriptar> [Out-File]
Ejemplo:
.\encrypt.ps1 KEY application.properties
#>

param(
    [Parameter(Mandatory=$true)]
    [string]$Item,

    [Parameter(Mandatory=$true)]
    [string]$InFile,

    [Parameter(Mandatory=$false)]
    [string]$OutFile = "$InFile.gpg"
)

# Detener el script en caso de error
$ErrorActionPreference = "Stop"

# Verificar que el archivo exista
if (-not (Test-Path $InFile)) {
    Write-Error "Archivo a encriptar no encontrado: $InFile"
    exit 2
}

# Verificar BW_SESSION
if (-not $env:BW_SESSION) {
    if (-not (Get-Command bw -ErrorAction SilentlyContinue)) {
        Write-Error "'bw' CLI no encontrada. Instalá Bitwarden CLI."
        exit 3
    }
    Write-Output "BW_SESSION no detectado. Ejecutando: bw unlock --raw"
    $env:BW_SESSION = bw unlock --raw
}

# Obtener la clave
try {
    $Key = bw get password $Item
} catch {
    Write-Error "No se pudo obtener la clave desde Bitwarden"
    exit 4
}

# Encriptar con AES256
$Key | gpg --batch --yes --passphrase-fd 0 --pinentry-mode loopback `
    --symmetric --cipher-algo AES256 -o $OutFile $InFile

# Limpieza
Remove-Variable Key
Write-Output "Encrypted -> $OutFile"
