package com.example.demo.infrastructure.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.application.dto.PermissionDTO;
import com.example.demo.infrastructure.convert.PermissionConvert;
import com.example.demo.infrastructure.mapper.PermissionMapper;
import com.example.demo.infrastructure.persistence.entity.PermissionDO;
import com.example.demo.infrastructure.util.SqlUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 权限查询服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionQueryService {

    private final PermissionMapper permissionMapper;
    private final PermissionConvert permissionConvert;

    /**
     * 分页查询权限列表
     *
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @param keyword  关键字
     * @return 权限DTO分页列表
     */
    @Cacheable(value = "permission:page", key = "#pageNum + '-' + #pageSize + '-' + #keyword")
    public IPage<PermissionDTO> getPermissionsPage(int pageNum, int pageSize, String keyword) {
        log.debug("分页查询权限列表: pageNum={}, pageSize={}, keyword={}", pageNum, pageSize, keyword);
        
        // 构建查询条件
        LambdaQueryWrapper<PermissionDO> queryWrapper = new LambdaQueryWrapper<>();
        SqlUtil.buildLikeQuery(
            queryWrapper, 
            keyword, 
            PermissionDO::getName, 
            PermissionDO::getCode, 
            PermissionDO::getDescription
        );
        
        // 构建分页对象
        Page<PermissionDO> page = SqlUtil.buildPage(pageNum, pageSize, "create_time", false);
        Page<PermissionDO> permissionDOPage = permissionMapper.selectPage(page, queryWrapper);
        
        // 转换为DTO
        Page<PermissionDTO> permissionDTOPage = new Page<>(
                permissionDOPage.getCurrent(),
                permissionDOPage.getSize(),
                permissionDOPage.getTotal());
        
        List<PermissionDTO> permissionDTOList = permissionDOPage.getRecords().stream()
                .map(permissionDO -> permissionConvert.toDto(permissionConvert.toDomain(permissionDO)))
                .collect(Collectors.toList());
        
        permissionDTOPage.setRecords(permissionDTOList);
        return permissionDTOPage;
    }
    
    /**
     * 获取所有权限列表
     *
     * @return 权限DTO列表
     */
    @Cacheable(value = "permission:all")
    public List<PermissionDTO> getAllPermissions() {
        log.debug("查询所有权限列表");
        
        List<PermissionDO> permissionDOList = permissionMapper.selectList(null);
        return permissionDOList.stream()
                .map(permissionDO -> permissionConvert.toDto(permissionConvert.toDomain(permissionDO)))
                .collect(Collectors.toList());
    }
    
    /**
     * 根据角色ID查询权限列表
     *
     * @param roleId 角色ID
     * @return 权限DTO列表
     */
    @Cacheable(value = "permission:role", key = "#roleId")
    public List<PermissionDTO> getPermissionsByRoleId(Long roleId) {
        log.debug("根据角色ID查询权限列表: roleId={}", roleId);
        
        List<PermissionDO> permissionDOList = permissionMapper.findPermissionsByRoleId(roleId);
        return permissionDOList.stream()
                .map(permissionDO -> permissionConvert.toDto(permissionConvert.toDomain(permissionDO)))
                .collect(Collectors.toList());
    }
} 