-- ===========================================
-- Tomcat 监控测试数据生成脚本
-- 用于前端开发和测试
-- ===========================================

USE hospital_monitor;

-- 检查并创建 Tomcat 实例（如果不存在）
INSERT INTO tomcat_instance (instance_code, instance_name, server_id, hospital_id, http_port, jmx_port, status)
SELECT 'tomcat_001', '本地 Tomcat 实例', 1, 1, 8080, 9999, 1
WHERE NOT EXISTS (SELECT 1 FROM tomcat_instance WHERE instance_code = 'tomcat_001');

-- 生成最近 24 小时的 Tomcat 监控数据
DELIMITER $$

CREATE PROCEDURE generate_tomcat_test_data()
BEGIN
    DECLARE i INT DEFAULT 0;
    DECLARE current_time DATETIME;
    DECLARE jvm_used BIGINT;
    DECLARE jvm_max BIGINT;
    DECLARE gc_count BIGINT;
    DECLARE thread_count INT;
    DECLARE thread_busy INT;
    DECLARE request_count BIGINT;
    DECLARE error_count BIGINT;

    -- 从 24 小时前开始
    SET current_time = DATE_SUB(NOW(), INTERVAL 24 HOUR);

    -- 基础值
    SET jvm_max = 2048;
    SET gc_count = 1000;
    SET request_count = 50000;
    SET error_count = 50;

    WHILE i < 144 DO
        -- 生成随机但合理的数据
        SET jvm_used = FLOOR(800 + RAND() * 600); -- 800-1400 MB
        SET gc_count = gc_count + FLOOR(1 + RAND() * 5);
        SET thread_count = FLOOR(50 + RAND() * 50); -- 50-100
        SET thread_busy = FLOOR(thread_count * (0.2 + RAND() * 0.4)); -- 20%-60% 使用率
        SET request_count = request_count + FLOOR(100 + RAND() * 500);
        SET error_count = error_count + FLOOR(RAND() * 2);

        INSERT INTO tomcat_metric_202604 (
            instance_id, jvm_heap_used, jvm_heap_max, jvm_non_heap_used,
            gc_count, gc_time, thread_count, thread_busy,
            request_count, error_count, bytes_sent, bytes_received,
            max_time, uptime, session_count, session_expired,
            collect_time
        ) VALUES (
            1, -- instance_id
            jvm_used,
            jvm_max,
            FLOOR(256 + RAND() * 256), -- non heap 256-512 MB
            gc_count,
            gc_count * 10, -- GC time proportional to count
            thread_count,
            thread_busy,
            request_count,
            error_count,
            FLOOR(request_count * 1024), -- bytes sent
            FLOOR(request_count * 512), -- bytes received
            FLOOR(50 + RAND() * 200), -- max time 50-250ms
            i * 600000 + 3600000, -- uptime increases over time
            FLOOR(100 + RAND() * 200), -- session count 100-300
            FLOOR(10 + RAND() * 20), -- session expired 10-30
            current_time
        );

        SET i = i + 1;
        SET current_time = DATE_ADD(current_time, INTERVAL 10 MINUTE);
    END WHILE;
END$$

DELIMITER ;

-- 执行存储过程生成数据
CALL generate_tomcat_test_data();

-- 删除存储过程
DROP PROCEDURE IF EXISTS generate_tomcat_test_data;

-- 验证数据
SELECT 'Generated Tomcat test data' AS status, COUNT(*) AS record_count
FROM tomcat_metric_202604 WHERE instance_id = 1;
