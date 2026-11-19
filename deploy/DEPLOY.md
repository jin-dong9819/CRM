# 易配通 CRM · Ubuntu 22.04 后端部署手册（不使用 Docker）

> 目标：Ubuntu 22.04 纯净机器，从零把 Spring Boot 后端跑起来（systemd 托管）。
> 账号约定：下文 `deploy` 是你登录服务器的普通用户，`sudo` 提权；应用以 `crm` 系统用户运行。

---

## 0. 环境清单（照这个装，多了不用装）

| 组件 | 版本 | 是否必需 | 说明 |
|---|---|---|---|
| OpenJDK | 17 (LTS) | 必需 | pom 中 `java.version=17`，Spring Boot 3.5 最低 17 |
| MySQL | 8.0+ | 必需 | Ubuntu 22.04 官方源即 8.0 |
| Nginx | 1.18+ | 强烈建议 | 对外统一 80/443，反代 `/api` → 8000 |
| Redis | — | **不需要** | pom 与 yml 中已注释停用，验证码改内存存储 |
| Maven | — | **不需要** | 本地打包好上传 jar，服务器不装 Maven |
| Docker | — | 不需要 | 本方案全程裸机 |

架构：`浏览器 → Nginx(80) → 静态 dist + /api 反代 → 127.0.0.1:8000 → MySQL(3306)`

---

## 1. 服务器初始化

```bash
# 时区
sudo timedatectl set-timezone Asia/Shanghai
date   # 确认 CST

# 更新索引（首次较慢）
sudo apt update && sudo apt -y upgrade

# 常用工具
sudo apt -y install curl vim unzip ufw

# 创建运行用户（不能登录，只跑服务）
sudo useradd -r -s /sbin/nologin crm
```

---

## 2. 安装 JDK 17

只需要运行 jar，装 JRE 就够；但保留 JDK 方便以后排查 jstack。

```bash
sudo apt -y install openjdk-17-jdk-headless
java -version   # 应显示 openjdk version "17.0.x"
ls -d /usr/lib/jvm/java-17-openjdk-amd64   # 记下这个路径，systemd 要用
```

如果 `java -version` 没生效，手动选：

```bash
sudo update-alternatives --config java
```

---

## 3. 安装并初始化 MySQL 8

```bash
sudo apt -y install mysql-server
sudo systemctl enable --now mysql
sudo systemctl status mysql --no-pager
```

安全初始化（一路 Y，设置 root 密码，删除匿名用户）：

```bash
sudo mysql_secure_installation
```

创建**应用专用账号**（不要用 root 跑业务）：

```bash
sudo mysql -u root -p
```

```sql
CREATE DATABASE IF NOT EXISTS crm
  DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

CREATE USER 'crm_app'@'127.0.0.1' IDENTIFIED BY '换成强密码';
GRANT ALL PRIVILEGES ON crm.* TO 'crm_app'@'127.0.0.1';
FLUSH PRIVILEGES;
EXIT;
```

确认字符集（必须是 utf8mb4，否则中文报错/乱码）：

```bash
mysql -u root -p -e "SHOW VARIABLES LIKE 'character_set_server';"
```

若输出不是 `utf8mb4`，编辑 `/etc/mysql/mysql.conf.d/mysqld.cnf`，在 `[mysqld]` 下加：

```ini
character-set-server = utf8mb4
collation-server     = utf8mb4_general_ci
```

然后 `sudo systemctl restart mysql`。

---

## 4. 导入数据库（顺序不能乱）

把项目 `db/` 目录上传到服务器，例如 `/opt/crm/db/`，然后在**该目录下**执行：

```bash
cd /opt/crm/db

# 4.1 建库 + 系统表（部门/角色/员工/权限） + 初始管理员
mysql -u root -p < crm_schema.sql

# 4.2 业务表 + 示例业务数据（线索/客户/商机/订单）
mysql -u root -p < crm_business.sql

# 4.3 注册用户表（crm_schema.sql 里没有这张表，必须单独导）
mysql -u root -p crm < sys_user.sql

# 4.4 重建前端两级菜单树（可重复执行）
mysql -u root -p < upgrade_menu_tree.sql

# 4.5 组织架构重建 + 产品分类/产品基础数据
#     注意：此脚本会清空 sys_dept 后重建，必须放在 4.1 之后
mysql -u root -p < crm_mfg_data.sql

# 4.6 可选：事务演示表（仅测试用，生产可不导）
# mysql -u root -p crm < demo_account.sql
```

