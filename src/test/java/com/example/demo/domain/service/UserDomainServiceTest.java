package com.example.demo.domain.service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.demo.domain.model.entity.Permission;
import com.example.demo.domain.model.entity.Role;
import com.example.demo.domain.model.entity.User;
import com.example.demo.domain.model.valueobject.Email;
import com.example.demo.domain.model.valueobject.PermissionId;
import com.example.demo.domain.model.valueobject.Phone;
import com.example.demo.domain.model.valueobject.RoleId;
import com.example.demo.domain.model.valueobject.UserId;
import com.example.demo.domain.repository.RoleRepository;
import com.example.demo.domain.repository.UserRepository;

/**
 * UserDomainService的单元测试
 */
@ExtendWith(MockitoExtension.class)
public class UserDomainServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private UserDomainService userDomainService;

    private User user;
    private Role adminRole;
    private Role userRole;
    private Permission viewUserPermission;
    private Permission editUserPermission;

    @BeforeEach
    public void setUp() {
        // 初始化测试数据
        UserId userId = new UserId(1L);
        RoleId adminRoleId = new RoleId(1L);
        RoleId userRoleId = new RoleId(2L);
        PermissionId viewPermissionId = new PermissionId(1L);
        PermissionId editPermissionId = new PermissionId(2L);

        // 创建权限
        viewUserPermission = new Permission(
                viewPermissionId,
                "查看用户",
                "USER_VIEW",
                "查看用户信息权限",
                "用户管理",
                true,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        editUserPermission = new Permission(
                editPermissionId,
                "编辑用户",
                "USER_EDIT",
                "编辑用户信息权限",
                "用户管理",
                true,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        // 创建角色及权限关系
        Set<Permission> adminPermissions = new HashSet<>();
        adminPermissions.add(viewUserPermission);
        adminPermissions.add(editUserPermission);

        Set<Permission> userPermissions = new HashSet<>();
        userPermissions.add(viewUserPermission);

        adminRole = new Role(
                adminRoleId,
                "管理员",
                "ADMIN",
                "系统管理员",
                true,
                LocalDateTime.now(),
                LocalDateTime.now(),
                adminPermissions
        );

        userRole = new Role(
                userRoleId,
                "普通用户",
                "USER",
                "普通用户",
                true,
                LocalDateTime.now(),
                LocalDateTime.now(),
                userPermissions
        );

        // 创建用户
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
    }

    @Test
    public void testAssignRoleToUser() {
        // 配置模拟行为
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(roleRepository.findByCode("ADMIN")).thenReturn(Optional.of(adminRole));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // 执行测试
        User result = userDomainService.assignRoleToUser("testuser", "ADMIN");

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getRoles().size());
        assertTrue(result.getRoles().contains(adminRole));

        // 验证调用
        verify(userRepository).findByUsername("testuser");
        verify(roleRepository).findByCode("ADMIN");
        verify(userRepository).save(user);
    }

    @Test
    public void testAssignRoleToUserWhenUserNotFound() {
        // 配置模拟行为
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // 执行测试并验证异常
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userDomainService.assignRoleToUser("nonexistent", "ADMIN");
        });

        assertEquals("用户不存在：nonexistent", exception.getMessage());

        // 验证调用
        verify(userRepository).findByUsername("nonexistent");
        verify(roleRepository, never()).findByCode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void testAssignRoleToUserWhenRoleNotFound() {
        // 配置模拟行为
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(roleRepository.findByCode("NONEXISTENT")).thenReturn(Optional.empty());

        // 执行测试并验证异常
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userDomainService.assignRoleToUser("testuser", "NONEXISTENT");
        });

        assertEquals("角色不存在：NONEXISTENT", exception.getMessage());

        // 验证调用
        verify(userRepository).findByUsername("testuser");
        verify(roleRepository).findByCode("NONEXISTENT");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void testAssignRoleToUserWithNullArguments() {
        // 测试null参数
        assertThrows(NullPointerException.class, () -> {
            userDomainService.assignRoleToUser(null, "ADMIN");
        });

        assertThrows(NullPointerException.class, () -> {
            userDomainService.assignRoleToUser("testuser", null);
        });

        // 验证调用
        verify(userRepository, never()).findByUsername(anyString());
        verify(roleRepository, never()).findByCode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void testRemoveRoleFromUser() {
        // 先添加角色
        user.addRole(adminRole);

        // 配置模拟行为
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(roleRepository.findByCode("ADMIN")).thenReturn(Optional.of(adminRole));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // 执行测试
        User result = userDomainService.removeRoleFromUser("testuser", "ADMIN");

        // 验证结果
        assertNotNull(result);
        assertEquals(0, result.getRoles().size());

        // 验证调用
        verify(userRepository).findByUsername("testuser");
        verify(roleRepository).findByCode("ADMIN");
        verify(userRepository).save(user);
    }

    @Test
    public void testGetUserPermissionCodes() {
        // 为用户添加角色
        user.addRole(adminRole);

        // 配置模拟行为
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        // 执行测试
        List<String> permissionCodes = userDomainService.getUserPermissionCodes("testuser");

        // 验证结果
        assertNotNull(permissionCodes);
        assertEquals(2, permissionCodes.size());
        assertTrue(permissionCodes.contains("USER_VIEW"));
        assertTrue(permissionCodes.contains("USER_EDIT"));

        // 验证调用
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    public void testHasPermission() {
        // 为用户添加角色
        user.addRole(userRole); // 只有查看权限

        // 配置模拟行为
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        // 执行测试 - 有权限
        boolean hasViewPermission = userDomainService.hasPermission("testuser", "USER_VIEW");
        // 执行测试 - 无权限
        boolean hasEditPermission = userDomainService.hasPermission("testuser", "USER_EDIT");

        // 验证结果
        assertTrue(hasViewPermission);
        assertFalse(hasEditPermission);

        // 验证调用
        verify(userRepository, times(2)).findByUsername("testuser");
    }

    @Test
    public void testHasRole() {
        // 为用户添加角色
        user.addRole(userRole);

        // 配置模拟行为
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        // 执行测试 - 有角色
        boolean hasUserRole = userDomainService.hasRole("testuser", "USER");
        // 执行测试 - 无角色
        boolean hasAdminRole = userDomainService.hasRole("testuser", "ADMIN");

        // 验证结果
        assertTrue(hasUserRole);
        assertFalse(hasAdminRole);

        // 验证调用
        verify(userRepository, times(2)).findByUsername("testuser");
    }

    @Test
    public void testHasRoleWithNonexistentUser() {
        // 配置模拟行为
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // 执行测试
        boolean hasRole = userDomainService.hasRole("nonexistent", "ADMIN");

        // 验证结果
        assertFalse(hasRole);

        // 验证调用
        verify(userRepository).findByUsername("nonexistent");
    }
} 