package com.monitor.controller;

import com.monitor.dto.Result;
import com.monitor.entity.SysUser;
import com.monitor.mapper.SysUserMapper;
import com.monitor.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 初始化接口
 */
@Slf4j
@RestController
@RequestMapping("/api/init")
@RequiredArgsConstructor
public class InitController {

    private final SysUserMapper sysUserMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    /**
     * 初始化管理员用户
     */
    @PostMapping("/admin")
    public Result<Map<String, Object>> initAdmin() {
        try {
            // 检查是否已存在 admin 用户
            SysUser existingUser = sysUserMapper.selectByUsername("admin");
            if (existingUser != null) {
                // 更新密码为 admin123
                existingUser.setPassword(passwordEncoder.encode("admin123"));
                existingUser.setRealName("系统管理员");
                existingUser.setStatus(1);
                existingUser.setUpdateTime(LocalDateTime.now());
                sysUserMapper.updateById(existingUser);
                log.info("已更新 admin 用户密码");
            } else {
                SysUser user = new SysUser();
                user.setUsername("admin");
                user.setPassword(passwordEncoder.encode("admin123"));
                user.setRealName("系统管理员");
                user.setStatus(1);
                sysUserMapper.insert(user);
                log.info("已创建 admin 用户");
            }

            // 生成 token
            SysUser user = sysUserMapper.selectByUsername("admin");
            String token = jwtUtil.generateToken(user.getId(), user.getUsername());

            Map<String, Object> result = new HashMap<>();
            result.put("token", token);
            result.put("userId", user.getId());
            result.put("username", user.getUsername());
            result.put("realName", user.getRealName());

            return Result.success(result);

        } catch (Exception e) {
            log.error("Init admin error", e);
            return Result.error(500, "初始化失败：" + e.getMessage());
        }
    }
}
