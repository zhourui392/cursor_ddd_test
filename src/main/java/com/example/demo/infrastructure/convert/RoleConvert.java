package com.example.demo.infrastructure.convert;

import com.example.demo.application.dto.RoleDTO;
import com.example.demo.domain.model.entity.Role;
import com.example.demo.domain.model.valueobject.PermissionId;
import com.example.demo.domain.model.valueobject.RoleId;
import com.example.demo.infrastructure.persistence.entity.RoleDO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;
import java.util.Set;

/**
 * 角色对象映射器
 */
@Mapper(
    componentModel = "spring",
    uses = {PermissionConvert.class},
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface RoleConvert {
    
    /**
     * 将领域实体转换为DTO
     */
    @Mapping(source = "id.value", target = "id")
    RoleDTO toDto(Role role);
    
    /**
     * 将DTO列表转换为领域实体列表
     */
    List<RoleDTO> toDtoList(List<Role> roles);
    
    /**
     * 将DTO集合转换为领域实体集合
     */
    Set<RoleDTO> toDtoSet(Set<Role> roles);

    /**
     * 将数据对象转换为领域实体
     */
    @Mapping(target = "id", expression = "java(new RoleId(source.getId()))")
    Role toDomain(RoleDO source);
    
    /**
     * 将领域实体列表转换为数据对象列表
     */
    List<Role> toDomainList(List<RoleDO> source);
    
    /**
     * 将领域实体集合转换为数据对象集合
     */
    Set<Role> toDomainSet(Set<RoleDO> source);
    
    /**
     * 将领域实体转换为数据对象
     */
    @Mapping(source = "id.value", target = "id")
    RoleDO toData(Role source);
    
    /**
     * 更新数据对象
     */
    @Mapping(target = "id", expression = "java(source.getId().getValue())")
    void updateDataFromDomain(Role source, @MappingTarget RoleDO target);
} 