[CmdletBinding()]
param()

$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
. (Join-Path $PSScriptRoot 'load-local-env.ps1') -RequireOracleAdmin

$docker = Get-Command docker -ErrorAction SilentlyContinue
if (-not $docker) {
    throw 'Docker Desktop is not installed or docker.exe is not on Path.'
}

try {
    docker info 2>$null | Out-Null
    $dockerReady = $LASTEXITCODE -eq 0
}
catch {
    $dockerReady = $false
}

if (-not $dockerReady) {
    $dockerDesktop = 'C:\Program Files\Docker\Docker\Docker Desktop.exe'
    if (-not (Test-Path -LiteralPath $dockerDesktop)) {
        throw 'Docker is installed, but its engine is not running. Start Docker Desktop and retry.'
    }

    Write-Host 'Starting Docker Desktop...'
    Start-Process -FilePath $dockerDesktop -WindowStyle Hidden | Out-Null

    for ($attempt = 1; $attempt -le 30; $attempt++) {
        Start-Sleep -Seconds 2
        try {
            docker info 2>$null | Out-Null
            $dockerReady = $LASTEXITCODE -eq 0
        }
        catch {
            $dockerReady = $false
        }
        if ($dockerReady) {
            break
        }
    }

    if (-not $dockerReady) {
        throw 'Docker Desktop did not become ready within 60 seconds. Open it and inspect its status.'
    }
}

Push-Location $projectRoot
try {
    docker compose --env-file .env.local up -d oracle
    if ($LASTEXITCODE -ne 0) {
        throw "Oracle container startup failed with exit code $LASTEXITCODE."
    }
}
finally {
    Pop-Location
}

Write-Host 'Oracle startup requested. First initialization can take several minutes.'
Write-Host 'Watch it with: docker logs -f servlet-learning-oracle'
Write-Host 'Wait until the logs say DATABASE IS READY TO USE before creating the table.'
