package com.storage.files.controllers;

import com.storage.files.controllers.FolderControllerApi;
import com.storage.files.dto.*;
import com.storage.files.services.FolderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class FolderController implements FolderControllerApi {

    private final FolderService folderService;

    @Override
    public ResponseEntity<FolderDto> createFolder(FolderCreateDto createDto) {
        FolderDto folder = folderService.createFolder(createDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(folder);
    }

    @Override
    public ResponseEntity<FolderDto> getFolderById(Long id) {
        FolderDto folder = folderService.getFolderById(id);
        return ResponseEntity.ok(folder);
    }

    @Override
    public ResponseEntity<FolderDto> updateFolder(Long id, FolderUpdateDto updateDto) {
        FolderDto folder = folderService.updateFolder(id, updateDto);
        return ResponseEntity.ok(folder);
    }

    @Override
    public ResponseEntity<Void> deleteFolder(Long id) {
        folderService.deleteFolder(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<List<FolderTreeDto>> getFolderTree() {
        List<FolderTreeDto> tree = folderService.getFolderTree();
        return ResponseEntity.ok(tree);
    }

    @Override
    public ResponseEntity<FolderContentsDto> getFolderContents(Long id) {
        FolderContentsDto contents = folderService.getFolderContents(id);
        return ResponseEntity.ok(contents);
    }
}