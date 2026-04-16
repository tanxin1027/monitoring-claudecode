package com.monitor.controller;

import com.monitor.dto.Result;
import com.monitor.entity.SysUser;
import com.monitor.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 测试接口
 */
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestController {

    private final SysUserMapper sysUserMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * 重置 admin 密码为 admin123
     */
    @PostMapping("/reset-password")
    public Result<Map<String, Object>> resetPassword() {
        try {
            String encoded = passwordEncoder.encode("admin123");
            System.out.println("Encoded password: " + encoded);

            SysUser user = sysUserMapper.selectByUsername("admin");
            if (user != null) {
                user.setPassword(encoded);
                sysUserMapper.updateById(user);
            } else {
                user = new SysUser();
                user.setUsername("admin");
                user.setPassword(encoded);
                user.setRealName("系统管理员");
                user.setStatus(1);
                sysUserMapper.insert(user);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("encodedPassword", encoded);
            return Result.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, e.getMessage());
        }
    }
}
