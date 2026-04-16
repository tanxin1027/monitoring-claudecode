package com.monitor.service;

import com.monitor.dto.PageResult;
import com.monitor.dto.TomcatMetricDTO;
import com.monitor.entity.TomcatInstance;
import java.util.List;

/**
 * Tomcat 监控服务接口
 */
public interface TomcatMonitorService {

    /**
     * 获取 Tomcat 实例列表（分页）
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param searchKey 搜索关键词
     * @return 分页结果
     */
    PageResult<TomcatInstance> getInstanceList(Integer pageNum, Integer pageSize, String searchKey);

    /**
     * 获取 Tomcat 实例详情
     * @param id 实例 ID
     * @return 实例详情
     */
    TomcatInstance getInstanceDetail(Long id);

    /**
     * 获取 Tomcat 实时监控数据
     * @param id 实例 ID
     * @return 监控数据
     */
    TomcatMetricDTO getRealtimeMetric(Long id);

    /**
     * 获取 Tomcat 历史监控数据
     * @param id 实例 ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param interval 时间间隔（分钟）
     * @return 历史监控数据列表
     */
    List<TomcatMetricDTO> getHistoryMetric(Long id, String startTime, String endTime, Integer interval);

    /**
     * 新增 Tomcat 实例
     * @param instance 实例信息
     * @return 是否成功
     */
    boolean addInstance(TomcatInstance instance);

    /**
     * 修改 Tomcat 实例
     * @param instance 实例信息
     * @return 是否成功
     */
    boolean updateInstance(TomcatInstance instance);

    /**
     * 删除 Tomcat 实例
     * @param id 实例 ID
     * @return 是否成功
     */
    boolean deleteInstance(Long id);
}