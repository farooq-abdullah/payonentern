[CmdletBinding()]
param()

$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
. (Join-Path $PSScriptRoot 'load-local-env.ps1') -RequireDatabase

$running = docker inspect -f '{{.State.Running}}' servlet-learning-oracle 2>$null
if ($LASTEXITCODE -ne 0 -or $running -ne 'true') {
    throw 'The servlet-learning-oracle container is not running. Run scripts/start-oracle.ps1 first.'
}

$sqlFile = Join-Path $projectRoot 'database\01-create-users.sql'
$connection = "$env:DB_USER/$env:DB_PASSWORD@//localhost:1521/FREEPDB1"

Get-Content -LiteralPath $sqlFile -Raw |
    docker exec -i servlet-learning-oracle sqlplus -s $connection

if ($LASTEXITCODE -ne 0) {
    throw "Schema creation failed with exit code $LASTEXITCODE."
}

Write-Host 'APP_USERS table created and checked.'
