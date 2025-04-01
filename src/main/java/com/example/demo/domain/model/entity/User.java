package com.example.demo.domain.model.entity;

import com.example.demo.domain.model.valueobject.Email;
import com.example.demo.domain.model.valueobject.Phone;
import com.example.demo.domain.model.valueobject.UserId;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 用户领域实体（聚合根）
 */
@Getter
@AllArgsConstructor
public class User {
    private UserId id;
    private String username;
    private String password;
    private String nickname;
    private Email email;
    private Phone phone;
    private Boolean status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private LocalDateTime lastLoginTime;
    private Set<Role> roles = new HashSet<>();

    private User(String username, String password) {
        this.username = Objects.requireNonNull(username, "用户名不能为空");
        this.password = Objects.requireNonNull(password, "密码不能为空");
        this.status = true;
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 创建新用户
     */
    public static User create(String username, String password, String nickname, String email, String phone) {
        User user = new User(username, password);
        user.nickname = nickname;
        user.email = new Email(email);
        user.phone = new Phone(phone);
        return user;
    }

    /**
     * 添加角色
     */
    public void addRole(Role role) {
        this.roles.add(Objects.requireNonNull(role, "角色不能为空"));
    }

    public void addRoles(List<Role> roleList) {
        this.roles.addAll(roleList);
    }

    /**
     * 移除角色
     */
    public void removeRole(Role role) {
        this.roles.remove(Objects.requireNonNull(role, "角色不能为空"));
    }

    /**
     * 更新基本信息
     */
    public void updateProfile(String nickname, String email, String phone) {
        this.nickname = nickname;
        this.email = new Email(email);
        this.phone = new Phone(phone);
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 更新密码
     */
    public void changePassword(String newPassword) {
        this.password = Objects.requireNonNull(newPassword, "新密码不能为空");
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 禁用用户
     */
    public void disable() {
        this.status = false;
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 启用用户
     */
    public void enable() {
        this.status = true;
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 记录登录时间
     */
    public void recordLogin() {
        this.lastLoginTime = LocalDateTime.now();
    }

} 