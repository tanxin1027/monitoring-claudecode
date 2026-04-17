package com.monitor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.monitor.dto.AlarmGroupDTO;
import com.monitor.dto.AlarmRecordDTO;
import com.monitor.entity.AlarmRecord;
import com.monitor.entity.AlarmRule;
import com.monitor.mapper.AlarmRecordMapper;
import com.monitor.mapper.AlarmRuleMapper;
import com.monitor.service.AlarmRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 告警记录服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AlarmRecordServiceImpl extends ServiceImpl<AlarmRecordMapper, AlarmRecord> implements AlarmRecordService {

    private final AlarmRuleMapper alarmRuleMapper;

    @Override
    public Page<AlarmRecordDTO> pageList(Integer pageNum, Integer pageSize, Integer status, Integer severity, String keyword) {
        LambdaQueryWrapper<AlarmRecord> wrapper = new LambdaQueryWrapper<>();

        if (status != null) {
            wrapper.eq(AlarmRecord::getStatus, status);
        }

        if (severity != null) {
            wrapper.eq(AlarmRecord::getSeverity, severity);
        }

        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.like(AlarmRecord::getAlarmContent, keyword)
                   .or().like(AlarmRecord::getInstanceName, keyword);
        }

        wrapper.orderByDesc(AlarmRecord::getAlarmTime);

        Page<AlarmRecord> page = new Page<>(pageNum, pageSize);
        Page<AlarmRecord> resultPage = this.page(page, wrapper);

        Page<AlarmRecordDTO> result = new Page<>();
        result.setRecords(resultPage.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList()));
        result.setTotal(resultPage.getTotal());
        result.setCurrent(resultPage.getCurrent());
        result.setSize(resultPage.getSize());

        return result;
    }

    @Override
    public AlarmRecordDTO getRecordById(Long id) {
        AlarmRecord record = this.getById(id);
        return record != null ? convertToDTO(record) : null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean process(Long id, String handler, String handleRemark) {
        AlarmRecord record = new AlarmRecord();
        record.setId(id);
        record.setStatus(1); // 已处理
        record.setHandler(handler);
        record.setHandleRemark(handleRemark);
        record.setHandleTime(LocalDateTime.now());
        return this.updateById(record);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchProcess(List<Long> ids, String handler, String handleRemark) {
        int count = 0;
        for (Long id : ids) {
            if (process(id, handler, handleRemark)) {
                count++;
            }
        }
        return count;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean ignore(Long id, String handler) {
        AlarmRecord record = new AlarmRecord();
        record.setId(id);
        record.setStatus(2); // 已忽略
        record.setHandler(handler);
        record.setHandleTime(LocalDateTime.now());
        return this.updateById(record);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createRecord(AlarmRecordDTO recordDTO) {
        AlarmRecord record = convertToEntity(recordDTO);
        record.setAlarmTime(LocalDateTime.now());
        record.setCreateTime(LocalDateTime.now());
        return this.save(record);
    }

    @Override
    public int countUnprocessed() {
        LambdaQueryWrapper<AlarmRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AlarmRecord::getStatus, 0);
        return (int) this.count(wrapper);
    }

    @Override
    public AlarmRecord findLatestByGroupKey(String groupKey) {
        if (groupKey == null || groupKey.isEmpty()) {
            return null;
        }

        LambdaQueryWrapper<AlarmRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AlarmRecord::getGroupKey, groupKey)
               .eq(AlarmRecord::getStatus, 0) // 只查询未处理的记录
               .orderByDesc(AlarmRecord::getAlarmTime)
               .last("LIMIT 1");

        return this.getOne(wrapper);
    }

    @Override
    public List<AlarmGroupDTO> getAlarmGroups(Integer pageNum, Integer pageSize, Integer status) {
        // 查询所有告警记录（按状态过滤）
        LambdaQueryWrapper<AlarmRecord> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(AlarmRecord::getStatus, status);
        }
        // 只查询有 group_key 的记录（连续告警记录）
        wrapper.isNotNull(AlarmRecord::getGroupKey);
        wrapper.orderByDesc(AlarmRecord::getAlarmTime);

        List<AlarmRecord> allRecords = this.list(wrapper);

        // 按 group_key 分组
        Map<String, List<AlarmRecord>> groupedRecords = allRecords.stream()
                .collect(Collectors.groupingBy(AlarmRecord::getGroupKey));

        // 转换为 AlarmGroupDTO（按最新告警时间排序）
        List<AlarmGroupDTO> result = new ArrayList<>();
        for (Map.Entry<String, List<AlarmRecord>> entry : groupedRecords.entrySet()) {
            List<AlarmRecord> records = entry.getValue();
            if (records.isEmpty()) {
                continue;
            }

            // 获取最新的记录作为代表
            AlarmRecord latestRecord = records.stream()
                    .max((r1, r2) -> r2.getAlarmTime().compareTo(r1.getAlarmTime()))
                    .orElse(null);

            if (latestRecord == null) {
                continue;
            }

            AlarmGroupDTO groupDTO = new AlarmGroupDTO();
            groupDTO.setGroupKey(entry.getKey());
            groupDTO.setRuleId(latestRecord.getRuleId());
            groupDTO.setMetricType(latestRecord.getMetricType());
            groupDTO.setInstanceId(latestRecord.getInstanceId());
            groupDTO.setInstanceName(latestRecord.getInstanceName());
            groupDTO.setContinuousCount(latestRecord.getContinuousCount() != null ? latestRecord.getContinuousCount() : 1);
            groupDTO.setCurrentValue(latestRecord.getCurrentValue());
            groupDTO.setSeverity(latestRecord.getSeverity());
            groupDTO.setStatus(latestRecord.getStatus());

            // 计算首次和最近告警时间
            LocalDateTime firstTime = records.stream()
                    .map(AlarmRecord::getAlarmTime)
                    .min(LocalDateTime::compareTo)
                    .orElse(null);
            LocalDateTime lastTime = records.stream()
                    .map(AlarmRecord::getAlarmTime)
                    .max(LocalDateTime::compareTo)
                    .orElse(null);
            groupDTO.setFirstAlarmTime(firstTime);
            groupDTO.setLastAlarmTime(lastTime);

            // 查询规则信息
            AlarmRule rule = alarmRuleMapper.selectById(latestRecord.getRuleId());
            if (rule != null) {
                groupDTO.setRuleName(rule.getRuleName());
                groupDTO.setMetricKey(rule.getMetricKey());
                groupDTO.setNotifyMethod(rule.getNotifyMethod());
            }

            // 设置通知状态（如果达到阈值则认为已通知）
            Integer continuousThreshold = rule != null && rule.getContinuousThreshold() != null ? rule.getContinuousThreshold() : 3;
            groupDTO.setNotifyStatus(groupDTO.getContinuousCount() >= continuousThreshold ? 1 : 0);

            // 转换记录列表（按时间排序）
            List<AlarmRecordDTO> recordDTOs = records.stream()
                    .sorted((r1, r2) -> r2.getAlarmTime().compareTo(r1.getAlarmTime()))
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
            groupDTO.setRecords(recordDTOs);

            result.add(groupDTO);
        }

        // 按最新告警时间排序
        result.sort((g1, g2) -> {
            if (g1.getLastAlarmTime() == null) return 1;
            if (g2.getLastAlarmTime() == null) return -1;
            return g2.getLastAlarmTime().compareTo(g1.getLastAlarmTime());
        });

        // 分页
        int total = result.size();
        int fromIndex = (pageNum - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, total);

        if (fromIndex >= total) {
            return new ArrayList<>();
        }

        return result.subList(fromIndex, toIndex);
    }

    @Override
    public List<AlarmRecordDTO> getRecordsByGroupKey(String groupKey) {
        if (groupKey == null || groupKey.isEmpty()) {
            return new ArrayList<>();
        }

        LambdaQueryWrapper<AlarmRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AlarmRecord::getGroupKey, groupKey)
               .orderByDesc(AlarmRecord::getAlarmTime);

        List<AlarmRecord> records = this.list(wrapper);
        return records.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private AlarmRecordDTO convertToDTO(AlarmRecord record) {
        AlarmRecordDTO dto = new AlarmRecordDTO();
        BeanUtils.copyProperties(record, dto);
        return dto;
    }

    private AlarmRecord convertToEntity(AlarmRecordDTO dto) {
        AlarmRecord record = new AlarmRecord();
        BeanUtils.copyProperties(dto, record);
        return record;
    }
}
