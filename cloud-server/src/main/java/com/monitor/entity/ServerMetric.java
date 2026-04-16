package com.monitor.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 服务器性能监控数据实体
 */
@Data
@TableName("server_metric_202604")
public class ServerMetric implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 服务器 ID
     */
    @TableField("server_id")
    private Long serverId;

    /**
     * CPU 使用率 (%)
     */
    @TableField("cpu_usage")
    private BigDecimal cpuUsage;

    /**
     * 1 分钟负载
     */
    @TableField("cpu_load_1m")
    private BigDecimal cpuLoad1m;

    /**
     * 5 分钟负载
     */
    @TableField("cpu_load_5m")
    private BigDecimal cpuLoad5m;

    /**
     * 15 分钟负载
     */
    @TableField("cpu_load_15m")
    private BigDecimal cpuLoad15m;

    /**
     * 已用内存 (MB)
     */
    @TableField("memory_used")
    private Long memoryUsed;

    /**
     * 总内存 (MB)
     */
    @TableField("memory_total")
    private Long memoryTotal;

    /**
     * 内存使用率 (%)
     */
    @TableField("memory_usage")
    private BigDecimal memoryUsage;

    /**
     * 磁盘数据 JSON
     */
    @TableField("disk_data")
    private String diskData;

    /**
     * 网络数据 JSON
     */
    @TableField("network_data")
    private String networkData;

    /**
     * 进程数
     */
    @TableField("process_count")
    private Integer processCount;

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
