package com.storage.files.services;

import com.storage.files.dto.FileDto;
import com.storage.files.dto.FolderDto;
import com.storage.files.models.FileEntity;
import com.storage.files.models.FolderEntity;
import com.storage.files.exceptions.FileNotFoundException;
import com.storage.files.exceptions.FolderNotFoundException;
import com.storage.files.mappers.FileMapper;
import com.storage.files.mappers.FolderMapper;
import com.storage.files.repository.FileRepository;
import com.storage.files.repository.FolderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommonFileFolderService {

    private final FileRepository fileRepository;
    private final FolderRepository folderRepository;
    private final FileMapper fileMapper;
    private final FolderMapper folderMapper;

    /**
     * Получить файл по ID
     */
    public FileEntity getFileEntityById(Long id) {
        return fileRepository.findById(id)
                .orElseThrow(() -> new FileNotFoundException("File not found with id: " + id));
    }

    /**
     * Получить папку по ID
     */
    public FolderEntity getFolderEntityById(Long id) {
        return folderRepository.findById(id)
                .orElseThrow(() -> new FolderNotFoundException("Folder not found with id: " + id));
    }

    /**
     * Получить DTO файла по ID
     */
    public FileDto getFileDtoById(Long id) {
        return fileMapper.toDto(getFileEntityById(id));
    }

    /**
     * Получить DTO папки по ID
     */
    public FolderDto getFolderDtoById(Long id) {
        return folderMapper.toDto(getFolderEntityById(id));
    }

    /**
     * Проверить существование файла
     */
    public boolean fileExists(Long id) {
        return fileRepository.existsById(id);
    }

    /**
     * Проверить существование папки
     */
    public boolean folderExists(Long id) {
        return folderRepository.existsById(id);
    }

    /**
     * Получить все файлы в папке
     */
    public List<FileDto> getFilesInFolder(Long folderId) {
        List<FileEntity> files = fileRepository.findByFolderId(folderId);
        return fileMapper.toDtoList(files.stream()
                .filter(f -> !f.getIsDeleted())
                .toList());
    }

    /**
     * Получить все подпапки
     */
    public List<FolderDto> getSubfolders(Long parentId) {
        List<FolderEntity> folders = folderRepository.findByParentId(parentId);
        return folderMapper.toDtoList(folders);
    }

    /**
     * Проверить, есть ли файлы в папке
     */
    public boolean hasFilesInFolder(Long folderId) {
        return !getFilesInFolder(folderId).isEmpty();
    }

    /**
     * Получить количество файлов в папке
     */
    public long getFilesCountInFolder(Long folderId) {
        return fileRepository.findByFolderId(folderId).stream()
                .filter(f -> !f.getIsDeleted())
                .count();
    }

    /**
     * Переместить все файлы из одной папки в другую
     */
    @Transactional
    public void moveAllFilesFromFolder(Long sourceFolderId, Long targetFolderId) {
        log.info("Moving all files from folder {} to folder {}", sourceFolderId, targetFolderId);

        List<FileEntity> files = fileRepository.findByFolderId(sourceFolderId);
        files.stream()
                .filter(f -> !f.getIsDeleted())
                .forEach(file -> {
                    file.setFolderId(targetFolderId);
                    fileRepository.save(file);
                });
    }

    /**
     * Получить полный путь к папке (иерархия)
     */
    public List<FolderDto> getFolderPath(Long folderId) {
        List<FolderDto> path = new java.util.ArrayList<>();
        FolderDto current = getFolderDtoById(folderId);

        while (current != null) {
            path.add(0, current);
            if (current.getParentId() != null) {
                current = getFolderDtoById(current.getParentId());
            } else {
                current = null;
            }
        }

        return path;
    }

    /**
     * Получить все файлы пользователя
     */
    public List<FileDto> getFilesByUser(Long userId) {
        List<FileEntity> files = fileRepository.findByUserId(userId);
        return fileMapper.toDtoList(files.stream()
                .filter(f -> !f.getIsDeleted())
                .toList());
    }

    /**
     * Получить количество файлов пользователя
     */
    public long getFilesCountByUser(Long userId) {
        return fileRepository.findByUserId(userId).stream()
                .filter(f -> !f.getIsDeleted())
                .count();
    }

    /**
     * Получить общий размер файлов пользователя
     */
    public long getTotalFileSizeByUser(Long userId) {
        return fileRepository.findByUserId(userId).stream()
                .filter(f -> !f.getIsDeleted())
                .mapToLong(FileEntity::getFileSize)
                .sum();
    }

    /**
     * Проверить, есть ли у пользователя файлы
     */
    public boolean userHasFiles(Long userId) {
        return getFilesCountByUser(userId) > 0;
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