> `upgrade_permission_menu.sql` / `upgrade_analysis_menu.sql` 属于早期增量脚本，
> 菜单已由 `upgrade_menu_tree.sql` 整体重建，**无需再执行**。

校验：

```bash
mysql -u root -p crm -e "SHOW TABLES;"
mysql -u root -p crm -e "SELECT COUNT(*) FROM sys_employee; SELECT COUNT(*) FROM sys_permission; SELECT COUNT(*) FROM crm_product;"
```

字符集统一：

```bash
mysql -u root -p -e "
SELECT TABLE_NAME, TABLE_COLLATION FROM information_schema.TABLES
WHERE TABLE_SCHEMA='crm' AND TABLE_COLLATION NOT LIKE 'utf8mb4%';"
# 期望：Empty set
```

---

## 5. 本地打包 jar（在 Windows 开发机做）

```bash
cd D:/develop/code/codeIDEA/springboot-test/crm_backend
mvn -v          # 需 3.8+，本地当前 3.10.0
java -version   # 需 17

mvn clean package -DskipTests
```

产物：`target/springboot-test-1.0-SNAPSHOT.jar`（Spring Boot 已 repackage，是可直接 `java -jar` 的胖 jar）

本地冒烟（可选）：

```bash
java -jar target/springboot-test-1.0-SNAPSHOT.jar --spring.profiles.active=prod
```

> 不要用 `mvn spring-boot:run` 验证生产包。pom 里给它配了 `-Dserver.port=8080` 的 jvmArguments，
> 那条只对 `spring-boot:run` 生效，对 `java -jar` 无影响。

---

## 6. 上传到服务器并落位

```bash
# 建目录
ssh deploy@<服务器IP> "sudo mkdir -p /opt/crm/{app,logs,web,db} && sudo chown -R crm:crm /opt/crm"

# 上传 jar
scp target/springboot-test-1.0-SNAPSHOT.jar deploy@<服务器IP>:/tmp/crm-backend.jar
ssh deploy@<服务器IP> "sudo mv /tmp/crm-backend.jar /opt/crm/app/crm-backend.jar && sudo chown crm:crm /opt/crm/app/crm-backend.jar"

# 上传生产配置（放 /etc/crm，systemd 里用 --spring.config.additional-location 指过去）
scp deploy/application-prod.yml deploy@<服务器IP>:/tmp/
ssh deploy@<服务器IP> "sudo mkdir -p /etc/crm && sudo mv /tmp/application-prod.yml /etc/crm/application-prod.yml && sudo chown root:root /etc/crm/application-prod.yml && sudo chmod 644 /etc/crm/application-prod.yml"
```

> `/etc/crm` 不在 Spring Boot 默认搜索路径内，必须靠 `crm.service` 里的
> `--spring.config.additional-location=file:/etc/crm/` 才能加载（见第 8 步）。
> 少了这个参数，应用会静默回落到 jar 内的开发配置（`root/root`、`sms.dev-mode=true`、SQL 打屏），**不报错但等于没生效**。
> jar 同级的 `/opt/crm/app/config/` 也是默认搜索位置，两种做法二选一，**不要两处都放**，否则改哪份都可能不生效。

目录结构最终应为：

```
/opt/crm/
├── app/
│   ├── crm-backend.jar
│   └── crm.env                     # 敏感环境变量，权限 600
├── logs/                           # app.log / stdout.log / stderr.log
├── db/                             # 导入用的 sql 脚本
├── backup/                         # 每次发版前的带时间戳 jar 备份
└── frontend/                       # 前端 dist 内容（Nginx root）

/etc/crm/
└── application-prod.yml            # 生产配置（root:root 644）
```

---

## 7. 写环境变量文件

```bash
sudo tee /opt/crm/app/crm.env > /dev/null <<'EOF'
DB_URL=jdbc:mysql://127.0.0.1:3306/crm?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
DB_USERNAME=crm_app
DB_PASSWORD=换成第3步设置的强密码
SMS_DEV_MODE=false
RONG_ACCOUNT_SID=你的容联云SID
RONG_AUTH_TOKEN=你的容联云Token
RONG_APP_ID=你的容联云AppId
RONG_TEMPLATE_ID=1
EOF

sudo chown crm:crm /opt/crm/app/crm.env
sudo chmod 600 /opt/crm/app/crm.env
```

