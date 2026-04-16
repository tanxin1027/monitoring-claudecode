package com.monitor.agent.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * Tomcat 监控配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "tomcat")
public class TomcatConfig {

    /**
     * 是否启用 Tomcat 监控
     */
    private boolean enabled = false;

    /**
     * Tomcat 实例列表
     */
    private List<TomcatInstance> instances = new ArrayList<>();

    @Data
    public static class TomcatInstance {
        /**
         * 实例编码
         */
        private String instanceCode;

        /**
         * 实例名称
         */
        private String instanceName;

        /**
         * JMX 主机
         */
        private String host;

        /**
         * JMX 端口
         */
        private Integer jmxPort;
    }
}
