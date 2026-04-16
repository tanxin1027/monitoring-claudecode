package com.monitor.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * MySQL 性能监控数据实体
 */
@Data
@TableName("mysql_metric_202604")
public class MysqlMetric implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 实例 ID
     */
    @TableField("instance_id")
    private Long instanceId;

    /**
     * 当前连接数
     */
    @TableField("connections_current")
    private Integer connectionsCurrent;

    /**
     * 最大连接数
     */
    @TableField("connections_max")
    private Integer connectionsMax;

    /**
     * 连接使用率（百分比）
     */
    @TableField("connections_usage")
    private Double connectionsUsage;

    /**
     * QPS
     */
    @TableField("qps")
    private BigDecimal qps;

    /**
     * QPS 总数
     */
    @TableField("qps_total")
    private Long qpsTotal;

    /**
     * TPS
     */
    @TableField("tps")
    private BigDecimal tps;

    /**
     * TPS 总数
     */
    @TableField("tps_total")
    private Long tpsTotal;

    /**
     * 慢查询速率（每秒）
     */
    @TableField("slow_queries_rate")
    private Long slowQueriesRate;

    /**
     * 慢查询总数
     */
    @TableField("slow_queries_total")
    private Long slowQueriesTotal;

    /**
     * 运行线程数
     */
    @TableField("threads_running")
    private Integer threadsRunning;

    /**
     * 已连接线程数
     */
    @TableField("threads_connected")
    private Integer threadsConnected;

    /**
     * 已创建线程数
     */
    @TableField("threads_created")
    private Long threadsCreated;

    /**
     * 接收字节数
     */
    @TableField("bytes_received")
    private Long bytesReceived;

    /**
     * 发送字节数
     */
    @TableField("bytes_sent")
    private Long bytesSent;

    /**
     * 缓冲池大小
     */
    @TableField("buffer_pool_size")
    private Long bufferPoolSize;

    /**
     * 缓冲池空闲页数
     */
    @TableField("buffer_pool_free_pages")
    private Long bufferPoolFreePages;

    /**
     * 缓冲池脏页数
     */
    @TableField("buffer_pool_dirty_pages")
    private Long bufferPoolDirtyPages;

    /**
     * 缓冲池读请求数
     */
    @TableField("buffer_pool_read_requests")
    private Long bufferPoolReadRequests;

    /**
     * 缓冲池写入请求数
     */
    @TableField("buffer_pool_write_requests")
    private Long bufferPoolWriteRequests;

    /**
     * 缓冲池物理读次数
     */
    @TableField("buffer_pool_reads")
    private Long bufferPoolReads;

    /**
     * 缓冲池命中率
     */
    @TableField("buffer_pool_hit_rate")
    private Double bufferPoolHitRate;

    /**
     * 行锁等待时间 (ms)
     */
    @TableField("innodb_row_lock_time")
    private Long innodbRowLockTime;

    /**
     * 行锁等待次数
     */
    @TableField("innodb_row_lock_waits")
    private Long innodbRowLockWaits;

    /**
     * 当前行锁等待数
     */
    @TableField("innodb_row_lock_current_waits")
    private Long innodbRowLockCurrentWaits;

    /**
     * 表锁等待次数
     */
    @TableField("table_locks_waited")
    private Long tableLocksWaited;

    /**
     * 表锁立即获得次数
     */
    @TableField("table_locks_immediate")
    private Long tableLocksImmediate;

    /**
     * 表锁等待率
     */
    @TableField("table_lock_wait_rate")
    private Double tableLockWaitRate;

    /**
     * 临时表创建总数
     */
    @TableField("tmp_tables_created")
    private Long tmpTablesCreated;

    /**
     * 磁盘临时表创建数
     */
    @TableField("tmp_disk_tables_created")
    private Long tmpDiskTablesCreated;

    /**
     * 磁盘临时表比率
     */
    @TableField("tmp_disk_table_rate")
    private Double tmpDiskTableRate;

    /**
     * 排序行数
     */
    @TableField("sort_rows")
    private Long sortRows;

    /**
     * 全表扫描排序次数
     */
    @TableField("sort_scan")
    private Long sortScan;

    /**
     * 范围排序次数
     */
    @TableField("sort_range")
    private Long sortRange;

    /**
     * 排序合并通过次数
     */
    @TableField("sort_merge_passes")
    private Long sortMergePasses;

    /**
     * 从库 IO 线程运行状态
     */
    @TableField("slave_io_running")
    private Boolean slaveIoRunning;

    /**
     * 从库 SQL 线程运行状态
     */
    @TableField("slave_sql_running")
    private Boolean slaveSqlRunning;

    /**
     * 落后主库的秒数
     */
    @TableField("seconds_behind_master")
    private Integer secondsBehindMaster;

    /**
     * 采集时间
     */
    @TableField("collect_time")
    private LocalDateTime collectTime;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;
}
