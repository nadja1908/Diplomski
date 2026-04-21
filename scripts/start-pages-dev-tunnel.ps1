# Quick public URL za lokalni gateway (port 9000) — za GitHub Pages + VITE_API_BASE_URL.
# Zahteva: Docker stack pokrenut (npr. docker compose up -d), gateway na localhost:9000.
# URL ispisuje cloudflared; kopiraj ga u GitHub repo variable VITE_API_BASE_URL i ponovo pokreni Pages deploy.

$ErrorActionPreference = 'Stop'
$cf = Join-Path $env:TEMP 'cloudflared.exe'
if (-not (Test-Path $cf)) {
  Write-Host 'Preuzimam cloudflared...' -ForegroundColor Cyan
  Invoke-WebRequest -Uri 'https://github.com/cloudflare/cloudflared/releases/latest/download/cloudflared-windows-amd64.exe' -OutFile $cf -UseBasicParsing
}
Write-Host 'Pokrećem tunnel na http://localhost:9000 — URL će biti ispod (trycloudflare.com).' -ForegroundColor Green
Write-Host 'Zaustavi: Ctrl+C u ovom prozoru.' -ForegroundColor Yellow
& $cf tunnel --url http://localhost:9000
