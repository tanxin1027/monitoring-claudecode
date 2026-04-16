package com.monitor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.monitor.entity.AgentToken;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * Agent Token Mapper
 */
@Mapper
public interface AgentTokenMapper extends BaseMapper<AgentToken> {

    /**
     * 根据 token 查询
     */
    @Select("SELECT * FROM agent_token WHERE token = #{token}")
    AgentToken selectByToken(@Param("token") String token);
}
