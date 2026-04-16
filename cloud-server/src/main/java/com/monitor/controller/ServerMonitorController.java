package com.monitor.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.monitor.dto.Result;
import com.monitor.entity.ServerInfo;
import com.monitor.entity.ServerMetric;
import com.monitor.service.ServerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 服务器监控接口
 */
@Slf4j
@RestController
@RequestMapping("/api/monitor/server")
@RequiredArgsConstructor
public class ServerMonitorController {

    private final ServerService serverService;

    /**
     * 分页查询服务器列表
     */
    @GetMapping("/list")
    public Result<Page<ServerInfo>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long hospitalId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {

        try {
            Page<ServerInfo> page = serverService.pageList(pageNum, pageSize, hospitalId, status, keyword);
            return Result.success(page);
        } catch (Exception e) {
            log.error("Query server list error", e);
            return Result.error(500, "查询失败：" + e.getMessage());
        }
    }

    /**
     * 获取服务器详情
     */
    @GetMapping("/{id}")
    public Result<ServerInfo> get(@PathVariable Long id) {
        try {
            ServerInfo server = serverService.getById(id);
            if (server == null) {
                return Result.error(404, "服务器不存在");
            }
            return Result.success(server);
        } catch (Exception e) {
            log.error("Get server detail error", e);
            return Result.error(500, "查询失败");
        }
    }

    /**
     * 获取服务器监控数据（最新）
     */
    @GetMapping("/{id}/metric")
    public Result<ServerMetric> getLatestMetric(@PathVariable Long id) {
        try {
            ServerMetric metric = serverService.getLatestMetric(id);
            return metric != null ? Result.success(metric) : Result.success(null);
        } catch (Exception e) {
            log.error("Get server metric error", e);
            return Result.error(500, "查询失败");
        }
    }

    /**
     * 获取服务器历史监控数据
     */
    @GetMapping("/{id}/metric/history")
    public Result<List<ServerMetric>> getHistoryMetric(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {

        try {
            List<ServerMetric> metrics = serverService.getHistoryMetric(id, startTime, endTime);
            return Result.success(metrics);
        } catch (Exception e) {
            log.error("Get history metric error", e);
            return Result.error(500, "查询失败：" + e.getMessage());
        }
    }

    /**
     * 新增服务器
     */
    @PostMapping
    public Result<Boolean> add(@RequestBody ServerInfo server) {
        try {
            boolean success = serverService.addServer(server);
            return success ? Result.success(true) : Result.error(500, "新增失败");
        } catch (Exception e) {
            log.error("Add server error", e);
            return Result.error(500, "新增失败：" + e.getMessage());
        }
    }

    /**
     * 修改服务器
     */
    @PutMapping
    public Result<Boolean> update(@RequestBody ServerInfo server) {
        try {
            boolean success = serverService.updateServer(server);
            return success ? Result.success(true) : Result.error(500, "修改失败");
        } catch (Exception e) {
            log.error("Update server error", e);
            return Result.error(500, "修改失败：" + e.getMessage());
        }
    }

    /**
     * 删除服务器
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        try {
            boolean success = serverService.deleteServer(id);
            return success ? Result.success(true) : Result.error(500, "删除失败");
        } catch (Exception e) {
            log.error("Delete server error", e);
            return Result.error(500, "删除失败：" + e.getMessage());
        }
    }

    /**
     * 获取服务器状态统计
     */
    @GetMapping("/stats")
    public Result<ServerService.ServerStats> getStats(@RequestParam(required = false) Long hospitalId) {
        try {
            ServerService.ServerStats stats = serverService.getServerStats(hospitalId);
            return Result.success(stats);
        } catch (Exception e) {
            log.error("Get server stats error", e);
            return Result.error(500, "查询失败");
        }
    }
}
