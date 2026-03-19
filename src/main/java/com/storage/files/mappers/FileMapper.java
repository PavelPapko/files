package com.storage.files.mappers;

import com.storage.files.dto.FileDto;
import com.storage.files.dto.FileUpdateDto;
import com.storage.files.models.FileEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class FileMapper {

    public FileDto toDto(FileEntity entity) {
        if (entity == null) {
            return null;
        }

        return FileDto.builder()
                .id(entity.getId())
                .filename(entity.getFilename())
                .originalFilename(entity.getOriginalFilename())
                .filePath(entity.getFilePath())
                .fileSize(entity.getFileSize())
                .contentType(entity.getContentType())
                .uploadedBy(entity.getUploadedBy())
                .uploadedDate(entity.getUploadedDate())
                .lastModifiedDate(entity.getLastModifiedDate())
                .description(entity.getDescription())
                .isDeleted(entity.getIsDeleted())
                .version(entity.getVersion())
                .userId(entity.getUserId())
                .folderId(entity.getFolderId())
                .build();
    }

    public FileEntity toEntity(FileDto dto) {
        if (dto == null) {
            return null;
        }

        FileEntity entity = new FileEntity();
        entity.setId(dto.getId());
        entity.setFilename(dto.getFilename());
        entity.setOriginalFilename(dto.getOriginalFilename());
        entity.setFilePath(dto.getFilePath());
        entity.setFileSize(dto.getFileSize());
        entity.setContentType(dto.getContentType());
        entity.setUploadedBy(dto.getUploadedBy());
        entity.setUploadedDate(dto.getUploadedDate());
        entity.setLastModifiedDate(dto.getLastModifiedDate());
        entity.setDescription(dto.getDescription());
        entity.setIsDeleted(dto.getIsDeleted());
        entity.setVersion(dto.getVersion());
        entity.setUserId(dto.getUserId());
        entity.setFolderId(dto.getFolderId());

        setDefaultValues(entity);
        return entity;
    }

    public void updateEntity(FileEntity entity, FileUpdateDto updateDto) {
        if (entity == null || updateDto == null) {
            return;
        }

        if (updateDto.getDescription() != null) {
            entity.setDescription(updateDto.getDescription());
        }
        if (updateDto.getFolderId() != null) {
            entity.setFolderId(updateDto.getFolderId());
        }
        if (updateDto.getOriginalFilename() != null) {
            entity.setOriginalFilename(updateDto.getOriginalFilename());
        }
    }

    public List<FileDto> toDtoList(List<FileEntity> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<FileEntity> toEntityList(List<FileDto> dtos) {
        if (dtos == null) {
            return null;
        }
        return dtos.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    private void setDefaultValues(FileEntity entity) {
        if (entity.getIsDeleted() == null) {
            entity.setIsDeleted(false);
        }
        if (entity.getVersion() == null) {
            entity.setVersion(0);
        }
    }
}