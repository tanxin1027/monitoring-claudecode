package com.monitor.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 告警规则 DTO
 */
@Data
public class AlarmRuleDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    /**
     * 规则名称
     */
    private String ruleName;

    /**
     * 指标类型 1-服务器 2-MySQL 3-Tomcat
     */
    private Integer metricType;

    /**
     * 指标键
     */
    private String metricKey;

    /**
     * 操作符
     */
    private String operator;

    /**
     * 阈值（字符串类型，支持数字和百分比）
     */
    private String thresholdValue;

    /**
     * 连续超标次数阈值，达到此次数后发送通知
     */
    private Integer continuousThreshold;

    /**
     * 通知方式 1-短信 2-微信
     */
    private Integer notifyMethod;

    /**
     * 严重程度 1-提示 2-警告 3-严重
     */
    private Integer severity;

    /**
     * 通知方式 email,sms,webhook
     */
    private String notifyType;

    /**
     * 通知目标
     */
    private String notifyTarget;

    /**
     * 状态 0-禁用 1-启用
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
