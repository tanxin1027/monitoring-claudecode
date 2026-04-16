package com.monitor.controller;

import com.monitor.dto.LoginRequest;
import com.monitor.dto.Result;
import com.monitor.entity.SysUser;
import com.monitor.mapper.SysUserMapper;
import com.monitor.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证接口
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final SysUserMapper sysUserMapper;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody LoginRequest request) {
        try {
            // 查询用户
            SysUser user = sysUserMapper.selectByUsername(request.getUsername());

            if (user == null) {
                return Result.error(401, "用户名或密码错误");
            }

            // 验证密码
            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                return Result.error(401, "用户名或密码错误");
            }

            // 检查用户状态
            if (user.getStatus() == 0) {
                return Result.error(401, "用户已被禁用");
            }

            // 生成 Token
            String token = jwtUtil.generateToken(user.getId(), user.getUsername());

            Map<String, Object> result = new HashMap<>();
            result.put("token", token);
            result.put("userId", user.getId());
            result.put("username", user.getUsername());
            result.put("realName", user.getRealName());

            return Result.success(result);

        } catch (Exception e) {
            log.error("Login error", e);
            return Result.error(500, "登录失败：" + e.getMessage());
        }
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/info")
    public Result<Map<String, Object>> getUserInfo(@RequestHeader("Authorization") String token) {
        try {
            // 去除 Bearer 前缀
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            Long userId = jwtUtil.getUserIdFromToken(token);
            SysUser user = sysUserMapper.selectById(userId);

            if (user == null) {
                return Result.error(404, "用户不存在");
            }

            Map<String, Object> result = new HashMap<>();
            result.put("id", user.getId());
            result.put("username", user.getUsername());
            result.put("realName", user.getRealName());
            result.put("email", user.getEmail());
            result.put("phone", user.getPhone());

            return Result.success(result);

        } catch (Exception e) {
            log.error("Get user info error", e);
            return Result.error(500, "获取用户信息失败");
        }
    }
}
