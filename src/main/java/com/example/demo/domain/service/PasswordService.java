package com.example.demo.domain.service;

/**
 * 密码领域服务接口
 */
public interface PasswordService {
    
    /**
     * 加密密码
     * 
     * @param rawPassword 原始密码
     * @return 加密后的密码
     */
    String encryptPassword(String rawPassword);
    
    /**
     * 验证密码
     * 
     * @param rawPassword 原始密码
     * @param encodedPassword 加密后的密码
     * @return 是否匹配
     */
    boolean matches(String rawPassword, String encodedPassword);
} 