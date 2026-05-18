$root = Split-Path -Parent $PSScriptRoot
Push-Location (Join-Path $root "frontend")
npm run dev -- --host 0.0.0.0
Pop-Location
