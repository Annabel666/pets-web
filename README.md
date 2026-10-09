# 曾沛慈 · 礼物厅

前端是 Vue，放在 `frontend/`。后端是 Spring Boot + MyBatis + MySQL，放在 `backend/`。

在仓库根目录执行下面的命令。打开：http://127.0.0.1:8080

数据库是本机 Docker 容器 `pets-mysql`，端口 3307，库名 `pets_hall`，账号 `root` / `pets_hall`。不使用电脑上原来的 MySQL。这台电脑还没有这个容器时，先执行一次：

```
docker run -d --name pets-mysql --restart unless-stopped -p 3307:3306 -e MYSQL_ROOT_PASSWORD=pets_hall -e MYSQL_DATABASE=pets_hall mysql:8.0
```

Maven 在 `tools\apache-maven-3.9.9`，不用另外安装。

重新生成种子数据、构建页面并启动：

```
python scripts\build_seed.py
docker start pets-mysql
cd frontend
npm install
npm run build
cd ..
tools\apache-maven-3.9.9\bin\mvn.cmd -f backend\pom.xml spring-boot:run
```

改完页面后，在 `frontend` 里重新执行 `npm run build`，再启动后端。

日常只启动、不重新生成数据和页面时，用 `scripts\start-hall.ps1`。它会打开已有的 `pets-mysql` 并启动后端。`scripts\backup-hall.ps1` 备份账号和留言。`scripts\reset-password.ps1 -Username 名字 -Password 新密码` 在这台电脑上改密码。
