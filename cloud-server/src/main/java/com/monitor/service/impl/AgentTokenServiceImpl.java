package com.monitor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.monitor.entity.AgentToken;
import com.monitor.entity.ServerInfo;
import com.monitor.mapper.AgentTokenMapper;
import com.monitor.mapper.ServerInfoMapper;
import com.monitor.service.AgentTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Agent Token 服务实现
 */
@Service
@RequiredArgsConstructor
public class AgentTokenServiceImpl extends ServiceImpl<AgentTokenMapper, AgentToken> implements AgentTokenService {

    private final ServerInfoMapper serverInfoMapper;

    @Override
    public Page<AgentToken> pageList(Integer pageNum, Integer pageSize, Long serverId) {
        Page<AgentToken> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<AgentToken> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(AgentToken::getCreateTime);

        if (serverId != null) {
            wrapper.eq(AgentToken::getServerId, serverId);
        }

        Page<AgentToken> result = this.page(page, wrapper);

        // 关联查询服务器名称
        if (result.getRecords() != null && !result.getRecords().isEmpty()) {
            Map<Long, String> serverNameMap = new HashMap<>();
            List<Long> serverIds = result.getRecords().stream()
                    .map(AgentToken::getServerId)
                    .distinct()
                    .collect(Collectors.toList());

            for (Long id : serverIds) {
                ServerInfo server = serverInfoMapper.selectById(id);
                if (server != null) {
                    serverNameMap.put(id, server.getServerName());
                }
            }

            for (AgentToken token : result.getRecords()) {
                token.setServerName(serverNameMap.get(token.getServerId()));
            }
        }

        return result;
    }
}
