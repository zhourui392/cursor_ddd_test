package com.example.demo.domain.model.valueobject;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * 手机号值对象
 */
public class Phone {
    private static final Pattern PHONE_PATTERN = 
        Pattern.compile("^\\d{11}$");
        
    private final String value;

    public Phone(String value) {
        if (value != null && !value.isEmpty() && !isValidPhone(value)) {
            throw new IllegalArgumentException("无效的手机号格式");
        }
        this.value = value;
    }

    private boolean isValidPhone(String phone) {
        return PHONE_PATTERN.matcher(phone).matches();
    }

    public String getValue() {
        return value;
    }

    public boolean isEmpty() {
        return value == null || value.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Phone phone = (Phone) o;
        return Objects.equals(value, phone.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
} 