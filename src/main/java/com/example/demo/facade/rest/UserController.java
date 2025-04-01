package com.example.demo.facade.rest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.application.command.UpdateUserCommand;
import com.example.demo.application.dto.PermissionDTO;
import com.example.demo.application.dto.RoleDTO;
import com.example.demo.application.dto.UserDTO;
import com.example.demo.application.service.UserApplicationService;
import com.example.demo.domain.service.UserDomainService;
import com.example.demo.facade.dto.ApiResponse;
import com.example.demo.infrastructure.utils.SecurityUtils;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    
    private final UserApplicationService userApplicationService;
    private final UserDomainService userDomainService;
    
    /**
     * 获取所有用户
     */
    @GetMapping
    @PreAuthorize("hasAuthority('USER_VIEW')")
    public ResponseEntity<ApiResponse<List<UserDTO>>> getAllUsers() {
        List<UserDTO> users = userApplicationService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success(users));
    }
    
    /**
     * 获取当前登录用户信息（包含角色和权限）
     */
    @GetMapping("/current")
    public ResponseEntity<ApiResponse<UserDTO>> getCurrentUser() {
        String username = SecurityUtils.getCurrentUsername();
        if (username == null) {
            return ResponseEntity.ok(ApiResponse.error("用户未登录"));
        }
        
        // 获取用户信息（包含角色和权限）
        UserDTO user = userApplicationService.getUserByUsername(username);
        
        // 添加调试信息：检查角色和权限是否正确加载
        if (user.getRoles() != null) {
            // 确保每个角色的权限都已加载
            Set<RoleDTO> roles = user.getRoles();
            
            // 提取所有权限代码，用于前端权限控制
            Set<String> permissionCodes = roles.stream()
                .filter(role -> role.getPermissions() != null)
                .flatMap(role -> role.getPermissions().stream())
                .map(PermissionDTO::getCode)
                .collect(Collectors.toSet());
            
            // 输出用户角色和权限信息到日志（实际生产中可移除）
            System.out.println("User: " + username + ", Roles: " + 
                roles.stream().map(RoleDTO::getCode).collect(Collectors.joining(", ")) + 
                ", Permissions: " + String.join(", ", permissionCodes));
        }
        
        return ResponseEntity.ok(ApiResponse.success(user));
    }
    
    /**
     * 获取当前登录用户的角色和权限
     * 使用领域服务提供的方法
     */
    @GetMapping("/current/permissions")
    public ResponseEntity<ApiResponse<Object>> getCurrentUserPermissions() {
        String username = SecurityUtils.getCurrentUsername();
        if (username == null) {
            return ResponseEntity.ok(ApiResponse.error("用户未登录"));
        }
        
        // 使用领域服务获取用户权限
        List<String> permissionCodes = userDomainService.getUserPermissionCodes(username);
        
        // 构建包含角色和权限的响应对象
        Map<String, Object> result = new HashMap<>();
        
        // 获取用户信息和角色信息
        UserDTO user = userApplicationService.getUserByUsername(username);
        List<String> roleCodes = user.getRoles().stream()
            .map(RoleDTO::getCode)
            .collect(Collectors.toList());
        
        result.put("roles", roleCodes);
        result.put("permissions", permissionCodes);
        
        return ResponseEntity.ok(ApiResponse.success(result));
    }
    
    /**
     * 根据ID获取用户
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_VIEW')")
    public ResponseEntity<ApiResponse<UserDTO>> getUserById(@PathVariable Long id) {
        UserDTO user = userApplicationService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(user));
    }
    
    /**
     * 更新用户信息
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_EDIT')")
    public ResponseEntity<ApiResponse<UserDTO>> updateUser(
            @PathVariable Long id, 
            @Valid @RequestBody UpdateUserCommand command) {
        command.setId(id);
        UserDTO updatedUser = userApplicationService.updateUser(command);
        return ResponseEntity.ok(ApiResponse.success(updatedUser));
    }
    
    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_DELETE')")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        userApplicationService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success(null, "用户已删除"));
    }
    
    /**
     * 为用户添加角色
     */
    @PostMapping("/{username}/roles/{roleCode}")
    @PreAuthorize("hasAuthority('USER_EDIT')")
    public ResponseEntity<ApiResponse<Void>> addRoleToUser(
            @PathVariable String username, 
            @PathVariable String roleCode) {
        userApplicationService.addRoleToUser(username, roleCode);
        return ResponseEntity.ok(ApiResponse.success(null, "角色已添加到用户"));
    }
    
    /**
     * 从用户中移除角色
     */
    @DeleteMapping("/{username}/roles/{roleCode}")
    @PreAuthorize("hasAuthority('USER_EDIT')")
    public ResponseEntity<ApiResponse<Void>> removeRoleFromUser(
            @PathVariable String username, 
            @PathVariable String roleCode) {
        userApplicationService.removeRoleFromUser(username, roleCode);
        return ResponseEntity.ok(ApiResponse.success(null, "角色已从用户中移除"));
    }
    
    /**
     * 检查用户是否拥有特定权限
     */
    @GetMapping("/{username}/has-permission/{permissionCode}")
    @PreAuthorize("hasAuthority('USER_VIEW')")
    public ResponseEntity<ApiResponse<Boolean>> hasPermission(
            @PathVariable String username,
            @PathVariable String permissionCode) {
        boolean hasPermission = userDomainService.hasPermission(username, permissionCode);
        return ResponseEntity.ok(ApiResponse.success(hasPermission));
    }
    
    /**
     * 检查用户是否拥有特定角色
     */
    @GetMapping("/{username}/has-role/{roleCode}")
    @PreAuthorize("hasAuthority('USER_VIEW')")
    public ResponseEntity<ApiResponse<Boolean>> hasRole(
            @PathVariable String username,
            @PathVariable String roleCode) {
        boolean hasRole = userDomainService.hasRole(username, roleCode);
        return ResponseEntity.ok(ApiResponse.success(hasRole));
    }
} 