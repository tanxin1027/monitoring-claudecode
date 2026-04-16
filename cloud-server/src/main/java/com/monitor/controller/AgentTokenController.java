package com.monitor.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.monitor.dto.Result;
import com.monitor.entity.AgentToken;
import com.monitor.service.AgentTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Agent Token 管理接口
 */
@Slf4j
@RestController
@RequestMapping("/api/agent/token")
@RequiredArgsConstructor
public class AgentTokenController {

    private final AgentTokenService agentTokenService;

    /**
     * 分页查询 Token 列表
     */
    @GetMapping("/list")
    public Result<Page<AgentToken>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long serverId) {

        try {
            Page<AgentToken> page = agentTokenService.pageList(pageNum, pageSize, serverId);
            return Result.success(page);
        } catch (Exception e) {
            log.error("Query token list error", e);
            return Result.error(500, "查询失败：" + e.getMessage());
        }
    }

    /**
     * 生成新 Token
     */
    @PostMapping("/generate")
    public Result<Map<String, Object>> generateToken(
            @RequestParam Long serverId,
            @RequestParam(required = false) String remark) {

        try {
            // 生成 Token
            String token = generateUniqueToken();

            AgentToken agentToken = new AgentToken();
            agentToken.setToken(token);
            agentToken.setServerId(serverId);
            agentToken.setRemark(remark);
            agentToken.setStatus(1);

            agentTokenService.save(agentToken);

            Map<String, Object> result = new HashMap<>();
            result.put("tokenId", agentToken.getId());
            result.put("token", token);

            return Result.success(result);

        } catch (Exception e) {
            log.error("Generate token error", e);
            return Result.error(500, "生成失败：" + e.getMessage());
        }
    }

    /**
     * 禁用 Token
     */
    @PutMapping("/{id}/disable")
    public Result<Boolean> disableToken(@PathVariable Long id) {
        try {
            AgentToken token = agentTokenService.getById(id);
            if (token == null) {
                return Result.error(404, "Token 不存在");
            }

            token.setStatus(0);
            agentTokenService.updateById(token);

            return Result.success(true);

        } catch (Exception e) {
            log.error("Disable token error", e);
            return Result.error(500, "操作失败");
        }
    }

    /**
     * 启用 Token
     */
    @PutMapping("/{id}/enable")
    public Result<Boolean> enableToken(@PathVariable Long id) {
        try {
            AgentToken token = agentTokenService.getById(id);
            if (token == null) {
                return Result.error(404, "Token 不存在");
            }

            token.setStatus(1);
            agentTokenService.updateById(token);

            return Result.success(true);

        } catch (Exception e) {
            log.error("Enable token error", e);
            return Result.error(500, "操作失败");
        }
    }

    /**
     * 删除 Token
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteToken(@PathVariable Long id) {
        try {
            boolean success = agentTokenService.removeById(id);
            return success ? Result.success(true) : Result.error(500, "删除失败");
        } catch (Exception e) {
            log.error("Delete token error", e);
            return Result.error(500, "删除失败：" + e.getMessage());
        }
    }

    /**
     * 生成唯一 Token
     */
    private String generateUniqueToken() {
        return "agt_" + UUID.randomUUID().toString().replace("-", "");
    }
}
