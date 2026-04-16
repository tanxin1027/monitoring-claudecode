package com.monitor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.monitor.dto.AlarmRecordDTO;
import com.monitor.entity.AlarmRecord;
import com.monitor.mapper.AlarmRecordMapper;
import com.monitor.service.AlarmRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 告警记录服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AlarmRecordServiceImpl extends ServiceImpl<AlarmRecordMapper, AlarmRecord> implements AlarmRecordService {

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
