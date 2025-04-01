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
 * Phone值对象的单元测试
 */
public class PhoneTest {

    @Test
    public void testValidPhone() {
        // 有效的手机号
        Phone phone = new Phone("13800138000");
        assertEquals("13800138000", phone.getValue());
        assertFalse(phone.isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "13800138000",
            "13900139000",
            "15812345678",
            "18987654321",
            "17612345678",
            "19912345678"
    })
    public void testMultipleValidPhones(String validPhone) {
        // 测试多个有效手机号
        Phone phone = new Phone(validPhone);
        assertEquals(validPhone, phone.getValue());
    }

    @Test
    public void testInvalidPhone() {
        // 无效的手机号
        assertThrows(IllegalArgumentException.class, () -> new Phone("1380013800")); // 少一位
        assertThrows(IllegalArgumentException.class, () -> new Phone("138001380001")); // 多一位
        assertThrows(IllegalArgumentException.class, () -> new Phone("abcdefghijk")); // 非数字
        assertThrows(IllegalArgumentException.class, () -> new Phone("138-0013-8000")); // 包含非数字字符
        assertThrows(IllegalArgumentException.class, () -> new Phone("+8613800138000")); // 带国际区号
    }

    @Test
    public void testNullAndEmptyPhone() {
        // null和空字符串处理
        Phone nullPhone = new Phone(null);
        assertTrue(nullPhone.isEmpty());
        assertNull(nullPhone.getValue());

        Phone emptyPhone = new Phone("");
        assertTrue(emptyPhone.isEmpty());
        assertEquals("", emptyPhone.getValue());
    }

    @Test
    public void testEquality() {
        // 相等性测试
        Phone phone1 = new Phone("13800138000");
        Phone phone2 = new Phone("13800138000");
        Phone phone3 = new Phone("13900139000");
        
        // 自反性
        assertEquals(phone1, phone1);
        
        // 对称性
        assertEquals(phone1, phone2);
        assertEquals(phone2, phone1);
        
        // 传递性
        assertNotEquals(phone1, phone3);
        assertNotEquals(phone2, phone3);
        
        // 与null比较
        assertNotEquals(phone1, null);
        
        // 与其他类型比较
        assertNotEquals(phone1, "13800138000");
        
        // hashCode一致性
        assertEquals(phone1.hashCode(), phone2.hashCode());
    }

    @Test
    public void testToString() {
        // toString方法测试
        Phone phone = new Phone("13800138000");
        assertEquals("13800138000", phone.toString());
    }
} 