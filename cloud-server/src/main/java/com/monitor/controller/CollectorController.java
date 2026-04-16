package com.monitor.controller;

import com.monitor.dto.MysqlMetricDTO;
import com.monitor.dto.Result;
import com.monitor.dto.ServerMetricDTO;
import com.monitor.dto.TomcatMetricDTO;
import com.monitor.service.CollectorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 数据采集接口
 */
@Slf4j
@RestController
@RequestMapping("/api/collector")
@RequiredArgsConstructor
public class CollectorController {

    private final CollectorService collectorService;

    /**
     * 接收服务器性能数据
     * @param metricDTO 性能数据
     * @param token 认证 token
     */
    @PostMapping("/server/metric")
    public Result<Boolean> receiveServerMetric(
            @RequestBody ServerMetricDTO metricDTO,
            @RequestHeader(value = "X-Agent-Token", required = false) String token) {

        log.info("Receive server metric: {}", metricDTO.getServerCode());

        boolean success = collectorService.receiveServerMetric(metricDTO, token);

        if (success) {
            return Result.success(true);
        } else {
            return Result.error(401, "Authentication failed or server not found");
        }
    }

    /**
     * 接收 MySQL 性能数据
     * @param metricDTO MySQL 性能数据
     * @param token 认证 token
     */
    @PostMapping("/mysql/metric")
    public Result<Boolean> receiveMysqlMetric(
            @RequestBody MysqlMetricDTO metricDTO,
            @RequestHeader(value = "X-Agent-Token", required = false) String token) {

        log.info("Receive MySQL metric for instance: {}", metricDTO.getInstanceCode());

        boolean success = collectorService.receiveMysqlMetric(metricDTO, token);

        if (success) {
            return Result.success(true);
        } else {
            return Result.error(401, "Authentication failed or instance not found");
        }
    }

    /**
     * 接收 Tomcat 性能数据
     * @param metricDTO Tomcat 性能数据
     * @param token 认证 token
     */
    @PostMapping("/tomcat/metric")
    public Result<Boolean> receiveTomcatMetric(
            @RequestBody TomcatMetricDTO metricDTO,
            @RequestHeader(value = "X-Agent-Token", required = false) String token) {

        log.info("Receive Tomcat metric for instance: {}", metricDTO.getInstanceCode());
        log.info("Tomcat metric DTO: jvmHeapUsed={}, jvmHeapMax={}, threadCount={}, requestCount={}, uptime={}",
            metricDTO.getJvmHeapUsed(), metricDTO.getJvmHeapMax(),
            metricDTO.getThreadCount(), metricDTO.getRequestCount(), metricDTO.getUptime());

        boolean success = collectorService.receiveTomcatMetric(metricDTO, token);

        if (success) {
            return Result.success(true);
        } else {
            return Result.error(401, "Authentication failed or instance not found");
        }
    }

    /**
     * 健康检查接口
     */
    @GetMapping("/health")
    public Result<String> health() {
        return Result.success("OK");
    }
}
