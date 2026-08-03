[CmdletBinding()]
param()

$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
$catalinaHome = if ($env:CATALINA_HOME) { $env:CATALINA_HOME } else { [Environment]::GetEnvironmentVariable('CATALINA_HOME', 'User') }

if (Test-Path -LiteralPath (Join-Path $projectRoot '.env.local')) {
    . (Join-Path $PSScriptRoot 'load-local-env.ps1')
}

if ([string]::IsNullOrWhiteSpace($catalinaHome) -or -not (Test-Path -LiteralPath $catalinaHome)) {
    throw 'CATALINA_HOME is not configured to an existing Tomcat directory.'
}

$env:CATALINA_HOME = $catalinaHome

& (Join-Path $PSScriptRoot 'build.ps1')
& (Join-Path $PSScriptRoot 'stop-tomcat.ps1')

$war = Join-Path $projectRoot 'target\servlet-learning.war'
$webapps = Join-Path $catalinaHome 'webapps'
$deployedWar = Join-Path $webapps 'servlet-learning.war'
$expandedApp = Join-Path $webapps 'servlet-learning'

if (-not (Test-Path -LiteralPath $war)) {
    throw "Build output was not found: $war"
}

# These are the only deployment targets this script removes.
if (Test-Path -LiteralPath $deployedWar) {
    Remove-Item -LiteralPath $deployedWar -Force
}
if (Test-Path -LiteralPath $expandedApp) {
    Remove-Item -LiteralPath $expandedApp -Recurse -Force
}

Copy-Item -LiteralPath $war -Destination $deployedWar -Force

$env:JAVA_HOME = if ($env:JAVA_HOME) { $env:JAVA_HOME } else { [Environment]::GetEnvironmentVariable('JAVA_HOME', 'User') }
$startupScript = Join-Path $catalinaHome 'bin\startup.bat'
Start-Process -FilePath $startupScript -WorkingDirectory (Join-Path $catalinaHome 'bin') -WindowStyle Hidden | Out-Null

Write-Host 'Tomcat start requested. Application URLs:'
Write-Host 'http://localhost:8080/servlet-learning/'
Write-Host 'http://localhost:8080/servlet-learning/register'
Write-Host 'http://localhost:8080/servlet-learning/login'
