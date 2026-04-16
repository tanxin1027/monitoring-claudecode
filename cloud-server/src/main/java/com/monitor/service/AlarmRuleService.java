package com.monitor.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.monitor.dto.AlarmRuleDTO;
import com.monitor.entity.AlarmRule;

import java.util.List;

/**
 * 告警规则服务接口
 */
public interface AlarmRuleService {

    /**
     * 分页查询告警规则列表
     */
    Page<AlarmRuleDTO> pageList(Integer pageNum, Integer pageSize, Integer status, String keyword);

    /**
     * 查询所有启用的告警规则
     */
    List<AlarmRuleDTO> listEnabled();

    /**
     * 根据 ID 查询告警规则
     */
    AlarmRuleDTO getByRuleId(Long id);

    /**
     * 新增告警规则
     */
    boolean add(AlarmRuleDTO ruleDTO);

    /**
     * 修改告警规则
     */
    boolean update(AlarmRuleDTO ruleDTO);

    /**
     * 删除告警规则
     */
    boolean delete(Long id);

    /**
     * 启用/禁用告警规则
     */
    boolean toggleStatus(Long id, Integer status);
}
