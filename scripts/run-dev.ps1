param(
    [switch]$SkipSqlCheck
)

$ErrorActionPreference = "Stop"
$projectRoot = Split-Path -Parent $PSScriptRoot
Set-Location $projectRoot

if (-not $env:SPRING_PROFILES_ACTIVE) {
    $env:SPRING_PROFILES_ACTIVE = "dev"
}

if (-not $env:DB_USERNAME) {
    $env:DB_USERNAME = "tam"
}

if (-not $env:JPA_SHOW_SQL) {
    $env:JPA_SHOW_SQL = "false"
}

if ([string]::IsNullOrWhiteSpace($env:DB_PASSWORD)) {
    Write-Error "Thieu DB_PASSWORD. Vi du: `$env:DB_PASSWORD='123456'; .\scripts\run-dev.ps1"
    exit 1
}

if (-not $SkipSqlCheck) {
    & (Join-Path $PSScriptRoot "verify-sqlserver.ps1")
}

Write-Host "Chay QLVT voi profile $env:SPRING_PROFILES_ACTIVE..."
& .\mvnw.cmd clean spring-boot:run
