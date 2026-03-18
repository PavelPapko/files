package com.storage.files.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FolderCreateDto {
    @NotBlank(message = "Folder name is required")
    private String name;

    private Long parentId;
    private Long createdBy;
}