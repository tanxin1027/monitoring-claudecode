package com.monitor.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * MySQL 实例实体
 */
@Data
@TableName("mysql_instance")
public class MysqlInstance implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String instanceCode;
    private String instanceName;
    private Long serverId;
    private Long hospitalId;
    private Integer port;
    private String dbVersion;
    private String username;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
