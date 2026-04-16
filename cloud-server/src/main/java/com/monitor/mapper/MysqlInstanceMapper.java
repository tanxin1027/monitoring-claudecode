package com.monitor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.monitor.entity.MysqlInstance;
import org.apache.ibatis.annotations.Mapper;

/**
 * MySQL 实例 Mapper
 */
@Mapper
public interface MysqlInstanceMapper extends BaseMapper<MysqlInstance> {
}
