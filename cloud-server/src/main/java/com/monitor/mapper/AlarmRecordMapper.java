package com.monitor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.monitor.entity.AlarmRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 告警记录 Mapper
 */
@Mapper
public interface AlarmRecordMapper extends BaseMapper<AlarmRecord> {
}
