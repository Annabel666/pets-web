param(
  [Parameter(Mandatory = $true)][string]$Username,
  [Parameter(Mandatory = $true)][string]$Password
)

$ErrorActionPreference = "Stop"
$root = Split-Path $PSScriptRoot -Parent
$tmp = Join-Path ([System.IO.Path]::GetTempPath()) ("pets-reset-" + [guid]::NewGuid().ToString() + ".txt")
try {
  $utf8 = New-Object System.Text.UTF8Encoding $false
  [System.IO.File]::WriteAllLines($tmp, @($Username, $Password), $utf8)
  & (Join-Path $root "tools\apache-maven-3.9.9\bin\mvn.cmd") -f (Join-Path $root "backend\pom.xml") -q exec:java "-Dexec.mainClass=com.pets.hall.tools.ResetPassword" "-Dpets.file=$tmp"
  exit $LASTEXITCODE
} finally {
  if (Test-Path $tmp) { Remove-Item -Force $tmp }
}
