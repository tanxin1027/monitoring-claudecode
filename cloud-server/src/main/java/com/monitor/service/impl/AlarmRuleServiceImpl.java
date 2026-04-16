package com.monitor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.monitor.dto.AlarmRuleDTO;
import com.monitor.entity.AlarmRule;
import com.monitor.mapper.AlarmRuleMapper;
import com.monitor.service.AlarmRuleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 告警规则服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AlarmRuleServiceImpl extends ServiceImpl<AlarmRuleMapper, AlarmRule> implements AlarmRuleService {

    @Override
    public Page<AlarmRuleDTO> pageList(Integer pageNum, Integer pageSize, Integer status, String keyword) {
        LambdaQueryWrapper<AlarmRule> wrapper = new LambdaQueryWrapper<>();

        if (status != null) {
            wrapper.eq(AlarmRule::getStatus, status);
        }

        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.like(AlarmRule::getRuleName, keyword)
                   .or().like(AlarmRule::getMetricKey, keyword);
        }

        wrapper.orderByDesc(AlarmRule::getId);

        Page<AlarmRule> page = new Page<>(pageNum, pageSize);
        Page<AlarmRule> resultPage = this.page(page, wrapper);

        Page<AlarmRuleDTO> result = new Page<>();
        result.setRecords(resultPage.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList()));
        result.setTotal(resultPage.getTotal());
        result.setCurrent(resultPage.getCurrent());
        result.setSize(resultPage.getSize());

        return result;
    }

    @Override
    public List<AlarmRuleDTO> listEnabled() {
        LambdaQueryWrapper<AlarmRule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AlarmRule::getStatus, 1);
        List<AlarmRule> list = this.list(wrapper);
        return list.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public AlarmRuleDTO getByRuleId(Long id) {
        AlarmRule rule = this.getById(id);
        return rule != null ? convertToDTO(rule) : null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean add(AlarmRuleDTO ruleDTO) {
        AlarmRule rule = convertToEntity(ruleDTO);
        rule.setCreateTime(LocalDateTime.now());
        return this.save(rule);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean update(AlarmRuleDTO ruleDTO) {
        AlarmRule rule = convertToEntity(ruleDTO);
        rule.setUpdateTime(LocalDateTime.now());
        return this.updateById(rule);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean delete(Long id) {
        return this.removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean toggleStatus(Long id, Integer status) {
        AlarmRule rule = new AlarmRule();
        rule.setId(id);
        rule.setStatus(status);
        return this.updateById(rule);
    }

    private AlarmRuleDTO convertToDTO(AlarmRule rule) {
        AlarmRuleDTO dto = new AlarmRuleDTO();
        BeanUtils.copyProperties(rule, dto);
        return dto;
    }

    private AlarmRule convertToEntity(AlarmRuleDTO dto) {
        AlarmRule rule = new AlarmRule();
        BeanUtils.copyProperties(dto, rule);
        return rule;
    }
}
