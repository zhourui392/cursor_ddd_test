package com.example.demo.infrastructure.convert;

import com.example.demo.application.dto.UserDTO;
import com.example.demo.domain.model.entity.User;
import com.example.demo.domain.model.valueobject.RoleId;
import com.example.demo.domain.model.valueobject.UserId;
import com.example.demo.infrastructure.persistence.entity.UserDO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

/**
 * 用户对象映射器
 */
@Mapper(
    componentModel = "spring",
    uses = {RoleConvert.class},
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface UserConvert {
    
    /**
     * 将领域实体转换为DTO
     */
    @Mapping(source = "id.value", target = "id")
    @Mapping(source = "email.value", target = "email")
    @Mapping(source = "phone.value", target = "phone")
    UserDTO toDto(User user);
    
    /**
     * 将DTO列表转换为领域实体列表
     */
    List<UserDTO> toDtoList(List<User> users);

    /**
     * 将数据对象转换为领域实体
     */
    @Mapping(target = "id", expression = "java(new com.example.demo.domain.model.valueobject.UserId(source.getId()))")
    @Mapping(target = "email", expression = "java(new com.example.demo.domain.model.valueobject.Email(source.getEmail()))")
    @Mapping(target = "phone", expression = "java(new com.example.demo.domain.model.valueobject.Phone(source.getPhone()))")
    User toDomain(UserDO source);
    
    /**
     * 将领域实体列表转换为数据对象列表
     */
    List<User> toDomainList(List<UserDO> source);
    
    /**
     * 将领域实体转换为数据对象
     */
    @Mapping(source = "id.value", target = "id")
    @Mapping(source = "email.value", target = "email")
    @Mapping(source = "phone.value", target = "phone")
    UserDO toData(User source);
    
    /**
     * 更新数据对象
     */
    @Mapping(target = "id", expression = "java(source.getId().getValue())")
    @Mapping(target = "email", expression = "java(source.getEmail().getValue())")
    @Mapping(target = "phone", expression = "java(source.getPhone().getValue())")
    void updateDataFromDomain(User source, @MappingTarget UserDO target);
} 