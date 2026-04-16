package com.monitor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.monitor.entity.MysqlMetric;
import org.apache.ibatis.annotations.Mapper;

/**
 * MySQL 性能数据 Mapper
 */
@Mapper
public interface MysqlMetricMapper extends BaseMapper<MysqlMetric> {
}
