package com.example.demo.infrastructure.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.application.dto.RoleDTO;
import com.example.demo.infrastructure.convert.RoleConvert;
import com.example.demo.infrastructure.mapper.RoleMapper;
import com.example.demo.infrastructure.persistence.entity.RoleDO;
import com.example.demo.infrastructure.util.SqlUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色查询服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoleQueryService {

    private final RoleMapper roleMapper;
    private final RoleConvert roleConvert;

    /**
     * 分页查询角色列表
     *
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @param keyword  关键字
     * @return 角色DTO分页列表
     */
    @Cacheable(value = "role:page", key = "#pageNum + '-' + #pageSize + '-' + #keyword")
    public IPage<RoleDTO> getRolesPage(int pageNum, int pageSize, String keyword) {
        log.debug("分页查询角色列表: pageNum={}, pageSize={}, keyword={}", pageNum, pageSize, keyword);
        
        // 构建查询条件
        LambdaQueryWrapper<RoleDO> queryWrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            queryWrapper.like(RoleDO::getName, keyword)
                    .or()
                    .like(RoleDO::getCode, keyword)
                    .or()
                    .like(RoleDO::getDescription, keyword);
        }
        
        // 构建分页对象
        Page<RoleDO> page = new Page<>(pageNum, pageSize);
        if (pageSize <= 0) {
            page.setSize(-1); // 不分页
        }
        
        // 设置排序
        OrderItem orderItem = new OrderItem();
        orderItem.setColumn("create_time");
        orderItem.setAsc(false);
        page.addOrder(orderItem);
        
        Page<RoleDO> roleDOPage = roleMapper.selectPage(page, queryWrapper);
        
        // 转换为DTO
        Page<RoleDTO> roleDTOPage = new Page<>(
                roleDOPage.getCurrent(),
                roleDOPage.getSize(),
                roleDOPage.getTotal());
        
        List<RoleDTO> roleDTOList = roleDOPage.getRecords().stream()
                .map(roleDO -> roleConvert.toDto(roleConvert.toDomain(roleDO)))
                .collect(Collectors.toList());
        
        roleDTOPage.setRecords(roleDTOList);
        return roleDTOPage;
    }
    
    /**
     * 获取所有角色列表
     *
     * @return 角色DTO列表
     */
    @Cacheable(value = "role:all")
    public List<RoleDTO> getAllRoles() {
        log.debug("查询所有角色列表");
        
        List<RoleDO> roleDOList = roleMapper.selectList(null);
        return roleDOList.stream()
                .map(roleDO -> roleConvert.toDto(roleConvert.toDomain(roleDO)))
                .collect(Collectors.toList());
    }
    
    /**
     * 根据用户ID查询角色列表
     *
     * @param userId 用户ID
     * @return 角色DTO列表
     */
    @Cacheable(value = "role:user", key = "#userId")
    public List<RoleDTO> getRolesByUserId(Long userId) {
        log.debug("根据用户ID查询角色列表: userId={}", userId);
        
        List<RoleDO> roleDOList = roleMapper.findRolesByUserId(userId);
        return roleDOList.stream()
                .map(roleDO -> roleConvert.toDto(roleConvert.toDomain(roleDO)))
                .collect(Collectors.toList());
    }
} 