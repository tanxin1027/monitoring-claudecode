package com.monitor.agent.reporter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.monitor.agent.config.CloudServerProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * 数据上报器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataReporter {

    private final CloudServerProperties cloudServerProperties;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 上报服务器性能数据
     */
    public boolean reportServerMetric(Map<String, Object> metric) {
        String url = cloudServerProperties.getUrl() + "/api/collector/server/metric";
        String token = cloudServerProperties.getToken();

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Agent-Token", token);

            String jsonBody = objectMapper.writeValueAsString(metric);
            HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            if (response.getStatusCode().value() == 200) {
                log.debug("Server metric reported successfully");
                return true;
            } else {
                log.warn("Server metric report failed: {}", response.getStatusCode());
                return false;
            }

        } catch (Exception e) {
            log.error("Error reporting server metric", e);
            return false;
        }
    }

    /**
     * 上报 MySQL 性能数据
     */
    public boolean reportMysqlMetric(Map<String, Object> metric) {
        String url = cloudServerProperties.getUrl() + "/api/collector/mysql/metric";
        String token = cloudServerProperties.getToken();

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Agent-Token", token);

            String jsonBody = objectMapper.writeValueAsString(metric);
            HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            return response.getStatusCode().value() == 200;

        } catch (Exception e) {
            log.error("Error reporting MySQL metric", e);
            return false;
        }
    }

    /**
     * 上报 Tomcat 性能数据
     */
    public boolean reportTomcatMetric(Map<String, Object> metric) {
        String url = cloudServerProperties.getUrl() + "/api/collector/tomcat/metric";
        String token = cloudServerProperties.getToken();

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Agent-Token", token);

            String jsonBody = objectMapper.writeValueAsString(metric);
            HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            return response.getStatusCode().value() == 200;

        } catch (Exception e) {
            log.error("Error reporting Tomcat metric", e);
            return false;
        }
    }
}
