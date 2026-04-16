package com.monitor.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.monitor.entity.HospitalInfo;

/**
 * 医院服务接口
 */
public interface HospitalService extends IService<HospitalInfo> {

    /**
     * 分页查询医院列表
     */
    Page<HospitalInfo> pageList(Integer pageNum, Integer pageSize, String keyword);

    /**
     * 新增医院
     */
    boolean addHospital(HospitalInfo hospital);

    /**
     * 修改医院
     */
    boolean updateHospital(HospitalInfo hospital);

    /**
     * 删除医院
     */
    boolean deleteHospital(Long id);
}
