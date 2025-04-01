package com.example.demo.domain.model.valueobject;

import java.util.Objects;

/**
 * 权限ID值对象
 */
public class PermissionId {
    private final Long value;

    public PermissionId(Long value) {
        this.value = Objects.requireNonNull(value, "权限ID不能为空");
    }

    public Long getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PermissionId permissionId = (PermissionId) o;
        return Objects.equals(value, permissionId.value);
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