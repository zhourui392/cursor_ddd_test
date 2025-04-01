package com.example.demo.application.service.impl;

import com.example.demo.application.dto.PermissionDTO;
import com.example.demo.application.service.PermissionService;
import com.example.demo.domain.model.entity.Permission;
import com.example.demo.domain.model.valueobject.PermissionId;
import com.example.demo.domain.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 权限应用服务实现
 */
@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final PermissionRepository permissionRepository;

    @Override
    public List<PermissionDTO> findAll() {
        List<Permission> permissions = permissionRepository.findAll();
        return permissions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public PermissionDTO findById(Long id) {
        Permission permission = permissionRepository.findById(new PermissionId(id))
                .orElseThrow(() -> new RuntimeException("权限不存在"));
        return convertToDTO(permission);
    }

    @Override
    @Transactional
    public PermissionDTO create(PermissionDTO permissionDTO) {
        // 检查编码是否已存在
        if (permissionRepository.existsByCode(permissionDTO.getCode())) {
            throw new RuntimeException("权限编码已存在");
        }

        // 创建权限
        Permission permission = Permission.create(
                permissionDTO.getName(),
                permissionDTO.getCode(),
                permissionDTO.getDescription()
        );

        // 保存并返回
        Permission savedPermission = permissionRepository.save(permission);
        return convertToDTO(savedPermission);
    }

    @Override
    @Transactional
    public PermissionDTO update(Long id, PermissionDTO permissionDTO) {
        // 获取权限
        Permission permission = permissionRepository.findById(new PermissionId(id))
                .orElseThrow(() -> new RuntimeException("权限不存在"));

        // 如果修改了编码，检查新编码是否已存在
        if (!permission.getCode().equals(permissionDTO.getCode()) &&
                permissionRepository.existsByCode(permissionDTO.getCode())) {
            throw new RuntimeException("权限编码已存在");
        }

        // 更新权限信息
        permission.update(permissionDTO.getName(), permissionDTO.getDescription());
        
        // 状态更新
        if (permissionDTO.getStatus() != null) {
            if (permissionDTO.getStatus()) {
                permission.enable();
            } else {
                permission.disable();
            }
        }

        // 保存并返回
        Permission updatedPermission = permissionRepository.save(permission);
        return convertToDTO(updatedPermission);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Permission permission = permissionRepository.findById(new PermissionId(id))
                .orElseThrow(() -> new RuntimeException("权限不存在"));
        permissionRepository.delete(permission);
    }

    /**
     * 转换为DTO
     */
    private PermissionDTO convertToDTO(Permission permission) {
        PermissionDTO dto = new PermissionDTO();
        dto.setId(permission.getId().getValue());
        dto.setName(permission.getName());
        dto.setCode(permission.getCode());
        dto.setDescription(permission.getDescription());
        dto.setStatus(permission.getStatus());
        dto.setCreateTime(permission.getCreateTime());
        dto.setUpdateTime(permission.getUpdateTime());
        return dto;
    }
} 