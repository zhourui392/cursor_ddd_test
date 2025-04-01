package com.example.demo.application.service;

import java.util.List;

import com.example.demo.application.dto.RoleDTO;

/**
 * 角色应用服务接口
 */
public interface RoleService {
    /**
     * 获取所有角色
     */
    List<RoleDTO> findAll();
    
    /**
     * 根据ID获取角色
     */
    RoleDTO findById(Long id);
    
    /**
     * 创建角色
     */
    RoleDTO create(RoleDTO roleDTO);
    
    /**
     * 更新角色
     */
    RoleDTO update(Long id, RoleDTO roleDTO);
    
    /**
     * 删除角色
     */
    void delete(Long id);
    
    /**
     * 为角色添加权限
     */
    void addPermissionToRole(String roleCode, String permissionCode);
    
    /**
     * 从角色中移除权限
     */
    void removePermissionFromRole(String roleCode, String permissionCode);
} 