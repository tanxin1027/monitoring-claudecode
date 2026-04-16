package com.monitor.service;

import com.monitor.dto.AlarmRuleDTO;
import com.monitor.dto.AlarmRecordDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
                    // 触发告警，创建告警记录
                    createAlarmRecord(rule, serverId, serverName, metricKey, value);
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
                    createAlarmRecord(rule, instanceId, instanceName, metricKey, value);
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
                    createAlarmRecord(rule, instanceId, instanceName, metricKey, value);
                }
            }
        } catch (Exception e) {
            log.error("Check tomcat metric alarm error, instanceId={}, metricKey={}, value={}", instanceId, metricKey, value, e);
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
        try {
            AlarmRecordDTO recordDTO = new AlarmRecordDTO();
            recordDTO.setRuleId(rule.getId());
            recordDTO.setMetricType(getMetricTypeCode(rule.getMetricType()));
            recordDTO.setInstanceId(instanceId);
            recordDTO.setInstanceName(instanceName);
            recordDTO.setAlarmContent(String.format("告警规则 [%s] 被触发：%s %s %.2f，当前值：%.2f",
                    rule.getRuleName(), metricKey, rule.getOperator(), rule.getThresholdValue(), value.doubleValue()));
            recordDTO.setCurrentValue(String.valueOf(value));
            recordDTO.setSeverity(rule.getSeverity());
            recordDTO.setStatus(0); // 未处理

            alarmRecordService.createRecord(recordDTO);
            log.info("Created alarm record: ruleId={}, instanceId={}, metricKey={}, value={}",
                    rule.getId(), instanceId, metricKey, value);
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
