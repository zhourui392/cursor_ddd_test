package com.example.demo.domain.service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.demo.domain.model.entity.Role;
import com.example.demo.domain.model.entity.User;
import com.example.demo.domain.repository.RoleRepository;
import com.example.demo.domain.repository.UserRepository;

import lombok.RequiredArgsConstructor;

/**
 * 用户领域服务
 * 处理跨越多个聚合的操作，或不适合放在实体中的复杂业务逻辑
 */
@Service
@RequiredArgsConstructor
public class UserDomainService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    
    /**
     * 为用户分配角色
     * 注意：此方法涉及两个聚合（User和Role），应该放在领域服务中
     *
     * @param username 用户名
     * @param roleCode 角色编码
     * @return 分配角色后的用户
     */
    public User assignRoleToUser(String username, String roleCode) {
        // 1. 参数校验
        Objects.requireNonNull(username, "用户名不能为空");
        Objects.requireNonNull(roleCode, "角色编码不能为空");
        
        // 2. 获取用户和角色
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在：" + username));
                
        Role role = roleRepository.findByCode(roleCode)
                .orElseThrow(() -> new IllegalArgumentException("角色不存在：" + roleCode));
        
        // 3. 判断用户是否已分配该角色
        boolean hasRole = user.getRoles().stream()
                .anyMatch(r -> r.getCode().equals(roleCode));
                
        if (!hasRole) {
            // 4. 分配角色
            user.addRole(role);
            user = userRepository.save(user);
        }
        
        return user;
    }
    
    /**
     * 移除用户角色
     *
     * @param username 用户名
     * @param roleCode 角色编码
     * @return 移除角色后的用户
     */
    public User removeRoleFromUser(String username, String roleCode) {
        // 1. 参数校验
        Objects.requireNonNull(username, "用户名不能为空");
        Objects.requireNonNull(roleCode, "角色编码不能为空");
        
        // 2. 获取用户和角色
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在：" + username));
                
        Role role = roleRepository.findByCode(roleCode)
                .orElseThrow(() -> new IllegalArgumentException("角色不存在：" + roleCode));
        
        // 3. 判断用户是否已分配该角色
        boolean hasRole = user.getRoles().stream()
                .anyMatch(r -> r.getCode().equals(roleCode));
                
        if (hasRole) {
            // 4. 移除角色
            user.removeRole(role);
            user = userRepository.save(user);
        }
        
        return user;
    }
    
    /**
     * 获取用户所有权限代码
     *
     * @param username 用户名
     * @return 权限代码列表
     */
    public List<String> getUserPermissionCodes(String username) {
        Objects.requireNonNull(username, "用户名不能为空");
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在：" + username));
                
        return user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(permission -> permission.getCode())
                .distinct()
                .collect(Collectors.toList());
    }
    
    /**
     * 检查用户是否拥有特定权限
     *
     * @param username 用户名
     * @param permissionCode 权限代码
     * @return 是否拥有权限
     */
    public boolean hasPermission(String username, String permissionCode) {
        List<String> permissionCodes = getUserPermissionCodes(username);
        return permissionCodes.contains(permissionCode);
    }
    
    /**
     * 检查用户是否为特定角色
     *
     * @param username 用户名
     * @param roleCode 角色代码
     * @return 是否为特定角色
     */
    public boolean hasRole(String username, String roleCode) {
        Objects.requireNonNull(username, "用户名不能为空");
        Objects.requireNonNull(roleCode, "角色编码不能为空");
        
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (!userOpt.isPresent()) {
            return false;
        }
        
        User user = userOpt.get();
        return user.getRoles().stream()
                .anyMatch(role -> role.getCode().equals(roleCode));
    }
} 