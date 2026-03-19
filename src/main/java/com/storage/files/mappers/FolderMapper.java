package com.storage.files.mappers;

import com.storage.files.dto.FolderCreateDto;
import com.storage.files.dto.FolderDto;
import com.storage.files.dto.FolderTreeDto;
import com.storage.files.dto.FolderUpdateDto;
import com.storage.files.models.FolderEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class FolderMapper {

    public FolderDto toDto(FolderEntity entity) {
        if (entity == null) {
            return null;
        }

        return FolderDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .parentId(entity.getParentId())
                .createdBy(entity.getCreatedBy())
                .createdDate(entity.getCreatedDate())
                .build();
    }

    public FolderEntity toEntity(FolderDto dto) {
        if (dto == null) {
            return null;
        }

        FolderEntity entity = new FolderEntity();
        entity.setId(dto.getId());
        entity.setName(dto.getName());
        entity.setParentId(dto.getParentId());
        entity.setCreatedBy(dto.getCreatedBy());
        entity.setCreatedDate(dto.getCreatedDate());

        return entity;
    }

    public FolderEntity toEntity(FolderCreateDto createDto) {
        if (createDto == null) {
            return null;
        }

        FolderEntity entity = new FolderEntity();
        entity.setName(createDto.getName());
        entity.setParentId(createDto.getParentId());
        entity.setCreatedBy(createDto.getCreatedBy());

        return entity;
    }

    public void updateEntity(FolderEntity entity, FolderUpdateDto updateDto) {
        if (entity == null || updateDto == null) {
            return;
        }

        if (updateDto.getName() != null) {
            entity.setName(updateDto.getName());
        }
        if (updateDto.getParentId() != null) {
            entity.setParentId(updateDto.getParentId());
        }
    }

    public FolderTreeDto toTreeDto(FolderEntity entity) {
        if (entity == null) {
            return null;
        }

        return FolderTreeDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .children(new ArrayList<>()) // пустой список, заполняется рекурсивно в сервисе
                .build();
    }

    public List<FolderDto> toDtoList(List<FolderEntity> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<FolderTreeDto> toTreeDtoList(List<FolderEntity> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream()
                .map(this::toTreeDto)
                .collect(Collectors.toList());
    }

    public List<FolderEntity> toEntityList(List<FolderDto> dtos) {
        if (dtos == null) {
            return null;
        }
        return dtos.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }
}