> 关键：`SMS_DEV_MODE` 必须是 `false`。
> 为 `true` 时验证码会由接口直接返回并打进日志，等于任何人可注册任意手机号。
> 如果暂时还没有短信服务商账号，先不要开放注册入口，不要用 dev-mode 顶着上线。

---

## 8. 注册 systemd 服务

> 现行单元是 **`crm-backend.service`**（`User=root`，jar 名 `crm-backend.jar`，端口 8000）。
> 早期的 `crm.service`（`User=crm`，jar 名 `crm.jar`）已 `stop` + `disable`，文件保留可回滚，不要两个同时启用——会抢 8000。

```bash
# 上传 deploy/crm-backend.service
scp deploy/crm-backend.service deploy@<服务器IP>:/tmp/
ssh deploy@<服务器IP> "sudo mv /tmp/crm-backend.service /etc/systemd/system/crm-backend.service"

sudo systemctl daemon-reload
sudo systemctl enable crm-backend
sudo systemctl start crm-backend
sudo systemctl status crm-backend --no-pager
```

常用运维命令：

```bash
sudo systemctl restart crm-backend   # 重启
sudo systemctl stop crm-backend      # 停止
journalctl -u crm-backend -f         # 跟随日志（该单元未重定向 stdout，日志全在 journald）
```

---

## 9. 验证后端

```bash
# 本机探活（8000 不对外，只能在服务器上访问）
curl -i http://127.0.0.1:8000/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"empNo":"E1001","password":"123456"}'
```

期望返回 `{"code":200,"msg":"登录成功","data":{"token":"..."}}`。

初始员工来自 `crm_schema.sql`：编号 `E1001 ~ E1006`，演示密码统一 `123456`。
查询员工：`mysql -u root -p crm -e "SELECT emp_no,name FROM sys_employee;"`

> 这些是演示账号，正式使用前务必改密码（登录后走修改密码接口，或直接更新 `password` 为新的 BCrypt 密文）。

若返回 401/500，先看 `/opt/crm/logs/stderr.log` 与 `/opt/crm/logs/app.log`。

---

## 10. 防火墙与对外暴露

```bash
sudo ufw allow OpenSSH
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
# 不要放行 8000 和 3306
sudo ufw enable
sudo ufw status verbose
```

云厂商的**安全组**同样只放 22 / 80 / 443。

---

## 11. Nginx 反向代理（前端 + 接口）

> 现行方案：**直接替换主配置 `/etc/nginx/nginx.conf`**（单 `server` 块，含 gzip），
> **不再使用** `sites-available/` 那一套。`deploy/nginx.conf` 就是服务器上的实际文件。
> `deploy/nginx-crm.conf` 是早期的站点分离版，已失效，仅作留档。

```bash
sudo apt -y install nginx
scp deploy/nginx.conf deploy@<服务器IP>:/tmp/
ssh deploy@<服务器IP> "sudo cp /etc/nginx/nginx.conf /etc/nginx/nginx.conf.bak && sudo mv /tmp/nginx.conf /etc/nginx/nginx.conf"

sudo nginx -t && sudo systemctl reload nginx
```

> `proxy_pass` 末尾**不能**带斜杠：后端 Controller 自带 `/api` 前缀，剥掉就全站 404。
> 静态资源用 `try_files $uri =404` 而不是回退首页，避免 404 的 js 返回 HTML 导致白屏。

前端构建与上传（在开发机）：

```bash
cd crm_web
# 生产必须关掉 mock
echo "VITE_USE_MOCK=false" > .env.production
npm ci
npm run build

scp -r dist/* deploy@<服务器IP>:/tmp/web/
ssh deploy@<服务器IP> "sudo cp -r /tmp/web/* /opt/crm/frontend/ && sudo chown -R crm:crm /opt/crm/frontend && sudo systemctl reload nginx"
```

HTTPS（有域名时）：

```bash
sudo apt -y install certbot python3-certbot-nginx
sudo certbot --nginx -d crm.example.com
```

---

## 12. 上线后必须知道的三个限制

1. **登录态存在内存里**（`com.demo.util.TokenStore`），重启后端所有人被踢下线。
   单机部署没问题，但**不能起多实例负载均衡**，否则 A 机登录的 token 到 B 机就是 401。
