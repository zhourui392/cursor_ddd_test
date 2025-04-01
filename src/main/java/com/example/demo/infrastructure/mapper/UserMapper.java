package com.example.demo.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.infrastructure.persistence.entity.UserDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Optional;

/**
 * 用户Mapper接口
 */
@Mapper
public interface UserMapper extends BaseMapper<UserDO> {
    
    /**
     * 根据用户名查询用户
     * 
     * @param username 用户名
     * @return 用户信息
     */
    @Select("SELECT * FROM user WHERE username = #{username}")
    UserDO selectByUsername(@Param("username") String username);
    
    /**
     * 检查用户名是否存在
     * 
     * @param username 用户名
     * @return 数量
     */
    @Select("SELECT COUNT(1) FROM user WHERE username = #{username}")
    Integer countByUsername(@Param("username") String username);
    
    /**
     * 根据邮箱查询用户
     * 
     * @param email 邮箱
     * @return 用户信息
     */
    @Select("SELECT * FROM user WHERE email = #{email}")
    UserDO selectByEmail(@Param("email") String email);
    
    /**
     * 根据手机号查询用户
     * 
     * @param phone 手机号
     * @return 用户信息
     */
    @Select("SELECT * FROM user WHERE phone = #{phone}")
    UserDO selectByPhone(@Param("phone") String phone);
} 