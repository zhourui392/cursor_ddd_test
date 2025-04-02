package com.example.demo.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * 权限数据对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("permission")
public class PermissionDO {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField(value = "name", condition = "name IS NOT NULL")
    private String name;
    
    @TableField(value = "code", condition = "code IS NOT NULL")
    private String code;
    
    @TableField("description")
    private String description;
    
    @TableField("module")
    private String module;
    
    @TableField("status")
    private Boolean status = true;
    
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    // 由于MybatisPlus不直接支持多对多映射，这个关系需要在查询时处理
    // 这里先移除，稍后在转换器或服务层处理关联关系
    @TableField(exist = false)
    private Set<RoleDO> roles = new HashSet<>();
} 