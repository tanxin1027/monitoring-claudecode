package com.monitor.agent.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * MySQL 监控配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "mysql")
public class MysqlConfig {

    /**
     * 是否启用 MySQL 监控
     */
    private boolean enabled = false;

    /**
     * MySQL 实例列表
     */
    private List<MysqlInstance> instances = new ArrayList<>();

    @Data
    public static class MysqlInstance {
        /**
         * 实例编码
         */
        private String instanceCode;

        /**
         * 实例名称
         */
        private String instanceName;

        /**
         * JDBC URL
         */
        private String url;

        /**
         * 用户名
         */
        private String username;

        /**
         * 密码
         */
        private String password;
    }
}
