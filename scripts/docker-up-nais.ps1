# NAIS: provera Dockera, pull, build, podizanje stack-a (PowerShell)
# Pokretanje:  powershell -ExecutionPolicy Bypass -File .\scripts\docker-up-nais.ps1
# Ili iz NAIS-projekat:  .\scripts\docker-up-nais.ps1
$ErrorActionPreference = 'Stop'
$proj = Split-Path $PSScriptRoot -Parent
Set-Location $proj

Write-Host "`n=== 1) Folder ===" -ForegroundColor Cyan
Write-Host (Get-Location).Path

Write-Host "`n=== 2) docker-compose.yml ===" -ForegroundColor Cyan
if (-not (Test-Path '.\docker-compose.yml')) {
    Write-Host "GRESKA: Nisi u NAIS-projekat (nema docker-compose.yml)." -ForegroundColor Red
    exit 1
}

Write-Host "`n=== 3) Docker (ako se zaglavi >60s, ukljuci Docker Desktop i ponovi) ===" -ForegroundColor Cyan
$job = Start-Job { docker version 2>&1 }
if (-not (Wait-Job $job -Timeout 60)) {
    Write-Host "TIMEOUT: docker version se nije odazvao za 60s. Pokreni Docker Desktop i ponovi skriptu." -ForegroundColor Red
    Stop-Job $job -ErrorAction SilentlyContinue
    Remove-Job $job -Force -ErrorAction SilentlyContinue
    exit 1
}
Receive-Job $job
Remove-Job $job -Force

Write-Host "`n=== 4) docker compose pull (slike) ===" -ForegroundColor Cyan
docker compose pull

Write-Host "`n=== 5) docker compose build ===" -ForegroundColor Cyan
docker compose build

Write-Host "`n=== 6) docker compose up -d ===" -ForegroundColor Cyan
docker compose up -d

Write-Host "`n=== 7) Status kontejnera ===" -ForegroundColor Cyan
docker compose ps -a

Write-Host "`n=== 8) Poslednje log linije (gateway, relational, vector) ===" -ForegroundColor Cyan
docker compose logs gateway-api relational-database-service vector-database-service --tail 30 2>&1

Write-Host "`nGotovo. Gateway obicno: http://localhost:9000" -ForegroundColor Green
