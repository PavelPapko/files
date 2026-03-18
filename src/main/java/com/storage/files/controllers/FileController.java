package com.storage.files.controllers;

import com.storage.files.controllers.FileControllerApi;
import com.storage.files.dto.FileDto;
import com.storage.files.dto.FileUpdateDto;
import com.storage.files.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class FileController implements FileControllerApi {

    private final FileService fileService;

    @Override
    public ResponseEntity<FileDto> uploadFile(MultipartFile file, String description, Long folderId) {
        FileDto uploadedFile = fileService.uploadFile(file, description, folderId);
        return ResponseEntity.status(HttpStatus.CREATED).body(uploadedFile);
    }

    @Override
    public ResponseEntity<Resource> downloadFile(Long id) {
        Resource resource = fileService.downloadFile(id);
        return ResponseEntity.ok(resource);
    }

    @Override
    public ResponseEntity<FileDto> getFileInfo(Long id) {
        FileDto file = fileService.getFileInfo(id);
        return ResponseEntity.ok(file);
    }

    @Override
    public ResponseEntity<FileDto> updateFileInfo(Long id, FileUpdateDto updateDto) {
        FileDto updatedFile = fileService.updateFileInfo(id, updateDto);
        return ResponseEntity.ok(updatedFile);
    }

    @Override
    public ResponseEntity<Void> deleteFile(Long id) {
        fileService.deleteFile(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<List<FileDto>> getFiles(int page, int size, String sortBy, String sortDir) {
        List<FileDto> files = fileService.getFiles(page, size, sortBy, sortDir);
        return ResponseEntity.ok(files);
    }

    @Override
    public ResponseEntity<List<FileDto>> searchFiles(String filename, String uploadedBy, Long folderId) {
        List<FileDto> files = fileService.searchFiles(filename, uploadedBy, folderId);
        return ResponseEntity.ok(files);
    }
}