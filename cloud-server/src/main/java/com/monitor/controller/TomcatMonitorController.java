package com.monitor.controller;

import com.monitor.dto.TomcatMetricDTO;
import com.monitor.dto.PageResult;
import com.monitor.dto.Result;
import com.monitor.entity.TomcatInstance;
import com.monitor.service.TomcatMonitorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Tomcat 监控管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/monitor/tomcat")
@RequiredArgsConstructor
public class TomcatMonitorController {

    private final TomcatMonitorService tomcatMonitorService;

    /**
     * 获取 Tomcat 实例列表（分页）
     */
    @GetMapping("/list")
    public Result<PageResult<TomcatInstance>> getInstanceList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String searchKey) {

        PageResult<TomcatInstance> result = tomcatMonitorService.getInstanceList(pageNum, pageSize, searchKey);
        return Result.success(result);
    }

    /**
     * 获取 Tomcat 实例详情
     */
    @GetMapping("/{id}")
    public Result<TomcatInstance> getInstanceDetail(@PathVariable Long id) {
        TomcatInstance instance = tomcatMonitorService.getInstanceDetail(id);
        if (instance == null) {
            return Result.error(404, "Tomcat 实例不存在");
        }
        return Result.success(instance);
    }

    /**
     * 获取 Tomcat 实时监控数据
     */
    @GetMapping("/{id}/metric")
    public Result<TomcatMetricDTO> getRealtimeMetric(@PathVariable Long id) {
        TomcatMetricDTO metric = tomcatMonitorService.getRealtimeMetric(id);
        if (metric == null) {
            return Result.error(404, "暂无监控数据");
        }
        return Result.success(metric);
    }

    /**
     * 获取 Tomcat 历史监控数据
     */
    @GetMapping("/{id}/metric/history")
    public Result<List<TomcatMetricDTO>> getHistoryMetric(
            @PathVariable Long id,
            @RequestParam String startTime,
            @RequestParam String endTime,
            @RequestParam(defaultValue = "5") Integer interval) {

        List<TomcatMetricDTO> metrics = tomcatMonitorService.getHistoryMetric(id, startTime, endTime, interval);
        return Result.success(metrics);
    }

    /**
     * 新增 Tomcat 实例
     */
    @PostMapping
    public Result<Boolean> addInstance(@RequestBody TomcatInstance instance) {
        boolean success = tomcatMonitorService.addInstance(instance);
        if (success) {
            return Result.success(true);
        } else {
            return Result.error(500, "新增实例失败");
        }
    }

    /**
     * 修改 Tomcat 实例
     */
    @PutMapping
    public Result<Boolean> updateInstance(@RequestBody TomcatInstance instance) {
        boolean success = tomcatMonitorService.updateInstance(instance);
        if (success) {
            return Result.success(true);
        } else {
            return Result.error(500, "修改实例失败");
        }
    }

    /**
     * 删除 Tomcat 实例
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteInstance(@PathVariable Long id) {
        boolean success = tomcatMonitorService.deleteInstance(id);
        if (success) {
            return Result.success(true);
        } else {
            return Result.error(500, "删除实例失败");
        }
    }
}