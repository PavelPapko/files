package com.storage.files.mappers;


import com.storage.files.dto.FileDto;
import com.storage.files.dto.FileUpdateDto;
import com.storage.files.models.FileEntity;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface FileMapper {

    FileDto toDto(FileEntity entity);

    FileEntity toEntity(FileDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(@MappingTarget FileEntity entity, FileUpdateDto updateDto);

    @AfterMapping
    default void setDefaultValues(@MappingTarget FileEntity entity) {
        if (entity.getIsDeleted() == null) {
            entity.setIsDeleted(false);
        }
        if (entity.getVersion() == null) {
            entity.setVersion(0);
        }
    }
}