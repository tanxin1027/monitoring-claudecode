# 医院监控平台

## 项目简介

一套基于 SpringBoot 的服务器监控平台，支持监控医院内网服务器的性能、MySQL、Tomcat 等指标。

## 架构说明

```
┌─────────────────────────────────────────────────────────────┐
│                      云端平台                                 │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────┐      │
│  │  管理后台   │  │  数据采集   │  │  数据可视化     │      │
│  │  (Admin)    │  │  │(Collector)│  │  (Dashboard)    │      │
│  └─────────────┘  └─────────────┘  └─────────────────┘      │
│                                                              │
│  ┌─────────────┐  ┌─────────────┐                           │
│  │    MySQL    │  │    Redis    │                           │
│  │  (存储数据)  │  │  (缓存/Token)│                          │
│  └─────────────┘  └─────────────┘                           │
└─────────────────────────────────────────────────────────────┘
                            ↑ HTTPS
                            │ Agent 主动上报
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                    医院内网                                   │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────┐      │
│  │   Agent     │  │   系统采集   │  │  MySQL/Tomcat   │      │
│  │  (上报器)   │←─┤  (CPU/内存) │  │    采集器       │      │
│  └─────────────┘  └─────────────┘  └─────────────────┘      │
└─────────────────────────────────────────────────────────────┘
```

## 技术栈

### 云端服务端
- Spring Boot 2.7.18
- MyBatis Plus 3.5.3.1
- MySQL 8.0
- Redis
- Spring Security + JWT

### Agent 端
- Spring Boot 2.7.18
- OSHI (系统信息采集)
- JMX (Tomcat 监控)

## 项目结构

```
hospital-monitor/
├── cloud-server/          # 云端服务端
│   ├── src/main/java/com/monitor/
│   │   ├── controller/    # 控制器层
│   │   ├── service/       # 服务层
│   │   ├── mapper/        # 数据访问层
│   │   ├── entity/        # 实体类
│   │   ├── dto/           # 数据传输对象
│   │   ├── config/        # 配置类
│   │   └── util/          # 工具类
│   └── src/main/resources/
│       ├── application.yml
│       └── mapper/        # MyBatis XML
│
├── agent/                 # 内网采集 Agent
│   ├── src/main/java/com/monitor/agent/
│   │   ├── collector/     # 数据采集器
│   │   ├── reporter/      # 数据上报器
│   │   └── config/        # 配置类
│   └── src/main/resources/
│       └── application.yml
│
└── database/              # 数据库脚本
    └── schema.sql
```

## 快速开始

### 1. 数据库初始化

```bash
mysql -u root -p < database/schema.sql
```

### 2. 配置云端服务端

编辑 `cloud-server/src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/hospital_monitor
    username: root
    password: 123456

  redis:
    host: your-redis-host
    port: 6379
```

### 3. 启动云端服务端

```bash
cd cloud-server
mvn spring-boot:run
```

### 4. 配置 Agent 端

编辑 `agent/src/main/resources/application.yml`:

```yaml
cloud:
  server:
    url: https://your-cloud-server.com
    token: your-agent-token
    report-interval: 30
```

### 5. 启动 Agent 端

```bash
cd agent
mvn spring-boot:run
```

## API 接口

### 认证接口
- `POST /api/auth/login` - 用户登录

### 医院管理
- `GET /api/hospital/list` - 医院列表
- `GET /api/hospital/{id}` - 医院详情
- `POST /api/hospital` - 新增医院
- `PUT /api/hospital` - 修改医院
- `DELETE /api/hospital/{id}` - 删除医院

### 服务器监控
- `GET /api/monitor/server/list` - 服务器列表
- `GET /api/monitor/server/{id}` - 服务器详情
- `GET /api/monitor/server/{id}/metric` - 最新监控数据
- `GET /api/monitor/server/stats` - 服务器状态统计

### 数据采集 (Agent 调用)
- `POST /api/collector/server/metric` - 上报服务器性能数据

## 监控指标

### 服务器性能
- CPU 使用率、负载
- 内存使用量、使用率
- 磁盘使用量、使用率（按挂载点）
- 网络流量（接收/发送）
- 进程数

### MySQL
- 连接数
- QPS/TPS
- 线程数
- 流量统计
- InnoDB 缓冲池
- 慢查询数

### Tomcat
- JVM 内存使用
- GC 次数/时间
- 线程池状态
- Request 统计
- Session 状态

## 数据权限

系统支持基于医院的数据隔离：
- 超级管理员：可查看所有医院数据
- 医院管理员：只能查看本医院数据
- 普通用户：根据分配的权限查看

## 告警功能

支持配置告警规则，当监控指标超过阈值时触发告警：
- 支持 CPU、内存、磁盘等指标
- 支持 MySQL 连接数、QPS 等指标
- 支持 Tomcat JVM 内存、GC 等指标

## License

MIT
