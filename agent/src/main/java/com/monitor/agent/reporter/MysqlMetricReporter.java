package com.monitor.agent.reporter;

import com.monitor.agent.collector.MysqlCollector;
import com.monitor.agent.config.MysqlConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * MySQL 性能数据定时上报
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MysqlMetricReporter {

    private final MysqlCollector mysqlCollector;
    private final DataReporter dataReporter;
    private final MysqlConfig mysqlConfig;

    /**
     * 定时上报 MySQL 性能数据
     */
    @Scheduled(fixedDelayString = "${cloud.server.report-interval:30}000")
    public void report() {
        if (!mysqlConfig.isEnabled() || mysqlConfig.getInstances().isEmpty()) {
            log.debug("MySQL monitoring is disabled or no instances configured");
            return;
        }

        log.debug("Collecting MySQL metrics...");

        for (MysqlConfig.MysqlInstance instance : mysqlConfig.getInstances()) {
            try {
                Map<String, Object> metric = new HashMap<>();
                metric.put("instanceCode", instance.getInstanceCode());
                metric.put("instanceName", instance.getInstanceName());

                // 采集 MySQL 性能数据
                Map<String, Object> collected = mysqlCollector.collectMysqlMetric(
                    instance.getUrl(),
                    instance.getUsername(),
                    instance.getPassword()
                );
                metric.putAll(collected);

                if (collected.containsKey("error")) {
                    log.warn("Failed to collect MySQL metric for {}: {}",
                        instance.getInstanceCode(), collected.get("error"));
                    continue;
                }

                boolean success = dataReporter.reportMysqlMetric(metric);

                if (success) {
                    log.info("MySQL metric reported successfully for instance: {}",
                        instance.getInstanceCode());
                } else {
                    log.warn("Failed to report MySQL metric for instance: {}",
                        instance.getInstanceCode());
                }

            } catch (Exception e) {
                log.error("Error collecting MySQL metrics for instance: {}",
                    instance.getInstanceCode(), e);
            }
        }
    }
}
