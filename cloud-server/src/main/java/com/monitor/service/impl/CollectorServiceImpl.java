package com.monitor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.monitor.dto.MysqlMetricDTO;
import com.monitor.dto.ServerMetricDTO;
import com.monitor.dto.TomcatMetricDTO;
import com.monitor.entity.*;
import com.monitor.mapper.*;
import com.monitor.service.CollectorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import cn.hutool.json.JSONUtil;

/**
 * 数据采集服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CollectorServiceImpl implements CollectorService {

    private final AgentTokenMapper agentTokenMapper;
    private final ServerInfoMapper serverInfoMapper;
    private final ServerMetricMapper serverMetricMapper;
    private final MysqlInstanceMapper mysqlInstanceMapper;
    private final TomcatInstanceMapper tomcatInstanceMapper;
    private final MysqlMetricMapper mysqlMetricMapper;
    private final TomcatMetricMapper tomcatMetricMapper;
    private final StringRedisTemplate redisTemplate;

    private static final DateTimeFormatter MONTH_FORMAT = DateTimeFormatter.ofPattern("yyyyMM");

    @Override
    public boolean validateAgentToken(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        AgentToken agentToken = agentTokenMapper.selectByToken(token);
        if (agentToken == null || agentToken.getStatus() != 1) {
            return false;
        }
        // 更新最后使用时间
        agentToken.setLastUsedTime(LocalDateTime.now());
        agentTokenMapper.updateById(agentToken);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean receiveServerMetric(ServerMetricDTO metricDTO, String token) {
        try {
            // 验证 token
            if (!validateAgentToken(token)) {
                log.warn("Invalid agent token: {}", token);
                return false;
            }

            // 获取服务器信息
            ServerInfo serverInfo = serverInfoMapper.selectByServerCode(metricDTO.getServerCode());
            if (serverInfo == null) {
                log.warn("Server not found: {}", metricDTO.getServerCode());
                return false;
            }

            // 检查服务器状态
            if (serverInfo.getStatus() == 2) {
                log.warn("Server is disabled: {}", metricDTO.getServerCode());
                return false;
            }

            // 更新服务器心跳时间
            updateServerHeartbeat(serverInfo.getId(), metricDTO.getAgentVersion());

            // 保存监控数据
            saveServerMetric(serverInfo.getId(), metricDTO);

            log.debug("Received metric from server: {}", metricDTO.getServerCode());
            return true;

        } catch (Exception e) {
            log.error("Error processing server metric", e);
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean receiveMysqlMetric(MysqlMetricDTO metricDTO, String token) {
        try {
            // 验证 token
            if (!validateAgentToken(token)) {
                log.warn("Invalid agent token: {}", token);
                return false;
            }

            // 获取 MySQL 实例信息
            LambdaQueryWrapper<MysqlInstance> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(MysqlInstance::getInstanceCode, metricDTO.getInstanceCode());
            MysqlInstance instance = mysqlInstanceMapper.selectOne(wrapper);

            if (instance == null) {
                log.warn("MySQL instance not found: {}", metricDTO.getInstanceCode());
                return false;
            }

            // 检查实例状态
            if (instance.getStatus() == 0) {
                log.warn("MySQL instance is disabled: {}", metricDTO.getInstanceCode());
                return false;
            }

            // 保存监控数据
            saveMysqlMetric(instance.getId(), instance.getServerId(), metricDTO);

            log.debug("Received metric from MySQL instance: {}", metricDTO.getInstanceCode());
            return true;

        } catch (Exception e) {
            log.error("Error processing MySQL metric", e);
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean receiveTomcatMetric(TomcatMetricDTO metricDTO, String token) {
        try {
            // 验证 token
            if (!validateAgentToken(token)) {
                log.warn("Invalid agent token: {}", token);
                return false;
            }

            // 获取 Tomcat 实例信息
            LambdaQueryWrapper<TomcatInstance> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(TomcatInstance::getInstanceCode, metricDTO.getInstanceCode());
            TomcatInstance instance = tomcatInstanceMapper.selectOne(wrapper);

            if (instance == null) {
                log.warn("Tomcat instance not found: {}", metricDTO.getInstanceCode());
                return false;
            }

            // 检查实例状态
            if (instance.getStatus() == 0) {
                log.warn("Tomcat instance is disabled: {}", metricDTO.getInstanceCode());
                return false;
            }

            // 保存监控数据
            saveTomcatMetric(instance.getId(), metricDTO);

            log.debug("Received metric from Tomcat instance: {}", metricDTO.getInstanceCode());
            return true;

        } catch (Exception e) {
            log.error("Error processing Tomcat metric", e);
            return false;
        }
    }

    /**
     * 更新服务器心跳
     */
    private void updateServerHeartbeat(Long serverId, String agentVersion) {
        serverInfoMapper.updateStatus(serverId, 1);

        // 更新 agent 版本
        if (agentVersion != null) {
            ServerInfo updateInfo = new ServerInfo();
            updateInfo.setId(serverId);
            updateInfo.setAgentVersion(agentVersion);
            serverInfoMapper.updateById(updateInfo);
        }

        // 记录心跳日志到 Redis（异步写入数据库）
        redisTemplate.opsForList().leftPush(
            "agent:heartbeat:log:" + serverId,
            LocalDateTime.now().toString()
        );
        redisTemplate.expire("agent:heartbeat:log:" + serverId, 1, TimeUnit.DAYS);
    }

    /**
     * 保存服务器性能数据
     */
    private void saveServerMetric(Long serverId, ServerMetricDTO metricDTO) {
        ServerMetric metric = new ServerMetric();
        metric.setServerId(serverId);
        metric.setCpuUsage(metricDTO.getCpuUsage());
        metric.setCpuLoad1m(metricDTO.getCpuLoad1m());
        metric.setCpuLoad5m(metricDTO.getCpuLoad5m());
        metric.setCpuLoad15m(metricDTO.getCpuLoad15m());
        metric.setMemoryUsed(metricDTO.getMemoryUsed());
        metric.setMemoryTotal(metricDTO.getMemoryTotal());
        metric.setMemoryUsage(metricDTO.getMemoryUsage());
        metric.setProcessCount(metricDTO.getProcessCount());

        // 序列化磁盘和网络数据
        if (metricDTO.getDisks() != null) {
            metric.setDiskData(JSONUtil.toJsonStr(metricDTO.getDisks()));
        }
        if (metricDTO.getNetworks() != null) {
            metric.setNetworkData(JSONUtil.toJsonStr(metricDTO.getNetworks()));
        }

        // 设置采集时间
        if (metricDTO.getCollectTime() != null) {
            metric.setCollectTime(LocalDateTime.ofEpochSecond(metricDTO.getCollectTime(), 0, java.time.ZoneOffset.of("+8")));
        } else {
            metric.setCollectTime(LocalDateTime.now());
        }

        metric.setCreateTime(LocalDateTime.now());

        // 动态表名处理（按月分表）
        String currentMonth = LocalDateTime.now().format(MONTH_FORMAT);
        // 这里可以根据月份切换到对应的表
        // 为简化处理，这里直接插入当前表
        serverMetricMapper.insert(metric);
    }

    /**
     * 保存 MySQL 性能数据
     */
    private void saveMysqlMetric(Long instanceId, Long serverId, MysqlMetricDTO metricDTO) {
        MysqlMetric metric = new MysqlMetric();
        metric.setInstanceId(instanceId);
        metric.setConnectionsCurrent(metricDTO.getConnectionsCurrent());
        metric.setConnectionsMax(metricDTO.getConnectionsMax());
        metric.setConnectionsUsage(metricDTO.getConnectionsUsage());
        metric.setQps(metricDTO.getQps());
        metric.setQpsTotal(metricDTO.getQpsTotal());
        metric.setTps(metricDTO.getTps());
        metric.setTpsTotal(metricDTO.getTpsTotal());
        metric.setSlowQueriesRate(metricDTO.getSlowQueriesRate());
        metric.setSlowQueriesTotal(metricDTO.getSlowQueriesTotal());
        metric.setThreadsRunning(metricDTO.getThreadsRunning());
        metric.setThreadsConnected(metricDTO.getThreadsConnected());
        metric.setThreadsCreated(metricDTO.getThreadsCreated());
        metric.setBytesReceived(metricDTO.getBytesReceived());
        metric.setBytesSent(metricDTO.getBytesSent());
        metric.setBufferPoolSize(metricDTO.getBufferPoolSize());
        metric.setBufferPoolFreePages(metricDTO.getBufferPoolFreePages());
        metric.setBufferPoolDirtyPages(metricDTO.getBufferPoolDirtyPages());
        metric.setBufferPoolReadRequests(metricDTO.getBufferPoolReadRequests());
        metric.setBufferPoolWriteRequests(metricDTO.getBufferPoolWriteRequests());
        metric.setBufferPoolReads(metricDTO.getBufferPoolReads());
        metric.setBufferPoolHitRate(metricDTO.getBufferPoolHitRate());
        metric.setInnodbRowLockTime(metricDTO.getInnodbRowLockTime());
        metric.setInnodbRowLockWaits(metricDTO.getInnodbRowLockWaits());
        metric.setInnodbRowLockCurrentWaits(metricDTO.getInnodbRowLockCurrentWaits());
        metric.setTableLocksWaited(metricDTO.getTableLocksWaited());
        metric.setTableLocksImmediate(metricDTO.getTableLocksImmediate());
        metric.setTableLockWaitRate(metricDTO.getTableLockWaitRate());
        metric.setTmpTablesCreated(metricDTO.getTmpTablesCreated());
        metric.setTmpDiskTablesCreated(metricDTO.getTmpDiskTablesCreated());
        metric.setTmpDiskTableRate(metricDTO.getTmpDiskTableRate());
        metric.setSortRows(metricDTO.getSortRows());
        metric.setSortScan(metricDTO.getSortScan());
        metric.setSortRange(metricDTO.getSortRange());
        metric.setSortMergePasses(metricDTO.getSortMergePasses());
        metric.setSlaveIoRunning(metricDTO.getSlaveIoRunning());
        metric.setSlaveSqlRunning(metricDTO.getSlaveSqlRunning());
        metric.setSecondsBehindMaster(metricDTO.getSecondsBehindMaster());

        // 设置采集时间（秒级时间戳转换为 LocalDateTime）
        if (metricDTO.getCollectTime() != null) {
            metric.setCollectTime(LocalDateTime.ofEpochSecond(metricDTO.getCollectTime(), 0, java.time.ZoneOffset.of("+8")));
        } else {
            metric.setCollectTime(LocalDateTime.now());
        }

        metric.setCreateTime(LocalDateTime.now());

        mysqlMetricMapper.insert(metric);
    }

    /**
     * 保存 Tomcat 性能数据
     */
    private void saveTomcatMetric(Long instanceId, TomcatMetricDTO metricDTO) {
        log.info("Saving Tomcat metric for instanceId={}: jvmHeapUsed={}, jvmHeapMax={}, jvmNonHeapUsed={}, gcCount={}, gcTime={}, threadCount={}, threadBusy={}, requestCount={}, errorCount={}, bytesSent={}, bytesReceived={}, maxTime={}, uptime={}, sessionCount={}, sessionExpired={}, collectTime={}",
            instanceId,
            metricDTO.getJvmHeapUsed(), metricDTO.getJvmHeapMax(), metricDTO.getJvmNonHeapUsed(),
            metricDTO.getGcCount(), metricDTO.getGcTime(),
            metricDTO.getThreadCount(), metricDTO.getThreadBusy(),
            metricDTO.getRequestCount(), metricDTO.getErrorCount(),
            metricDTO.getBytesSent(), metricDTO.getBytesReceived(),
            metricDTO.getMaxTime(), metricDTO.getUptime(),
            metricDTO.getSessionCount(), metricDTO.getSessionExpired(),
            metricDTO.getCollectTime());

        TomcatMetric metric = new TomcatMetric();
        metric.setInstanceId(instanceId);
        metric.setJvmHeapUsed(metricDTO.getJvmHeapUsed());
        metric.setJvmHeapMax(metricDTO.getJvmHeapMax());
        metric.setJvmNonHeapUsed(metricDTO.getJvmNonHeapUsed());
        metric.setGcCount(metricDTO.getGcCount());
        metric.setGcTime(metricDTO.getGcTime());
        metric.setThreadCount(metricDTO.getThreadCount());
        metric.setThreadBusy(metricDTO.getThreadBusy());
        metric.setRequestCount(metricDTO.getRequestCount());
        metric.setErrorCount(metricDTO.getErrorCount());
        metric.setBytesSent(metricDTO.getBytesSent());
        metric.setBytesReceived(metricDTO.getBytesReceived());
        metric.setMaxTime(metricDTO.getMaxTime());
        metric.setUptime(metricDTO.getUptime());
        metric.setSessionCount(metricDTO.getSessionCount());
        metric.setSessionExpired(metricDTO.getSessionExpired());

        // 设置采集时间（秒级时间戳转换为 LocalDateTime）
        if (metricDTO.getCollectTime() != null) {
            metric.setCollectTime(LocalDateTime.ofEpochSecond(metricDTO.getCollectTime(), 0, java.time.ZoneOffset.of("+8")));
        } else {
            metric.setCollectTime(LocalDateTime.now());
        }

        metric.setCreateTime(LocalDateTime.now());

        tomcatMetricMapper.insert(metric);
        log.info("Tomcat metric saved successfully, id={}", metric.getId());
    }
}
