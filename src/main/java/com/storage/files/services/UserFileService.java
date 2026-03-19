package com.storage.files.services;

import com.storage.files.dto.FileDto;
import com.storage.files.models.FileEntity;
import com.storage.files.exceptions.FileNotFoundException;
import com.storage.files.mappers.FileMapper;
import com.storage.files.repository.FileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserFileService {

    private final FileRepository fileRepository;
    private final FileMapper fileMapper;

    /**
     * Получить все файлы пользователя
     */
    public List<FileDto> getUserFiles(Long userId) {
        log.info("Getting files for user id: {}", userId);

        List<FileEntity> files = fileRepository.findByUserId(userId);

        return fileMapper.toDtoList(files.stream()
                .filter(file -> !file.getIsDeleted())
                .toList());
    }

    /**
     * Получить количество файлов пользователя
     */
    public long getUserFilesCount(Long userId) {
        log.info("Getting files count for user id: {}", userId);

        return fileRepository.findByUserId(userId).stream()
                .filter(file -> !file.getIsDeleted())
                .count();
    }

    /**
     * Получить общий размер файлов пользователя
     */
    public long getUserTotalFileSize(Long userId) {
        log.info("Getting total file size for user id: {}", userId);

        return fileRepository.findByUserId(userId).stream()
                .filter(file -> !file.getIsDeleted())
                .mapToLong(FileEntity::getFileSize)
                .sum();
    }

    /**
     * Проверить, есть ли у пользователя файлы
     */
    public boolean userHasFiles(Long userId) {
        return getUserFilesCount(userId) > 0;
    }

    /**
     * Получить файлы пользователя по типу контента
     */
    public List<FileDto> getUserFilesByContentType(Long userId, String contentType) {
        log.info("Getting files for user id: {} with content type: {}", userId, contentType);

        return fileMapper.toDtoList(fileRepository.findByUserId(userId).stream()
                .filter(file -> !file.getIsDeleted())
                .filter(file -> contentType.equals(file.getContentType()))
                .toList());
    }

    /**
     * Переместить все файлы пользователя в другую папку
     */
    @Transactional
    public void moveUserFilesToFolder(Long userId, Long targetFolderId) {
        log.info("Moving all files for user id: {} to folder: {}", userId, targetFolderId);

        List<FileEntity> files = fileRepository.findByUserId(userId);
        files.stream()
                .filter(file -> !file.getIsDeleted())
                .forEach(file -> {
                    file.setFolderId(targetFolderId);
                    fileRepository.save(file);
                });
    }

    /**
     * Удалить все файлы пользователя (soft delete)
     */
    @Transactional
    public void deleteAllUserFiles(Long userId) {
        log.info("Soft deleting all files for user id: {}", userId);

        List<FileEntity> files = fileRepository.findByUserId(userId);
        files.stream()
                .filter(file -> !file.getIsDeleted())
                .forEach(file -> {
                    file.setIsDeleted(true);
                    fileRepository.save(file);
                });
    }
}