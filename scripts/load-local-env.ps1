[CmdletBinding()]
param(
    [switch]$RequireDatabase
)

$projectRoot = Split-Path -Parent $PSScriptRoot
$envFile = Join-Path $projectRoot '.env.local'

if (-not (Test-Path -LiteralPath $envFile)) {
    throw "Missing $envFile. Copy .env.example to .env.local and fill in its values."
}

foreach ($line in Get-Content -LiteralPath $envFile) {
    $trimmedLine = $line.Trim()
    if (-not $trimmedLine -or $trimmedLine.StartsWith('#')) {
        continue
    }

    $parts = $trimmedLine.Split('=', 2)
    if ($parts.Count -ne 2 -or [string]::IsNullOrWhiteSpace($parts[0])) {
        throw "Invalid .env.local line: $line"
    }

    $name = $parts[0].Trim()
    $value = $parts[1].Trim()
    Set-Item -Path "Env:$name" -Value $value
}

function Assert-ConfiguredValue([string]$Name) {
    $value = [Environment]::GetEnvironmentVariable($Name, 'Process')
    if ([string]::IsNullOrWhiteSpace($value) -or $value -eq 'CHANGE_ME') {
        throw "Set $Name in $envFile before running this command."
    }
}

if ($RequireDatabase) {
    Assert-ConfiguredValue 'DB_URL'
    Assert-ConfiguredValue 'DB_USER'
    Assert-ConfiguredValue 'DB_PASSWORD'
}
