package com.storage.files.mappers;

import com.storage.files.dto.FolderCreateDto;
import com.storage.files.dto.FolderDto;
import com.storage.files.dto.FolderTreeDto;
import com.storage.files.dto.FolderUpdateDto;
import com.storage.files.models.FolderEntity;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface FolderMapper {

    FolderDto toDto(FolderEntity entity);

    FolderEntity toEntity(FolderDto dto);

    FolderEntity toEntity(FolderCreateDto createDto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(@MappingTarget FolderEntity entity, FolderUpdateDto updateDto);

    FolderTreeDto toTreeDto(FolderEntity entity);

    List<FolderTreeDto> toTreeDtoList(List<FolderEntity> entities);
}