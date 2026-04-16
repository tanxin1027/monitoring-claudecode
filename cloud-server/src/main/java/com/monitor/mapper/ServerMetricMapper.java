package com.monitor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.monitor.entity.ServerMetric;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 服务器性能数据 Mapper
 */
@Mapper
public interface ServerMetricMapper extends BaseMapper<ServerMetric> {

    /**
     * 批量插入
     */
    int batchInsert(@Param("list") List<ServerMetric> list);

    /**
     * 根据服务器 ID 和时间范围查询
     */
    List<ServerMetric> selectByServerIdAndTimeRange(
        @Param("serverId") Long serverId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );
}
