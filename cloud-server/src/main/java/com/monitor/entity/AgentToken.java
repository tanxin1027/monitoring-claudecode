package com.monitor.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Agent Token 实体
 */
@Data
@TableName("agent_token")
public class AgentToken implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * Token 值
     */
    private String token;

    /**
     * 关联服务器 ID
     */
    private Long serverId;

    /**
     * 服务器名称（非数据库字段）
     */
    @TableField(exist = false)
    private String serverName;

    /**
     * 备注
     */
    private String remark;

    /**
     * 状态 0-禁用 1-启用
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 最后使用时间
     */
    private LocalDateTime lastUsedTime;
}
