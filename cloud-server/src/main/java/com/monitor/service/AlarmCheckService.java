package com.monitor.service;

import com.monitor.dto.AlarmRuleDTO;
import com.monitor.dto.AlarmRecordDTO;
import com.monitor.entity.AlarmRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 告警检查服务 - 用于检查指标是否触发告警规则
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AlarmCheckService {

    private final AlarmRuleService alarmRuleService;
    private final AlarmRecordService alarmRecordService;
    private final AlarmNotificationService alarmNotificationService;

    /**
     * 检查服务器指标是否触发告警规则
     */
    public void checkServerMetric(Long serverId, String serverName, String metricKey, Number value) {
        try {
            // 获取所有启用的告警规则
            List<AlarmRuleDTO> rules = alarmRuleService.listEnabled();

            for (AlarmRuleDTO rule : rules) {
                // 只检查匹配的规则 (metricType: 1=服务器)
                if (rule.getMetricType() != 1 || !metricKey.equals(rule.getMetricKey())) {
                    continue;
                }

                // 检查是否触发告警
                if (evaluateRule(rule, value)) {
                    // 触发告警，创建告警记录并处理连续告警
                    handleContinuousAlarm(rule, serverId, serverName, metricKey, value);
                }
            }
        } catch (Exception e) {
            log.error("Check server metric alarm error, serverId={}, metricKey={}, value={}", serverId, metricKey, value, e);
        }
    }

    /**
     * 检查 MySQL 指标是否触发告警规则
     */
    public void checkMysqlMetric(Long instanceId, String instanceName, String metricKey, Number value) {
        try {
            List<AlarmRuleDTO> rules = alarmRuleService.listEnabled();

            for (AlarmRuleDTO rule : rules) {
                // 只检查匹配的规则 (metricType: 2=MySQL)
                if (rule.getMetricType() != 2 || !metricKey.equals(rule.getMetricKey())) {
                    continue;
                }

                if (evaluateRule(rule, value)) {
                    handleContinuousAlarm(rule, instanceId, instanceName, metricKey, value);
                }
            }
        } catch (Exception e) {
            log.error("Check mysql metric alarm error, instanceId={}, metricKey={}, value={}", instanceId, metricKey, value, e);
        }
    }

    /**
     * 检查 Tomcat 指标是否触发告警规则
     */
    public void checkTomcatMetric(Long instanceId, String instanceName, String metricKey, Number value) {
        try {
            List<AlarmRuleDTO> rules = alarmRuleService.listEnabled();

            for (AlarmRuleDTO rule : rules) {
                // 只检查匹配的规则 (metricType: 3=Tomcat)
                if (rule.getMetricType() != 3 || !metricKey.equals(rule.getMetricKey())) {
                    continue;
                }

                if (evaluateRule(rule, value)) {
                    handleContinuousAlarm(rule, instanceId, instanceName, metricKey, value);
                }
            }
        } catch (Exception e) {
            log.error("Check tomcat metric alarm error, instanceId={}, metricKey={}, value={}", instanceId, metricKey, value, e);
        }
    }

    /**
     * 处理连续告警逻辑
     *
     * @param rule 告警规则
     * @param instanceId 实例 ID
     * @param instanceName 实例名称
     * @param metricKey 指标键
     * @param value 当前值
     */
    private void handleContinuousAlarm(AlarmRuleDTO rule, Long instanceId, String instanceName, String metricKey, Number value) {
        // 生成连续告警分组标识：metricType_instanceId_metricKey_ruleId
        String groupKey = String.format("%d_%d_%s_%d", rule.getMetricType(), instanceId, metricKey, rule.getId());

        // 查询该分组下最新的未处理告警记录
        AlarmRecord lastAlarm = alarmRecordService.findLatestByGroupKey(groupKey);

        int continuousCount = 1;
        if (lastAlarm != null && lastAlarm.getContinuousCount() != null) {
            // 如果上次告警还未处理，累加连续次数
            continuousCount = lastAlarm.getContinuousCount() + 1;
        }

        // 创建告警记录
        createAlarmRecord(rule, instanceId, instanceName, metricKey, value, groupKey, continuousCount);

        // 检查是否达到连续告警阈值，需要发送通知
        Integer continuousThreshold = rule.getContinuousThreshold();
        if (continuousThreshold == null || continuousThreshold <= 0) {
            continuousThreshold = 3; // 默认 3 次
        }

        if (continuousCount >= continuousThreshold) {
            // 达到阈值，发送通知
            sendNotification(rule, instanceId, instanceName, metricKey, value, continuousCount);

            log.info("Continuous alarm threshold reached, notification sent. ruleId={}, groupKey={}, continuousCount={}, threshold={}",
                    rule.getId(), groupKey, continuousCount, continuousThreshold);
        }
    }

    /**
     * 发送告警通知（短信/微信）
     */
    private void sendNotification(AlarmRuleDTO rule, Long instanceId, String instanceName, String metricKey, Number value, int continuousCount) {
        // 获取管理员联系方式（从系统配置或用户表获取，这里简化处理）
        // TODO: 从系统配置中获取管理员手机号和微信 OpenID
        String adminPhone = "13800138000"; // 示例管理员手机号
        String adminOpenId = "oXXXX-openid-xxx"; // 示例微信 OpenID

        // 创建临时告警记录用于通知（实际记录已创建）
        AlarmRecord tempRecord = new AlarmRecord();
        tempRecord.setInstanceId(instanceId);
        tempRecord.setInstanceName(instanceName);
        tempRecord.setAlarmContent(String.format("告警规则 [%s] 被触发：%s %s %.2f，当前值：%.2f，连续告警 %d次",
                rule.getRuleName(), metricKey, rule.getOperator(),
                Double.parseDouble(rule.getThresholdValue()), value.doubleValue(), continuousCount));
        tempRecord.setCurrentValue(String.valueOf(value));
        tempRecord.setSeverity(rule.getSeverity());
        tempRecord.setContinuousCount(continuousCount);
        tempRecord.setAlarmTime(LocalDateTime.now());

        // 根据规则配置发送通知
        Integer notifyMethod = rule.getNotifyMethod();
        if (notifyMethod == null) {
            notifyMethod = 2; // 默认微信
        }

        if (notifyMethod == 1) {
            // 短信通知
            alarmNotificationService.sendSmsNotification(tempRecord, adminPhone);
        } else {
            // 微信通知
            alarmNotificationService.sendWechatNotification(tempRecord, adminOpenId);
        }
    }

    /**
     * 评估规则是否被触发
     *
     * @param rule 告警规则
     * @param value 当前值
     * @return true if 触发告警
     */
    private boolean evaluateRule(AlarmRuleDTO rule, Number value) {
        if (value == null) {
            return false;
        }

        double doubleValue = value.doubleValue();
        double threshold;
        try {
            threshold = Double.parseDouble(rule.getThresholdValue());
        } catch (NumberFormatException e) {
            log.warn("Invalid threshold value: {}", rule.getThresholdValue());
            return false;
        }
        String operator = rule.getOperator();

        switch (operator) {
            case ">":
                return doubleValue > threshold;
            case ">=":
                return doubleValue >= threshold;
            case "<":
                return doubleValue < threshold;
            case "<=":
                return doubleValue <= threshold;
            case "==":
                return doubleValue == threshold;
            case "!=":
                return doubleValue != threshold;
            default:
                log.warn("Unknown operator: {}", operator);
                return false;
        }
    }

    /**
     * 创建告警记录
     */
    private void createAlarmRecord(AlarmRuleDTO rule, Long instanceId, String instanceName, String metricKey, Number value) {
        createAlarmRecord(rule, instanceId, instanceName, metricKey, value, null, 1);
    }

    /**
     * 创建告警记录（支持连续告警）
     */
    private void createAlarmRecord(AlarmRuleDTO rule, Long instanceId, String instanceName, String metricKey, Number value, String groupKey, int continuousCount) {
        try {
            AlarmRecordDTO recordDTO = new AlarmRecordDTO();
            recordDTO.setRuleId(rule.getId());
            recordDTO.setMetricType(getMetricTypeCode(rule.getMetricType()));
            recordDTO.setInstanceId(instanceId);
            recordDTO.setInstanceName(instanceName);
            recordDTO.setGroupKey(groupKey);
            recordDTO.setContinuousCount(continuousCount);

            // 将阈值字符串转换为数字用于格式化
            double threshold;
            try {
                threshold = Double.parseDouble(rule.getThresholdValue());
            } catch (NumberFormatException e) {
                threshold = 0;
            }

            recordDTO.setAlarmContent(String.format("告警规则 [%s] 被触发：%s %s %.2f，当前值：%.2f",
                    rule.getRuleName(), metricKey, rule.getOperator(), threshold, value.doubleValue()));
            recordDTO.setCurrentValue(String.valueOf(value));
            recordDTO.setSeverity(rule.getSeverity());
            recordDTO.setStatus(0); // 未处理

            alarmRecordService.createRecord(recordDTO);
            log.info("Created alarm record: ruleId={}, instanceId={}, metricKey={}, value={}, continuousCount={}",
                    rule.getId(), instanceId, metricKey, value, continuousCount);
        } catch (Exception e) {
            log.error("Create alarm record error, ruleId={}, instanceId={}", rule.getId(), instanceId, e);
        }
    }

    /**
     * 将指标类型代码转换为对应的值（用于告警记录的 metricType 字段）
     */
    private Integer getMetricTypeCode(Integer metricType) {
        // 直接使用相同的值，因为实体类中metricType已经是1-服务器，2-MySQL，3-Tomcat
        return metricType;
    }
}
