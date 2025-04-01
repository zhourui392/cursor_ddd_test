package com.example.demo.domain.model.valueobject;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Email值对象
 */
public class Email {
    private static final Pattern EMAIL_PATTERN = 
        Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
        
    private final String value;

    public Email(String value) {
        if (value != null && !value.isEmpty() && !isValidEmail(value)) {
            throw new IllegalArgumentException("无效的Email格式");
        }
        this.value = value;
    }

    private boolean isValidEmail(String email) {
        return EMAIL_PATTERN.matcher(email).matches();
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
        Email email = (Email) o;
        return Objects.equals(value, email.value);
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