package com.monitor.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 服务器信息实体
 */
@Data
@TableName("server_info")
public class ServerInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 服务器 ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 服务器编码
     */
    private String serverCode;

    /**
     * 服务器名称
     */
    private String serverName;

    /**
     * 所属医院 ID
     */
    private Long hospitalId;

    /**
     * IP 地址
     */
    private String ipAddress;

    /**
     * 服务器类型 1-物理机 2-虚拟机 3-容器
     */
    private Integer serverType;

    /**
     * 操作系统类型
     */
    private String osType;

    /**
     * 操作系统版本
     */
    private String osVersion;

    /**
     * CPU 核心数
     */
    private Integer cpuCores;

    /**
     * 总内存 (MB)
     */
    private Long memoryTotal;

    /**
     * 总磁盘 (GB)
     */
    private Long diskTotal;

    /**
     * Agent 版本
     */
    private String agentVersion;

    /**
     * 最后心跳时间
     */
    private LocalDateTime lastHeartbeatTime;

    /**
     * 状态 0-离线 1-在线 2-禁用
     */
    private Integer status;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
