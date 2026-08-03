[CmdletBinding()]
param()

$ErrorActionPreference = 'Stop'
$catalinaHome = if ($env:CATALINA_HOME) { $env:CATALINA_HOME } else { [Environment]::GetEnvironmentVariable('CATALINA_HOME', 'User') }

if ([string]::IsNullOrWhiteSpace($catalinaHome) -or -not (Test-Path -LiteralPath $catalinaHome)) {
    throw 'CATALINA_HOME is not configured to an existing Tomcat directory.'
}

$env:CATALINA_HOME = $catalinaHome

$shutdownScript = Join-Path $catalinaHome 'bin\shutdown.bat'
if (-not (Test-Path -LiteralPath $shutdownScript)) {
    throw "Tomcat shutdown script was not found at $shutdownScript"
}

$env:JAVA_HOME = if ($env:JAVA_HOME) { $env:JAVA_HOME } else { [Environment]::GetEnvironmentVariable('JAVA_HOME', 'User') }
& $shutdownScript

for ($attempt = 1; $attempt -le 12; $attempt++) {
    $listener = Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue
    if (-not $listener) {
        Write-Host 'Tomcat is stopped (or was not running).'
        return
    }
    Start-Sleep -Seconds 1
}

throw 'Port 8080 is still listening after the Tomcat shutdown request. Do not deploy over an unknown process.'
