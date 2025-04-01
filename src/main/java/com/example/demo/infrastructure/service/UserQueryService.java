package com.example.demo.infrastructure.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.application.dto.UserDTO;
import com.example.demo.infrastructure.convert.UserConvert;
import com.example.demo.infrastructure.mapper.UserMapper;
import com.example.demo.infrastructure.persistence.entity.UserDO;
import com.example.demo.infrastructure.util.SqlUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户查询服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserQueryService {

    private final UserMapper userMapper;
    private final UserConvert userConvert;

    /**
     * 分页查询用户列表
     *
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @param keyword  关键字
     * @return 用户DTO分页列表
     */
    @Cacheable(value = "user:page", key = "#pageNum + '-' + #pageSize + '-' + #keyword")
    public IPage<UserDTO> getUsersPage(int pageNum, int pageSize, String keyword) {
        log.debug("分页查询用户列表: pageNum={}, pageSize={}, keyword={}", pageNum, pageSize, keyword);
        
        // 构建查询条件
        LambdaQueryWrapper<UserDO> queryWrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            queryWrapper.like(UserDO::getUsername, keyword)
                    .or()
                    .like(UserDO::getNickname, keyword)
                    .or()
                    .like(UserDO::getEmail, keyword);
        }
        
        // 构建分页对象
        Page<UserDO> page = SqlUtil.buildPage(pageNum, pageSize, "create_time", false);
        Page<UserDO> userDOPage = userMapper.selectPage(page, queryWrapper);
        
        // 转换为DTO
        Page<UserDTO> userDTOPage = new Page<>(
                userDOPage.getCurrent(),
                userDOPage.getSize(),
                userDOPage.getTotal());
        
        List<UserDTO> userDTOList = userDOPage.getRecords().stream()
                .map(userDO -> userConvert.toDto(userConvert.toDomain(userDO)))
                .collect(Collectors.toList());
        
        userDTOPage.setRecords(userDTOList);
        return userDTOPage;
    }
    
    /**
     * 获取所有用户列表
     *
     * @return 用户DTO列表
     */
    @Cacheable(value = "user:all")
    public List<UserDTO> getAllUsers() {
        log.debug("查询所有用户列表");
        
        List<UserDO> userDOList = userMapper.selectList(null);
        return userDOList.stream()
                .map(userDO -> userConvert.toDto(userConvert.toDomain(userDO)))
                .collect(Collectors.toList());
    }
} 