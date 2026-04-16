package com.monitor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.monitor.entity.TomcatMetric;
import org.apache.ibatis.annotations.Mapper;

/**
 * Tomcat 性能数据 Mapper
 */
@Mapper
public interface TomcatMetricMapper extends BaseMapper<TomcatMetric> {
}
