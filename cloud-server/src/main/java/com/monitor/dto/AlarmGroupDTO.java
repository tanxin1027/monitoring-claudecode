package com.monitor.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 告警分组 DTO（用于前端分组展示）
 */
@Data
public class AlarmGroupDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 分组标识
     */
    private String groupKey;

    /**
     * 规则 ID
     */
    private Long ruleId;

    /**
     * 规则名称
     */
    private String ruleName;

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
     * 指标键
     */
    private String metricKey;

    /**
     * 当前连续告警次数
     */
    private Integer continuousCount;

    /**
     * 最新告警值
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
     * 首次告警时间
     */
    private LocalDateTime firstAlarmTime;

    /**
     * 最近告警时间
     */
    private LocalDateTime lastAlarmTime;

    /**
     * 告警记录列表（展开详情用）
     */
    private List<AlarmRecordDTO> records;

    /**
     * 通知方式 1-短信 2-微信
     */
    private Integer notifyMethod;

    /**
     * 通知状态 0-未通知 1-已通知
     */
    private Integer notifyStatus;
}
