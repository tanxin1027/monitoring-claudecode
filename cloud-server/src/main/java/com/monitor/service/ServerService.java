package com.monitor.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.monitor.entity.ServerInfo;
import com.monitor.entity.ServerMetric;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 服务器服务接口
 */
public interface ServerService extends IService<ServerInfo> {

    /**
     * 分页查询服务器列表
     */
    Page<ServerInfo> pageList(Integer pageNum, Integer pageSize, Long hospitalId, Integer status, String keyword);

    /**
     * 新增服务器
     */
    boolean addServer(ServerInfo server);

    /**
     * 修改服务器
     */
    boolean updateServer(ServerInfo server);

    /**
     * 删除服务器
     */
    boolean deleteServer(Long id);

    /**
     * 获取最新的监控数据
     */
    ServerMetric getLatestMetric(Long serverId);

    /**
     * 获取历史监控数据
     */
    List<ServerMetric> getHistoryMetric(Long serverId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取服务器统计信息
     */
    ServerStats getServerStats(Long hospitalId);

    /**
     * 服务器统计信息 DTO
     */
    class ServerStats {
        private Integer total;
        private Integer online;
        private Integer offline;
        private Integer disabled;

        public ServerStats(Integer total, Integer online, Integer offline, Integer disabled) {
            this.total = total;
            this.online = online;
            this.offline = offline;
            this.disabled = disabled;
        }

        public Integer getTotal() { return total; }
        public Integer getOnline() { return online; }
        public Integer getOffline() { return offline; }
        public Integer getDisabled() { return disabled; }
    }
}
