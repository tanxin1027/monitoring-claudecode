# 医院监控平台 - 项目总结

## 已完成功能

### 1. 项目结构
```
hospital-monitor/
├── cloud-server/          # 云端服务端
├── agent/                 # 内网采集 Agent
├── database/              # 数据库脚本
├── README.md              # 项目说明
├── DEPLOY.md              # 部署指南
└── pom.xml                # 父工程
```

### 2. 云端服务端 (cloud-server)

#### 核心模块
- **认证模块** (`/api/auth`)
  - 用户登录 (JWT Token)
  - 用户信息查询

- **数据采集模块** (`/api/collector`)
  - 服务器性能数据接收
  - Agent Token 验证
  - 心跳更新

- **医院管理模块** (`/api/hospital`)
  - 医院 CRUD
  - 医院列表查询
  - 数据权限隔离

- **服务器监控模块** (`/api/monitor/server`)
  - 服务器 CRUD
  - 实时性能数据
  - 历史数据查询
  - 状态统计

- **Token 管理模块** (`/api/agent/token`)
  - Token 生成
  - Token 启用/禁用
  - Token 列表查询

- **数据可视化模块** (`/api/dashboard`)
  - 仪表盘概览
  - 服务器统计
  - 告警统计
  - 性能排行

#### 技术实现
- Spring Boot 2.7.18
- MyBatis Plus (数据访问)
- Spring Security + JWT (认证)
- Redis (缓存)
- MySQL (数据存储)

### 3. Agent 端 (agent)

#### 采集器
- **SystemCollector**: 系统信息采集
  - CPU 使用率、负载
  - 内存使用
  - 磁盘使用 (按挂载点)
  - 网络流量
  - 进程数

- **MysqlCollector**: MySQL 监控
  - 连接数
  - QPS/TPS
  - 线程状态
  - 流量统计
  - InnoDB 指标
  - 慢查询

- **TomcatCollector**: Tomcat 监控 (JMX)
  - JVM 内存
  - GC 统计
  - 线程池
  - Request 统计
  - Session 状态

#### 上报器
- **DataReporter**: HTTP 数据上报
- **ServerMetricReporter**: 定时上报任务

### 4. 数据库设计

#### 系统管理
- `sys_user` - 用户表
- `sys_role` - 角色表
- `sys_menu` - 菜单权限表
- `sys_user_role` - 用户角色关联
- `sys_role_menu` - 角色菜单关联

#### 医院管理
- `hospital_info` - 医院信息
- `sys_user_hospital` - 用户医院权限

#### 服务器管理
- `server_info` - 服务器信息
- `server_metric_202604` - 服务器性能数据 (按月分表)

#### MySQL 监控
- `mysql_instance` - MySQL 实例
- `mysql_metric_202604` - MySQL 性能数据

#### Tomcat 监控
- `tomcat_instance` - Tomcat 实例
- `tomcat_metric_202604` - Tomcat 性能数据

#### 告警模块
- `alarm_rule` - 告警规则
- `alarm_record` - 告警记录

#### Agent 管理
- `agent_token` - Agent 认证 Token
- `agent_heartbeat_log` - 心跳日志

## 待完成功能

### 1. 前端管理界面
需要开发前端 Vue/React 应用：
- 登录页面
- 仪表盘
- 医院管理页面
- 服务器管理页面
- 监控数据可视化 (图表)
- 告警管理页面
- 系统设置页面

### 2. MySQL/Tomcat 完整集成
- MySQL 实例管理接口
- Tomcat 实例管理接口
- MySQL 性能数据上报接口
- Tomcat 性能数据上报接口

### 3. 告警功能
- 告警规则配置
- 告警触发逻辑
- 告警通知 (邮件/短信/Webhook)
- 告警处理流程

### 4. 数据分表策略
- 按月自动创建分表
- 历史数据归档
- 数据清理任务

### 5. 数据权限完善
- 基于角色的数据访问
- 医院数据隔离
- 操作日志审计

## 下一步工作

### 1. 完善后端接口
```bash
# MySQL 监控接口
- POST /api/collector/mysql/metric
- GET /api/monitor/mysql/list
- GET /api/monitor/mysql/{id}/metric

# Tomcat 监控接口
- POST /api/collector/tomcat/metric
- GET /api/monitor/tomcat/list
- GET /api/monitor/tomcat/{id}/metric
```

### 2. 开发告警模块
```java
// 告警检测服务
public interface AlarmService {
    void checkAlarmRules();
    void triggerAlarm(Alarm alarm);
    void sendNotification(Alarm alarm);
}
```

### 3. 数据定时清理
```java
// 定时任务
@Scheduled(cron = "0 0 2 * * ?")
public void cleanOldData() {
    // 删除 3 个月前的数据
}
```

## 使用说明

### 快速启动

1. 初始化数据库
```bash
mysql -u root -p < database/schema.sql
```

2. 配置云端服务
```yaml
# cloud-server/application.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/hospital_monitor
    username: root
    password: your-password
  redis:
    host: localhost
```

3. 启动云端服务
```bash
cd cloud-server
mvn spring-boot:run
```

4. 生成 Agent Token
```bash
# 调用 API 或直接在数据库插入
INSERT INTO agent_token (token, server_id, status) VALUES ('agt_test123', 1, 1);
```

5. 配置并启动 Agent
```yaml
# agent/application.yml
cloud:
  server:
    url: http://localhost:8080
    token: agt_test123
```

```bash
cd agent
mvn spring-boot:run
```

### API 测试

```bash
# 登录
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

# 获取服务器列表
curl -X GET "http://localhost:8080/api/monitor/server/list?pageNum=1&pageSize=10" \
  -H "Authorization: Bearer <token>"

# Agent 上报数据
curl -X POST http://localhost:8080/api/collector/server/metric \
  -H "Content-Type: application/json" \
  -H "X-Agent-Token: agt_test123" \
  -d '{"serverCode":"server001","cpuUsage":25.5,"memoryUsed":4096,...}'
```

## 技术栈总结

| 模块 | 技术 |
|------|------|
| 后端框架 | Spring Boot 2.7.18 |
| ORM | MyBatis Plus |
| 数据库 | MySQL 8.0 |
| 缓存 | Redis |
| 认证 | Spring Security + JWT |
| 系统采集 | OSHI |
| Java 版本 | JDK 11 |

## 项目规模

- Java 文件：30+
- 数据库表：20+
- API 接口：30+
- 代码行数：约 5000+
