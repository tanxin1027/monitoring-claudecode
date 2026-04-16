package com.monitor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.monitor.entity.HospitalInfo;
import com.monitor.mapper.HospitalInfoMapper;
import com.monitor.service.HospitalService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 医院服务实现
 */
@Service
public class HospitalServiceImpl extends ServiceImpl<HospitalInfoMapper, HospitalInfo> implements HospitalService {

    @Override
    public Page<HospitalInfo> pageList(Integer pageNum, Integer pageSize, String keyword) {
        Page<HospitalInfo> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<HospitalInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(HospitalInfo::getCreateTime);

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w
                .like(HospitalInfo::getHospitalName, keyword)
                .or().like(HospitalInfo::getHospitalCode, keyword)
                .or().like(HospitalInfo::getProvince, keyword)
                .or().like(HospitalInfo::getCity, keyword)
            );
        }

        return this.page(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addHospital(HospitalInfo hospital) {
        return this.save(hospital);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateHospital(HospitalInfo hospital) {
        return this.updateById(hospital);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteHospital(Long id) {
        // TODO: 检查是否有关联的服务器
        return this.removeById(id);
    }
}
