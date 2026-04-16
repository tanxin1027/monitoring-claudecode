package com.monitor.agent.reporter;

import com.monitor.agent.collector.SystemCollector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 服务器性能数据定时上报
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ServerMetricReporter {

    private final SystemCollector systemCollector;
    private final DataReporter dataReporter;

    /**
     * 定时上报服务器性能数据
     */
    @Scheduled(fixedDelayString = "${cloud.server.report-interval:30}000")
    public void report() {
        log.debug("Collecting server metrics...");

        try {
            Map<String, Object> metric = systemCollector.collectServerMetric();
            boolean success = dataReporter.reportServerMetric(metric);

            if (success) {
                log.info("Server metric reported successfully");
            } else {
                log.warn("Failed to report server metric");
            }

        } catch (Exception e) {
            log.error("Error collecting server metrics", e);
        }
    }
}
