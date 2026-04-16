package com.monitor.dto;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 服务器性能数据采集 DTO
 */
@Data
public class ServerMetricDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 服务器编码（标识唯一服务器）
     */
    private String serverCode;

    /**
     * Agent 版本
     */
    private String agentVersion;

    /**
     * CPU 使用率
     */
    private BigDecimal cpuUsage;

    /**
     * CPU 1 分钟负载
     */
    private BigDecimal cpuLoad1m;

    /**
     * CPU 5 分钟负载
     */
    private BigDecimal cpuLoad5m;

    /**
     * CPU 15 分钟负载
     */
    private BigDecimal cpuLoad15m;

    /**
     * 内存使用量 (MB)
     */
    private Long memoryUsed;

    /**
     * 总内存 (MB)
     */
    private Long memoryTotal;

    /**
     * 内存使用率
     */
    private BigDecimal memoryUsage;

    /**
     * 磁盘数据
     */
    private List<DiskInfo> disks;

    /**
     * 网络数据
     */
    private List<NetInfo> networks;

    /**
     * 进程数
     */
    private Integer processCount;

    /**
     * 采集时间
     */
    private Long collectTime;

    /**
     * 磁盘信息
     */
    @Data
    public static class DiskInfo implements Serializable {
        private String path;
        private Long used;
        private Long total;
        private BigDecimal usage;
    }

    /**
     * 网络信息
     */
    @Data
    public static class NetInfo implements Serializable {
        private String iface;
        private Long rxBytes;
        private Long txBytes;
    }
}
