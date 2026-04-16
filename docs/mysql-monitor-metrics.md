# MySQL 监控指标说明

## 监控指标详解

### 1. 连接指标
- **connections_current** - 当前连接数
- **connections_max** - 最大连接数
- **connections_usage** - 连接使用率 (%)

### 2. 性能指标
- **qps** - 每秒查询数 (Queries Per Second)
- **qps_total** - 累计查询总数
- **tps** - 每秒事务数 (Transactions Per Second)
- **tps_total** - 累计事务总数

### 3. 慢查询指标
- **slow_queries_rate** - 每秒慢查询数
- **slow_queries_total** - 累计慢查询总数

### 4. 线程指标
- **threads_running** - 运行中的线程数
- **threads_connected** - 已连接线程数
- **threads_created** - 已创建线程数

### 5. 流量指标
- **bytes_received** - 接收字节数
- **bytes_sent** - 发送字节数

### 6. 缓冲池指标
- **buffer_pool_size** - 缓冲池大小 (字节)
- **buffer_pool_free_pages** - 缓冲池空闲页数
- **buffer_pool_dirty_pages** - 缓冲池脏页数
- **buffer_pool_read_requests** - 缓冲池读请求数
- **buffer_pool_write_requests** - 缓冲池写请求数
- **buffer_pool_reads** - 缓冲池物理读次数
- **buffer_pool_hit_rate** - 缓冲池命中率 (%)

### 7. 锁指标
- **innodb_row_lock_time** - 行锁等待时间 (ms)
- **innodb_row_lock_waits** - 行锁等待次数
- **innodb_row_lock_current_waits** - 当前行锁等待数
- **table_locks_waited** - 表锁等待次数
- **table_locks_immediate** - 表锁立即获得次数
- **table_lock_wait_rate** - 表锁等待率 (%)

### 8. 临时表指标
- **tmp_tables_created** - 临时表创建总数
- **tmp_disk_tables_created** - 磁盘临时表创建数
- **tmp_disk_table_rate** - 磁盘临时表比率 (%)

### 9. 排序指标
- **sort_rows** - 排序行数
- **sort_scan** - 全表扫描排序次数
- **sort_range** - 范围排序次数
- **sort_merge_passes** - 排序合并通过次数

### 10. 复制指标
- **slave_io_running** - 从库 IO 线程运行状态
- **slave_sql_running** - 从库 SQL 线程运行状态
- **seconds_behind_master** - 落后主库的秒数

## 重要阈值参考

### 性能指标阈值
- **缓冲池命中率 > 95%** - 理想状态
- **磁盘临时表比率 < 25%** - 避免过多临时表使用磁盘
- **表锁等待率 < 5%** - 避免表锁竞争
- **连接使用率 < 80%** - 预留连接资源

### 异常指标信号
- **慢查询数持续增长** - 可能存在慢 SQL
- **行锁等待次数较多** - 可能存在死锁风险
- **缓冲池命中率下降** - 可能内存不足
- **复制延迟超过 30 秒** - 主从同步延迟

## 数据处理逻辑

### 速率计算
- 对于累积计数器（如 Questions, Com_commit），采用前后差值除以时间间隔的方式计算速率
- 防止计数器重置（MySQL 重启）导致的负值计算
- 第一次采集时初始化基准值

### 版本兼容性
- MySQL 8.0+ 不再支持 `Slow_queries` 状态变量，系统会自动检测并适配
- 根据不同 MySQL 版本特性调整采集策略

### 故障处理
- 连接失败时记录错误日志
- 某些指标不可用时返回默认值（如 0）
- 持续监控连接状态，确保采集连续性

## 数据上报流程

1. **采集** - 通过 JDBC 连接 MySQL 执行状态查询
2. **计算** - 根据历史数据计算速率指标
3. **验证** - 检查数据合理性
4. **上报** - 通过 HTTP POST 发送到云端服务
5. **存储** - 云端验证并存入数据库