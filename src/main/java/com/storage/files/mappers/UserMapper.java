package com.storage.files.mappers;

import com.storage.files.dto.UserCreateDto;
import com.storage.files.dto.UserDto;
import com.storage.files.dto.UserUpdateDto;
import com.storage.files.models.UserEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserMapper {

    public UserDto toDto(UserEntity entity) {
        if (entity == null) {
            return null;
        }

        return UserDto.builder()
                .id(entity.getId())
                .username(entity.getUsername())
                .email(entity.getEmail())
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .role(entity.getRole())
                .isActive(entity.getIsActive())
                .createdDate(entity.getCreatedDate())
                .lastLoginDate(entity.getLastLoginDate())
                .build();
    }

    public UserEntity toEntity(UserDto dto) {
        if (dto == null) {
            return null;
        }

        UserEntity entity = new UserEntity();
        entity.setId(dto.getId());
        entity.setUsername(dto.getUsername());
        entity.setEmail(dto.getEmail());
        entity.setFirstName(dto.getFirstName());
        entity.setLastName(dto.getLastName());
        entity.setRole(dto.getRole());
        entity.setIsActive(dto.getIsActive());
        entity.setCreatedDate(dto.getCreatedDate());
        entity.setLastLoginDate(dto.getLastLoginDate());

        return entity;
    }

    public UserEntity toEntity(UserCreateDto createDto) {
        if (createDto == null) {
            return null;
        }

        UserEntity entity = new UserEntity();
        entity.setUsername(createDto.getUsername());
        entity.setEmail(createDto.getEmail());
        entity.setFirstName(createDto.getFirstName());
        entity.setLastName(createDto.getLastName());
        entity.setRole(createDto.getRole() != null ? createDto.getRole() : "USER");

        // password_hash устанавливается отдельно в сервисе после хеширования
        // поэтому здесь его не устанавливаем

        return entity;
    }

    public void updateEntity(UserEntity entity, UserUpdateDto updateDto) {
        if (entity == null || updateDto == null) {
            return;
        }

        if (updateDto.getEmail() != null) {
            entity.setEmail(updateDto.getEmail());
        }
        if (updateDto.getFirstName() != null) {
            entity.setFirstName(updateDto.getFirstName());
        }
        if (updateDto.getLastName() != null) {
            entity.setLastName(updateDto.getLastName());
        }
        if (updateDto.getIsActive() != null) {
            entity.setIsActive(updateDto.getIsActive());
        }
        if (updateDto.getRole() != null) {
            entity.setRole(updateDto.getRole());
        }
    }

    public List<UserDto> toDtoList(List<UserEntity> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<UserEntity> toEntityList(List<UserDto> dtos) {
        if (dtos == null) {
            return null;
        }
        return dtos.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }
}