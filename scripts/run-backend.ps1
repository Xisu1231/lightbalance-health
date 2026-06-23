$root = Split-Path -Parent $PSScriptRoot
$toolsDir = Join-Path $root "tools"
$mavenRepo = Join-Path $root ".m2"
$localEnv = Join-Path $PSScriptRoot "local.env.ps1"

if (Test-Path $localEnv) {
  . $localEnv
}

$defaultDatasource = "jdbc:h2:mem:health_balance;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE"
if (-not $env:SPRING_DATASOURCE_URL) {
  $env:SPRING_DATASOURCE_URL = $defaultDatasource
}

$env:JAVA_HOME = Join-Path $toolsDir "jdk-17"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
$mavenHome = Join-Path $toolsDir "apache-maven-3.9.11"

Push-Location (Join-Path $root "backend")
& "$mavenHome\bin\mvn.cmd" "-Dmaven.repo.local=$mavenRepo" spring-boot:run
Pop-Location
