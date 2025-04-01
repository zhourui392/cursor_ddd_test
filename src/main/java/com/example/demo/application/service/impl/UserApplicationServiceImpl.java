package com.example.demo.application.service.impl;

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
import com.example.demo.infrastructure.security.JwtUtil;
import com.example.demo.infrastructure.convert.UserConvert;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户应用服务实现类
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

    @Override
    public UserDTO registerUser(RegisterUserCommand command) {
        if (userRepository.existsByUsername(command.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }

        User user = User.create(
                command.getUsername(),
                passwordService.encryptPassword(command.getPassword()),
                command.getNickname(),
                command.getEmail(),
                command.getPhone()
        );

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

        User savedUser = userRepository.save(user);
        return userConvert.toDto(savedUser);
    }

    @Override
    public String login(LoginCommand command) {
        User user = userRepository.findByUsername(command.getUsername())
                .orElseThrow(() -> new RuntimeException("用户名或密码错误"));

        if (!passwordService.matches(command.getPassword(), user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        if (!user.getStatus()) {
            throw new RuntimeException("用户已被禁用");
        }

        // 记录登录时间
        user.recordLogin();
        userRepository.save(user);

        // 生成JWT token
        return jwtUtil.generateToken(user.getUsername());
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(new UserId(id))
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        return userConvert.toDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        return userConvert.toDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(userConvert::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDTO updateUser(UpdateUserCommand command) {
        User user = userRepository.findById(new UserId(command.getId()))
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        user.updateProfile(command.getNickname(), command.getEmail(), command.getPhone());

        if (command.getStatus() != null) {
            if (command.getStatus()) {
                user.enable();
            } else {
                user.disable();
            }
        }

        User savedUser = userRepository.save(user);
        return userConvert.toDto(savedUser);
    }

    @Override
    public void deleteUser(Long id) {
        User user = userRepository.findById(new UserId(id))
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        userRepository.delete(user);
    }

    @Override
    public void addRoleToUser(String username, String roleCode) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        Role role = roleRepository.findByCode(roleCode)
                .orElseThrow(() -> new RuntimeException("角色不存在"));
        user.addRole(role);
        userRepository.save(user);
    }

    @Override
    public void removeRoleFromUser(String username, String roleCode) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        Role role = roleRepository.findByCode(roleCode)
                .orElseThrow(() -> new RuntimeException("角色不存在"));
        user.removeRole(role);
        userRepository.save(user);
    }

    @Override
    public void updateLastLoginTime(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        user.recordLogin();
        userRepository.save(user);
    }
} 