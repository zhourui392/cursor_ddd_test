package com.example.demo.domain.model.valueobject;

import java.util.Objects;

/**
 * 角色ID值对象
 */
public class RoleId {
    private final Long value;

    public RoleId(Long value) {
        this.value = Objects.requireNonNull(value, "角色ID不能为空");
    }

    public Long getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RoleId roleId = (RoleId) o;
        return Objects.equals(value, roleId.value);
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