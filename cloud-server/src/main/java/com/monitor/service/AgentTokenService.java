package com.monitor.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.monitor.entity.AgentToken;

/**
 * Agent Token 服务接口
 */
public interface AgentTokenService extends IService<AgentToken> {

    /**
     * 分页查询 Token 列表
     */
    Page<AgentToken> pageList(Integer pageNum, Integer pageSize, Long serverId);
}
