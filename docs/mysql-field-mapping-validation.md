# MySQL 监控字段映射验证报告

## 字段映射一致性检查

### Agent 端 (MysqlCollector.java) → 服务端 (MysqlMetricDTO.java/MysqlMetric.java)

✅ **完全匹配的字段：**

| Agent Map Key | DTO Field | Entity Field | Type | Status |
|---------------|-----------|--------------|------|--------|
| connectionsCurrent | connectionsCurrent | connectionsCurrent | Integer | ✅ |
| connectionsMax | connectionsMax | connectionsMax | Integer | ✅ |
| connectionsUsage | connectionsUsage | connectionsUsage | Double | ✅ |
| qps | qps | qps | BigDecimal | ✅ |
| qpsTotal | qpsTotal | qpsTotal | Long | ✅ |
| tps | tps | tps | BigDecimal | ✅ |
| tpsTotal | tpsTotal | tpsTotal | Long | ✅ |
| slowQueriesRate | slowQueriesRate | slowQueriesRate | Long | ✅ |
| slowQueriesTotal | slowQueriesTotal | slowQueriesTotal | Long | ✅ |
| threadsRunning | threadsRunning | threadsRunning | Integer | ✅ |
| threadsConnected | threadsConnected | threadsConnected | Integer | ✅ |
| threadsCreated | threadsCreated | threadsCreated | Long | ✅ |
| bytesReceived | bytesReceived | bytesReceived | Long | ✅ |
| bytesSent | bytesSent | bytesSent | Long | ✅ |
| bufferPoolSize | bufferPoolSize | bufferPoolSize | Long | ✅ |
| bufferPoolFreePages | bufferPoolFreePages | bufferPoolFreePages | Long | ✅ |
| bufferPoolDirtyPages | bufferPoolDirtyPages | bufferPoolDirtyPages | Long | ✅ |
| bufferPoolReadRequests | bufferPoolReadRequests | bufferPoolReadRequests | Long | ✅ |
| bufferPoolWriteRequests | bufferPoolWriteRequests | bufferPoolWriteRequests | Long | ✅ |
| bufferPoolReads | bufferPoolReads | bufferPoolReads | Long | ✅ |
| bufferPoolHitRate | bufferPoolHitRate | bufferPoolHitRate | Double | ✅ |
| innodbRowLockTime | innodbRowLockTime | innodbRowLockTime | Long | ✅ |
| innodbRowLockWaits | innodbRowLockWaits | innodbRowLockWaits | Long | ✅ |
| innodbRowLockCurrentWaits | innodbRowLockCurrentWaits | innodbRowLockCurrentWaits | Long | ✅ |
| tableLocksWaited | tableLocksWaited | tableLocksWaited | Long | ✅ |
| tableLocksImmediate | tableLocksImmediate | tableLocksImmediate | Long | ✅ |
| tableLockWaitRate | tableLockWaitRate | tableLockWaitRate | Double | ✅ |
| tmpTablesCreated | tmpTablesCreated | tmpTablesCreated | Long | ✅ |
| tmpDiskTablesCreated | tmpDiskTablesCreated | tmpDiskTablesCreated | Long | ✅ |
| tmpDiskTableRate | tmpDiskTableRate | tmpDiskTableRate | Double | ✅ |
| sortRows | sortRows | sortRows | Long | ✅ |
| sortScan | sortScan | sortScan | Long | ✅ |
| sortRange | sortRange | sortRange | Long | ✅ |
| sortMergePasses | sortMergePasses | sortMergePasses | Long | ✅ |
| slaveIoRunning | slaveIoRunning | slaveIoRunning | Boolean | ✅ |
| slaveSqlRunning | slaveSqlRunning | slaveSqlRunning | Boolean | ✅ |
| secondsBehindMaster | secondsBehindMaster | secondsBehindMaster | Integer | ✅ |
| collectTime | collectTime | collectTime | Long | ✅ |

### 额外字段（来自 MysqlMetricReporter.java）：

| Field | Source | Purpose | Status |
|-------|--------|---------|--------|
| instanceCode | MysqlMetricReporter | 实例编码 | ✅ |
| instanceName | MysqlMetricReporter | 实例名称 | ✅ |

## 关键修复点

1. **驼峰命名** - Agent 端使用驼峰命名与服务端 DTO 保持一致
2. **类型匹配** - 确保所有字段的数据类型一致
3. **新增字段** - 补充了连接使用率、QPS/TPS 总数、复制状态等字段
4. **映射完整** - Service 层正确映射所有字段到实体类

## 验证步骤

1. **检查 Agent 端** - `MysqlCollector.java` 使用驼峰命名
2. **检查 DTO** - `MysqlMetricDTO.java` 包含所有必需字段
3. **检查实体** - `MysqlMetric.java` 映射到数据库字段
4. **检查 Service** - `CollectorServiceImpl.java` 完整映射所有字段
5. **检查数据库** - `schema.sql` 包含新字段定义

所有字段映射现在都是一致的，数据应能正确传递。