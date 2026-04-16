package com.monitor.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Tomcat 性能监控数据实体
 */
@Data
@TableName("tomcat_metric_202604")
public class TomcatMetric implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 实例 ID
     */
    @TableField("instance_id")
    private Long instanceId;

    /**
     * 堆内存已用 (MB)
     */
    @TableField("jvm_heap_used")
    private Long jvmHeapUsed;

    /**
     * 堆内存最大 (MB)
     */
    @TableField("jvm_heap_max")
    private Long jvmHeapMax;

    /**
     * 非堆内存已用 (MB)
     */
    @TableField("jvm_non_heap_used")
    private Long jvmNonHeapUsed;

    /**
     * GC 次数
     */
    @TableField("gc_count")
    private Long gcCount;

    /**
     * GC 总时间 (ms)
     */
    @TableField("gc_time")
    private Long gcTime;

    /**
     * 线程数
     */
    @TableField("thread_count")
    private Integer threadCount;

    /**
     * 繁忙线程数
     */
    @TableField("thread_busy")
    private Integer threadBusy;

    /**
     * 请求数
     */
    @TableField("request_count")
    private Long requestCount;

    /**
     * 错误数
     */
    @TableField("error_count")
    private Long errorCount;

    /**
     * 发送字节数
     */
    @TableField("bytes_sent")
    private Long bytesSent;

    /**
     * 接收字节数
     */
    @TableField("bytes_received")
    private Long bytesReceived;

    /**
     * 最大处理时间 (ms)
     */
    @TableField("max_time")
    private Long maxTime;

    /**
     * 运行时间 (ms)
     */
    @TableField("uptime")
    private Long uptime;

    /**
     * 活跃会话数
     */
    @TableField("session_count")
    private Integer sessionCount;

    /**
     * 过期会话数
     */
    @TableField("session_expired")
    private Integer sessionExpired;

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
