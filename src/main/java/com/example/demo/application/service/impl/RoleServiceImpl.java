package com.example.demo.application.service.impl;

import com.example.demo.application.dto.PermissionDTO;
import com.example.demo.application.dto.RoleDTO;
import com.example.demo.application.service.RoleService;
import com.example.demo.domain.model.entity.Permission;
import com.example.demo.domain.model.entity.Role;
import com.example.demo.domain.model.valueobject.PermissionId;
import com.example.demo.domain.model.valueobject.RoleId;
import com.example.demo.domain.repository.PermissionRepository;
import com.example.demo.domain.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色应用服务实现
 */
@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    @Override
    public List<RoleDTO> findAll() {
        List<Role> roles = roleRepository.findAll();
        return roles.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public RoleDTO findById(Long id) {
        Role role = roleRepository.findById(new RoleId(id))
                .orElseThrow(() -> new RuntimeException("角色不存在"));
        return convertToDTO(role);
    }

    @Override
    @Transactional
    public RoleDTO create(RoleDTO roleDTO) {
        // 检查编码是否已存在
        if (roleRepository.existsByCode(roleDTO.getCode())) {
            throw new RuntimeException("角色编码已存在");
        }

        // 创建角色
        Role role = Role.create(
                roleDTO.getName(),
                roleDTO.getCode(),
                roleDTO.getDescription()
        );

        // 处理权限
        if (roleDTO.getPermissions() != null && !roleDTO.getPermissions().isEmpty()) {
            List<Permission> permissions = new ArrayList<>();
            for (PermissionDTO permissionDTO : roleDTO.getPermissions()) {
                Permission permission = permissionRepository.findByCode(permissionDTO.getCode())
                        .orElseThrow(() -> new RuntimeException("权限不存在：" + permissionDTO.getCode()));
                permissions.add(permission);
            }
            role.addPermissions(permissions);
        }

        // 保存并返回
        Role savedRole = roleRepository.save(role);
        return convertToDTO(savedRole);
    }

    @Override
    @Transactional
    public RoleDTO update(Long id, RoleDTO roleDTO) {
        // 获取角色
        Role role = roleRepository.findById(new RoleId(id))
                .orElseThrow(() -> new RuntimeException("角色不存在"));

        // 如果修改了编码，检查新编码是否已存在
        if (!role.getCode().equals(roleDTO.getCode()) &&
                roleRepository.existsByCode(roleDTO.getCode())) {
            throw new RuntimeException("角色编码已存在");
        }

        // 更新角色信息
        role.update(roleDTO.getName(), roleDTO.getDescription());
        
        // 状态更新
        if (roleDTO.getStatus() != null) {
            if (roleDTO.getStatus()) {
                role.enable();
            } else {
                role.disable();
            }
        }

        // 保存并返回
        Role updatedRole = roleRepository.save(role);
        return convertToDTO(updatedRole);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Role role = roleRepository.findById(new RoleId(id))
                .orElseThrow(() -> new RuntimeException("角色不存在"));
        roleRepository.delete(role);
    }

    @Override
    @Transactional
    public void addPermissionToRole(String roleCode, String permissionCode) {
        // 获取角色
        Role role = roleRepository.findByCode(roleCode)
                .orElseThrow(() -> new RuntimeException("角色不存在"));
        
        // 获取权限
        Permission permission = permissionRepository.findByCode(permissionCode)
                .orElseThrow(() -> new RuntimeException("权限不存在"));
        
        // 添加权限
        role.addPermission(permission);
        
        // 保存角色
        roleRepository.save(role);
    }

    @Override
    @Transactional
    public void removePermissionFromRole(String roleCode, String permissionCode) {
        // 获取角色
        Role role = roleRepository.findByCode(roleCode)
                .orElseThrow(() -> new RuntimeException("角色不存在"));
        
        // 获取权限
        Permission permission = permissionRepository.findByCode(permissionCode)
                .orElseThrow(() -> new RuntimeException("权限不存在"));
        
        // 移除权限
        role.removePermission(permission);
        
        // 保存角色
        roleRepository.save(role);
    }

    /**
     * 转换为DTO
     */
    private RoleDTO convertToDTO(Role role) {
        RoleDTO dto = new RoleDTO();
        dto.setId(role.getId().getValue());
        dto.setName(role.getName());
        dto.setCode(role.getCode());
        dto.setDescription(role.getDescription());
        dto.setStatus(role.getStatus());
        dto.setCreateTime(role.getCreateTime());
        dto.setUpdateTime(role.getUpdateTime());
        
        // 转换权限
        if (role.getPermissions() != null && !role.getPermissions().isEmpty()) {
            List<PermissionDTO> permissionDTOs = role.getPermissions().stream()
                    .map(permission -> {
                        PermissionDTO permissionDTO = new PermissionDTO();
                        permissionDTO.setId(permission.getId().getValue());
                        permissionDTO.setName(permission.getName());
                        permissionDTO.setCode(permission.getCode());
                        permissionDTO.setDescription(permission.getDescription());
                        permissionDTO.setStatus(permission.getStatus());
                        permissionDTO.setCreateTime(permission.getCreateTime());
                        permissionDTO.setUpdateTime(permission.getUpdateTime());
                        return permissionDTO;
                    })
                    .collect(Collectors.toList());
            dto.setPermissions(permissionDTOs);
        } else {
            dto.setPermissions(new ArrayList<>());
        }
        
        return dto;
    }
} 