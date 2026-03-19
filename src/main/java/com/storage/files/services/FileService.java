package com.storage.files.services;

import com.storage.files.dto.FileDto;
import com.storage.files.dto.FileUpdateDto;
import com.storage.files.models.FileEntity;
import com.storage.files.exceptions.FileNotFoundException;
import com.storage.files.exceptions.FileStorageException;
import com.storage.files.exceptions.UnauthorizedException;
import com.storage.files.exceptions.BadRequestException;
import com.storage.files.mappers.FileMapper;
import com.storage.files.repository.FileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FileService {

    private final FileRepository fileRepository;
    private final FileMapper fileMapper;
    private final FileStorageService fileStorageService;
    private final CommonFileFolderService commonService; // вместо FolderService
    // FolderService удален!

    /**
     * Загрузить новый файл
     */
    @Transactional
    public FileDto uploadFile(MultipartFile file, String description, Long folderId, Long userId) {
        log.info("Uploading file: {}, size: {}, folderId: {}, userId: {}",
                file.getOriginalFilename(), file.getSize(), folderId, userId);

        validateFile(file);

        try {
            // Проверяем существование папки, если указана (через общий сервис)
            if (folderId != null) {
                commonService.getFolderDtoById(folderId);
            }

            // Генерируем уникальное имя файла
            String uniqueFilename = generateUniqueFilename(file.getOriginalFilename());

            // Сохраняем файл в хранилище
            String filePath = fileStorageService.storeFile(file, uniqueFilename);

            // Создаем сущность для метаданных
            FileEntity fileEntity = FileEntity.builder()
                    .filename(uniqueFilename)
                    .originalFilename(file.getOriginalFilename())
                    .filePath(filePath)
                    .fileSize(file.getSize())
                    .contentType(file.getContentType())
                    .description(description)
                    .folderId(folderId)
                    .userId(userId)
                    .uploadedBy(userId != null ? "user-" + userId : "anonymous")
                    .uploadedDate(LocalDateTime.now())
                    .lastModifiedDate(LocalDateTime.now())
                    .isDeleted(false)
                    .version(0)
                    .build();

            FileEntity savedEntity = fileRepository.save(fileEntity);
            log.info("File uploaded successfully with id: {}, filename: {}", savedEntity.getId(), savedEntity.getFilename());

            return fileMapper.toDto(savedEntity);

        } catch (Exception e) {
            log.error("Failed to upload file: {}", file.getOriginalFilename(), e);
            throw new FileStorageException("Could not upload file: " + file.getOriginalFilename(), e);
        }
    }

    /**
     * Скачать файл по ID
     */
    public Resource downloadFile(Long id) {
        log.info("Downloading file with id: {}", id);

        FileEntity fileEntity = commonService.getFileEntityById(id); // через общий сервис

        if (fileEntity.getIsDeleted()) {
            throw new FileNotFoundException("File not found with id: " + id);
        }

        return fileStorageService.loadFileAsResource(fileEntity.getFilename());
    }

    /**
     * Получить информацию о файле
     */
    public FileDto getFileInfo(Long id) {
        log.info("Getting file info for id: {}", id);

        FileEntity fileEntity = commonService.getFileEntityById(id); // через общий сервис

        if (fileEntity.getIsDeleted()) {
            throw new FileNotFoundException("File not found with id: " + id);
        }

        return fileMapper.toDto(fileEntity);
    }

    /**
     * Обновить информацию о файле
     */
    @Transactional
    public FileDto updateFileInfo(Long id, FileUpdateDto updateDto) {
        log.info("Updating file info for id: {} with data: {}", id, updateDto);

        FileEntity fileEntity = commonService.getFileEntityById(id); // через общий сервис

        if (fileEntity.getIsDeleted()) {
            throw new FileNotFoundException("Cannot update deleted file with id: " + id);
        }

        fileMapper.updateEntity(fileEntity, updateDto);

        // Проверяем существование папки, если обновляется (через общий сервис)
        if (updateDto.getFolderId() != null) {
            commonService.getFolderDtoById(updateDto.getFolderId());
        }

        fileEntity.setLastModifiedDate(LocalDateTime.now());
        fileEntity.setVersion(fileEntity.getVersion() + 1);

        FileEntity updatedEntity = fileRepository.save(fileEntity);
        log.info("File info updated successfully for id: {}", id);

        return fileMapper.toDto(updatedEntity);
    }

    /**
     * Пометить файл как удаленный (soft delete)
     */
    @Transactional
    public void deleteFile(Long id) {
        log.info("Soft deleting file with id: {}", id);

        FileEntity fileEntity = commonService.getFileEntityById(id); // через общий сервис

        if (fileEntity.getIsDeleted()) {
            log.warn("File with id: {} is already deleted", id);
            return;
        }

        fileRepository.softDelete(id);
        log.info("File with id: {} soft deleted successfully", id);
    }

    /**
     * Полностью удалить файл (hard delete)
     */
    @Transactional
    public void permanentlyDeleteFile(Long id) {
        log.info("Permanently deleting file with id: {}", id);

        FileEntity fileEntity = commonService.getFileEntityById(id); // через общий сервис

        // Удаляем физический файл
        fileStorageService.deleteFile(fileEntity.getFilename());

        // Удаляем запись из БД
        fileRepository.delete(fileEntity);

        log.info("File with id: {} permanently deleted", id);
    }

    /**
     * Получить список файлов с пагинацией
     */
    public List<FileDto> getFiles(int page, int size, String sortBy, String sortDir) {
        log.info("Getting files page: {}, size: {}, sortBy: {}, sortDir: {}", page, size, sortBy, sortDir);

        Sort sort = Sort.by(Sort.Direction.fromString(sortDir != null ? sortDir : "desc"),
                sortBy != null ? sortBy : "uploadedDate");
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<FileEntity> filePage = fileRepository.findByIsDeletedFalse(pageable);

        return fileMapper.toDtoList(filePage.getContent());
    }

    /**
     * Поиск файлов по параметрам
     */
    public List<FileDto> searchFiles(String filename, String uploadedBy, Long folderId) {
        log.info("Searching files with filename: {}, uploadedBy: {}, folderId: {}", filename, uploadedBy, folderId);

        List<FileEntity> files = fileRepository.searchFiles(filename, uploadedBy, folderId);

        return fileMapper.toDtoList(files);
    }

    /**
     * Получить файлы пользователя
     */
    public List<FileDto> getUserFiles(Long userId) {
        log.info("Getting files for user id: {}", userId);

        List<FileEntity> files = fileRepository.findByUserId(userId);

        return fileMapper.toDtoList(files.stream()
                .filter(file -> !file.getIsDeleted())
                .toList());
    }

    /**
     * Получить файлы по папке (через общий сервис)
     */
    public List<FileDto> getFilesByFolder(Long folderId) {
        log.info("Getting files for folder id: {}", folderId);
        return commonService.getFilesInFolder(folderId);
    }

    /**
     * Проверить существование файла по имени
     */
    public boolean existsByFilename(String filename) {
        return fileRepository.existsByFilename(filename);
    }

    /**
     * Проверить владельца файла
     */
    public void validateFileOwnership(Long fileId, Long userId) {
        FileEntity file = commonService.getFileEntityById(fileId);
        if (!file.getUserId().equals(userId)) {
            throw new UnauthorizedException("User does not have permission to access this file");
        }
    }

    /**
     * Переместить файл в другую папку
     */
    @Transactional
    public FileDto moveFileToFolder(Long fileId, Long targetFolderId) {
        log.info("Moving file id: {} to folder: {}", fileId, targetFolderId);

        FileUpdateDto updateDto = new FileUpdateDto();
        updateDto.setFolderId(targetFolderId);

        return updateFileInfo(fileId, updateDto);
    }

    /**
     * Получить статистику по файлам
     */
    public FileStatistics getFileStatistics() {
        log.info("Getting file statistics");

        List<FileEntity> allFiles = fileRepository.findAll();
        List<FileEntity> activeFiles = allFiles.stream()
                .filter(f -> !f.getIsDeleted())
                .toList();

        long totalCount = activeFiles.size();
        long totalSize = activeFiles.stream().mapToLong(FileEntity::getFileSize).sum();
        long averageSize = totalCount > 0 ? totalSize / totalCount : 0;

        return new FileStatistics(totalCount, totalSize, averageSize);
    }

    /**
     * Внутренний класс для статистики
     */
    public record FileStatistics(long totalCount, long totalSize, long averageSize) {}

    private String generateUniqueFilename(String originalFilename) {
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        return UUID.randomUUID() + extension;
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new FileStorageException("File is empty");
        }

        long maxSize = 100 * 1024 * 1024; // 100MB
        if (file.getSize() > maxSize) {
            throw new FileStorageException("File size exceeds maximum limit of 100MB");
        }

        String contentType = file.getContentType();
        if (contentType != null && !isAllowedContentType(contentType)) {
            throw new FileStorageException("File type not allowed: " + contentType);
        }
    }

    private boolean isAllowedContentType(String contentType) {
        return contentType.startsWith("image/") ||
                contentType.startsWith("text/") ||
                contentType.equals("application/pdf") ||
                contentType.equals("application/msword") ||
                contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document") ||
                contentType.equals("application/vnd.ms-excel") ||
                contentType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet") ||
                contentType.equals("application/json") ||
                contentType.equals("application/xml");
    }
}