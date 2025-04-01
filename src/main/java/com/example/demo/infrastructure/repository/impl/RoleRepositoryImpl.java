package com.example.demo.infrastructure.repository.impl;

import com.example.demo.domain.model.entity.Role;
import com.example.demo.domain.model.valueobject.RoleId;
import com.example.demo.domain.repository.RoleRepository;
import com.example.demo.infrastructure.convert.RoleConvert;
import com.example.demo.infrastructure.mapper.PermissionMapper;
import com.example.demo.infrastructure.mapper.RoleMapper;
import com.example.demo.infrastructure.mapper.RolePermissionMapper;
import com.example.demo.infrastructure.persistence.entity.PermissionDO;
import com.example.demo.infrastructure.persistence.entity.RoleDO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 角色仓储实现
 */
@Repository
@RequiredArgsConstructor
public class RoleRepositoryImpl implements RoleRepository {

    private final RoleMapper roleMapper;
    private final PermissionMapper permissionMapper;
    private final RolePermissionMapper rolePermissionMapper;
    private final RoleConvert roleConvert;

    @Override
    @Transactional
    public Role save(Role role) {
        RoleDO roleDO;
        if (role.getId() == null) {
            roleDO = roleConvert.toData(role);
        } else {
            roleDO = roleMapper.selectById(role.getId().getValue());
            if (roleDO == null) {
                roleDO = roleConvert.toData(role);
            } else {
                roleConvert.updateDataFromDomain(role, roleDO);
            }
        }
        
        if (roleDO.getId() == null) {
            roleMapper.insert(roleDO);
        } else {
            roleMapper.updateById(roleDO);
        }
        
        // 保存角色权限关联关系
        if (role.getId() != null) {
            // 删除该角色原有的所有权限关联
            rolePermissionMapper.deleteByRoleId(roleDO.getId());
        }
        
        // 添加新的权限关联
        if (role.getPermissions() != null && !role.getPermissions().isEmpty()) {
            // 将权限ID提取为列表
            List<Long> permissionIds = role.getPermissions().stream()
                .filter(permission -> permission.getId() != null)
                .map(permission -> permission.getId().getValue())
                .collect(Collectors.toList());
                
            if (!permissionIds.isEmpty()) {
                // 使用批量插入
                rolePermissionMapper.batchInsert(roleDO.getId(), permissionIds);
            }
        }
        
        return roleConvert.toDomain(roleDO);
    }

    @Override
    public Optional<Role> findById(RoleId id) {
        RoleDO roleDO = roleMapper.selectById(id.getValue());
        if (roleDO != null) {
            // 加载角色的权限
            loadPermissionsForRole(roleDO);
        }
        return Optional.ofNullable(roleDO)
                .map(roleConvert::toDomain);
    }

    @Override
    public Optional<Role> findByCode(String code) {
        RoleDO roleDO = roleMapper.selectByCode(code);
        if (roleDO != null) {
            // 加载角色的权限
            loadPermissionsForRole(roleDO);
        }
        return Optional.ofNullable(roleDO)
                .map(roleConvert::toDomain);
    }

    @Override
    public List<Role> findAll() {
        List<RoleDO> roleDOList = roleMapper.selectList(null);
        // 为每个角色加载权限
        roleDOList.forEach(this::loadPermissionsForRole);
        return roleDOList.stream()
                .map(roleConvert::toDomain)
                .collect(Collectors.toList());
    }

    /**
     * 加载角色的权限列表
     * 
     * @param roleDO 角色数据对象
     */
    private void loadPermissionsForRole(RoleDO roleDO) {
        // 使用MyBatis查询角色的权限列表
        List<PermissionDO> permissions = permissionMapper.findPermissionsByRoleId(roleDO.getId());
        if (!permissions.isEmpty()) {
            roleDO.setPermissions(new java.util.HashSet<>(permissions));
        }
    }

    @Override
    public boolean existsByCode(String code) {
        Integer count = roleMapper.countByCode(code);
        return count != null && count > 0;
    }

    @Override
    @Transactional
    public void delete(Role role) {
        // 删除角色权限关联
        if (role.getId() != null) {
            rolePermissionMapper.deleteByRoleId(role.getId().getValue());
        }
        roleMapper.deleteById(role.getId().getValue());
    }
} 