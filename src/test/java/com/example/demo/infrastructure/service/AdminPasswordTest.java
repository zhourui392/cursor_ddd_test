package com.example.demo.infrastructure.service;

import com.example.demo.domain.service.PasswordService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 测试admin用户密码验证
 */
@SpringBootTest
@ActiveProfiles("test")
public class AdminPasswordTest {

    @Autowired
    private PasswordService passwordService;

    /**
     * 测试admin用户的密码是否为123456
     * 
     * 数据库中存储的admin用户密码哈希值为：
     * $2a$12$fsdyyZAzWd1waqAMHn14oeTWrwkpzgo8M2VXKwbtTfX9wUoVg/4OK
     */
    @Test
    @DisplayName("验证admin用户密码是否为123456")
    public void testAdminPassword() {
        // 数据库中存储的admin用户密码哈希值
        String storedPasswordHash = "$2a$12$fsdyyZAzWd1waqAMHn14oeTWrwkpzgo8M2VXKwbtTfX9wUoVg/4OK";
        
        // 使用密码服务验证密码
        boolean matches = passwordService.matches("123456", storedPasswordHash);
        
        // 断言密码匹配
        assertTrue(matches, "Admin密码应该是123456");
    }
    
    /**
     * 使用BCryptPasswordEncoder直接验证密码
     * 这个测试不依赖于应用程序的PasswordService实现
     */
    @Test
    @DisplayName("使用BCryptPasswordEncoder直接验证admin密码")
    public void testAdminPasswordWithBCrypt() {
        // 数据库中存储的admin用户密码哈希值
        String storedPasswordHash = "$2a$12$fsdyyZAzWd1waqAMHn14oeTWrwkpzgo8M2VXKwbtTfX9wUoVg/4OK";
        
        // 创建BCryptPasswordEncoder实例
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // 直接使用encoder验证密码
        boolean matches = encoder.matches("123456", storedPasswordHash);
        
        // 断言密码匹配
        assertTrue(matches, "Admin密码应该是123456");
    }
}
