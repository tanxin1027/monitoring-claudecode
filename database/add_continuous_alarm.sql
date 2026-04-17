-- 为告警规则表添加连续告警配置字段
USE hospital_monitor;

-- 添加连续超标次数阈值字段
ALTER TABLE alarm_rule
ADD COLUMN continuous_threshold INT DEFAULT 3 COMMENT '连续超标次数阈值，达到此次数后发送通知' AFTER threshold_value;

-- 添加通知方法字段 1-短信 2-微信
ALTER TABLE alarm_rule
ADD COLUMN notify_method TINYINT DEFAULT 2 COMMENT '通知方式 1-短信 2-微信' AFTER notify_type;

-- 为告警记录表添加连续告警相关字段
-- 添加连续告警次数字段
ALTER TABLE alarm_record
ADD COLUMN continuous_count INT DEFAULT 1 COMMENT '当前连续告警次数' AFTER severity;

-- 添加分组标识字段 (用于关联同一实例和指标的连续告警)
ALTER TABLE alarm_record
ADD COLUMN group_key VARCHAR(100) DEFAULT NULL COMMENT '连续告警分组标识' AFTER continuous_count;

-- 添加索引优化分组查询
ALTER TABLE alarm_record
ADD INDEX idx_group_key_status (group_key, status);