2. **验证码也存在内存里**（`com.demo.util.SmsCodeStore`），重启即失效，同上不能多实例。
3. 需要在多实例或重启不掉线时，把 `TokenStore` 换成 Redis / JWT，届时再放开 pom 里的 Redis 依赖。

---

## 13. 日常运维

**备份数据库**（每天 3 点，保留 14 天）：

```bash
sudo mkdir -p /opt/crm/backup
sudo tee /opt/crm/backup/backup.sh > /dev/null <<'EOF'
#!/bin/bash
set -e
D=/opt/crm/backup
mysqldump -u crm_app -p"$(grep '^DB_PASSWORD=' /opt/crm/app/crm.env | cut -d= -f2-)" \
  --single-transaction --routines --triggers --default-character-set=utf8mb4 \
  crm | gzip > "$D/crm_$(date +%F_%H%M).sql.gz"
find "$D" -name 'crm_*.sql.gz' -mtime +14 -delete
EOF
sudo chmod +x /opt/crm/backup/backup.sh

# 每天 03:00 执行
echo "0 3 * * * root /opt/crm/backup/backup.sh" | sudo tee /etc/cron.d/crm-backup
```

**更新版本**（带时间戳备份，可回滚）：

```bash
# 1. 服务器：先备份当前 jar
sudo mkdir -p /opt/crm/backup && sudo cp /opt/crm/app/crm-backend.jar \
  "/opt/crm/backup/crm-backend-$(date +%Y%m%d-%H%M%S).jar"

# 2. 本地：打包并上传（root 可直接写 /opt/crm，不用中转 /tmp）
cd crm_backend && mvn package -DskipTests
scp -i ~/.ssh/id_ed25519_tc target/springboot-test-1.0-SNAPSHOT.jar \
  root@<IP>:/opt/crm/app/crm-backend.jar.new

# 3. 服务器：停服、替换、启动（用 root 登录时不需要 sudo）
systemctl stop crm-backend
chown crm:crm /opt/crm/app/crm-backend.jar.new
mv /opt/crm/app/crm-backend.jar.new /opt/crm/app/crm-backend.jar
systemctl start crm-backend
systemctl status crm-backend --no-pager
journalctl -u crm-backend -n 100 --no-pager

# 4. 必须验证（启动成功 ≠ 可用，Hikari 懒初始化）
curl -s -o /dev/null -w '%{http_code}\n' -X POST http://127.0.0.1:8000/api/auth/login \
  -H "Content-Type: application/json" -d '{"empNo":"E1001","password":"123456"}'
```

回滚：`sudo cp /opt/crm/backup/crm-backend-<时间戳>.jar /opt/crm/app/crm-backend.jar && sudo systemctl restart crm-backend`

> 只改 `/etc/crm/application-prod.yml` → 只需 `systemctl restart crm-backend`；
> 改了 `.service` 文件 → 必须先 `systemctl daemon-reload` 再 restart，否则新参数不生效。

---

## 14. 常见故障速查

| 现象 | 原因 | 处理 |
|---|---|---|
| `systemctl status crm-backend` 显示 `status=1/FAILURE` | 启动异常，看 stderr.log | `tail -50 /opt/crm/logs/stderr.log` |
| 日志报 `Access denied for user 'crm_app'` | 密码错 / 用户 host 限制 | 确认 `crm.env` 密码，用户必须是 `'crm_app'@'127.0.0.1'` |
| 日志报 `Unknown database 'crm'` | 第 4 步没执行成功 | 重跑 `crm_schema.sql` |
| 中文变问号 | 建库/连接字符集不是 utf8mb4 | 按第 3、4 步检查并修正 |
| `no main manifest attribute` | 上传的是原始 jar 不是 repackage 后的 | 重新 `mvn clean package`，确认 `target/*.jar` 能用 `java -jar` 起 |
| 前端页面能开，接口全 502 | Nginx 反代目标或后端未启动 | `curl 127.0.0.1:8000/api/...` 先确认后端；再查 `proxy_pass` 末尾不能带 `/` |
| 前端刷新子路由 404 | 缺 history 兜底 | 确认 `try_files $uri $uri/ /index.html;` |
| 端口被占 | 8000 冲突 | `sudo ss -lntp \| grep 8000` |
| 服务器上 `curl 127.0.0.1:8000` 通，外部访问 8000 不通 | 正常现象 | `application-prod.yml` 里 `server.address: 127.0.0.1`，只允许 Nginx 反代；确实要直连时删掉该行并放行 ufw |
