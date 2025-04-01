package com.example.demo.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.Mockito;
import static org.mockito.Mockito.when;

/**
 * PasswordService接口的单元测试
 * 使用模拟实现测试接口约定
 */
public class PasswordServiceTest {

    /**
     * 创建PasswordService的测试实现类，模拟加密和验证逻辑
     */
    private static class TestPasswordServiceImpl implements PasswordService {
        @Override
        public String encryptPassword(String rawPassword) {
            if (rawPassword == null || rawPassword.trim().isEmpty()) {
                throw new IllegalArgumentException("密码不能为空");
            }
            // 简单的加密实现：前缀 + 反转字符串
            return "ENCRYPTED:" + new StringBuilder(rawPassword).reverse().toString();
        }

        @Override
        public boolean matches(String rawPassword, String encodedPassword) {
            if (rawPassword == null || encodedPassword == null) {
                return false;
            }
            // 验证密码匹配
            String expectedEncoded = encryptPassword(rawPassword);
            return expectedEncoded.equals(encodedPassword);
        }
    }

    private PasswordService passwordService;

    @BeforeEach
    public void setUp() {
        // 使用测试实现初始化
        passwordService = new TestPasswordServiceImpl();
    }

    @Test
    public void testEncryptPassword() {
        // 加密测试
        String rawPassword = "password123";
        String encryptedPassword = passwordService.encryptPassword(rawPassword);
        
        assertNotNull(encryptedPassword);
        assertNotEquals(rawPassword, encryptedPassword);
        assertTrue(encryptedPassword.startsWith("ENCRYPTED:"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "\t", "\n"})
    public void testEncryptPasswordWithInvalidInput(String invalidPassword) {
        // 测试无效输入
        assertThrows(IllegalArgumentException.class, () -> {
            passwordService.encryptPassword(invalidPassword);
        });
    }

    @Test
    public void testPasswordMatches() {
        // 密码匹配测试
        String rawPassword = "securePassword";
        String encryptedPassword = passwordService.encryptPassword(rawPassword);
        
        assertTrue(passwordService.matches(rawPassword, encryptedPassword));
        assertFalse(passwordService.matches("wrongPassword", encryptedPassword));
    }

    @Test
    public void testPasswordMatchesWithNullParameters() {
        // 参数为null的情况
        assertFalse(passwordService.matches(null, "ENCRYPTED:abc"));
        assertFalse(passwordService.matches("password", null));
        assertFalse(passwordService.matches(null, null));
    }
    
    @Test
    public void testWithMock() {
        // 使用Mockito模拟测试
        PasswordService mockPasswordService = Mockito.mock(PasswordService.class);
        
        // 配置模拟行为
        when(mockPasswordService.encryptPassword("password123")).thenReturn("MOCKED_HASH");
        when(mockPasswordService.matches("password123", "MOCKED_HASH")).thenReturn(true);
        when(mockPasswordService.matches(Mockito.argThat(arg -> !"password123".equals(arg)), anyString())).thenReturn(false);
        
        // 测试模拟对象
        assertEquals("MOCKED_HASH", mockPasswordService.encryptPassword("password123"));
        assertTrue(mockPasswordService.matches("password123", "MOCKED_HASH"));
        assertFalse(mockPasswordService.matches("wrongpassword", "MOCKED_HASH"));
    }
} 