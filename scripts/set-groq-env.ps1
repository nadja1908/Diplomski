# Upisuje GROQ_API_KEY u NAIS-projekat/.env (fajl je u .gitignore).
# Pokretanje iz foldera NAIS-projekat:
#   .\scripts\set-groq-env.ps1
# ili (ključ vidi istorija PowerShell-a — pažljivo):
#   .\scripts\set-groq-env.ps1 -Key "gsk_..."

param(
    [Parameter(Mandatory = $false)]
    [string] $Key
)

$ErrorActionPreference = "Stop"
$root = Resolve-Path (Join-Path $PSScriptRoot "..")
$envFile = Join-Path $root ".env"

if (-not (Test-Path $envFile)) {
    Write-Error "Nema $envFile. Kreiraj .env u korenu projekta."
    exit 1
}

if (-not $Key) {
    $sec = Read-Host "Zalepi Groq API key (gsk_...)" -AsSecureString
    $ptr = [System.Runtime.InteropServices.Marshal]::SecureStringToBSTR($sec)
    try {
        $Key = [System.Runtime.InteropServices.Marshal]::PtrToStringBSTR($ptr)
    } finally {
        [System.Runtime.InteropServices.Marshal]::ZeroFreeBSTR($ptr)
    }
}

$Key = $Key.Trim()
if ($Key.Length -lt 10) {
    Write-Error "Ključ izgleda predugačko kratak."
    exit 1
}

$lines = Get-Content -Path $envFile
$done = $false
$newLines = foreach ($line in $lines) {
    if ($line -match '^\s*GROQ_API_KEY\s*=') {
        $done = $true
        "GROQ_API_KEY=$Key"
    } else {
        $line
    }
}
if (-not $done) {
    $newLines = @($newLines) + "GROQ_API_KEY=$Key"
}

Set-Content -Path $envFile -Value $newLines -Encoding utf8
Write-Host "GROQ_API_KEY je upisan u .env" -ForegroundColor Green
Write-Host "Sledeće: docker compose up -d --build relational-database-service" -ForegroundColor Cyan
