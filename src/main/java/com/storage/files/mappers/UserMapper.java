package com.storage.files.mappers;

import com.storage.files.dto.UserCreateDto;
import com.storage.files.dto.UserDto;
import com.storage.files.UserUpdateDto;
import com.storage.files.mappers.UserEntity;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    UserDto toDto(UserEntity entity);

    UserEntity toEntity(UserDto dto);

    @Mapping(target = "passwordHash", source = "password")
    UserEntity toEntity(UserCreateDto createDto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(@MappingTarget UserEntity entity, UserUpdateDto updateDto);

    @AfterMapping
    default void setDefaultRole(@MappingTarget UserEntity entity) {
        if (entity.getRole() == null) {
            entity.setRole("USER");
        }
    }
}