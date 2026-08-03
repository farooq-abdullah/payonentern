[CmdletBinding()]
param()

$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
$env:JAVA_HOME = if ($env:JAVA_HOME) { $env:JAVA_HOME } else { [Environment]::GetEnvironmentVariable('JAVA_HOME', 'User') }
$mavenHome = if ($env:MAVEN_HOME) { $env:MAVEN_HOME } else { [Environment]::GetEnvironmentVariable('MAVEN_HOME', 'User') }
$mavenCommand = Get-Command mvn -ErrorAction SilentlyContinue

if ($mavenCommand) {
    $maven = $mavenCommand.Source
}
elseif ($mavenHome -and (Test-Path -LiteralPath (Join-Path $mavenHome 'bin\mvn.cmd'))) {
    $maven = Join-Path $mavenHome 'bin\mvn.cmd'
}
else {
    throw 'Maven was not found. Configure MAVEN_HOME or add Maven\bin to Path.'
}

Push-Location $projectRoot
try {
    & $maven clean package
    if ($LASTEXITCODE -ne 0) {
        throw "Maven build failed with exit code $LASTEXITCODE."
    }
}
finally {
    Pop-Location
}
