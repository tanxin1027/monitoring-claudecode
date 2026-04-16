-- MySQL 监控数据验证脚本
-- 用于验证 MySQL 监控采集是否正常工作

-- 1. 验证慢查询变量是否存在（MySQL 5.7+ 通常支持）
SELECT VARIABLE_NAME, VARIABLE_VALUE
FROM information_schema.GLOBAL_STATUS
WHERE VARIABLE_NAME = 'Slow_queries';

-- 2. 验证主要监控指标是否存在
SELECT VARIABLE_NAME, VARIABLE_VALUE
FROM information_schema.GLOBAL_STATUS
WHERE VARIABLE_NAME IN (
    'Questions',
    'Com_commit',
    'Com_rollback',
    'Threads_connected',
    'Threads_running',
    'Max_connections',
    'Bytes_received',
    'Bytes_sent',
    'Innodb_buffer_pool_size',
    'Innodb_buffer_pool_pages_free',
    'Innodb_buffer_pool_pages_dirty',
    'Innodb_buffer_pool_read_requests',
    'Innodb_buffer_pool_reads',
    'Innodb_row_lock_time',
    'Innodb_row_lock_waits',
    'Table_locks_waited',
    'Table_locks_immediate',
    'Created_tmp_tables',
    'Created_tmp_disk_tables',
    'Sort_rows',
    'Sort_scan',
    'Sort_range',
    'Sort_merge_passes'
);

-- 3. 检查 MySQL 版本
SELECT VERSION() AS mysql_version;

-- 4. 验证性能模式是否启用
SELECT @@performance_schema AS performance_schema_enabled;

-- 5. 查看当前会话的连接 ID（用于调试）
SELECT CONNECTION_ID() AS current_connection_id;

-- 6. 检查当前数据库
SELECT DATABASE() AS current_database;

-- 7. 验证是否有复制配置（如果适用）
SHOW SLAVE STATUS\G;

-- 8. 检查 MySQL 运行时间
SELECT
    VARIABLE_VALUE AS uptime_seconds
FROM information_schema.GLOBAL_STATUS
WHERE VARIABLE_NAME = 'Uptime';

-- 9. 检查并发连接数
SELECT
    VARIABLE_VALUE AS max_used_connections
FROM information_schema.GLOBAL_STATUS
WHERE VARIABLE_NAME = 'Max_used_connections';

-- 10. 检查连接错误
SELECT
    VARIABLE_VALUE AS aborted_connects
FROM information_schema.GLOBAL_STATUS
WHERE VARIABLE_NAME = 'Aborted_connects';

-- 结果解释：
-- - Slow_queries 应该返回一个数字值（即使为0）
-- - 主要监控指标应该都有对应的值
-- - 如果某个指标不存在，程序应能处理这种情况
-- - 版本信息有助于确定兼容性
-- - 如果没有从库配置，SHOW SLAVE STATUS 会返回空结果，这是正常的