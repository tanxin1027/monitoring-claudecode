package com.monitor.agent.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 云端服务器配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "cloud.server")
public class CloudServerProperties {

    /**
     * 云端服务器 URL
     */
    private String url;

    /**
     * 认证 token
     */
    private String token;

    /**
     * 数据上报间隔 (秒)
     */
    private Integer reportInterval = 30;
}
