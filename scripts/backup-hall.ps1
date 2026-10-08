$ErrorActionPreference = "Stop"
$root = Split-Path $PSScriptRoot -Parent
$destDir = Join-Path $root "backups"
New-Item -ItemType Directory -Force -Path $destDir | Out-Null
$stamp = Get-Date -Format "yyyyMMdd-HHmmss"
$remote = "/tmp/pets-hall-$stamp.sql"
$local = Join-Path $destDir "pets-hall-$stamp.sql"

docker exec pets-mysql sh -c "mysqldump -uroot -ppets_hall --default-character-set=utf8mb4 --no-tablespaces pets_hall hall_user guestbook guestbook_agree hall_listen > $remote"
if ($LASTEXITCODE -ne 0) {
  Write-Host "备份没有完成。请确认 pets-mysql 已启动。"
  exit 1
}
docker cp "pets-mysql:$remote" $local
docker exec pets-mysql rm -f $remote
Write-Host "已备份到 $local"
