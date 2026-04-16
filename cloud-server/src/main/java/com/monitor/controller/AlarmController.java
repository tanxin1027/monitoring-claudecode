package com.monitor.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.monitor.dto.AlarmRecordDTO;
import com.monitor.dto.AlarmRuleDTO;
import com.monitor.dto.Result;
import com.monitor.service.AlarmRecordService;
import com.monitor.service.AlarmRuleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 告警管理接口
 */
@Slf4j
@RestController
@RequestMapping("/api/alarm")
@RequiredArgsConstructor
public class AlarmController {

    private final AlarmRuleService alarmRuleService;
    private final AlarmRecordService alarmRecordService;

    // ==================== 告警规则管理 ====================

    /**
     * 分页查询告警规则列表
     */
    @GetMapping("/rule/list")
    public Result<Page<AlarmRuleDTO>> listRules(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {

        try {
            Page<AlarmRuleDTO> page = alarmRuleService.pageList(pageNum, pageSize, status, keyword);
            return Result.success(page);
        } catch (Exception e) {
            log.error("Query alarm rule list error", e);
            return Result.error(500, "查询失败：" + e.getMessage());
        }
    }

    /**
     * 查询所有启用的告警规则
     */
    @GetMapping("/rule/enabled")
    public Result<List<AlarmRuleDTO>> listEnabledRules() {
        try {
            List<AlarmRuleDTO> rules = alarmRuleService.listEnabled();
            return Result.success(rules);
        } catch (Exception e) {
            log.error("Query enabled alarm rules error", e);
            return Result.error(500, "查询失败：" + e.getMessage());
        }
    }

    /**
     * 根据 ID 查询告警规则
     */
    @GetMapping("/rule/{id}")
    public Result<AlarmRuleDTO> getRule(@PathVariable Long id) {
        try {
            AlarmRuleDTO rule = alarmRuleService.getByRuleId(id);
            return rule != null ? Result.success(rule) : Result.error(404, "规则不存在");
        } catch (Exception e) {
            log.error("Get alarm rule error", e);
            return Result.error(500, "查询失败：" + e.getMessage());
        }
    }

    /**
     * 新增告警规则
     */
    @PostMapping("/rule")
    public Result<Boolean> addRule(@RequestBody AlarmRuleDTO ruleDTO) {
        try {
            boolean success = alarmRuleService.add(ruleDTO);
            return success ? Result.success(true) : Result.error(500, "新增失败");
        } catch (Exception e) {
            log.error("Add alarm rule error", e);
            return Result.error(500, "新增失败：" + e.getMessage());
        }
    }

    /**
     * 修改告警规则
     */
    @PutMapping("/rule")
    public Result<Boolean> updateRule(@RequestBody AlarmRuleDTO ruleDTO) {
        try {
            boolean success = alarmRuleService.update(ruleDTO);
            return success ? Result.success(true) : Result.error(500, "修改失败");
        } catch (Exception e) {
            log.error("Update alarm rule error", e);
            return Result.error(500, "修改失败：" + e.getMessage());
        }
    }

    /**
     * 删除告警规则
     */
    @DeleteMapping("/rule/{id}")
    public Result<Boolean> deleteRule(@PathVariable Long id) {
        try {
            boolean success = alarmRuleService.delete(id);
            return success ? Result.success(true) : Result.error(500, "删除失败");
        } catch (Exception e) {
            log.error("Delete alarm rule error", e);
            return Result.error(500, "删除失败：" + e.getMessage());
        }
    }

    /**
     * 启用/禁用告警规则
     */
    @PutMapping("/rule/{id}/toggle")
    public Result<Boolean> toggleRule(@PathVariable Long id, @RequestParam Integer status) {
        try {
            boolean success = alarmRuleService.toggleStatus(id, status);
            return success ? Result.success(true) : Result.error(500, "操作失败");
        } catch (Exception e) {
            log.error("Toggle alarm rule error", e);
            return Result.error(500, "操作失败：" + e.getMessage());
        }
    }

    // ==================== 告警记录管理 ====================

    /**
     * 分页查询告警记录列表
     */
    @GetMapping("/record/list")
    public Result<Page<AlarmRecordDTO>> listRecords(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer severity,
            @RequestParam(required = false) String keyword) {

        try {
            Page<AlarmRecordDTO> page = alarmRecordService.pageList(pageNum, pageSize, status, severity, keyword);
            return Result.success(page);
        } catch (Exception e) {
            log.error("Query alarm record list error", e);
            return Result.error(500, "查询失败：" + e.getMessage());
        }
    }

    /**
     * 根据 ID 查询告警记录
     */
    @GetMapping("/record/{id}")
    public Result<AlarmRecordDTO> getRecord(@PathVariable Long id) {
        try {
            AlarmRecordDTO record = alarmRecordService.getRecordById(id);
            return record != null ? Result.success(record) : Result.error(404, "记录不存在");
        } catch (Exception e) {
            log.error("Get alarm record error", e);
            return Result.error(500, "查询失败：" + e.getMessage());
        }
    }

    /**
     * 处理告警
     */
    @PutMapping("/record/{id}/process")
    public Result<Boolean> processRecord(
            @PathVariable Long id,
            @RequestParam String handler,
            @RequestParam(required = false) String handleRemark) {
        try {
            boolean success = alarmRecordService.process(id, handler, handleRemark);
            return success ? Result.success(true) : Result.error(500, "处理失败");
        } catch (Exception e) {
            log.error("Process alarm record error", e);
            return Result.error(500, "处理失败：" + e.getMessage());
        }
    }

    /**
     * 批量处理告警
     */
    @PutMapping("/record/batch-process")
    public Result<Integer> batchProcessRecords(
            @RequestBody List<Long> ids,
            @RequestParam String handler,
            @RequestParam(required = false) String handleRemark) {
        try {
            int count = alarmRecordService.batchProcess(ids, handler, handleRemark);
            return Result.success(count);
        } catch (Exception e) {
            log.error("Batch process alarm records error", e);
            return Result.error(500, "批量处理失败：" + e.getMessage());
        }
    }

    /**
     * 忽略告警
     */
    @PutMapping("/record/{id}/ignore")
    public Result<Boolean> ignoreRecord(
            @PathVariable Long id,
            @RequestParam String handler) {
        try {
            boolean success = alarmRecordService.ignore(id, handler);
            return success ? Result.success(true) : Result.error(500, "忽略失败");
        } catch (Exception e) {
            log.error("Ignore alarm record error", e);
            return Result.error(500, "忽略失败：" + e.getMessage());
        }
    }

    /**
     * 创建告警记录（由系统自动调用）
     */
    @PostMapping("/record")
    public Result<Boolean> createRecord(@RequestBody AlarmRecordDTO recordDTO) {
        try {
            boolean success = alarmRecordService.createRecord(recordDTO);
            return success ? Result.success(true) : Result.error(500, "创建失败");
        } catch (Exception e) {
            log.error("Create alarm record error", e);
            return Result.error(500, "创建失败：" + e.getMessage());
        }
    }

    /**
     * 统计未处理告警数
     */
    @GetMapping("/record/unprocessed-count")
    public Result<Integer> countUnprocessed() {
        try {
            int count = alarmRecordService.countUnprocessed();
            return Result.success(count);
        } catch (Exception e) {
            log.error("Count unprocessed alarms error", e);
            return Result.error(500, "查询失败：" + e.getMessage());
        }
    }
}
