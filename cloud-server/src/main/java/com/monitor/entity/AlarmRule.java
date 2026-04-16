package com.monitor.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 告警规则实体
 */
@Data
@TableName("alarm_rule")
public class AlarmRule implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 规则名称
     */
    @TableField("rule_name")
    private String ruleName;

    /**
     * 指标类型 1-服务器 2-MySQL 3-Tomcat
     */
    @TableField("metric_type")
    private Integer metricType;

    /**
     * 指标键
     */
    @TableField("metric_key")
    private String metricKey;

    /**
     * 操作符 > < >= <= = between
     */
    @TableField("operator")
    private String operator;

    /**
     * 阈值
     */
    @TableField("threshold_value")
    private String thresholdValue;

    /**
     * 严重程度 1-提示 2-警告 3-严重
     */
    @TableField("severity")
    private Integer severity;

    /**
     * 通知方式 email,sms,webhook
     */
    @TableField("notify_type")
    private String notifyType;

    /**
     * 通知目标
     */
    @TableField("notify_target")
    private String notifyTarget;

    /**
     * 状态 0-禁用 1-启用
     */
    @TableField("status")
    private Integer status;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField("update_time")
    private LocalDateTime updateTime;
}
