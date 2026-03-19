package com.storage.files.controllers;

import com.storage.files.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "Файлы", description = "API для управления файлами")
@RequestMapping("/api/v1/files")
public interface FileControllerApi {

    @Operation(summary = "Загрузить файл", description = "Загружает новый файл в систему")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Файл успешно загружен",
                    content = @Content(schema = @Schema(implementation = FileDto.class))),
            @ApiResponse(responseCode = "400", description = "Некорректные данные"),
            @ApiResponse(responseCode = "401", description = "Не авторизован")
    })
    @PostMapping(consumes = "multipart/form-data")
    ResponseEntity<FileDto> uploadFile(
            @Parameter(description = "Файл для загрузки") @RequestPart("file") MultipartFile file,
            @Parameter(description = "Описание файла") @RequestParam(required = false) String description,
            @Parameter(description = "ID папки") @RequestParam(required = false) Long folderId,
            @Parameter(description = "ID пользователя") @RequestParam(required = false) Long userId);


    @Operation(summary = "Скачать файл", description = "Скачивает файл по ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Файл найден"),
            @ApiResponse(responseCode = "404", description = "Файл не найден")
    })
    @GetMapping("/{id}/download")
    ResponseEntity<Resource> downloadFile(@PathVariable Long id);

    @Operation(summary = "Получить информацию о файле", description = "Возвращает метаданные файла")
    @GetMapping("/{id}")
    ResponseEntity<FileDto> getFileInfo(@PathVariable Long id);

    @Operation(summary = "Обновить информацию о файле", description = "Обновляет метаданные файла")
    @PutMapping("/{id}")
    ResponseEntity<FileDto> updateFileInfo(
            @PathVariable Long id,
            @RequestBody FileUpdateDto updateDto);

    @Operation(summary = "Удалить файл", description = "Помечает файл как удаленный (soft delete)")
    @DeleteMapping("/{id}")
    ResponseEntity<Void> deleteFile(@PathVariable Long id);

    @Operation(summary = "Получить список файлов", description = "Возвращает список файлов с пагинацией")
    @GetMapping
    ResponseEntity<List<FileDto>> getFiles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDir);

    @Operation(summary = "Поиск файлов", description = "Поиск файлов по различным параметрам")
    @GetMapping("/search")
    ResponseEntity<List<FileDto>> searchFiles(
            @RequestParam(required = false) String filename,
            @RequestParam(required = false) String uploadedBy,
            @RequestParam(required = false) Long folderId);
}