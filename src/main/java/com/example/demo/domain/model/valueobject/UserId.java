package com.example.demo.domain.model.valueobject;

import java.util.Objects;

/**
 * 用户ID值对象
 */
public class UserId {
    private final Long value;

    public UserId(Long value) {
        this.value = Objects.requireNonNull(value, "用户ID不能为空");
    }

    public Long getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserId userId = (UserId) o;
        return Objects.equals(value, userId.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
} 