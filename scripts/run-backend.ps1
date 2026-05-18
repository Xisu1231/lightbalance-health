$root = Split-Path -Parent $PSScriptRoot
$toolsDir = Join-Path $root "tools"
$mavenRepo = Join-Path $root ".m2"
$env:JAVA_HOME = Join-Path $toolsDir "jdk-17"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
$mavenHome = Join-Path $toolsDir "apache-maven-3.9.11"

Push-Location (Join-Path $root "backend")
& "$mavenHome\bin\mvn.cmd" "-Dmaven.repo.local=$mavenRepo" spring-boot:run
Pop-Location
