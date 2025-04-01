package com.example.demo.infrastructure.convert;

import com.example.demo.application.dto.PermissionDTO;
import com.example.demo.domain.model.entity.Permission;
import com.example.demo.domain.model.valueobject.PermissionId;
import com.example.demo.infrastructure.persistence.entity.PermissionDO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;
import java.util.Set;

/**
 * 权限对象映射器
 */
@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface PermissionConvert {
    
    /**
     * 将领域实体转换为DTO
     */
    @Mapping(source = "id.value", target = "id")
    PermissionDTO toDto(Permission permission);
    
    /**
     * 将DTO列表转换为领域实体列表
     */
    List<PermissionDTO> toDtoList(List<Permission> permissions);
    
    /**
     * 将DTO集合转换为领域实体集合
     */
    Set<PermissionDTO> toDtoSet(Set<Permission> permissions);

    /**
     * 将数据对象转换为领域实体
     */
    @Mapping(target = "id", expression = "java(new PermissionId(source.getId()))")
    Permission toDomain(PermissionDO source);

    /**
     * 将领域实体列表转换为数据对象列表
     */
    List<Permission> toDomainList(List<PermissionDO> source);
    
    /**
     * 将领域实体集合转换为数据对象集合
     */
    Set<Permission> toDomainSet(Set<PermissionDO> source);
    
    /**
     * 将领域实体转换为数据对象
     */
    @Mapping(source = "id.value", target = "id")
    PermissionDO toData(Permission source);
    
    /**
     * 更新数据对象
     */
    @Mapping(target = "id", expression = "java(source.getId().getValue())")
    void updateDataFromDomain(Permission source, @MappingTarget PermissionDO target);
} 