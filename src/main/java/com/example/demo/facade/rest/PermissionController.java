package com.example.demo.facade.rest;

import com.example.demo.application.service.PermissionService;
import com.example.demo.facade.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 权限控制器
 */
@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    /**
     * 获取权限列表
     */
    @GetMapping
    public ApiResponse<List<com.example.demo.application.dto.PermissionDTO>> getPermissionList(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String code) {
        
        // 调用应用服务获取所有权限
        List<com.example.demo.application.dto.PermissionDTO> permissions = permissionService.findAll();
        
        // 过滤权限
        if (name != null || code != null) {
            permissions = permissions.stream()
                    .filter(p -> (name == null || p.getName().contains(name)) &&
                            (code == null || p.getCode().contains(code)))
                    .toList();
        }
        
        return ApiResponse.success(permissions);
    }

    /**
     * 根据ID获取权限
     */
    @GetMapping("/{id}")
    public ApiResponse<com.example.demo.application.dto.PermissionDTO> getPermissionById(@PathVariable Long id) {
        try {
            com.example.demo.application.dto.PermissionDTO permission = permissionService.findById(id);
            return ApiResponse.success(permission);
        } catch (RuntimeException e) {
            return ApiResponse.error("404", e.getMessage());
        }
    }

    /**
     * 创建权限
     */
    @PostMapping
    public ApiResponse<com.example.demo.application.dto.PermissionDTO> createPermission(
            @RequestBody com.example.demo.application.dto.PermissionDTO permissionDTO) {
        try {
            com.example.demo.application.dto.PermissionDTO createdPermission = permissionService.create(permissionDTO);
            return ApiResponse.success(createdPermission);
        } catch (RuntimeException e) {
            return ApiResponse.error("400", e.getMessage());
        }
    }

    /**
     * 更新权限
     */
    @PutMapping("/{id}")
    public ApiResponse<com.example.demo.application.dto.PermissionDTO> updatePermission(
            @PathVariable Long id,
            @RequestBody com.example.demo.application.dto.PermissionDTO permissionDTO) {
        try {
            com.example.demo.application.dto.PermissionDTO updatedPermission = permissionService.update(id, permissionDTO);
            return ApiResponse.success(updatedPermission);
        } catch (RuntimeException e) {
            return ApiResponse.error("400", e.getMessage());
        }
    }

    /**
     * 删除权限
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deletePermission(@PathVariable Long id) {
        try {
            permissionService.delete(id);
            return ApiResponse.success(null);
        } catch (RuntimeException e) {
            return ApiResponse.error("404", e.getMessage());
        }
    }
} 