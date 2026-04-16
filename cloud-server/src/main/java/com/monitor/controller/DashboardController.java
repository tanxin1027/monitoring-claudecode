package com.monitor.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.monitor.dto.Result;
import com.monitor.entity.AlarmRecord;
import com.monitor.entity.MysqlInstance;
import com.monitor.entity.ServerInfo;
import com.monitor.entity.ServerMetric;
import com.monitor.entity.TomcatInstance;
import com.monitor.mapper.AlarmRecordMapper;
import com.monitor.mapper.MysqlInstanceMapper;
import com.monitor.mapper.ServerInfoMapper;
import com.monitor.mapper.ServerMetricMapper;
import com.monitor.mapper.TomcatInstanceMapper;
import com.monitor.service.ServerService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 数据可视化展示接口
 */
@Slf4j
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final ServerService serverService;
    private final ServerInfoMapper serverInfoMapper;
    private final ServerMetricMapper serverMetricMapper;
    private final MysqlInstanceMapper mysqlInstanceMapper;
    private final TomcatInstanceMapper tomcatInstanceMapper;
    private final AlarmRecordMapper alarmRecordMapper;

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

            // MySQL 实例统计
            LambdaQueryWrapper<MysqlInstance> mysqlWrapper = new LambdaQueryWrapper<>();
            if (hospitalId != null) {
                mysqlWrapper.eq(MysqlInstance::getHospitalId, hospitalId);
            }
            long mysqlTotal = mysqlInstanceMapper.selectCount(mysqlWrapper);
            mysqlWrapper.eq(MysqlInstance::getStatus, 1);
            long mysqlOnline = mysqlInstanceMapper.selectCount(mysqlWrapper);

            // Tomcat 实例统计
            LambdaQueryWrapper<TomcatInstance> tomcatWrapper = new LambdaQueryWrapper<>();
            if (hospitalId != null) {
                tomcatWrapper.eq(TomcatInstance::getHospitalId, hospitalId);
            }
            long tomcatTotal = tomcatInstanceMapper.selectCount(tomcatWrapper);
            tomcatWrapper.eq(TomcatInstance::getStatus, 1);
            long tomcatOnline = tomcatInstanceMapper.selectCount(tomcatWrapper);

            overview.setMysqlTotal((int) mysqlTotal);
            overview.setMysqlOnline((int) mysqlOnline);
            overview.setTomcatTotal((int) tomcatTotal);
            overview.setTomcatOnline((int) tomcatOnline);

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
            // 未处理告警数
            LambdaQueryWrapper<AlarmRecord> unhandleWrapper = new LambdaQueryWrapper<>();
            unhandleWrapper.eq(AlarmRecord::getStatus, 0);
            if (hospitalId != null) {
                // 需要根据实例 ID 关联查询医院 ID，这里简化处理
            }
            long unhandleCount = alarmRecordMapper.selectCount(unhandleWrapper);

            // 今日告警数
            LocalDateTime todayStart = LocalDate.now().atStartOfDay();
            LambdaQueryWrapper<AlarmRecord> todayWrapper = new LambdaQueryWrapper<>();
            todayWrapper.ge(AlarmRecord::getAlarmTime, todayStart);
            long todayCount = alarmRecordMapper.selectCount(todayWrapper);

            // 各等级告警数
            int[] severityLevels = new int[3];
            for (int i = 1; i <= 3; i++) {
                final int severity = i;
                LambdaQueryWrapper<AlarmRecord> severityWrapper = new LambdaQueryWrapper<>();
                severityWrapper.eq(AlarmRecord::getSeverity, severity);
                severityWrapper.ge(AlarmRecord::getAlarmTime, todayStart);
                severityLevels[i - 1] = (int) alarmRecordMapper.selectCount(severityWrapper).longValue();
            }

            AlarmStats stats = new AlarmStats();
            stats.setUnhandleCount((int) unhandleCount);
            stats.setTodayCount((int) todayCount);
            stats.setSeverityLevels(severityLevels);

            return Result.success(stats);

        } catch (Exception e) {
            log.error("Get alarm stats error", e);
            return Result.error(500, "查询失败");
        }
    }

    /**
     * 获取实时性能排行（CPU Top10）
     */
    @GetMapping("/top/cpu")
    public Result<List<ServerRanking>> getCpuTop(@RequestParam(defaultValue = "10") Integer limit) {
        try {
            // 查询最新的 CPU 使用率排行
            // 先获取每个服务器最新的监控记录，然后按 CPU 使用率排序
            List<ServerRanking> rankings = serverMetricMapper.getCpuTop(limit);
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
            // 查询最新的内存使用率排行
            List<ServerRanking> rankings = serverMetricMapper.getMemoryTop(limit);
            return Result.success(rankings);

        } catch (Exception e) {
            log.error("Get memory top error", e);
            return Result.error(500, "查询失败");
        }
    }

    /**
     * 获取最新告警记录
     */
    @GetMapping("/alarm/recent")
    public Result<List<AlarmRecord>> getRecentAlarms(@RequestParam(defaultValue = "10") Integer limit) {
        try {
            LambdaQueryWrapper<AlarmRecord> wrapper = new LambdaQueryWrapper<>();
            wrapper.orderByDesc(AlarmRecord::getAlarmTime);
            wrapper.last("LIMIT " + limit);
            List<AlarmRecord> records = alarmRecordMapper.selectList(wrapper);
            return Result.success(records);

        } catch (Exception e) {
            log.error("Get recent alarms error", e);
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
        private Integer mysqlOnline;
        private Integer tomcatTotal;
        private Integer tomcatOnline;
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
        private Long collectTime;        // 采集时间（秒级时间戳）
    }
}
