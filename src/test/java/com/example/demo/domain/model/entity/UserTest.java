package com.example.demo.domain.model.entity;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.demo.domain.model.valueobject.Email;
import com.example.demo.domain.model.valueobject.Phone;
import com.example.demo.domain.model.valueobject.RoleId;
import com.example.demo.domain.model.valueobject.UserId;

/**
 * User实体的单元测试
 */
public class UserTest {

    private User user;
    private Role role1;
    private Role role2;

    @BeforeEach
    public void setUp() {
        // 初始化测试用户
        user = User.create(
                "testuser",
                "password123",
                "Test User",
                "test@example.com",
                "13800138000"
        );

        // 设置用户ID
        UserId userId = new UserId(1L);
        user = new User(
                userId,
                "testuser",
                "password123",
                "Test User",
                new Email("test@example.com"),
                new Phone("13800138000"),
                true,
                LocalDateTime.now(),
                LocalDateTime.now(),
                null,
                new HashSet<>()
        );

        // 初始化测试角色
        RoleId roleId1 = new RoleId(1L);
        role1 = new Role(
                roleId1,
                "管理员",
                "ADMIN",
                "系统管理员",
                true,
                LocalDateTime.now(),
                LocalDateTime.now(),
                new HashSet<>()
        );

        RoleId roleId2 = new RoleId(2L);
        role2 = new Role(
                roleId2,
                "普通用户",
                "USER",
                "普通用户",
                true,
                LocalDateTime.now(),
                LocalDateTime.now(),
                new HashSet<>()
        );
    }

    @Test
    public void testCreateUser() {
        // 测试创建用户
        User newUser = User.create(
                "newuser",
                "newpassword",
                "New User",
                "new@example.com",
                "13900139000"
        );

        assertEquals("newuser", newUser.getUsername());
        assertEquals("newpassword", newUser.getPassword());
        assertEquals("New User", newUser.getNickname());
        assertEquals("new@example.com", newUser.getEmail().getValue());
        assertEquals("13900139000", newUser.getPhone().getValue());
        assertTrue(newUser.getStatus());
        assertNotNull(newUser.getCreateTime());
        assertNotNull(newUser.getUpdateTime());
        assertNull(newUser.getLastLoginTime());
        assertTrue(newUser.getRoles().isEmpty());
    }

    @Test
    public void testCreateUserWithInvalidArguments() {
        // 测试创建用户时的参数验证
        assertThrows(NullPointerException.class, () -> User.create(
                null,  // 用户名为null
                "password123",
                "Test User",
                "test@example.com",
                "13800138000"
        ));

        assertThrows(NullPointerException.class, () -> User.create(
                "testuser",
                null,  // 密码为null
                "Test User",
                "test@example.com",
                "13800138000"
        ));

        // 无效的邮箱格式
        assertThrows(IllegalArgumentException.class, () -> User.create(
                "testuser",
                "password123",
                "Test User",
                "invalid-email",  // 无效的邮箱
                "13800138000"
        ));

        // 无效的手机号格式
        assertThrows(IllegalArgumentException.class, () -> User.create(
                "testuser",
                "password123",
                "Test User",
                "test@example.com",
                "invalid-phone"  // 无效的手机号
        ));
    }

    @Test
    public void testAddRole() {
        // 测试添加单个角色
        user.addRole(role1);
        
        assertEquals(1, user.getRoles().size());
        assertTrue(user.getRoles().contains(role1));
        
        // 重复添加相同角色，应该不影响结果
        user.addRole(role1);
        assertEquals(1, user.getRoles().size());
        
        // 添加不同角色
        user.addRole(role2);
        assertEquals(2, user.getRoles().size());
        assertTrue(user.getRoles().contains(role2));
    }

    @Test
    public void testAddNullRole() {
        // 测试添加null角色时的异常处理
        assertThrows(NullPointerException.class, () -> user.addRole(null));
    }

    @Test
    public void testAddRoles() {
        // 测试批量添加角色
        user.addRoles(Arrays.asList(role1, role2));
        
        assertEquals(2, user.getRoles().size());
        assertTrue(user.getRoles().contains(role1));
        assertTrue(user.getRoles().contains(role2));
    }

    @Test
    public void testRemoveRole() {
        // 测试移除角色
        user.addRole(role1);
        user.addRole(role2);
        assertEquals(2, user.getRoles().size());
        
        user.removeRole(role1);
        assertEquals(1, user.getRoles().size());
        assertFalse(user.getRoles().contains(role1));
        assertTrue(user.getRoles().contains(role2));
        
        // 移除不存在的角色，不应有影响
        user.removeRole(role1);
        assertEquals(1, user.getRoles().size());
    }

    @Test
    public void testRemoveNullRole() {
        // 测试移除null角色时的异常处理
        assertThrows(NullPointerException.class, () -> user.removeRole(null));
    }

    @Test
    public void testUpdateProfile() {
        // 测试更新基本信息
        user.updateProfile("New Name", "new@example.com", "13900139000");
        
        assertEquals("New Name", user.getNickname());
        assertEquals("new@example.com", user.getEmail().getValue());
        assertEquals("13900139000", user.getPhone().getValue());
    }

    @Test
    public void testChangePassword() {
        // 测试修改密码
        user.changePassword("newpassword123");
        
        assertEquals("newpassword123", user.getPassword());
    }

    @Test
    public void testChangePasswordWithNull() {
        // 测试修改密码为null时的异常处理
        assertThrows(NullPointerException.class, () -> user.changePassword(null));
    }

    @Test
    public void testDisableAndEnable() {
        // 测试禁用和启用用户
        assertTrue(user.getStatus()); // 初始状态为启用
        
        user.disable();
        assertFalse(user.getStatus());
        
        user.enable();
        assertTrue(user.getStatus());
    }

    @Test
    public void testRecordLogin() {
        // 测试记录登录时间
        assertNull(user.getLastLoginTime());
        
        user.recordLogin();
        assertNotNull(user.getLastLoginTime());
    }
} 