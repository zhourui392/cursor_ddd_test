package com.example.demo.application.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 角色数据传输对象
 */
@Data
public class RoleDTO {
    /**
     * 角色ID
     */
    private Long id;
    
    /**
     * 角色名称
     */
    private String name;
    
    /**
     * 角色编码
     */
    private String code;
    
    /**
     * 角色描述
     */
    private String description;
    
    /**
     * 角色状态
     */
    private Boolean status;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 角色拥有的权限列表
     */
    private List<PermissionDTO> permissions;
} 