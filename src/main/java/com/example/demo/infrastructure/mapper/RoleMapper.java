package com.example.demo.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.infrastructure.persistence.entity.RoleDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 角色Mapper接口
 */
@Mapper
public interface RoleMapper extends BaseMapper<RoleDO> {
    
    /**
     * 根据编码查询角色
     * 
     * @param code 角色编码
     * @return 角色信息
     */
    @Select("SELECT * FROM role WHERE code = #{code}")
    RoleDO selectByCode(@Param("code") String code);
    
    /**
     * 检查角色编码是否存在
     * 
     * @param code 角色编码
     * @return 数量
     */
    @Select("SELECT COUNT(1) FROM role WHERE code = #{code}")
    Integer countByCode(@Param("code") String code);
    
    /**
     * 根据用户ID查询角色列表
     * 
     * @param userId 用户ID
     * @return 角色列表
     */
    List<RoleDO> findRolesByUserId(@Param("userId") Long userId);
} 