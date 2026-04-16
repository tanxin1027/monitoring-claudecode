-- 测试 Slow_queries 变量是否存在
SELECT VARIABLE_NAME, VARIABLE_VALUE
FROM information_schema.GLOBAL_STATUS
WHERE VARIABLE_NAME = 'Slow_queries';

-- 如果上面没有返回任何行，则说明变量不存在
-- 另外检查一下 MySQL 版本
SELECT VERSION() AS mysql_version;

-- 检查是否有慢查询日志相关配置
SHOW VARIABLES LIKE '%slow%';

-- 尝试其他方式获取可能的慢查询指标
SHOW GLOBAL STATUS LIKE '%slow%';
SHOW GLOBAL STATUS LIKE '%query%';