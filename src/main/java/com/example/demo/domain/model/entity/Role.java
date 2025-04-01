package com.example.demo.domain.model.entity;

import com.example.demo.domain.model.valueobject.RoleId;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 角色领域实体
 */
@Getter
@AllArgsConstructor
public class Role {
    private RoleId id;
    private String name;
    private String code;
    private String description;
    private Boolean status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Set<Permission> permissions = new HashSet<>();

    private Role(String name, String code) {
        this.name = Objects.requireNonNull(name, "角色名称不能为空");
        this.code = Objects.requireNonNull(code, "角色编码不能为空");
        this.status = true;
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 创建新角色
     */
    public static Role create(String name, String code, String description) {
        Role role = new Role(name, code);
        role.description = description;
        return role;
    }

    /**
     * 添加权限
     */
    public void addPermission(Permission permission) {
        this.permissions.add(Objects.requireNonNull(permission, "权限不能为空"));
    }

    /**
     * 添加权限
     */
    public void addPermissions(List<Permission> permissions) {
        this.permissions.addAll(permissions);
    }

    /**
     * 移除权限
     */
    public void removePermission(Permission permission) {
        this.permissions.remove(Objects.requireNonNull(permission, "权限不能为空"));
    }

    /**
     * 禁用角色
     */
    public void disable() {
        this.status = false;
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 启用角色
     */
    public void enable() {
        this.status = true;
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 更新角色信息
     */
    public void update(String name, String description) {
        this.name = Objects.requireNonNull(name, "角色名称不能为空");
        this.description = description;
        this.updateTime = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Role role = (Role) o;
        return Objects.equals(code, role.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }
} 