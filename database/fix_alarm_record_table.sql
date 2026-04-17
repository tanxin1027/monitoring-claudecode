-- 修复 alarm_record 表结构以匹配实体类
USE hospital_monitor;

-- 1. 添加缺失的字段（使用存储过程来避免重复添加错误）
DELIMITER $$

-- 添加 metric_type
CREATE PROCEDURE add_metric_type()
BEGIN
    DECLARE cnt INT;
    SELECT COUNT(*) INTO cnt FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = 'hospital_monitor' AND TABLE_NAME = 'alarm_record' AND COLUMN_NAME = 'metric_type';
    IF cnt = 0 THEN
        ALTER TABLE alarm_record ADD COLUMN metric_type TINYINT(4) COMMENT '指标类型 1-服务器 2-MySQL 3-Tomcat' AFTER rule_id;
    END IF;
END$$

-- 添加 instance_name
CREATE PROCEDURE add_instance_name()
BEGIN
    DECLARE cnt INT;
    SELECT COUNT(*) INTO cnt FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = 'hospital_monitor' AND TABLE_NAME = 'alarm_record' AND COLUMN_NAME = 'instance_name';
    IF cnt = 0 THEN
        ALTER TABLE alarm_record ADD COLUMN instance_name VARCHAR(255) COMMENT '实例名称' AFTER instance_id;
    END IF;
END$$

-- 添加 current_value
CREATE PROCEDURE add_current_value()
BEGIN
    DECLARE cnt INT;
    SELECT COUNT(*) INTO cnt FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = 'hospital_monitor' AND TABLE_NAME = 'alarm_record' AND COLUMN_NAME = 'current_value';
    IF cnt = 0 THEN
        ALTER TABLE alarm_record ADD COLUMN current_value VARCHAR(50) COMMENT '当前值' AFTER alarm_content;
    END IF;
END$$

-- 添加 handler
CREATE PROCEDURE add_handler()
BEGIN
    DECLARE cnt INT;
    SELECT COUNT(*) INTO cnt FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = 'hospital_monitor' AND TABLE_NAME = 'alarm_record' AND COLUMN_NAME = 'handler';
    IF cnt = 0 THEN
        ALTER TABLE alarm_record ADD COLUMN handler VARCHAR(50) COMMENT '处理人' AFTER status;
    END IF;
END IF;
END$$

-- 添加 alarm_time
CREATE PROCEDURE add_alarm_time()
BEGIN
    DECLARE cnt INT;
    SELECT COUNT(*) INTO cnt FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = 'hospital_monitor' AND TABLE_NAME = 'alarm_record' AND COLUMN_NAME = 'alarm_time';
    IF cnt = 0 THEN
        ALTER TABLE alarm_record ADD COLUMN alarm_time DATETIME COMMENT '告警时间' AFTER handle_time;
    END IF;
END IF;
END$$

CALL add_metric_type();
CALL add_instance_name();
CALL add_current_value();
CALL add_handler();
CALL add_alarm_time();

DROP PROCEDURE IF EXISTS add_metric_type;
DROP PROCEDURE IF EXISTS add_instance_name;
DROP PROCEDURE IF EXISTS add_current_value;
DROP PROCEDURE IF EXISTS add_handler;
DROP PROCEDURE IF EXISTS add_alarm_time;

DELIMITER $$

UPDATE alarm_record SET handler = CAST(handle_user AS CHAR) WHERE handle_user IS NOT NULL AND handler IS NULL;

$$

-- 3. 查看修改后的表结构
DESC alarm_record;
