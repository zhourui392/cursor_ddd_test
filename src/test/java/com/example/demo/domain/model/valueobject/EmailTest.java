package com.example.demo.domain.model.valueobject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Email值对象的单元测试
 */
public class EmailTest {

    @Test
    public void testValidEmail() {
        // 有效的邮箱地址
        Email email = new Email("test@example.com");
        assertEquals("test@example.com", email.getValue());
        assertFalse(email.isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "user@domain.com",
            "user.name@domain.com",
            "user+tag@domain.com",
            "user@sub.domain.com",
            "user@domain-name.com",
            "123@domain.com",
            "user@123.com"
    })
    public void testMultipleValidEmails(String validEmail) {
        // 测试多种有效邮箱格式
        Email email = new Email(validEmail);
        assertEquals(validEmail, email.getValue());
    }

    @Test
    public void testInvalidEmail() {
        // 无效的邮箱地址
        assertThrows(IllegalArgumentException.class, () -> new Email("invalid-email"));
        assertThrows(IllegalArgumentException.class, () -> new Email("user@"));
        assertThrows(IllegalArgumentException.class, () -> new Email("@domain.com"));
        assertThrows(IllegalArgumentException.class, () -> new Email("user@domain"));
        assertThrows(IllegalArgumentException.class, () -> new Email("user@.com"));
        assertThrows(IllegalArgumentException.class, () -> new Email("user domain.com"));
    }

    @Test
    public void testNullAndEmptyEmail() {
        // null和空字符串处理
        Email nullEmail = new Email(null);
        assertTrue(nullEmail.isEmpty());
        assertNull(nullEmail.getValue());

        Email emptyEmail = new Email("");
        assertTrue(emptyEmail.isEmpty());
        assertEquals("", emptyEmail.getValue());
    }

    @Test
    public void testEquality() {
        // 相等性测试
        Email email1 = new Email("test@example.com");
        Email email2 = new Email("test@example.com");
        Email email3 = new Email("other@example.com");
        
        // 自反性
        assertEquals(email1, email1);
        
        // 对称性
        assertEquals(email1, email2);
        assertEquals(email2, email1);
        
        // 传递性
        assertNotEquals(email1, email3);
        assertNotEquals(email2, email3);
        
        // 与null比较
        assertNotEquals(email1, null);
        
        // 与其他类型比较
        assertNotEquals(email1, "test@example.com");
        
        // hashCode一致性
        assertEquals(email1.hashCode(), email2.hashCode());
    }

    @Test
    public void testToString() {
        // toString方法测试
        Email email = new Email("test@example.com");
        assertEquals("test@example.com", email.toString());
    }
} 