# 曾沛慈 · 礼物厅

前端是 Vue，放在 `frontend/`。后端是 Spring Boot + MyBatis + MySQL，放在 `backend/`。

打开：http://127.0.0.1:8080

数据库是本机 Docker 容器 `pets-mysql`，端口 3307，库名 `pets_hall`，账号 `root` / `pets_hall`。不使用电脑上原来的 MySQL。

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

也可以用 `scripts\start-hall.ps1` 启动。`scripts\backup-hall.ps1` 备份账号和留言。`scripts\reset-password.ps1 -Username 名字 -Password 新密码` 在这台电脑上改密码。
