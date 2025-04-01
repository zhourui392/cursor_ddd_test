package com.example.demo.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * 角色DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleDTO {
    private Long id;
    private String name;
    private String code;
    private String description;
    private Boolean status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Set<PermissionDTO> permissions;
} 