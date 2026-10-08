$ErrorActionPreference = "Stop"
$root = Split-Path $PSScriptRoot -Parent
Set-Location $root

Write-Host "正在打开数据库…"
docker start pets-mysql
if ($LASTEXITCODE -ne 0) {
  Write-Host "数据库没有打开。请先启动 Docker。"
  exit 1
}

$ready = $false
for ($i = 0; $i -lt 20; $i++) {
  $client = New-Object System.Net.Sockets.TcpClient
  try {
    $wait = $client.BeginConnect("::1", 3307, $null, $null)
    if ($wait.AsyncWaitHandle.WaitOne(1000, $false) -and $client.Connected) {
      $ready = $true
      break
    }
  } catch {
  } finally {
    $client.Close()
  }
  Start-Sleep -Seconds 1
}
if (-not $ready) {
  Write-Host "数据库没有连上。请确认 Docker 已打开，并且 pets-mysql 在运行。"
  exit 1
}

Write-Host "数据库已就绪，正在打开礼物厅。关掉这个窗口就会停。电脑休眠时，厅也会停。"
$mvn = Join-Path $root "tools\apache-maven-3.9.9\bin\mvn.cmd"
$pom = Join-Path $root "backend\pom.xml"
$tries = 0
while ($tries -lt 3) {
  & $mvn -f $pom spring-boot:run
  if ($LASTEXITCODE -eq 0) { exit 0 }
  $tries++
  if ($tries -ge 3) {
    Write-Host "礼物厅没有留住。"
    exit 1
  }
  Write-Host "礼物厅停了，5 秒后重新打开。"
  Start-Sleep -Seconds 5
}
