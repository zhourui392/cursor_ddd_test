package com.example.demo.infrastructure.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.demo.domain.model.entity.Permission;
import com.example.demo.domain.model.valueobject.PermissionId;
import com.example.demo.domain.repository.PermissionRepository;
import com.example.demo.infrastructure.convert.PermissionConvert;
import com.example.demo.infrastructure.mapper.PermissionMapper;
import com.example.demo.infrastructure.persistence.entity.PermissionDO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 权限仓储实现
 */
@Repository
@RequiredArgsConstructor
public class PermissionRepositoryImpl implements PermissionRepository {

    private final PermissionMapper permissionMapper;
    private final PermissionConvert permissionConvert;

    @Override
    public Permission save(Permission permission) {
        PermissionDO permissionDO;
        if (permission.getId() == null) {
            permissionDO = permissionConvert.toData(permission);
        } else {
            permissionDO = permissionMapper.selectById(permission.getId().getValue());
            if (permissionDO == null) {
                permissionDO = permissionConvert.toData(permission);
            } else {
                permissionConvert.updateDataFromDomain(permission, permissionDO);
            }
        }
        
        if (permissionDO.getId() == null) {
            permissionMapper.insert(permissionDO);
        } else {
            permissionMapper.updateById(permissionDO);
        }
        
        return permissionConvert.toDomain(permissionDO);
    }

    @Override
    public Optional<Permission> findById(PermissionId id) {
        PermissionDO permissionDO = permissionMapper.selectById(id.getValue());
        return Optional.ofNullable(permissionDO)
                .map(permissionConvert::toDomain);
    }

    @Override
    public Optional<Permission> findByCode(String code) {
        PermissionDO permissionDO = permissionMapper.selectByCode(code);
        return Optional.ofNullable(permissionDO)
                .map(permissionConvert::toDomain);
    }

    @Override
    public List<Permission> findAll() {
        List<PermissionDO> permissionDOList = permissionMapper.selectList(null);
        return permissionDOList.stream()
                .map(permissionConvert::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByCode(String code) {
        Integer count = permissionMapper.countByCode(code);
        return count != null && count > 0;
    }

    @Override
    public void delete(Permission permission) {
        permissionMapper.deleteById(permission.getId().getValue());
    }
} 