package com.monitor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.monitor.entity.TomcatInstance;
import org.apache.ibatis.annotations.Mapper;

/**
 * Tomcat 实例 Mapper
 */
@Mapper
public interface TomcatInstanceMapper extends BaseMapper<TomcatInstance> {
}
