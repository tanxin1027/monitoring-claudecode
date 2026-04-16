package com.monitor.service;

import com.monitor.dto.MysqlMetricDTO;
import com.monitor.dto.ServerMetricDTO;
import com.monitor.dto.TomcatMetricDTO;

/**
 * 数据采集服务
 */
public interface CollectorService {

    /**
     * 接收服务器性能数据
     * @param metricDTO 性能数据
     * @param token 认证 token
     * @return 处理结果
     */
    boolean receiveServerMetric(ServerMetricDTO metricDTO, String token);

    /**
     * 接收 MySQL 性能数据
     * @param metricDTO MySQL 性能数据
     * @param token 认证 token
     * @return 处理结果
     */
    boolean receiveMysqlMetric(MysqlMetricDTO metricDTO, String token);

    /**
     * 接收 Tomcat 性能数据
     * @param metricDTO Tomcat 性能数据
     * @param token 认证 token
     * @return 处理结果
     */
    boolean receiveTomcatMetric(TomcatMetricDTO metricDTO, String token);

    /**
     * 验证 Agent  token
     * @param token 认证 token
     * @return 是否有效
     */
    boolean validateAgentToken(String token);
}
