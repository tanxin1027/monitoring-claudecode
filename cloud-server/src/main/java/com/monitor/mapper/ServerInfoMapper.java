package com.monitor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.monitor.entity.ServerInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 服务器信息 Mapper
 */
@Mapper
public interface ServerInfoMapper extends BaseMapper<ServerInfo> {

    /**
     * 更新服务器状态
     */
    @Update("UPDATE server_info SET status = #{status}, last_heartbeat_time = NOW() WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    /**
     * 根据 serverCode 查询
     */
    @Select("SELECT * FROM server_info WHERE server_code = #{serverCode}")
    ServerInfo selectByServerCode(@Param("serverCode") String serverCode);
}
