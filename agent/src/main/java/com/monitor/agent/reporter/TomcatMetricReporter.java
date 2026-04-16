package com.monitor.agent.reporter;

import com.monitor.agent.collector.TomcatCollector;
import com.monitor.agent.config.TomcatConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Tomcat 性能数据定时上报
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TomcatMetricReporter {

    private final TomcatCollector tomcatCollector;
    private final DataReporter dataReporter;
    private final TomcatConfig tomcatConfig;

    /**
     * 定时上报 Tomcat 性能数据
     */
    @Scheduled(fixedDelayString = "${cloud.server.report-interval:30}000")
    public void report() {
        if (!tomcatConfig.isEnabled() || tomcatConfig.getInstances().isEmpty()) {
            log.debug("Tomcat monitoring is disabled or no instances configured");
            return;
        }

        log.debug("Collecting Tomcat metrics...");

        for (TomcatConfig.TomcatInstance instance : tomcatConfig.getInstances()) {
            try {
                Map<String, Object> metric = new HashMap<>();
                metric.put("instanceCode", instance.getInstanceCode());
                metric.put("instanceName", instance.getInstanceName());

                // 采集 Tomcat 性能数据
                Map<String, Object> collected = tomcatCollector.collectTomcatMetric(
                    instance.getHost(),
                    instance.getJmxPort()
                );
                metric.putAll(collected);

                if (collected.containsKey("error")) {
                    log.warn("Failed to collect Tomcat metric for {}: {}",
                        instance.getInstanceCode(), collected.get("error"));
                    continue;
                }

                boolean success = dataReporter.reportTomcatMetric(metric);

                if (success) {
                    log.info("Tomcat metric reported successfully for instance: {}",
                        instance.getInstanceCode());
                } else {
                    log.warn("Failed to report Tomcat metric for instance: {}",
                        instance.getInstanceCode());
                }

            } catch (Exception e) {
                log.error("Error collecting Tomcat metrics for instance: {}",
                    instance.getInstanceCode(), e);
            }
        }
    }
}
