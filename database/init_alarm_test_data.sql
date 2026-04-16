-- 告警规则测试数据
-- 用于测试告警功能

-- 清空现有数据
TRUNCATE TABLE alarm_rule;

-- 插入服务器告警规则
INSERT INTO alarm_rule (rule_name, metric_type, metric_key, operator, threshold_value, severity, notify_type, notify_target, status) VALUES
('CPU 使用率告警', 1, 'cpuUsage', '>', '90', 2, NULL, NULL, 1),
('内存使用率告警', 1, 'memoryUsage', '>', '85', 2, NULL, NULL, 1),
('磁盘使用率告警', 1, 'diskUsage', '>', '90', 3, NULL, NULL, 1),
('服务器宕机告警', 1, 'status', '=', '0', 1, NULL, NULL, 1);

-- 插入 MySQL 告警规则
INSERT INTO alarm_rule (rule_name, metric_type, metric_key, operator, threshold_value, severity, notify_type, notify_target, status) VALUES
('MySQL 连接数告警', 2, 'connections', '>', '500', 3, NULL, NULL, 1),
('MySQL 慢查询告警', 2, 'slowQueries', '>', '10', 2, NULL, NULL, 1),
('MySQL CPU 使用率告警', 2, 'cpuUsage', '>', '80', 2, NULL, NULL, 1);

-- 插入 Tomcat 告警规则
INSERT INTO alarm_rule (rule_name, metric_type, metric_key, operator, threshold_value, severity, notify_type, notify_target, status) VALUES
('Tomcat JVM 内存使用率告警', 3, 'jvmHeapUsage', '>', '85', 2, NULL, NULL, 1),
('Tomcat 线程池告警', 3, 'threadPoolUsage', '>', '90', 2, NULL, NULL, 1),
('Tomcat 请求响应时间告警', 3, 'avgRequestTime', '>', '1000', 2, NULL, NULL, 1);

-- 查询插入的规则
SELECT * FROM alarm_rule ORDER BY id;
