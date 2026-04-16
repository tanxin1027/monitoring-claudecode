package com.monitor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.monitor.entity.ServerInfo;
import com.monitor.entity.ServerMetric;
import com.monitor.mapper.ServerInfoMapper;
import com.monitor.mapper.ServerMetricMapper;
import com.monitor.service.ServerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 服务器服务实现
 */
@Service
@RequiredArgsConstructor
public class ServerServiceImpl extends ServiceImpl<ServerInfoMapper, ServerInfo> implements ServerService {

    private final ServerMetricMapper serverMetricMapper;

    @Override
    public Page<ServerInfo> pageList(Integer pageNum, Integer pageSize, Long hospitalId, Integer status, String keyword) {
        Page<ServerInfo> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<ServerInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(ServerInfo::getCreateTime);

        if (hospitalId != null) {
            wrapper.eq(ServerInfo::getHospitalId, hospitalId);
        }

        if (status != null) {
            wrapper.eq(ServerInfo::getStatus, status);
        }

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w
                .like(ServerInfo::getServerName, keyword)
                .or().like(ServerInfo::getServerCode, keyword)
                .or().like(ServerInfo::getIpAddress, keyword)
            );
        }

        return this.page(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addServer(ServerInfo server) {
        server.setStatus(0); // 初始状态为离线
        return this.save(server);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateServer(ServerInfo server) {
        return this.updateById(server);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteServer(Long id) {
        return this.removeById(id);
    }

    @Override
    public ServerMetric getLatestMetric(Long serverId) {
        LambdaQueryWrapper<ServerMetric> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ServerMetric::getServerId, serverId);
        wrapper.orderByDesc(ServerMetric::getCollectTime);
        wrapper.last("LIMIT 1");
        return serverMetricMapper.selectOne(wrapper);
    }

    @Override
    public List<ServerMetric> getHistoryMetric(Long serverId, LocalDateTime startTime, LocalDateTime endTime) {
        return serverMetricMapper.selectByServerIdAndTimeRange(serverId, startTime, endTime);
    }

    @Override
    public ServerStats getServerStats(Long hospitalId) {
        LambdaQueryWrapper<ServerInfo> wrapper = new LambdaQueryWrapper<>();

        if (hospitalId != null) {
            wrapper.eq(ServerInfo::getHospitalId, hospitalId);
        }

        long total = this.count(wrapper);

        wrapper.eq(ServerInfo::getStatus, 1);
        long online = this.count(wrapper);

        wrapper = new LambdaQueryWrapper<>();
        if (hospitalId != null) {
            wrapper.eq(ServerInfo::getHospitalId, hospitalId);
        }
        wrapper.eq(ServerInfo::getStatus, 0);
        long offline = this.count(wrapper);

        wrapper = new LambdaQueryWrapper<>();
        if (hospitalId != null) {
            wrapper.eq(ServerInfo::getHospitalId, hospitalId);
        }
        wrapper.eq(ServerInfo::getStatus, 2);
        long disabled = this.count(wrapper);

        return new ServerStats((int)total, (int)online, (int)offline, (int)disabled);
    }
}
