package com.monitor.service;

import com.monitor.dto.MysqlMetricDTO;
import com.monitor.dto.PageResult;
import com.monitor.entity.MysqlInstance;
import java.util.List;

/**
 * MySQL 监控服务接口
 */
public interface MysqlMonitorService {

    /**
     * 获取 MySQL 实例列表（分页）
     */
    PageResult<MysqlInstance> getInstanceList(Integer pageNum, Integer pageSize, String searchKey);

    /**
     * 获取 MySQL 实例详情
     */
    MysqlInstance getInstanceDetail(Long id);

    /**
     * 获取 MySQL 实时监控数据
     */
    MysqlMetricDTO getRealtimeMetric(Long instanceId);

    /**
     * 获取 MySQL 历史监控数据
     */
    List<MysqlMetricDTO> getHistoryMetric(Long instanceId, String startTime, String endTime, Integer interval);

    /**
     * 新增 MySQL 实例
     */
    boolean addInstance(MysqlInstance instance);

    /**
     * 修改 MySQL 实例
     */
    boolean updateInstance(MysqlInstance instance);

    /**
     * 删除 MySQL 实例
     */
    boolean deleteInstance(Long id);
}