# Recovers "500 Internal Server Error" / missing Server in `docker version`
# when the Docker Desktop Linux engine pipe fails.
#
# Run in PowerShell:
#   Right-click -> Run as administrator (recommended for wsl --shutdown)
#   cd ...\NAIS-projekat\scripts
#   Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass -Force
#   .\windows-restart-docker-engine.ps1

$ErrorActionPreference = "Continue"

Write-Host "=== NAIS: Docker Desktop engine recovery ===" -ForegroundColor Cyan

# Prefer the context Docker Desktop registers
docker context use desktop-linux 2>$null | Out-Null

Write-Host "`n[1/4] Stopping Docker Desktop UI..."
Get-Process "Docker Desktop" -ErrorAction SilentlyContinue | Stop-Process -Force -ErrorAction SilentlyContinue
Start-Sleep -Seconds 4

Write-Host "`n[2/4] Shutting down WSL (fixes many dockerDesktopLinuxEngine 500 errors)..."
try {
    & wsl.exe --shutdown
    if ($LASTEXITCODE -ne 0) {
        Write-Host "  wsl --shutdown returned $LASTEXITCODE (run script as Administrator if this fails)." -ForegroundColor Yellow
    } else {
        Write-Host "  OK."
    }
} catch {
    Write-Host "  Could not run wsl: $_" -ForegroundColor Yellow
}
Start-Sleep -Seconds 3

Write-Host "`n[3/4] Restarting Docker service (if present)..."
$svc = Get-Service -Name "com.docker.service" -ErrorAction SilentlyContinue
if ($svc) {
    try {
        Restart-Service -Name "com.docker.service" -Force -ErrorAction Stop
        Write-Host "  com.docker.service restarted."
    } catch {
        Write-Host "  Could not restart service: $_" -ForegroundColor Yellow
    }
} else {
    Write-Host "  com.docker.service not found (normal on some installs)."
}

Write-Host "`n[4/4] Starting Docker Desktop..."
$dockerExe = @(
    "${env:ProgramFiles}\Docker\Docker\Docker Desktop.exe",
    "${env:ProgramFiles(x86)}\Docker\Docker\Docker Desktop.exe"
) | Where-Object { Test-Path $_ } | Select-Object -First 1

if (-not $dockerExe) {
    Write-Host "ERROR: Docker Desktop.exe not found. Install from https://www.docker.com/products/docker-desktop/" -ForegroundColor Red
    exit 1
}

Start-Process -FilePath $dockerExe
Write-Host "  Started: $dockerExe"

Write-Host "`nWaiting for engine (up to 3 minutes)..."
$deadline = (Get-Date).AddMinutes(3)
$ok = $false
while ((Get-Date) -lt $deadline) {
    Start-Sleep -Seconds 5
    $ver = & docker.exe version 2>&1 | Out-String
    if ($ver -match "(?s)Server:.*Version:" -and $ver -notmatch "500 Internal Server Error") {
        $ok = $true
        break
    }
    Write-Host "  ... still starting"
}

Write-Host ""
& docker.exe version

if (-not $ok) {
    Write-Host "`nStill broken. Manual steps:" -ForegroundColor Yellow
    Write-Host "  - Docker Desktop -> Troubleshoot -> Restart / Reset Kubernetes (or Reset to factory defaults)"
    Write-Host "  - Update Docker Desktop to the latest version"
    Write-Host "  - Settings -> General: confirm 'Use the WSL 2 based engine' matches your setup"
    exit 1
}

Write-Host "`nDocker engine is responding. You can run: cd .. ; docker compose up -d --build" -ForegroundColor Green
exit 0
