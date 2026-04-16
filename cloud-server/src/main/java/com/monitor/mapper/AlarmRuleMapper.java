package com.monitor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.monitor.entity.AlarmRule;
import org.apache.ibatis.annotations.Mapper;

/**
 * 告警规则 Mapper
 */
@Mapper
public interface AlarmRuleMapper extends BaseMapper<AlarmRule> {
}
