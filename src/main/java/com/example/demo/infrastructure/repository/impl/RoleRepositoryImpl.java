package com.example.demo.infrastructure.repository.impl;

import com.example.demo.domain.model.entity.Role;
import com.example.demo.domain.model.valueobject.RoleId;
import com.example.demo.domain.repository.RoleRepository;
import com.example.demo.infrastructure.convert.RoleConvert;
import com.example.demo.infrastructure.mapper.RoleMapper;
import com.example.demo.infrastructure.persistence.entity.RoleDO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

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
    private final RoleConvert roleConvert;

    @Override
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
        
        return roleConvert.toDomain(roleDO);
    }

    @Override
    public Optional<Role> findById(RoleId id) {
        RoleDO roleDO = roleMapper.selectById(id.getValue());
        return Optional.ofNullable(roleDO)
                .map(roleConvert::toDomain);
    }

    @Override
    public Optional<Role> findByCode(String code) {
        RoleDO roleDO = roleMapper.selectByCode(code);
        return Optional.ofNullable(roleDO)
                .map(roleConvert::toDomain);
    }

    @Override
    public List<Role> findAll() {
        List<RoleDO> roleDOList = roleMapper.selectList(null);
        return roleDOList.stream()
                .map(roleConvert::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByCode(String code) {
        Integer count = roleMapper.countByCode(code);
        return count != null && count > 0;
    }

    @Override
    public void delete(Role role) {
        roleMapper.deleteById(role.getId().getValue());
    }
} 