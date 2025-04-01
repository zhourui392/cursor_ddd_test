package com.example.demo.domain.repository;

import com.example.demo.domain.model.entity.Role;
import com.example.demo.domain.model.valueobject.RoleId;

import java.util.List;
import java.util.Optional;

/**
 * 角色仓储接口
 */
public interface RoleRepository {
    /**
     * 保存角色
     */
    Role save(Role role);
    
    /**
     * 根据ID查找角色
     */
    Optional<Role> findById(RoleId id);
    
    /**
     * 根据编码查找角色
     */
    Optional<Role> findByCode(String code);
    
    /**
     * 查询所有角色
     */
    List<Role> findAll();
    
    /**
     * 检查角色编码是否存在
     */
    boolean existsByCode(String code);
    
    /**
     * 删除角色
     */
    void delete(Role role);
} 