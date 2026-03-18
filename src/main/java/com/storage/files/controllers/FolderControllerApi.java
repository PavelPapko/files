package com.storage.files.controllers;

import com.storage.files.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Папки", description = "API для управления папками/категориями")
@RequestMapping("/api/v1/folders")
public interface FolderControllerApi {

    @Operation(summary = "Создать папку", description = "Создает новую папку")
    @PostMapping
    ResponseEntity<FolderDto> createFolder(@RequestBody FolderCreateDto createDto);

    @Operation(summary = "Получить папку по ID", description = "Возвращает информацию о папке")
    @GetMapping("/{id}")
    ResponseEntity<FolderDto> getFolderById(@PathVariable Long id);

    @Operation(summary = "Обновить папку", description = "Обновляет информацию о папке")
    @PutMapping("/{id}")
    ResponseEntity<FolderDto> updateFolder(
            @PathVariable Long id,
            @RequestBody FolderUpdateDto updateDto);

    @Operation(summary = "Удалить папку", description = "Удаляет папку")
    @DeleteMapping("/{id}")
    ResponseEntity<Void> deleteFolder(@PathVariable Long id);

    @Operation(summary = "Получить структуру папок", description = "Возвращает иерархическую структуру папок")
    @GetMapping("/tree")
    ResponseEntity<List<FolderTreeDto>> getFolderTree();

    @Operation(summary = "Получить содержимое папки", description = "Возвращает список файлов в папке")
    @GetMapping("/{id}/contents")
    ResponseEntity<FolderContentsDto> getFolderContents(@PathVariable Long id);
}