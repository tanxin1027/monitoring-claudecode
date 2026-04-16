# MySQL 监控 API 接口文档

## 接口概述
提供对 MySQL 实例的监控数据查询接口。

## 基础信息
- 基础路径：`/api/monitor/mysql`
- 认证方式：JWT Token（通过 Authorization Header）
- 返回格式：JSON

## 接口列表

### 1. 获取 MySQL 实例列表
- **URL**: `GET /api/monitor/mysql/list`
- **功能**: 获取当前用户有权限的所有 MySQL 实例列表
- **认证**: JWT Token
- **请求参数**:
  ```
  pageNum: 页码（默认 1）
  pageSize: 每页数量（默认 10）
  searchKey: 搜索关键字（可选）
  ```
- **响应示例**:
  ```json
  {
    "code": 200,
    "message": "success",
    "data": {
      "records": [
        {
          "id": 1,
          "instanceCode": "mysql_001",
          "instanceName": "MySQL 实例 1",
          "serverId": 1,
          "hospitalId": 1,
          "port": 3306,
          "dbVersion": "8.0.25",
          "status": 1,
          "createTime": "2026-04-15 10:00:00"
        }
      ],
      "total": 1,
      "pageNum": 1,
      "pageSize": 10
    }
  }
  ```

### 2. 获取 MySQL 实例详情
- **URL**: `GET /api/monitor/mysql/{id}`
- **功能**: 获取指定 MySQL 实例的详细信息
- **认证**: JWT Token
- **路径参数**:
  - `id`: 实例 ID
- **响应示例**:
  ```json
  {
    "code": 200,
    "message": "success",
    "data": {
      "id": 1,
      "instanceCode": "mysql_001",
      "instanceName": "MySQL 实例 1",
      "serverId": 1,
      "hospitalId": 1,
      "port": 3306,
      "dbVersion": "8.0.25",
      "username": "monitor_user",
      "status": 1,
      "createTime": "2026-04-15 10:00:00",
      "updateTime": "2026-04-15 10:00:00"
    }
  }
  ```

### 3. 获取 MySQL 实时监控数据
- **URL**: `GET /api/monitor/mysql/{id}/metric`
- **功能**: 获取指定 MySQL 实例的最新监控数据
- **认证**: JWT Token
- **路径参数**:
  - `id`: 实例 ID
- **响应示例**:
  ```json
  {
    "code": 200,
    "message": "success",
    "data": {
      "connectionsCurrent": 25,
      "connectionsMax": 1000,
      "connectionsUsage": 2.5,
      "qps": 150.50,
      "qpsTotal": 123456,
      "tps": 25.30,
      "tpsTotal": 2345,
      "slowQueriesRate": 0,
      "slowQueriesTotal": 12,
      "threadsRunning": 3,
      "threadsConnected": 25,
      "threadsCreated": 150,
      "bytesReceived": 104857600,
      "bytesSent": 52428800,
      "bufferPoolSize": 1073741824,
      "bufferPoolFreePages": 1200,
      "bufferPoolDirtyPages": 50,
      "bufferPoolReadRequests": 50000,
      "bufferPoolWriteRequests": 10000,
      "bufferPoolReads": 1000,
      "bufferPoolHitRate": 98.0,
      "innodbRowLockTime": 150,
      "innodbRowLockWaits": 2,
      "innodbRowLockCurrentWaits": 0,
      "tableLocksWaited": 5,
      "tableLocksImmediate": 1000,
      "tableLockWaitRate": 0.5,
      "tmpTablesCreated": 120,
      "tmpDiskTablesCreated": 2,
      "tmpDiskTableRate": 1.67,
      "sortRows": 800,
      "sortScan": 5,
      "sortRange": 10,
      "sortMergePasses": 2,
      "slaveIoRunning": true,
      "slaveSqlRunning": true,
      "secondsBehindMaster": 2,
      "collectTime": 1713177600
    }
  }
  ```

### 4. 获取 MySQL 历史监控数据
- **URL**: `GET /api/monitor/mysql/{id}/metric/history`
- **功能**: 获取指定 MySQL 实例的历史监控数据
- **认证**: JWT Token
- **路径参数**:
  - `id`: 实例 ID
- **查询参数**:
  - `startTime`: 开始时间（格式：yyyy-MM-dd HH:mm:ss）
  - `endTime`: 结束时间（格式：yyyy-MM-dd HH:mm:ss）
  - `interval`: 时间间隔（分钟，如 5, 15, 30, 60）
- **响应示例**:
  ```json
  {
    "code": 200,
    "message": "success",
    "data": [
      {
        "collectTime": "2026-04-15 10:00:00",
        "qps": 150.50,
        "tps": 25.30,
        "connectionsCurrent": 25,
        "slowQueriesRate": 0,
        "bufferPoolHitRate": 98.0
      },
      {
        "collectTime": "2026-04-15 10:05:00",
        "qps": 160.20,
        "tps": 28.50,
        "connectionsCurrent": 28,
        "slowQueriesRate": 0,
        "bufferPoolHitRate": 97.5
      }
    ]
  }
  ```

### 5. 新增 MySQL 实例
- **URL**: `POST /api/monitor/mysql`
- **功能**: 新增 MySQL 实例配置
- **认证**: JWT Token
- **请求体**:
  ```json
  {
    "instanceCode": "mysql_002",
    "instanceName": "MySQL 实例 2",
    "serverId": 1,
    "hospitalId": 1,
    "port": 3306,
    "username": "monitor_user",
    "password": "encrypted_password"
  }
  ```
- **响应示例**:
  ```json
  {
    "code": 200,
    "message": "success",
    "data": true
  }
  ```

### 6. 修改 MySQL 实例
- **URL**: `PUT /api/monitor/mysql`
- **功能**: 修改 MySQL 实例配置
- **认证**: JWT Token
- **请求体**:
  ```json
  {
    "id": 1,
    "instanceCode": "mysql_001",
    "instanceName": "MySQL 实例 1 (更新)",
    "serverId": 1,
    "hospitalId": 1,
    "port": 3306,
    "username": "monitor_user",
    "password": "new_encrypted_password",
    "status": 1
  }
  ```
- **响应示例**:
  ```json
  {
    "code": 200,
    "message": "success",
    "data": true
  }
  ```

### 7. 删除 MySQL 实例
- **URL**: `DELETE /api/monitor/mysql/{id}`
- **功能**: 删除 MySQL 实例配置
- **认证**: JWT Token
- **路径参数**:
  - `id`: 实例 ID
- **响应示例**:
  ```json
  {
    "code": 200,
    "message": "success",
    "data": true
  }
  ```

## 错误码说明
- 200: 成功
- 400: 参数错误
- 401: 认证失败
- 403: 权限不足
- 404: 资源不存在
- 500: 服务器内部错误

## 权限说明
- 超级管理员：可操作所有 MySQL 实例
- 医院管理员：仅可操作本医院的 MySQL 实例
- 普通用户：仅可查看权限范围内的 MySQL 实例

## 注意事项
1. 所有请求必须携带有效的 JWT Token
2. 密码等敏感信息需要加密传输
3. 历史数据查询时间范围不宜过大，建议不超过24小时
4. 监控数据按月份分区存储，跨月查询需要特殊处理