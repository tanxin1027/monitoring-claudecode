package com.monitor.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 告警记录实体
 */
@Data
@TableName("alarm_record")
public class AlarmRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 规则 ID
     */
    @TableField("rule_id")
    private Long ruleId;

    /**
     * 指标类型 1-服务器 2-MySQL 3-Tomcat
     */
    @TableField("metric_type")
    private Integer metricType;

    /**
     * 实例 ID（服务器 ID/MySQL 实例 ID/Tomcat 实例 ID）
     */
    @TableField("instance_id")
    private Long instanceId;

    /**
     * 实例名称
     */
    @TableField("instance_name")
    private String instanceName;

    /**
     * 告警内容
     */
    @TableField("alarm_content")
    private String alarmContent;

    /**
     * 当前值
     */
    @TableField("current_value")
    private String currentValue;

    /**
     * 严重程度 1-提示 2-警告 3-严重
     */
    @TableField("severity")
    private Integer severity;

    /**
     * 状态 0-未处理 1-已处理 2-已忽略
     */
    @TableField("status")
    private Integer status;

    /**
     * 处理人
     */
    @TableField("handler")
    private String handler;

    /**
     * 处理备注
     */
    @TableField("handle_remark")
    private String handleRemark;

    /**
     * 处理时间
     */
    @TableField("handle_time")
    private LocalDateTime handleTime;

    /**
     * 告警时间
     */
    @TableField("alarm_time")
    private LocalDateTime alarmTime;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;
}
