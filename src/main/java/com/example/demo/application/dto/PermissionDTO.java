package com.example.demo.application.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 权限数据传输对象
 */
@Data
public class PermissionDTO {
    /**
     * 权限ID
     */
    private Long id;
    
    /**
     * 权限名称
     */
    private String name;
    
    /**
     * 权限编码
     */
    private String code;
    
    /**
     * 权限描述
     */
    private String description;
    
    /**
     * 模块名称
     */
    private String module;
    
    /**
     * 权限状态
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
} 