[CmdletBinding()]
param()

$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
. (Join-Path $PSScriptRoot 'load-local-env.ps1') -RequireDatabase

Push-Location $projectRoot
try {
    mvn -q compile exec:java '-Dexec.mainClass=com.learning.dev.DatabaseConnectionCheck'
    if ($LASTEXITCODE -ne 0) {
        throw "Database connection check failed with exit code $LASTEXITCODE."
    }
}
finally {
    Pop-Location
}
