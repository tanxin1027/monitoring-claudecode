package com.monitor.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * MySQL 性能数据传输对象
 */
@Data
public class MysqlMetricDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 实例编码
     */
    private String instanceCode;

    /**
     * 实例名称
     */
    private String instanceName;

    /**
     * 当前连接数
     */
    private Integer connectionsCurrent;

    /**
     * 最大连接数
     */
    private Integer connectionsMax;

    /**
     * 连接使用率（百分比）
     */
    private Double connectionsUsage;

    /**
     * QPS（每秒查询数）
     */
    private BigDecimal qps;

    /**
     * QPS 总数
     */
    private Long qpsTotal;

    /**
     * TPS（每秒事务数）
     */
    private BigDecimal tps;

    /**
     * TPS 总数
     */
    private Long tpsTotal;

    /**
     * 慢查询速率（每秒慢查询数）
     */
    private Long slowQueriesRate;

    /**
     * 慢查询总数（累积值）
     */
    private Long slowQueriesTotal;

    /**
     * 运行线程数
     */
    private Integer threadsRunning;

    /**
     * 已连接线程数
     */
    private Integer threadsConnected;

    /**
     * 已创建线程数
     */
    private Long threadsCreated;

    /**
     * 接收字节数
     */
    private Long bytesReceived;

    /**
     * 发送字节数
     */
    private Long bytesSent;

    /**
     * 缓冲池大小（字节）
     */
    private Long bufferPoolSize;

    /**
     * 缓冲池空闲页数
     */
    private Long bufferPoolFreePages;

    /**
     * 缓冲池脏页数
     */
    private Long bufferPoolDirtyPages;

    /**
     * 缓冲池读请求数
     */
    private Long bufferPoolReadRequests;

    /**
     * 缓冲池写入请求数
     */
    private Long bufferPoolWriteRequests;

    /**
     * 缓冲池物理读次数
     */
    private Long bufferPoolReads;

    /**
     * 缓冲池命中率（百分比）
     */
    private Double bufferPoolHitRate;

    /**
     * 行锁等待时间 (ms)
     */
    private Long innodbRowLockTime;

    /**
     * 行锁等待次数
     */
    private Long innodbRowLockWaits;

    /**
     * 当前行锁等待数
     */
    private Long innodbRowLockCurrentWaits;

    /**
     * 表锁等待次数
     */
    private Long tableLocksWaited;

    /**
     * 表锁立即获得次数
     */
    private Long tableLocksImmediate;

    /**
     * 表锁等待率（百分比）
     */
    private Double tableLockWaitRate;

    /**
     * 临时表创建总数
     */
    private Long tmpTablesCreated;

    /**
     * 磁盘临时表创建数
     */
    private Long tmpDiskTablesCreated;

    /**
     * 磁盘临时表比率（百分比）
     */
    private Double tmpDiskTableRate;

    /**
     * 排序行数
     */
    private Long sortRows;

    /**
     * 全表扫描排序次数
     */
    private Long sortScan;

    /**
     * 范围排序次数
     */
    private Long sortRange;

    /**
     * 排序合并通过次数
     */
    private Long sortMergePasses;

    /**
     * 从库 IO 线程运行状态
     */
    private Boolean slaveIoRunning;

    /**
     * 从库 SQL 线程运行状态
     */
    private Boolean slaveSqlRunning;

    /**
     * 落后主库的秒数
     */
    private Integer secondsBehindMaster;

    /**
     * 采集时间（秒级时间戳）
     */
    private Long collectTime;
}
