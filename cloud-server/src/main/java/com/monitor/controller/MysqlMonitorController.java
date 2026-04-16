package com.monitor.controller;

import com.monitor.dto.MysqlMetricDTO;
import com.monitor.dto.PageResult;
import com.monitor.dto.Result;
import com.monitor.entity.MysqlInstance;
import com.monitor.service.MysqlMonitorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * MySQL 监控管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/monitor/mysql")
@RequiredArgsConstructor
public class MysqlMonitorController {

    private final MysqlMonitorService mysqlMonitorService;

    /**
     * 获取 MySQL 实例列表（分页）
     */
    @GetMapping("/list")
    public Result<PageResult<MysqlInstance>> getInstanceList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String searchKey) {

        PageResult<MysqlInstance> result = mysqlMonitorService.getInstanceList(pageNum, pageSize, searchKey);
        return Result.success(result);
    }

    /**
     * 获取 MySQL 实例详情
     */
    @GetMapping("/{id}")
    public Result<MysqlInstance> getInstanceDetail(@PathVariable Long id) {
        MysqlInstance instance = mysqlMonitorService.getInstanceDetail(id);
        if (instance == null) {
            return Result.error(404, "MySQL 实例不存在");
        }
        return Result.success(instance);
    }

    /**
     * 获取 MySQL 实时监控数据
     */
    @GetMapping("/{id}/metric")
    public Result<MysqlMetricDTO> getRealtimeMetric(@PathVariable Long id) {
        MysqlMetricDTO metric = mysqlMonitorService.getRealtimeMetric(id);
        if (metric == null) {
            return Result.error(404, "暂无监控数据");
        }
        return Result.success(metric);
    }

    /**
     * 获取 MySQL 历史监控数据
     */
    @GetMapping("/{id}/metric/history")
    public Result<List<MysqlMetricDTO>> getHistoryMetric(
            @PathVariable Long id,
            @RequestParam String startTime,
            @RequestParam String endTime,
            @RequestParam(defaultValue = "5") Integer interval) {

        List<MysqlMetricDTO> metrics = mysqlMonitorService.getHistoryMetric(id, startTime, endTime, interval);
        return Result.success(metrics);
    }

    /**
     * 新增 MySQL 实例
     */
    @PostMapping
    public Result<Boolean> addInstance(@RequestBody MysqlInstance instance) {
        boolean success = mysqlMonitorService.addInstance(instance);
        if (success) {
            return Result.success(true);
        } else {
            return Result.error(500, "新增实例失败");
        }
    }

    /**
     * 修改 MySQL 实例
     */
    @PutMapping
    public Result<Boolean> updateInstance(@RequestBody MysqlInstance instance) {
        boolean success = mysqlMonitorService.updateInstance(instance);
        if (success) {
            return Result.success(true);
        } else {
            return Result.error(500, "修改实例失败");
        }
    }

    /**
     * 删除 MySQL 实例
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteInstance(@PathVariable Long id) {
        boolean success = mysqlMonitorService.deleteInstance(id);
        if (success) {
            return Result.success(true);
        } else {
            return Result.error(500, "删除实例失败");
        }
    }
}