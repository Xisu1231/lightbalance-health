$root = Split-Path -Parent $PSScriptRoot

Push-Location (Join-Path $root "frontend")
npm run build
Pop-Location

$staticDir = Join-Path $root "backend\src\main\resources\static"
New-Item -ItemType Directory -Force $staticDir | Out-Null
Remove-Item (Join-Path $staticDir "*") -Recurse -Force -ErrorAction SilentlyContinue
Copy-Item (Join-Path $root "frontend\dist\*") $staticDir -Recurse -Force

Write-Host "Frontend build synced into Spring Boot static resources."
