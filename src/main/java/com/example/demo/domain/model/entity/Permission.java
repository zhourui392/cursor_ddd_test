package com.example.demo.domain.model.entity;

import com.example.demo.domain.model.valueobject.PermissionId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 权限领域实体
 */
@Getter
@AllArgsConstructor
public class Permission {
    private PermissionId id;
    private String name;
    private String code;
    private String description;
    private Boolean status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    private Permission(String name, String code) {
        this.name = Objects.requireNonNull(name, "权限名称不能为空");
        this.code = Objects.requireNonNull(code, "权限编码不能为空");
        this.status = true;
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 创建新权限
     */
    public static Permission create(String name, String code, String description) {
        Permission permission = new Permission(name, code);
        permission.description = description;
        return permission;
    }

    /**
     * 禁用权限
     */
    public void disable() {
        this.status = false;
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 启用权限
     */
    public void enable() {
        this.status = true;
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 更新权限信息
     */
    public void update(String name, String description) {
        this.name = Objects.requireNonNull(name, "权限名称不能为空");
        this.description = description;
        this.updateTime = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Permission that = (Permission) o;
        return Objects.equals(code, that.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }
} 