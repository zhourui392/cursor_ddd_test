package com.example.demo.infrastructure.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 简单测试admin用户密码验证
 * 不依赖Spring上下文，只使用BCryptPasswordEncoder
 */
public class AdminPasswordSimpleTest {

    /**
     * 使用BCryptPasswordEncoder直接验证密码
     */
    @Test
    @DisplayName("验证admin用户密码是否为123456")
    public void testAdminPassword() {
        // 数据库中存储的admin用户密码哈希值
        String storedPasswordHash = "$2a$10$uxN9VpNQF4abH1W5fR41K.0xdsgEOZO2eLGy4KPUgm8/QWrSNT656";
        
        // 创建BCryptPasswordEncoder实例
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // 验证密码
        boolean matches = encoder.matches("123456", storedPasswordHash);
        
        // 断言密码匹配
        assertTrue(matches, "Admin密码应该是123456");
    }
}
