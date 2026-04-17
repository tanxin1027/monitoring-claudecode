package com.monitor.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.monitor.dto.AlarmRecordDTO;

import java.util.List;

/**
 * 告警记录服务接口
 */
public interface AlarmRecordService {

    /**
     * 分页查询告警记录列表
     */
    Page<AlarmRecordDTO> pageList(Integer pageNum, Integer pageSize, Integer status, Integer severity, String keyword);

    /**
     * 根据 ID 查询告警记录
     */
    AlarmRecordDTO getRecordById(Long id);

    /**
     * 处理告警
     */
    boolean process(Long id, String handler, String handleRemark);

    /**
     * 批量处理告警
     */
    int batchProcess(List<Long> ids, String handler, String handleRemark);

    /**
     * 忽略告警
     */
    boolean ignore(Long id, String handler);

    /**
     * 创建告警记录
     */
    boolean createRecord(AlarmRecordDTO recordDTO);

    /**
     * 统计未处理告警数
     */
    int countUnprocessed();

    /**
     * 根据分组标识查询最新的告警记录
     *
     * @param groupKey 分组标识
     * @return 最新的告警记录
     */
    com.monitor.entity.AlarmRecord findLatestByGroupKey(String groupKey);

    /**
     * 按分组统计告警记录（用于前端分组展示）
     *
     * @return 分组告警统计列表
     */
    java.util.List<com.monitor.dto.AlarmGroupDTO> getAlarmGroups(Integer pageNum, Integer pageSize, Integer status);

    /**
     * 根据分组标识查询告警记录列表
     *
     * @param groupKey 分组标识
     * @return 告警记录列表
     */
    java.util.List<com.monitor.dto.AlarmRecordDTO> getRecordsByGroupKey(String groupKey);
}
