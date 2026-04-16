package com.monitor.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * Tomcat 性能数据传输对象
 */
@Data
public class TomcatMetricDTO implements Serializable {

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
     * 堆内存已用 (MB)
     */
    private Long jvmHeapUsed;

    /**
     * 堆内存最大 (MB)
     */
    private Long jvmHeapMax;

    /**
     * 非堆内存已用 (MB)
     */
    private Long jvmNonHeapUsed;

    /**
     * GC 次数
     */
    private Long gcCount;

    /**
     * GC 总时间 (ms)
     */
    private Long gcTime;

    /**
     * 线程数
     */
    private Integer threadCount;

    /**
     * 繁忙线程数
     */
    private Integer threadBusy;

    /**
     * 请求数
     */
    private Long requestCount;

    /**
     * 错误数
     */
    private Long errorCount;

    /**
     * 发送字节数
     */
    private Long bytesSent;

    /**
     * 接收字节数
     */
    private Long bytesReceived;

    /**
     * 最大处理时间 (ms)
     */
    private Long maxTime;

    /**
     * 运行时间 (ms)
     */
    private Long uptime;

    /**
     * 活跃会话数
     */
    private Integer sessionCount;

    /**
     * 过期会话数
     */
    private Integer sessionExpired;

    /**
     * 采集时间（秒级时间戳）
     */
    private Long collectTime;
}
