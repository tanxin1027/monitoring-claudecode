package com.monitor.controller;

import com.monitor.dto.Result;
import com.monitor.service.ServerService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 数据可视化展示接口
 */
@Slf4j
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final ServerService serverService;

    /**
     * 获取仪表盘概览数据
     */
    @GetMapping("/overview")
    public Result<DashboardOverview> getOverview(@RequestParam(required = false) Long hospitalId) {
        try {
            // 服务器统计
            ServerService.ServerStats serverStats = serverService.getServerStats(hospitalId);

            DashboardOverview overview = new DashboardOverview();
            overview.setServerTotal(serverStats.getTotal());
            overview.setServerOnline(serverStats.getOnline());
            overview.setServerOffline(serverStats.getOffline());

            // TODO: 添加 MySQL、Tomcat 统计
            overview.setMysqlTotal(0);
            overview.setTomcatTotal(0);

            return Result.success(overview);

        } catch (Exception e) {
            log.error("Get dashboard overview error", e);
            return Result.error(500, "查询失败");
        }
    }

    /**
     * 获取医院列表（用于图表展示）
     */
    @GetMapping("/hospitals")
    public Result<List<HospitalStat>> getHospitalStats() {
        try {
            // TODO: 实现医院统计查询
            List<HospitalStat> stats = new ArrayList<>();
            return Result.success(stats);

        } catch (Exception e) {
            log.error("Get hospital stats error", e);
            return Result.error(500, "查询失败");
        }
    }

    /**
     * 获取告警统计
     */
    @GetMapping("/alarm-stats")
    public Result<AlarmStats> getAlarmStats(@RequestParam(required = false) Long hospitalId) {
        try {
            // TODO: 实现告警统计查询
            AlarmStats stats = new AlarmStats();
            stats.setTodayCount(0);
            stats.setUnhandleCount(0);
            stats.setSeverityLevels(new int[]{0, 0, 0});

            return Result.success(stats);

        } catch (Exception e) {
            log.error("Get alarm stats error", e);
            return Result.error(500, "查询失败");
        }
    }

    /**
     * 获取实时性能排行（CPU  Top10）
     */
    @GetMapping("/top/cpu")
    public Result<List<ServerRanking>> getCpuTop(@RequestParam(defaultValue = "10") Integer limit) {
        try {
            // TODO: 实现 CPU 排行查询
            List<ServerRanking> rankings = new ArrayList<>();
            return Result.success(rankings);

        } catch (Exception e) {
            log.error("Get CPU top error", e);
            return Result.error(500, "查询失败");
        }
    }

    /**
     * 获取实时性能排行（内存 Top10）
     */
    @GetMapping("/top/memory")
    public Result<List<ServerRanking>> getMemoryTop(@RequestParam(defaultValue = "10") Integer limit) {
        try {
            // TODO: 实现内存排行查询
            List<ServerRanking> rankings = new ArrayList<>();
            return Result.success(rankings);

        } catch (Exception e) {
            log.error("Get memory top error", e);
            return Result.error(500, "查询失败");
        }
    }

    /**
     * 仪表盘概览数据
     */
    @Data
    public static class DashboardOverview {
        private Integer serverTotal;
        private Integer serverOnline;
        private Integer serverOffline;
        private Integer mysqlTotal;
        private Integer tomcatTotal;
    }

    /**
     * 医院统计
     */
    @Data
    public static class HospitalStat {
        private Long hospitalId;
        private String hospitalName;
        private Integer serverCount;
        private Integer onlineCount;
    }

    /**
     * 告警统计
     */
    @Data
    public static class AlarmStats {
        private Integer todayCount;      // 今日告警数
        private Integer unhandleCount;   // 未处理告警数
        private int[] severityLevels;    // 各等级告警数 [提示，警告，严重]
    }

    /**
     * 服务器排行
     */
    @Data
    public static class ServerRanking {
        private Long serverId;
        private String serverName;
        private Double value;            // CPU 或内存使用率
        private Long collectTime;        // 采集时间
    }
}
