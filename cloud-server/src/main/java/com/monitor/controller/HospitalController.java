package com.monitor.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.monitor.dto.Result;
import com.monitor.entity.HospitalInfo;
import com.monitor.service.HospitalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 医院管理接口
 */
@Slf4j
@RestController
@RequestMapping("/api/hospital")
@RequiredArgsConstructor
public class HospitalController {

    private final HospitalService hospitalService;

    /**
     * 分页查询医院列表
     */
    @GetMapping("/list")
    public Result<Page<HospitalInfo>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword) {

        try {
            Page<HospitalInfo> page = hospitalService.pageList(pageNum, pageSize, keyword);
            return Result.success(page);
        } catch (Exception e) {
            log.error("Query hospital list error", e);
            return Result.error(500, "查询失败：" + e.getMessage());
        }
    }

    /**
     * 获取医院详情
     */
    @GetMapping("/{id}")
    public Result<HospitalInfo> get(@PathVariable Long id) {
        try {
            HospitalInfo hospital = hospitalService.getById(id);
            if (hospital == null) {
                return Result.error(404, "医院不存在");
            }
            return Result.success(hospital);
        } catch (Exception e) {
            log.error("Get hospital detail error", e);
            return Result.error(500, "查询失败");
        }
    }

    /**
     * 新增医院
     */
    @PostMapping
    public Result<Boolean> add(@RequestBody HospitalInfo hospital) {
        try {
            boolean success = hospitalService.addHospital(hospital);
            return success ? Result.success(true) : Result.error(500, "新增失败");
        } catch (Exception e) {
            log.error("Add hospital error", e);
            return Result.error(500, "新增失败：" + e.getMessage());
        }
    }

    /**
     * 修改医院
     */
    @PutMapping
    public Result<Boolean> update(@RequestBody HospitalInfo hospital) {
        try {
            boolean success = hospitalService.updateHospital(hospital);
            return success ? Result.success(true) : Result.error(500, "修改失败");
        } catch (Exception e) {
            log.error("Update hospital error", e);
            return Result.error(500, "修改失败：" + e.getMessage());
        }
    }

    /**
     * 删除医院
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        try {
            boolean success = hospitalService.deleteHospital(id);
            return success ? Result.success(true) : Result.error(500, "删除失败");
        } catch (Exception e) {
            log.error("Delete hospital error", e);
            return Result.error(500, "删除失败：" + e.getMessage());
        }
    }

    /**
     * 查询所有医院（下拉框用）
     */
    @GetMapping("/all")
    public Result<List<HospitalInfo>> getAll() {
        try {
            List<HospitalInfo> list = hospitalService.list(
                new LambdaQueryWrapper<HospitalInfo>().eq(HospitalInfo::getStatus, 1)
            );
            return Result.success(list);
        } catch (Exception e) {
            log.error("Get all hospitals error", e);
            return Result.error(500, "查询失败");
        }
    }
}
