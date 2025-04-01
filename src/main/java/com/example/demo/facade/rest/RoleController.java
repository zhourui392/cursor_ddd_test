package com.example.demo.facade.rest;

import com.example.demo.application.service.RoleService;
import com.example.demo.facade.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色控制器
 */
@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    /**
     * 获取角色列表
     */
    @GetMapping
    public ApiResponse<List<com.example.demo.application.dto.RoleDTO>> getRoleList(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String code) {

        // 调用应用服务获取所有角色
        List<com.example.demo.application.dto.RoleDTO> roles = roleService.findAll();
        
        // 过滤角色
        if (name != null || code != null) {
            roles = roles.stream()
                    .filter(r -> (name == null || r.getName().contains(name)) &&
                            (code == null || r.getCode().contains(code)))
                    .toList();
        }

        return ApiResponse.success(roles);
    }

    /**
     * 根据ID获取角色
     */
    @GetMapping("/{id}")
    public ApiResponse<com.example.demo.application.dto.RoleDTO> getRoleById(@PathVariable Long id) {
        try {
            com.example.demo.application.dto.RoleDTO role = roleService.findById(id);
            return ApiResponse.success(role);
        } catch (RuntimeException e) {
            return ApiResponse.error("404", e.getMessage());
        }
    }

    /**
     * 创建角色
     */
    @PostMapping
    public ApiResponse<com.example.demo.application.dto.RoleDTO> createRole(
            @RequestBody com.example.demo.application.dto.RoleDTO roleDTO) {
        try {
            com.example.demo.application.dto.RoleDTO createdRole = roleService.create(roleDTO);
            return ApiResponse.success(createdRole);
        } catch (RuntimeException e) {
            return ApiResponse.error("400", e.getMessage());
        }
    }

    /**
     * 更新角色
     */
    @PutMapping("/{id}")
    public ApiResponse<com.example.demo.application.dto.RoleDTO> updateRole(
            @PathVariable Long id,
            @RequestBody com.example.demo.application.dto.RoleDTO roleDTO) {
        try {
            com.example.demo.application.dto.RoleDTO updatedRole = roleService.update(id, roleDTO);
            return ApiResponse.success(updatedRole);
        } catch (RuntimeException e) {
            return ApiResponse.error("400", e.getMessage());
        }
    }

    /**
     * 删除角色
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteRole(@PathVariable Long id) {
        try {
            roleService.delete(id);
            return ApiResponse.success(null);
        } catch (RuntimeException e) {
            return ApiResponse.error("404", e.getMessage());
        }
    }

    /**
     * 为角色添加权限
     */
    @PostMapping("/{roleCode}/permissions/{permissionCode}")
    public ApiResponse<Void> addPermissionToRole(
            @PathVariable String roleCode,
            @PathVariable String permissionCode) {
        try {
            roleService.addPermissionToRole(roleCode, permissionCode);
            return ApiResponse.success(null);
        } catch (RuntimeException e) {
            return ApiResponse.error("400", e.getMessage());
        }
    }

    /**
     * 从角色中移除权限
     */
    @DeleteMapping("/{roleCode}/permissions/{permissionCode}")
    public ApiResponse<Void> removePermissionFromRole(
            @PathVariable String roleCode,
            @PathVariable String permissionCode) {
        try {
            roleService.removePermissionFromRole(roleCode, permissionCode);
            return ApiResponse.success(null);
        } catch (RuntimeException e) {
            return ApiResponse.error("404", e.getMessage());
        }
    }
} 