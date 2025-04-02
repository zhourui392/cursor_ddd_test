package com.example.demo.infrastructure.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户角色关联Mapper接口
 */
@Mapper
public interface UserRoleMapper {
    
    /**
     * 添加用户角色关联
     * 
     * @param userId 用户ID
     * @param roleId 角色ID
     * @return 影响行数
     */
    int insert(@Param("userId") Long userId, @Param("roleId") Long roleId);
    
    /**
     * 批量添加用户角色关联
     * 
     * @param userId 用户ID
     * @param roleIds 角色ID列表
     * @return 影响行数
     */
    int batchInsert(@Param("userId") Long userId, @Param("roleIds") List<Long> roleIds);
    
    /**
     * 删除用户的所有角色关联
     * 
     * @param userId 用户ID
     * @return 影响行数
     */
    int deleteByUserId(@Param("userId") Long userId);
    
    /**
     * 删除用户特定角色关联
     * 
     * @param userId 用户ID
     * @param roleId 角色ID
     * @return 影响行数
     */
    int deleteByUserIdAndRoleId(@Param("userId") Long userId, @Param("roleId") Long roleId);
    
    /**
     * 检查用户是否有特定角色
     * 
     * @param userId 用户ID
     * @param roleId 角色ID
     * @return 数量
     */
    int countByUserIdAndRoleId(@Param("userId") Long userId, @Param("roleId") Long roleId);
} 