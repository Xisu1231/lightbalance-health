$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $PSScriptRoot
$toolsDir = Join-Path $root "tools"
$mavenRepo = Join-Path $root ".m2"
$jdkZip = Join-Path $toolsDir "jdk17.zip"
$mavenZip = Join-Path $toolsDir "maven.zip"
$jdkUrl = "https://aka.ms/download-jdk/microsoft-jdk-17.0.16-windows-x64.zip"
$mavenUrl = "https://archive.apache.org/dist/maven/maven-3/3.9.11/binaries/apache-maven-3.9.11-bin.zip"

New-Item -ItemType Directory -Force $toolsDir | Out-Null
New-Item -ItemType Directory -Force $mavenRepo | Out-Null

if (-not (Test-Path $jdkZip)) {
  curl.exe -L $jdkUrl --output $jdkZip
}
elseif ((Get-Item $jdkZip).Length -lt 150MB) {
  Remove-Item $jdkZip -Force
  curl.exe -L $jdkUrl --output $jdkZip
}

if (-not (Test-Path $mavenZip)) {
  curl.exe -L $mavenUrl --output $mavenZip
}
elseif ((Get-Item $mavenZip).Length -lt 8MB) {
  Remove-Item $mavenZip -Force
  curl.exe -L $mavenUrl --output $mavenZip
}

if (-not (Test-Path (Join-Path $toolsDir "jdk-17"))) {
  Expand-Archive -Path $jdkZip -DestinationPath $toolsDir -Force
  $extracted = Get-ChildItem $toolsDir -Directory | Where-Object { $_.Name -like "jdk-17*" } | Select-Object -First 1
  if ($extracted -and $extracted.Name -ne "jdk-17") {
    Rename-Item $extracted.FullName (Join-Path $toolsDir "jdk-17")
  }
}

if (-not (Test-Path (Join-Path $toolsDir "apache-maven-3.9.11"))) {
  Expand-Archive -Path $mavenZip -DestinationPath $toolsDir -Force
}

$env:JAVA_HOME = Join-Path $toolsDir "jdk-17"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"

Push-Location (Join-Path $root "frontend")
npm install
Pop-Location

$mavenHome = Join-Path $toolsDir "apache-maven-3.9.11"
Push-Location (Join-Path $root "backend")
& "$mavenHome\bin\mvn.cmd" "-Dmaven.repo.local=$mavenRepo" dependency:go-offline
Pop-Location

Write-Host "Bootstrap completed."
