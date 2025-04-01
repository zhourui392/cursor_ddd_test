package com.example.demo.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.infrastructure.persistence.entity.PermissionDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 权限Mapper接口
 */
@Mapper
public interface PermissionMapper extends BaseMapper<PermissionDO> {
    
    /**
     * 根据编码查询权限
     * 
     * @param code 权限编码
     * @return 权限对象
     */
    @Select("SELECT * FROM permission WHERE code = #{code}")
    PermissionDO selectByCode(@Param("code") String code);
    
    /**
     * 检查权限编码是否存在
     * 
     * @param code 权限编码
     * @return 是否存在
     */
    @Select("SELECT COUNT(1) FROM permission WHERE code = #{code}")
    Integer countByCode(@Param("code") String code);
    
    /**
     * 根据角色ID查询权限列表
     * 
     * @param roleId 角色ID
     * @return 权限列表
     */
    List<PermissionDO> findPermissionsByRoleId(@Param("roleId") Long roleId);
} 