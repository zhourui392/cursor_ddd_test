package com.example.demo.domain.model.entity;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.demo.domain.model.valueobject.PermissionId;
import com.example.demo.domain.model.valueobject.RoleId;

/**
 * Role实体的单元测试
 */
public class RoleTest {

    private Role role;
    private Permission permission1;
    private Permission permission2;

    @BeforeEach
    public void setUp() {
        // 初始化测试角色
        role = Role.create(
                "管理员",
                "ADMIN",
                "系统管理员"
        );

        // 设置角色ID
        RoleId roleId = new RoleId(1L);
        role = new Role(
                roleId,
                "管理员",
                "ADMIN",
                "系统管理员",
                true,
                LocalDateTime.now(),
                LocalDateTime.now(),
                new HashSet<>()
        );

        // 初始化测试权限
        PermissionId permissionId1 = new PermissionId(1L);
        permission1 = new Permission(
                permissionId1,
                "用户管理",
                "USER_MANAGE",
                "用户管理权限",
                "用户管理",
                true,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        PermissionId permissionId2 = new PermissionId(2L);
        permission2 = new Permission(
                permissionId2,
                "角色管理",
                "ROLE_MANAGE",
                "角色管理权限",
                "权限管理",
                true,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    @Test
    public void testCreateRole() {
        // 测试创建角色
        Role newRole = Role.create(
                "运营",
                "OPERATOR",
                "运营人员"
        );

        assertEquals("运营", newRole.getName());
        assertEquals("OPERATOR", newRole.getCode());
        assertEquals("运营人员", newRole.getDescription());
        assertTrue(newRole.getStatus());
        assertNotNull(newRole.getCreateTime());
        assertNotNull(newRole.getUpdateTime());
        assertTrue(newRole.getPermissions().isEmpty());
    }

    @Test
    public void testCreateRoleWithInvalidArguments() {
        // 测试创建角色时的参数验证
        assertThrows(NullPointerException.class, () -> Role.create(
                null,  // 角色名为null
                "ADMIN",
                "系统管理员"
        ));

        assertThrows(NullPointerException.class, () -> Role.create(
                "管理员",
                null,  // 角色编码为null
                "系统管理员"
        ));
    }

    @Test
    public void testAddPermission() {
        // 测试添加单个权限
        role.addPermission(permission1);
        
        assertEquals(1, role.getPermissions().size());
        assertTrue(role.getPermissions().contains(permission1));
        
        // 重复添加相同权限，应该不影响结果（Set特性）
        role.addPermission(permission1);
        assertEquals(1, role.getPermissions().size());
        
        // 添加不同权限
        role.addPermission(permission2);
        assertEquals(2, role.getPermissions().size());
        assertTrue(role.getPermissions().contains(permission2));
    }

    @Test
    public void testAddNullPermission() {
        // 测试添加null权限时的异常处理
        assertThrows(NullPointerException.class, () -> role.addPermission(null));
    }

    @Test
    public void testAddPermissions() {
        // 测试批量添加权限
        role.addPermissions(Arrays.asList(permission1, permission2));
        
        assertEquals(2, role.getPermissions().size());
        assertTrue(role.getPermissions().contains(permission1));
        assertTrue(role.getPermissions().contains(permission2));
    }

    @Test
    public void testRemovePermission() {
        // 测试移除权限
        role.addPermission(permission1);
        role.addPermission(permission2);
        assertEquals(2, role.getPermissions().size());
        
        role.removePermission(permission1);
        assertEquals(1, role.getPermissions().size());
        assertFalse(role.getPermissions().contains(permission1));
        assertTrue(role.getPermissions().contains(permission2));
        
        // 移除不存在的权限，不应有影响
        role.removePermission(permission1);
        assertEquals(1, role.getPermissions().size());
    }

    @Test
    public void testRemoveNullPermission() {
        // 测试移除null权限时的异常处理
        assertThrows(NullPointerException.class, () -> role.removePermission(null));
    }

    @Test
    public void testDisableAndEnable() {
        // 测试禁用和启用角色
        assertTrue(role.getStatus()); // 初始状态为启用
        
        role.disable();
        assertFalse(role.getStatus());
        
        role.enable();
        assertTrue(role.getStatus());
    }

    @Test
    public void testUpdate() {
        // 测试更新角色信息
        role.update("超级管理员", "最高权限管理员");
        
        assertEquals("超级管理员", role.getName());
        assertEquals("最高权限管理员", role.getDescription());
    }

    @Test
    public void testUpdateWithNullName() {
        // 测试更新时名称为null的异常处理
        assertThrows(NullPointerException.class, () -> role.update(null, "描述"));
    }

    @Test
    public void testEquality() {
        // 相等性测试 - 基于code的相等性判断
        Role role1 = new Role(
                new RoleId(1L),
                "管理员1",
                "ADMIN",
                "描述1",
                true,
                LocalDateTime.now(),
                LocalDateTime.now(),
                new HashSet<>()
        );
        
        Role role2 = new Role(
                new RoleId(2L),
                "管理员2",
                "ADMIN",  // 相同的code
                "描述2",
                true,
                LocalDateTime.now(),
                LocalDateTime.now(),
                new HashSet<>()
        );
        
        Role role3 = new Role(
                new RoleId(3L),
                "用户",
                "USER",  // 不同的code
                "普通用户",
                true,
                LocalDateTime.now(),
                LocalDateTime.now(),
                new HashSet<>()
        );
        
        // 即使ID、名称和描述不同，只要code相同就视为相等
        assertEquals(role1, role2);
        
        // 不同code的角色不相等
        assertNotEquals(role1, role3);
    }
} 