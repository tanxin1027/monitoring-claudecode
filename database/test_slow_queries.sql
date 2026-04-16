-- ===========================================
-- MySQL 慢查询数据测试脚本
-- 用于验证慢查询数据是否可以正常获取
-- ===========================================

-- 1. 检查 Slow_queries 全局状态变量
SHOW GLOBAL STATUS LIKE 'Slow_queries';

-- 2. 检查 slow_query_log 是否开启
SHOW VARIABLES LIKE 'slow_query_log';

-- 3. 检查慢查询时间阈值
SHOW VARIABLES LIKE 'long_query_time';

-- 4. 检查 slow_log 表（如果有数据）
SELECT * FROM mysql.slow_log LIMIT 10;

-- 5. 检查 performance_schema 是否开启
SELECT @@performance_schema;

-- 6. 从 performance_schema 获取慢查询相关统计
SELECT
    SCHEMA_NAME,
    DIGEST_TEXT,
    COUNT_STAR,
    SUM_TIMER_WAIT,
    AVG_TIMER_WAIT,
    SUM_NO_GOOD_INDEX_USED,
    SUM_SELECT_FULL_JOIN
FROM performance_schema.events_statements_summary_by_digest
WHERE SUM_NO_GOOD_INDEX_USED > 0 OR SUM_SELECT_FULL_JOIN > 0
ORDER BY SUM_TIMER_WAIT DESC
LIMIT 10;

-- 7. 查看当前慢查询数量（累计值）
SELECT
    VARIABLE_NAME,
    VARIABLE_VALUE
FROM information_schema.GLOBAL_STATUS
WHERE VARIABLE_NAME = 'Slow_queries';

-- ===========================================
-- 说明：
-- 1. 如果 Slow_queries 有值，说明可以通过 SHOW STATUS 获取
-- 2. 如果 slow_query_log=ON，说明慢查询日志已开启
-- 3. 如果 performance_schema=1，可以使用 performance_schema 查询
-- ===========================================
