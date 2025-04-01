package com.example.demo.application.service;

import java.util.List;

import com.example.demo.application.dto.PermissionDTO;

/**
 * 权限应用服务接口
 */
public interface PermissionService {
    /**
     * 获取所有权限
     */
    List<PermissionDTO> findAll();
    
    /**
     * 根据ID获取权限
     */
    PermissionDTO findById(Long id);
    
    /**
     * 创建权限
     */
    PermissionDTO create(PermissionDTO permissionDTO);
    
    /**
     * 更新权限
     */
    PermissionDTO update(Long id, PermissionDTO permissionDTO);
    
    /**
     * 删除权限
     */
    void delete(Long id);
} 