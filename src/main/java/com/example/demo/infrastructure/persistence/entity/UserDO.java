package com.example.demo.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * 用户数据对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("user")
public class UserDO {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField(value = "username")
    private String username;
    
    @TableField(value = "password")
    private String password;
    
    @TableField(value = "nickname")
    private String nickname;
    
    @TableField(value = "email")
    private String email;
    
    @TableField(value = "phone")
    private String phone;
    
    @TableField(value = "status")
    private Boolean status = true;
    
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableField(value = "last_login_time")
    private LocalDateTime lastLoginTime;
    
    // MyBatis Plus不直接支持多对多关系，需要在查询时处理
    @TableField(exist = false)
    private Set<RoleDO> roles = new HashSet<>();
} 