# =====================================
# 医院监控平台 - 部署指南
# =====================================

## 环境要求

- JDK 11+
- MySQL 8.0+
- Redis 6.0+
- Maven 3.6+

## 一、云端服务端部署

### 1. 准备数据库

```bash
# 创建数据库并导入表结构
mysql -u root -p < database/schema.sql
```

### 2. 修改配置

编辑 `cloud-server/src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://<MySQL 地址>:3306/hospital_monitor?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: <用户名>
    password: <密码>

  redis:
    host: <Redis 地址>
    port: 6379
```

### 3. 打包编译

```bash
cd cloud-server
mvn clean package -DskipTests
```

### 4. 启动服务

```bash
java -jar target/cloud-server-1.0.0.jar
```

或使用 systemd 服务：

```bash
# 创建服务文件
sudo vim /etc/systemd/system/hospital-monitor.service

# 内容如下：
[Unit]
Description=Hospital Monitor Server
After=syslog.target network.target

[Service]
User=root
ExecStart=/usr/bin/java -jar /opt/hospital-monitor/cloud-server.jar
SuccessExitStatus=143
Restart=on-failure

[Install]
WantedBy=multi-user.target

# 启动服务
sudo systemctl daemon-reload
sudo systemctl enable hospital-monitor
sudo systemctl start hospital-monitor
```

### 5. 生成 Agent Token

启动 Redis CLI，生成 token:

```bash
redis-cli
SET agent:token:your-token-here "server001"
EXPIRE agent:token:your-token-here 0  # 永不过期
```

或者添加一个管理接口来生成 token。

## 二、Agent 端部署

### 1. 修改配置

编辑 `agent/src/main/resources/application.yml`:

```yaml
cloud:
  server:
    url: https://<云端服务器地址>:8080
    token: <由云端分配的 token>
    report-interval: 30  # 上报间隔（秒）
```

### 2. 打包编译

```bash
cd agent
mvn clean package -DskipTests
```

### 3. 部署到医院内网服务器

```bash
# 上传 jar 包
scp target/agent-1.0.0.jar user@hospital-server:/opt/hospital-monitor/

# 启动
java -jar /opt/hospital-monitor/agent-1.0.0.jar
```

### 4. 配置 MySQL 监控（可选）

在 Agent 服务器上配置 MySQL 账号：

```sql
-- 创建监控账号
CREATE USER 'monitor'@'localhost' IDENTIFIED BY 'monitor123';
GRANT SELECT ON performance_schema.* TO 'monitor'@'localhost';
GRANT SHOW VIEW ON *.* TO 'monitor'@'localhost';
FLUSH PRIVILEGES;
```

### 5. 配置 Tomcat JMX（可选）

编辑 Tomcat 的 `catalina.sh`:

```bash
export CATALINA_OPTS="$CATALINA_OPTS -Dcom.sun.management.jmxremote
-Dcom.sun.management.jmxremote.port=9999
-Dcom.sun.management.jmxremote.authenticate=true
-Dcom.sun.management.jmxremote.ssl=false
-Dcom.sun.management.jmxremote.password.file=/path/to/jmxremote.password
-Dcom.sun.management.jmxremote.access.file=/path/to/jmxremote.access"
```

## 三、Nginx 反向代理配置

```nginx
# HTTPS 配置
server {
    listen 443 ssl;
    server_name monitor.yourdomain.com;

    ssl_certificate /path/to/cert.pem;
    ssl_certificate_key /path/to/key.pem;

    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }

    # Agent 数据上报接口
    location /api/collector/ {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_client_max_body_size 10M;
    }
}
```

## 四、防火墙配置

云端服务器需要开放以下端口：
- 8080 (应用端口)
- 443 (HTTPS)
- 3306 (MySQL，仅内网访问)
- 6379 (Redis，仅内网访问)

## 五、监控和维护

### 查看日志

```bash
# 应用日志
tail -f /var/log/hospital-monitor/server.log
```

### 数据清理

监控数据按月分表，定期清理旧数据：

```sql
-- 删除 3 个月前的数据
DROP TABLE server_metric_202601;
DROP TABLE mysql_metric_202601;
DROP TABLE tomcat_metric_202601;
```

## 六、常见问题

### Agent 无法连接云端

1. 检查云端服务器防火墙
2. 检查 token 是否正确
3. 查看 Agent 日志

### 监控数据不更新

1. 检查 Agent 是否正常运行
2. 检查云端日志是否有错误
3. 验证服务器是否在云端数据库中注册
