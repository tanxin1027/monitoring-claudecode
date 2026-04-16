package com.monitor.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Tomcat 实例实体
 */
@Data
@TableName("tomcat_instance")
public class TomcatInstance implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String instanceCode;
    private String instanceName;
    private Long serverId;
    private Long hospitalId;
    private String installPath;
    private Integer httpPort;
    private Integer shutdownPort;
    private Integer jmxPort;
    private String tomcatVersion;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
