package com.monitor.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 告警记录 DTO
 */
@Data
public class AlarmRecordDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    /**
     * 规则 ID
     */
    private Long ruleId;

    /**
     * 指标类型 1-服务器 2-MySQL 3-Tomcat
     */
    private Integer metricType;

    /**
     * 实例 ID
     */
    private Long instanceId;

    /**
     * 实例名称
     */
    private String instanceName;

    /**
     * 告警内容
     */
    private String alarmContent;

    /**
     * 当前值
     */
    private String currentValue;

    /**
     * 严重程度 1-提示 2-警告 3-严重
     */
    private Integer severity;

    /**
     * 状态 0-未处理 1-已处理 2-已忽略
     */
    private Integer status;

    /**
     * 处理人
     */
    private String handler;

    /**
     * 处理备注
     */
    private String handleRemark;

    /**
     * 处理时间
     */
    private LocalDateTime handleTime;

    /**
     * 告警时间
     */
    private LocalDateTime alarmTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
