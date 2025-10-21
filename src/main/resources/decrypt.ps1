<#
.SYNOPSIS
Script para desencriptar archivos usando GPG y Bitwarden.

.USAGE
.\decrypt.ps1 <Item-Bitwarden> <Archivo-encriptado> [Out-File]
Ejemplo:
.\decrypt.ps1 APP_CONFIG_ENCRYPTION_KEY application.properties.gpg
#>

param(
    [Parameter(Mandatory=$true)]
    [string]$Item,

    [Parameter(Mandatory=$true)]
    [string]$InFile,

    [Parameter(Mandatory=$false)]
    [string]$OutFile
)

# Detener el script en caso de error
$ErrorActionPreference = "Stop"

# Si no se pasa OutFile, quitar .gpg del nombre del input
if (-not $OutFile) {
    if ($InFile -match "\.gpg$") {
        $OutFile = $InFile -replace "\.gpg$", ""
    } else {
        $OutFile = "$InFile.decrypted"
    }
}

# Verificar que el archivo exista
if (-not (Test-Path $InFile)) {
    Write-Error "Input file no encontrado: $InFile"
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

# Desencriptar con AES256
$Key | gpg --batch --yes --passphrase-fd 0 --pinentry-mode loopback --decrypt -o $OutFile $InFile

# Limpieza
Remove-Variable Key
Write-Output "Decrypted -> $OutFile"
