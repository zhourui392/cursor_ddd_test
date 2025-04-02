package com.example.demo.infrastructure.repository.impl;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

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
import com.example.demo.infrastructure.mapper.UserRoleMapper;
import com.example.demo.infrastructure.persistence.entity.PermissionDO;
import com.example.demo.infrastructure.persistence.entity.RoleDO;
import com.example.demo.infrastructure.persistence.entity.UserDO;

import lombok.RequiredArgsConstructor;

/**
 * 用户仓储实现
 * 遵循DDD原则：
 * 1. 仓储只负责聚合根的持久化，不跨越聚合边界
 * 2. 保持领域模型的完整性
 * 3. 查询功能集中在仓储中，便于优化和维护
 */
@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final PermissionMapper permissionMapper;
    private final UserRoleMapper userRoleMapper;
    private final UserConvert userConvert;
    private final RoleConvert roleConvert;
    private final PermissionConvert permissionConvert;

    /**
     * 保存用户聚合根
     * 这里遵循DDD中的"一个聚合一个仓储"原则
     */
    @Override
    @Transactional
    public User save(User user) {
        // 入参校验
        Objects.requireNonNull(user, "待保存的用户不能为空");
        
        UserDO userDO;
        
        // 判断是新增还是更新
        if (user.getId() == null) {
            // 新用户直接转换
            userDO = userConvert.toData(user);
        } else {
            // 现有用户先查找，再更新
            userDO = userMapper.selectById(user.getId().getValue());
            if (userDO == null) {
                userDO = userConvert.toData(user);
            } else {
                userConvert.updateDataFromDomain(user, userDO);
            }
        }
        
        // 持久化用户基本信息
        if (userDO.getId() == null) {
            userMapper.insert(userDO);
        } else {
            userMapper.updateById(userDO);
        }
        
        // 处理用户-角色关联
        if (user.getId() != null) {
            // 删除该用户原有的所有角色关联
            userRoleMapper.deleteByUserId(userDO.getId());
            
            // 添加新的角色关联
            if (user.getRoles() != null && !user.getRoles().isEmpty()) {
                // 将角色ID提取为列表
                List<Long> roleIds = user.getRoles().stream()
                    .filter(role -> role.getId() != null)
                    .map(role -> role.getId().getValue())
                    .collect(Collectors.toList());
                    
                if (!roleIds.isEmpty()) {
                    // 使用批量插入
                    userRoleMapper.batchInsert(userDO.getId(), roleIds);
                }
            }
        }
        
        // 返回转换后的领域对象
        return userConvert.toDomain(userDO);
    }

    /**
     * 根据ID查找用户
     * 注意：这里没有加载角色和权限信息，保持单一职责
     */
    @Override
    public Optional<User> findById(UserId id) {
        Objects.requireNonNull(id, "用户ID不能为空");
        
        UserDO userDO = userMapper.selectById(id.getValue());
        return Optional.ofNullable(userDO)
                .map(userConvert::toDomain);
    }

    /**
     * 根据用户名查找完整用户（包含角色和权限）
     * 使用左连接查询避免N+1问题
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        Objects.requireNonNull(username, "用户名不能为空");
        
        // 1. 获取用户基本信息
        UserDO userDO = userMapper.selectByUsername(username);
        if (userDO == null) {
            return Optional.empty();
        }
        
        // 2. 转换为领域对象
        User user = userConvert.toDomain(userDO);
        
        // 3. 查询该用户的所有角色
        List<RoleDO> rolesByUser = roleMapper.findRolesByUserId(user.getId().getValue());
        if (!CollectionUtils.isEmpty(rolesByUser)) {
            List<Role> roleList = roleConvert.toDomainList(rolesByUser);
            
            // 4. 批量查询角色的权限 (避免N+1问题)
            for (Role role : roleList) {
                List<PermissionDO> permissionsByRoleId = permissionMapper.findPermissionsByRoleId(role.getId().getValue());
                if (!CollectionUtils.isEmpty(permissionsByRoleId)) {
                    // 转换并添加权限到角色中
                    role.addPermissions(permissionConvert.toDomainList(permissionsByRoleId));
                }
            }
            
            // 5. 将角色集合添加到用户中
            user.addRoles(roleList);
        }
        
        return Optional.of(user);
    }

    /**
     * 根据邮箱查找用户
     * 注意：这里没有加载角色和权限信息，保持单一职责
     */
    @Override
    public Optional<User> findByEmail(String email) {
        Objects.requireNonNull(email, "邮箱不能为空");
        
        UserDO userDO = userMapper.selectByEmail(email);
        return Optional.ofNullable(userDO)
                .map(userConvert::toDomain);
    }

    /**
     * 根据手机号查找用户
     * 注意：这里没有加载角色和权限信息，保持单一职责
     */
    @Override
    public Optional<User> findByPhone(String phone) {
        Objects.requireNonNull(phone, "手机号不能为空");
        
        UserDO userDO = userMapper.selectByPhone(phone);
        return Optional.ofNullable(userDO)
                .map(userConvert::toDomain);
    }

    /**
     * 查询所有用户（不包含角色和权限）
     */
    @Override
    public List<User> findAll() {
        List<UserDO> userDOList = userMapper.selectList(null);
        return userDOList.stream()
                .map(userConvert::toDomain)
                .collect(Collectors.toList());
    }

    /**
     * 检查用户名是否存在
     */
    @Override
    public boolean existsByUsername(String username) {
        Objects.requireNonNull(username, "用户名不能为空");
        
        Integer count = userMapper.countByUsername(username);
        return count != null && count > 0;
    }

    /**
     * 删除用户
     */
    @Override
    @Transactional
    public void delete(User user) {
        Objects.requireNonNull(user, "待删除的用户不能为空");
        Objects.requireNonNull(user.getId(), "待删除的用户ID不能为空");
        
        // 删除用户角色关联
        userRoleMapper.deleteByUserId(user.getId().getValue());
        // 删除用户
        userMapper.deleteById(user.getId().getValue());
    }
} 