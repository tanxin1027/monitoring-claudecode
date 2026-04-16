package com.monitor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.monitor.dto.MysqlMetricDTO;
import com.monitor.dto.PageResult;
import com.monitor.entity.MysqlInstance;
import com.monitor.entity.MysqlMetric;
import com.monitor.mapper.MysqlInstanceMapper;
import com.monitor.mapper.MysqlMetricMapper;
import com.monitor.service.MysqlMonitorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * MySQL 监控服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MysqlMonitorServiceImpl implements MysqlMonitorService {

    private final MysqlInstanceMapper mysqlInstanceMapper;
    private final MysqlMetricMapper mysqlMetricMapper;

    @Override
    public PageResult<MysqlInstance> getInstanceList(Integer pageNum, Integer pageSize, String searchKey) {
        LambdaQueryWrapper<MysqlInstance> wrapper = new LambdaQueryWrapper<>();

        // 数据权限控制：根据当前用户角色过滤医院数据
        // TODO: 实现具体的数据权限控制逻辑

        if (searchKey != null && !searchKey.trim().isEmpty()) {
            wrapper.and(w -> w.like(MysqlInstance::getInstanceCode, searchKey)
                    .or()
                    .like(MysqlInstance::getInstanceName, searchKey));
        }

        wrapper.orderByDesc(MysqlInstance::getId);

        IPage<MysqlInstance> page = new Page<>(pageNum, pageSize);
        IPage<MysqlInstance> resultPage = mysqlInstanceMapper.selectPage(page, wrapper);

        PageResult<MysqlInstance> result = new PageResult<>();
        result.setRecords(resultPage.getRecords());
        result.setTotal(resultPage.getTotal());
        result.setPageNum(pageNum);
        result.setPageSize(pageSize);

        return result;
    }

    @Override
    public MysqlInstance getInstanceDetail(Long id) {
        return mysqlInstanceMapper.selectById(id);
    }

    @Override
    public MysqlMetricDTO getRealtimeMetric(Long instanceId) {
        // 获取最新的监控数据
        LambdaQueryWrapper<MysqlMetric> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MysqlMetric::getInstanceId, instanceId)
               .orderByDesc(MysqlMetric::getCollectTime)
               .last("LIMIT 1");

        MysqlMetric latestMetric = mysqlMetricMapper.selectOne(wrapper);

        if (latestMetric == null) {
            return null;
        }

        return convertToDTO(latestMetric);
    }

    @Override
    public List<MysqlMetricDTO> getHistoryMetric(Long instanceId, String startTime, String endTime, Integer interval) {
        LambdaQueryWrapper<MysqlMetric> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MysqlMetric::getInstanceId, instanceId)
               .between(MysqlMetric::getCollectTime,
                       LocalDateTime.parse(startTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                       LocalDateTime.parse(endTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
               .orderByAsc(MysqlMetric::getCollectTime);

        List<MysqlMetric> metrics = mysqlMetricMapper.selectList(wrapper);

        // 按照时间间隔聚合数据（简化处理，实际可根据需求优化）
        return metrics.stream()
                     .map(this::convertToDTO)
                     .collect(Collectors.toList());
    }

    @Override
    public boolean addInstance(MysqlInstance instance) {
        // 检查实例编码是否重复
        LambdaQueryWrapper<MysqlInstance> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MysqlInstance::getInstanceCode, instance.getInstanceCode());
        long count = mysqlInstanceMapper.selectCount(wrapper);
        if (count > 0) {
            log.warn("MySQL 实例编码重复: {}", instance.getInstanceCode());
            return false;
        }

        // 设置默认值
        instance.setStatus(instance.getStatus() != null ? instance.getStatus() : 1);
        instance.setCreateTime(LocalDateTime.now());

        int result = mysqlInstanceMapper.insert(instance);
        return result > 0;
    }

    @Override
    public boolean updateInstance(MysqlInstance instance) {
        // 检查实例编码是否与其他实例冲突
        LambdaQueryWrapper<MysqlInstance> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MysqlInstance::getInstanceCode, instance.getInstanceCode())
               .ne(MysqlInstance::getId, instance.getId()); // 排除自己
        long count = mysqlInstanceMapper.selectCount(wrapper);
        if (count > 0) {
            log.warn("MySQL 实例编码重复: {}", instance.getInstanceCode());
            return false;
        }

        instance.setUpdateTime(LocalDateTime.now());
        int result = mysqlInstanceMapper.updateById(instance);
        return result > 0;
    }

    @Override
    public boolean deleteInstance(Long id) {
        int result = mysqlInstanceMapper.deleteById(id);
        return result > 0;
    }

    /**
     * 将实体转换为 DTO
     */
    private MysqlMetricDTO convertToDTO(MysqlMetric metric) {
        MysqlMetricDTO dto = new MysqlMetricDTO();
        dto.setConnectionsCurrent(metric.getConnectionsCurrent());
        dto.setConnectionsMax(metric.getConnectionsMax());
        dto.setConnectionsUsage(metric.getConnectionsUsage());
        dto.setQps(metric.getQps() != null ? metric.getQps() : java.math.BigDecimal.ZERO);
        dto.setQpsTotal(metric.getQpsTotal());
        dto.setTps(metric.getTps() != null ? metric.getTps() : java.math.BigDecimal.ZERO);
        dto.setTpsTotal(metric.getTpsTotal());
        dto.setSlowQueriesRate(metric.getSlowQueriesRate());
        dto.setSlowQueriesTotal(metric.getSlowQueriesTotal());
        dto.setThreadsRunning(metric.getThreadsRunning());
        dto.setThreadsConnected(metric.getThreadsConnected());
        dto.setThreadsCreated(metric.getThreadsCreated());
        dto.setBytesReceived(metric.getBytesReceived());
        dto.setBytesSent(metric.getBytesSent());
        dto.setBufferPoolSize(metric.getBufferPoolSize());
        dto.setBufferPoolFreePages(metric.getBufferPoolFreePages());
        dto.setBufferPoolDirtyPages(metric.getBufferPoolDirtyPages());
        dto.setBufferPoolReadRequests(metric.getBufferPoolReadRequests());
        dto.setBufferPoolWriteRequests(metric.getBufferPoolWriteRequests());
        dto.setBufferPoolReads(metric.getBufferPoolReads());
        dto.setBufferPoolHitRate(metric.getBufferPoolHitRate());
        dto.setInnodbRowLockTime(metric.getInnodbRowLockTime());
        dto.setInnodbRowLockWaits(metric.getInnodbRowLockWaits());
        dto.setInnodbRowLockCurrentWaits(metric.getInnodbRowLockCurrentWaits());
        dto.setTableLocksWaited(metric.getTableLocksWaited());
        dto.setTableLocksImmediate(metric.getTableLocksImmediate());
        dto.setTableLockWaitRate(metric.getTableLockWaitRate());
        dto.setTmpTablesCreated(metric.getTmpTablesCreated());
        dto.setTmpDiskTablesCreated(metric.getTmpDiskTablesCreated());
        dto.setTmpDiskTableRate(metric.getTmpDiskTableRate());
        dto.setSortRows(metric.getSortRows());
        dto.setSortScan(metric.getSortScan());
        dto.setSortRange(metric.getSortRange());
        dto.setSortMergePasses(metric.getSortMergePasses());
        dto.setSlaveIoRunning(metric.getSlaveIoRunning());
        dto.setSlaveSqlRunning(metric.getSlaveSqlRunning());
        dto.setSecondsBehindMaster(metric.getSecondsBehindMaster());

        // 设置收集时间戳
        if (metric.getCollectTime() != null) {
            dto.setCollectTime(metric.getCollectTime().toEpochSecond(java.time.ZoneOffset.ofHours(8)));
        }

        return dto;
    }
}