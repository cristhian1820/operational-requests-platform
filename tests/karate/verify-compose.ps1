$ErrorActionPreference = 'Stop'
$root = Resolve-Path (Join-Path $PSScriptRoot '../..')
Push-Location $root
try {
  docker compose --env-file .env.example config --quiet
  if ($LASTEXITCODE -ne 0) { throw 'docker compose config failed' }
  docker compose --env-file .env.example up -d
  if ($LASTEXITCODE -ne 0) { throw 'docker compose up failed' }
  $required = @('sqlserver-operational','sqlserver-analytics','kafka','keycloak','requests-service','indicators-service','requests-mfe','shell')
  $rows = docker compose --env-file .env.example ps --format json | ConvertFrom-Json
  foreach ($service in $required) {
    $row = $rows | Where-Object { $_.Service -eq $service }
    if (-not $row -or $row.Health -ne 'healthy') { throw "Service $service is not healthy" }
  }
  $checks = @('http://localhost:18081/actuator/health','http://localhost:8082/actuator/health','http://localhost:4200/','http://localhost:4201/remoteEntry.js')
  foreach ($uri in $checks) {
    $response = Invoke-WebRequest -UseBasicParsing -Uri $uri
    if ($response.StatusCode -ne 200) { throw "Health check failed: $uri" }
  }
  Write-Output 'Compose acceptance checks passed.'
} finally { Pop-Location }
