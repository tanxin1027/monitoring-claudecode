package com.monitor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.monitor.dto.PageResult;
import com.monitor.dto.TomcatMetricDTO;
import com.monitor.entity.TomcatInstance;
import com.monitor.entity.TomcatMetric;
import com.monitor.mapper.TomcatInstanceMapper;
import com.monitor.mapper.TomcatMetricMapper;
import com.monitor.service.TomcatMonitorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Tomcat 监控服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TomcatMonitorServiceImpl implements TomcatMonitorService {

    private final TomcatInstanceMapper tomcatInstanceMapper;
    private final TomcatMetricMapper tomcatMetricMapper;

    @Override
    public PageResult<TomcatInstance> getInstanceList(Integer pageNum, Integer pageSize, String searchKey) {
        LambdaQueryWrapper<TomcatInstance> wrapper = new LambdaQueryWrapper<>();

        // 数据权限控制：根据当前用户角色过滤医院数据
        // TODO: 实现具体的数据权限控制逻辑

        if (searchKey != null && !searchKey.trim().isEmpty()) {
            wrapper.and(w -> w.like(TomcatInstance::getInstanceCode, searchKey)
                    .or()
                    .like(TomcatInstance::getInstanceName, searchKey));
        }

        wrapper.orderByDesc(TomcatInstance::getId);

        IPage<TomcatInstance> page = new Page<>(pageNum, pageSize);
        IPage<TomcatInstance> resultPage = tomcatInstanceMapper.selectPage(page, wrapper);

        PageResult<TomcatInstance> result = new PageResult<>();
        result.setRecords(resultPage.getRecords());
        result.setTotal(resultPage.getTotal());
        result.setPageNum(pageNum);
        result.setPageSize(pageSize);

        return result;
    }

    @Override
    public TomcatInstance getInstanceDetail(Long id) {
        return tomcatInstanceMapper.selectById(id);
    }

    @Override
    public TomcatMetricDTO getRealtimeMetric(Long instanceId) {
        // 获取最新的监控数据
        LambdaQueryWrapper<TomcatMetric> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TomcatMetric::getInstanceId, instanceId)
               .orderByDesc(TomcatMetric::getCollectTime)
               .last("LIMIT 1");

        TomcatMetric latestMetric = tomcatMetricMapper.selectOne(wrapper);

        if (latestMetric == null) {
            return null;
        }

        return convertToDTO(latestMetric);
    }

    @Override
    public List<TomcatMetricDTO> getHistoryMetric(Long instanceId, String startTime, String endTime, Integer interval) {
        LambdaQueryWrapper<TomcatMetric> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TomcatMetric::getInstanceId, instanceId)
               .between(TomcatMetric::getCollectTime,
                       LocalDateTime.parse(startTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                       LocalDateTime.parse(endTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
               .orderByAsc(TomcatMetric::getCollectTime);

        List<TomcatMetric> metrics = tomcatMetricMapper.selectList(wrapper);

        // 按照时间间隔聚合数据（简化处理，实际可根据需求优化）
        return metrics.stream()
                     .map(this::convertToDTO)
                     .collect(Collectors.toList());
    }

    @Override
    public boolean addInstance(TomcatInstance instance) {
        // 检查实例编码是否重复
        LambdaQueryWrapper<TomcatInstance> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TomcatInstance::getInstanceCode, instance.getInstanceCode());
        long count = tomcatInstanceMapper.selectCount(wrapper);
        if (count > 0) {
            log.warn("Tomcat 实例编码重复: {}", instance.getInstanceCode());
            return false;
        }

        // 设置默认值
        instance.setStatus(instance.getStatus() != null ? instance.getStatus() : 1);
        instance.setCreateTime(LocalDateTime.now());

        int result = tomcatInstanceMapper.insert(instance);
        return result > 0;
    }

    @Override
    public boolean updateInstance(TomcatInstance instance) {
        // 检查实例编码是否与其他实例冲突
        LambdaQueryWrapper<TomcatInstance> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TomcatInstance::getInstanceCode, instance.getInstanceCode())
               .ne(TomcatInstance::getId, instance.getId()); // 排除自己
        long count = tomcatInstanceMapper.selectCount(wrapper);
        if (count > 0) {
            log.warn("Tomcat 实例编码重复: {}", instance.getInstanceCode());
            return false;
        }

        instance.setUpdateTime(LocalDateTime.now());
        int result = tomcatInstanceMapper.updateById(instance);
        return result > 0;
    }

    @Override
    public boolean deleteInstance(Long id) {
        int result = tomcatInstanceMapper.deleteById(id);
        return result > 0;
    }

    /**
     * 将实体转换为 DTO
     */
    private TomcatMetricDTO convertToDTO(TomcatMetric metric) {
        TomcatMetricDTO dto = new TomcatMetricDTO();
        dto.setInstanceCode(getInstanceCodeById(metric.getInstanceId())); // 设置实例编码
        dto.setJvmHeapUsed(metric.getJvmHeapUsed());
        dto.setJvmHeapMax(metric.getJvmHeapMax());
        dto.setJvmNonHeapUsed(metric.getJvmNonHeapUsed());
        dto.setGcCount(metric.getGcCount());
        dto.setGcTime(metric.getGcTime());
        dto.setThreadCount(metric.getThreadCount());
        dto.setThreadBusy(metric.getThreadBusy());
        dto.setRequestCount(metric.getRequestCount());
        dto.setErrorCount(metric.getErrorCount());
        dto.setBytesSent(metric.getBytesSent());
        dto.setBytesReceived(metric.getBytesReceived());
        dto.setMaxTime(metric.getMaxTime());
        dto.setUptime(metric.getUptime());
        dto.setSessionCount(metric.getSessionCount());
        dto.setSessionExpired(metric.getSessionExpired());

        // 设置收集时间戳
        if (metric.getCollectTime() != null) {
            dto.setCollectTime(metric.getCollectTime().toEpochSecond(java.time.ZoneOffset.ofHours(8)));
        }

        return dto;
    }

    /**
     * 根据实例 ID 获取实例编码
     */
    private String getInstanceCodeById(Long instanceId) {
        TomcatInstance instance = tomcatInstanceMapper.selectById(instanceId);
        return instance != null ? instance.getInstanceCode() : null;
    }
}