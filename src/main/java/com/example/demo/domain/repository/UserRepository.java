package com.example.demo.domain.repository;

import com.example.demo.domain.model.entity.User;
import com.example.demo.domain.model.valueobject.UserId;

import java.util.List;
import java.util.Optional;

/**
 * 用户仓储接口
 */
public interface UserRepository {
    /**
     * 保存用户
     */
    User save(User user);
    
    /**
     * 根据ID查找用户
     */
    Optional<User> findById(UserId id);
    
    /**
     * 根据用户名查找用户
     */
    Optional<User> findByUsername(String username);
    
    /**
     * 根据邮箱查找用户
     */
    Optional<User> findByEmail(String email);
    
    /**
     * 根据手机号查找用户
     */
    Optional<User> findByPhone(String phone);
    
    /**
     * 查询所有用户
     */
    List<User> findAll();
    
    /**
     * 检查用户名是否存在
     */
    boolean existsByUsername(String username);
    
    /**
     * 删除用户
     */
    void delete(User user);
} 