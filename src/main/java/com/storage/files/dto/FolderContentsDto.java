package com.storage.files.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FolderContentsDto {
    private FolderDto folder;
    private List<FileDto> files;
    private List<FolderDto> subfolders;
}