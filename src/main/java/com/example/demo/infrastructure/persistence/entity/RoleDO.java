package com.example.demo.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * 角色数据对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("role")
public class RoleDO {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField(value = "name")
    private String name;
    
    @TableField(value = "code")
    private String code;
    
    @TableField(value = "description")
    private String description;
    
    @TableField(value = "status")
    private Boolean status = true;
    
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    // MyBatis Plus不直接支持多对多关系，需要在查询时处理
    @TableField(exist = false)
    private Set<UserDO> users = new HashSet<>();
    
    // MyBatis Plus不直接支持多对多关系，需要在查询时处理
    @TableField(exist = false)
    private Set<PermissionDO> permissions = new HashSet<>();
} 