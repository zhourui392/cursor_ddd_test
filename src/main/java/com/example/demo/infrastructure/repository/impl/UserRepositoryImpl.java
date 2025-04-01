package com.example.demo.infrastructure.repository.impl;

import com.example.demo.domain.model.entity.Role;
import com.example.demo.domain.model.entity.User;
import com.example.demo.domain.model.valueobject.UserId;
import com.example.demo.domain.repository.UserRepository;
import com.example.demo.infrastructure.convert.PermissionConvert;
import com.example.demo.infrastructure.convert.RoleConvert;
import com.example.demo.infrastructure.convert.UserConvert;
import com.example.demo.infrastructure.mapper.PermissionMapper;
import com.example.demo.infrastructure.mapper.RoleMapper;
import com.example.demo.infrastructure.mapper.UserMapper;
import com.example.demo.infrastructure.persistence.entity.PermissionDO;
import com.example.demo.infrastructure.persistence.entity.RoleDO;
import com.example.demo.infrastructure.persistence.entity.UserDO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 用户仓储实现
 */
@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final PermissionMapper permissionMapper;
    private final UserConvert userConvert;
    private final RoleConvert roleConvert;
    private final PermissionConvert permissionConvert;

    @Override
    public User save(User user) {
        UserDO userDO;
        if (user.getId() == null) {
            userDO = userConvert.toData(user);
        } else {
            userDO = userMapper.selectById(user.getId().getValue());
            if (userDO == null) {
                userDO = userConvert.toData(user);
            } else {
                userConvert.updateDataFromDomain(user, userDO);
            }
        }
        
        if (userDO.getId() == null) {
            userMapper.insert(userDO);
        } else {
            userMapper.updateById(userDO);
        }
        
        return userConvert.toDomain(userDO);
    }

    @Override
    public Optional<User> findById(UserId id) {
        UserDO userDO = userMapper.selectById(id.getValue());
        return Optional.ofNullable(userDO)
                .map(userConvert::toDomain);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        UserDO userDO = userMapper.selectByUsername(username);
        Optional<User> userOptional = Optional.ofNullable(userDO).map(userConvert::toDomain);
        if (userOptional.isPresent()){
            User user = userOptional.get();
            List<RoleDO> rolesByUser = roleMapper.findRolesByUserId(user.getId().getValue());
            List<Role> roleList = roleConvert.toDomainList(rolesByUser);
            if (!CollectionUtils.isEmpty(roleList)){
                //查询权限
                for (Role role : roleList) {
                    List<PermissionDO> permissionsByRoleId = permissionMapper.findPermissionsByRoleId(role.getId().getValue());
                    role.addPermissions(permissionConvert.toDomainList(permissionsByRoleId));
                }
            }
            user.addRoles(roleList);
        }
        return userOptional;
    }

    @Override
    public Optional<User> findByEmail(String email) {
        UserDO userDO = userMapper.selectByEmail(email);
        return Optional.ofNullable(userDO)
                .map(userConvert::toDomain);
    }

    @Override
    public Optional<User> findByPhone(String phone) {
        UserDO userDO = userMapper.selectByPhone(phone);
        return Optional.ofNullable(userDO)
                .map(userConvert::toDomain);
    }

    @Override
    public List<User> findAll() {
        List<UserDO> userDOList = userMapper.selectList(null);
        return userDOList.stream()
                .map(userConvert::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByUsername(String username) {
        Integer count = userMapper.countByUsername(username);
        return count != null && count > 0;
    }

    @Override
    public void delete(User user) {
        userMapper.deleteById(user.getId().getValue());
    }
} 