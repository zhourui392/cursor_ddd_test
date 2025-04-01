package com.example.demo.domain.model.entity;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.demo.domain.model.valueobject.PermissionId;

/**
 * Permission实体的单元测试
 */
public class PermissionTest {

    private Permission permission;

    @BeforeEach
    public void setUp() {
        // 初始化测试权限
        permission = Permission.create(
                "用户管理",
                "USER_MANAGE",
                "用户管理权限"
        );

        // 设置权限ID
        PermissionId permissionId = new PermissionId(1L);
        permission = new Permission(
                permissionId,
                "用户管理",
                "USER_MANAGE",
                "用户管理权限",
                true,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    @Test
    public void testCreatePermission() {
        // 测试创建权限
        Permission newPermission = Permission.create(
                "角色管理",
                "ROLE_MANAGE",
                "角色管理权限"
        );

        assertEquals("角色管理", newPermission.getName());
        assertEquals("ROLE_MANAGE", newPermission.getCode());
        assertEquals("角色管理权限", newPermission.getDescription());
        assertTrue(newPermission.getStatus());
        assertNotNull(newPermission.getCreateTime());
        assertNotNull(newPermission.getUpdateTime());
    }

    @Test
    public void testCreatePermissionWithInvalidArguments() {
        // 测试创建权限时的参数验证
        assertThrows(NullPointerException.class, () -> Permission.create(
                null,  // 权限名为null
                "USER_MANAGE",
                "用户管理权限"
        ));

        assertThrows(NullPointerException.class, () -> Permission.create(
                "用户管理",
                null,  // 权限编码为null
                "用户管理权限"
        ));
    }

    @Test
    public void testDisableAndEnable() {
        // 测试禁用和启用权限
        assertTrue(permission.getStatus()); // 初始状态为启用
        
        permission.disable();
        assertFalse(permission.getStatus());
        assertNotEquals(permission.getCreateTime(), permission.getUpdateTime()); // 更新时间已变更
        
        permission.enable();
        assertTrue(permission.getStatus());
    }

    @Test
    public void testUpdate() {
        // 测试更新权限信息
        LocalDateTime originalUpdateTime = permission.getUpdateTime();
        permission.update("用户权限管理", "系统用户管理权限");
        
        assertEquals("用户权限管理", permission.getName());
        assertEquals("系统用户管理权限", permission.getDescription());
        assertTrue(permission.getUpdateTime().isAfter(originalUpdateTime) || 
                   permission.getUpdateTime().equals(originalUpdateTime));
    }

    @Test
    public void testUpdateWithNullName() {
        // 测试更新时名称为null的异常处理
        assertThrows(NullPointerException.class, () -> permission.update(null, "描述"));
    }

    @Test
    public void testEquality() {
        // 相等性测试 - 基于code的相等性判断
        Permission permission1 = new Permission(
                new PermissionId(1L),
                "用户管理1",
                "USER_MANAGE",
                "描述1",
                true,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        
        Permission permission2 = new Permission(
                new PermissionId(2L),
                "用户管理2",
                "USER_MANAGE",  // 相同的code
                "描述2",
                false,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        
        Permission permission3 = new Permission(
                new PermissionId(3L),
                "角色管理",
                "ROLE_MANAGE",  // 不同的code
                "角色管理权限",
                true,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        
        // 即使ID、名称和描述不同，只要code相同就视为相等
        assertEquals(permission1, permission2);
        
        // 不同code的权限不相等
        assertNotEquals(permission1, permission3);
        
        // 与null比较
        assertNotEquals(permission1, null);
        
        // 与其他类型比较
        assertNotEquals(permission1, "USER_MANAGE");
        
        // 自反性
        assertEquals(permission1, permission1);
    }
} 