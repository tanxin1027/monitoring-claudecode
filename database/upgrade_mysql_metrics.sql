-- ===========================================
-- MySQL 监控表结构升级脚本
-- 执行时间：2026-04-15
-- 说明：增加新的监控指标字段
-- ===========================================

USE hospital_monitor;

-- 1. 重命名现有字段
ALTER TABLE mysql_metric_202604
CHANGE COLUMN slow_queries slow_queries_rate BIGINT COMMENT '慢查询速率（每秒）';

-- 2. 添加新字段
ALTER TABLE mysql_metric_202604
ADD COLUMN slow_queries_total BIGINT COMMENT '慢查询总数' AFTER slow_queries_rate,
ADD COLUMN buffer_pool_free_pages BIGINT COMMENT '缓冲池空闲页数' AFTER buffer_pool_size,
ADD COLUMN buffer_pool_dirty_pages BIGINT COMMENT '缓冲池脏页数' AFTER buffer_pool_free_pages,
ADD COLUMN buffer_pool_read_requests BIGINT COMMENT '缓冲池读请求数' AFTER buffer_pool_dirty_pages,
ADD COLUMN buffer_pool_write_requests BIGINT COMMENT '缓冲池写入请求数' AFTER buffer_pool_read_requests,
ADD COLUMN buffer_pool_reads BIGINT COMMENT '缓冲池物理读次数' AFTER buffer_pool_write_requests,
ADD COLUMN buffer_pool_hit_rate DECIMAL(5,2) COMMENT '缓冲池命中率（%）' AFTER buffer_pool_reads,
ADD COLUMN innodb_row_lock_waits BIGINT COMMENT '行锁等待次数' AFTER innodb_row_lock_time,
ADD COLUMN innodb_row_lock_current_waits BIGINT COMMENT '当前行锁等待数' AFTER innodb_row_lock_waits,
ADD COLUMN table_locks_immediate BIGINT COMMENT '表锁立即获得次数' AFTER table_locks_waited,
ADD COLUMN table_lock_wait_rate DECIMAL(5,2) COMMENT '表锁等待率（%）' AFTER table_locks_immediate,
ADD COLUMN tmp_tables_created BIGINT COMMENT '临时表创建总数' AFTER table_lock_wait_rate,
ADD COLUMN tmp_disk_tables_created BIGINT COMMENT '磁盘临时表创建数' AFTER tmp_tables_created,
ADD COLUMN tmp_disk_table_rate DECIMAL(5,2) COMMENT '磁盘临时表比率（%）' AFTER tmp_disk_tables_created,
ADD COLUMN sort_rows BIGINT COMMENT '排序行数' AFTER tmp_disk_table_rate,
ADD COLUMN sort_scan BIGINT COMMENT '全表扫描排序次数' AFTER sort_rows,
ADD COLUMN sort_range BIGINT COMMENT '范围排序次数' AFTER sort_scan,
ADD COLUMN sort_merge_passes BIGINT COMMENT '排序合并通过次数' AFTER sort_range;

-- 3. 删除不再使用的字段
ALTER TABLE mysql_metric_202604
DROP COLUMN buffer_pool_free,
DROP COLUMN db_data;

-- ===========================================
-- 说明：
-- 1. slow_queries 重命名为 slow_queries_rate（速率）
-- 2. 新增 slow_queries_total（累积值）
-- 3. 新增缓冲池相关指标（命中率、脏页等）
-- 4. 新增 InnoDB 行锁详细指标
-- 5. 新增表锁等待率
-- 6. 新增临时表统计
-- 7. 新增排序统计
-- ===========================================
