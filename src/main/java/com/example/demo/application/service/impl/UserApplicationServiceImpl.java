package com.example.demo.application.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.application.command.CreateUserCommand;
import com.example.demo.application.command.LoginCommand;
import com.example.demo.application.command.RegisterUserCommand;
import com.example.demo.application.command.UpdateUserCommand;
import com.example.demo.application.dto.UserDTO;
import com.example.demo.application.service.UserApplicationService;
import com.example.demo.domain.model.entity.Role;
import com.example.demo.domain.model.entity.User;
import com.example.demo.domain.model.valueobject.UserId;
import com.example.demo.domain.repository.RoleRepository;
import com.example.demo.domain.repository.UserRepository;
import com.example.demo.domain.service.PasswordService;
import com.example.demo.domain.service.UserDomainService;
import com.example.demo.infrastructure.convert.UserConvert;
import com.example.demo.infrastructure.security.JwtUtil;

import lombok.RequiredArgsConstructor;

/**
 * 用户应用服务实现类
 * 应用层负责用例的编排和转换，不包含业务规则
 */
@Service
@RequiredArgsConstructor
@Transactional
public class UserApplicationServiceImpl implements UserApplicationService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordService passwordService;
    private final JwtUtil jwtUtil;
    private final UserConvert userConvert;
    private final UserDomainService userDomainService;

    /**
     * 用户注册
     */
    @Override
    public UserDTO registerUser(RegisterUserCommand command) {
        // 1. 业务规则验证
        if (userRepository.existsByUsername(command.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }

        // 2. 创建用户实体（工厂方法）
        User user = User.create(
                command.getUsername(),
                passwordService.encryptPassword(command.getPassword()),
                command.getNickname(),
                command.getEmail(),
                command.getPhone()
        );

        // 3. 分配角色
        // 如果未指定角色，默认赋予USER角色
        if (command.getRoles() == null || command.getRoles().isEmpty()) {
            Role userRole = roleRepository.findByCode("ROLE_USER")
                    .orElseThrow(() -> new RuntimeException("默认角色未找到"));
            user.addRole(userRole);
        } else {
            command.getRoles().forEach(roleName -> {
                Role role = roleRepository.findByCode(roleName)
                        .orElseThrow(() -> new RuntimeException("角色 " + roleName + " 未找到"));
                user.addRole(role);
            });
        }

        // 4. 保存用户
        User savedUser = userRepository.save(user);
        
        // 5. 转换为DTO并返回
        return userConvert.toDto(savedUser);
    }
    
    /**
     * 创建用户（管理员操作）
     */
    @Override
    public UserDTO createUser(CreateUserCommand command) {
        // 1. 业务规则验证
        if (userRepository.existsByUsername(command.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }

        // 2. 创建用户实体（工厂方法）
        User user = User.create(
                command.getUsername(),
                passwordService.encryptPassword(command.getPassword()),
                command.getNickname(),
                command.getEmail(),
                command.getPhone()
        );
        
        // 3. 设置状态
        if (command.getStatus() != null && !command.getStatus()) {
            user.disable();
        }

        // 4. 默认分配USER角色
        Role userRole = roleRepository.findByCode("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("默认角色未找到"));
        user.addRole(userRole);

        // 5. 保存用户
        User savedUser = userRepository.save(user);
        
        // 6. 转换为DTO并返回
        return userConvert.toDto(savedUser);
    }

    /**
     * 用户登录
     */
    @Override
    public String login(LoginCommand command) {
        // 1. 查找用户
        User user = userRepository.findByUsername(command.getUsername())
                .orElseThrow(() -> new RuntimeException("用户名或密码错误"));

        // 2. 验证密码
        if (!passwordService.matches(command.getPassword(), user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 3. 检查用户状态
        if (!user.getStatus()) {
            throw new RuntimeException("用户已被禁用");
        }

        // 4. 记录登录时间
        user.recordLogin();
        userRepository.save(user);

        // 5. 生成JWT令牌
        return jwtUtil.generateToken(user.getUsername());
    }

    /**
     * 根据ID获取用户信息
     */
    @Override
    @Transactional(readOnly = true)
    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(new UserId(id))
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        return userConvert.toDto(user);
    }

    /**
     * 根据用户名获取用户信息
     */
    @Override
    @Transactional(readOnly = true)
    public UserDTO getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        return userConvert.toDto(user);
    }

    /**
     * 获取所有用户
     */
    @Override
    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(userConvert::toDto)
                .collect(Collectors.toList());
    }

    /**
     * 更新用户信息
     */
    @Override
    public UserDTO updateUser(UpdateUserCommand command) {
        // 1. 查找用户
        User user = userRepository.findById(new UserId(command.getId()))
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 2. 更新基本信息
        user.updateProfile(command.getNickname(), command.getEmail(), command.getPhone());

        // 3. 更新状态
        if (command.getStatus() != null) {
            if (command.getStatus()) {
                user.enable();
            } else {
                user.disable();
            }
        }

        // 4. 保存更新
        User savedUser = userRepository.save(user);
        
        // 5. 转换为DTO并返回
        return userConvert.toDto(savedUser);
    }

    /**
     * 删除用户
     */
    @Override
    public void deleteUser(Long id) {
        User user = userRepository.findById(new UserId(id))
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        userRepository.delete(user);
    }

    /**
     * 为用户添加角色
     * 委托给领域服务处理
     */
    @Override
    public void addRoleToUser(String username, String roleCode) {
        try {
            userDomainService.assignRoleToUser(username, roleCode);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * 从用户中移除角色
     * 委托给领域服务处理
     */
    @Override
    public void removeRoleFromUser(String username, String roleCode) {
        userDomainService.removeRoleFromUser(username, roleCode);
    }

    /**
     * 更新用户最后登录时间
     */
    @Override
    public void updateLastLoginTime(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        user.recordLogin();
        userRepository.save(user);
    }
} 