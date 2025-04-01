package com.example.demo.application.service;

import com.example.demo.application.command.LoginCommand;
import com.example.demo.application.command.RegisterUserCommand;
import com.example.demo.application.command.UpdateUserCommand;
import com.example.demo.application.dto.UserDTO;

import java.util.List;

/**
 * 用户应用服务接口
 */
public interface UserApplicationService {
    /**
     * 注册用户
     */
    UserDTO registerUser(RegisterUserCommand command);
    
    /**
     * 登录
     */
    String login(LoginCommand command);
    
    /**
     * 获取用户信息
     */
    UserDTO getUserById(Long id);
    
    /**
     * 获取用户信息
     */
    UserDTO getUserByUsername(String username);
    
    /**
     * 获取所有用户
     */
    List<UserDTO> getAllUsers();
    
    /**
     * 更新用户
     */
    UserDTO updateUser(UpdateUserCommand command);
    
    /**
     * 删除用户
     */
    void deleteUser(Long id);
    
    /**
     * 为用户添加角色
     */
    void addRoleToUser(String username, String roleCode);
    
    /**
     * 从用户中移除角色
     */
    void removeRoleFromUser(String username, String roleCode);
    
    /**
     * 更新用户最后登录时间
     */
    void updateLastLoginTime(String username);
} 