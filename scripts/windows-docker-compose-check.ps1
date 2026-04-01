# Pokreni:  cd c:\Users\djord\Downloads\NAIS\NAIS-projekat
#           powershell -ExecutionPolicy Bypass -File .\scripts\windows-docker-compose-check.ps1
$ErrorActionPreference = 'Continue'
$proj = Split-Path $PSScriptRoot -Parent
Set-Location $proj

Write-Host "=== Folder: $(Get-Location) ===" -ForegroundColor Cyan
if (-not (Test-Path '.\docker-compose.yml')) {
    Write-Host "GRESKA: docker-compose.yml nije u ovom folderu." -ForegroundColor Red
    exit 1
}
Get-Item '.\docker-compose.yml' | Format-List FullName, Length

Write-Host "`n=== docker version ===" -ForegroundColor Cyan
docker version

Write-Host "`n=== docker compose version ===" -ForegroundColor Cyan
docker compose version

Write-Host "`n=== Servisi (compose config --services) ===" -ForegroundColor Cyan
docker compose config --services

Write-Host "`n=== Probni build: eureka-server ===" -ForegroundColor Cyan
docker compose build eureka-server

Write-Host "`nGotovo." -ForegroundColor Green
