package com.example.demo.infrastructure.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 角色权限关联Mapper接口
 */
@Mapper
public interface RolePermissionMapper {
    
    /**
     * 删除角色的所有权限关联
     * 
     * @param roleId 角色ID
     * @return 影响行数
     */
    int deleteByRoleId(@Param("roleId") Long roleId);
    
    /**
     * 添加角色权限关联
     * 
     * @param roleId 角色ID
     * @param permissionId 权限ID
     * @return 影响行数
     */
    int insert(@Param("roleId") Long roleId, @Param("permissionId") Long permissionId);
    
    /**
     * 批量添加角色权限关联
     * 
     * @param roleId 角色ID
     * @param permissionIds 权限ID列表
     * @return 影响行数
     */
    int batchInsert(@Param("roleId") Long roleId, @Param("permissionIds") java.util.List<Long> permissionIds);
} 