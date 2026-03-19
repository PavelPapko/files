package com.storage.files.services;

import com.storage.files.dto.*;
import com.storage.files.models.FolderEntity;
import com.storage.files.exceptions.FolderAlreadyExistsException;
import com.storage.files.exceptions.FolderNotFoundException;
import com.storage.files.exceptions.BadRequestException;
import com.storage.files.exceptions.UnauthorizedException;
import com.storage.files.mappers.FolderMapper;
import com.storage.files.repository.FolderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FolderService {

    private final FolderRepository folderRepository;
    private final FolderMapper folderMapper;
    private final CommonFileFolderService commonFileFolderService; // для работы с файлами
    private final CommonUserFolderService commonUserFolderService; // для работы с пользователями
    // UserService полностью удален!

    /**
     * Создать новую папку
     */
    @Transactional
    public FolderDto createFolder(FolderCreateDto createDto) {
        log.info("Creating folder with name: {}, parentId: {}", createDto.getName(), createDto.getParentId());

        // Проверяем уникальность имени в родительской папке
        if (existsByNameAndParentId(createDto.getName(), createDto.getParentId())) {
            throw new FolderAlreadyExistsException(
                    String.format("Folder with name '%s' already exists in parent folder %s",
                            createDto.getName(), createDto.getParentId()));
        }

        // Проверяем существование родительской папки (через общий сервис)
        if (createDto.getParentId() != null) {
            commonFileFolderService.getFolderDtoById(createDto.getParentId());
        }

        // Проверяем существование создателя (через CommonUserFolderService)
        if (createDto.getCreatedBy() != null) {
            commonUserFolderService.getUserDtoById(createDto.getCreatedBy());
        }

        FolderEntity folderEntity = folderMapper.toEntity(createDto);
        folderEntity.setCreatedDate(LocalDateTime.now());

        FolderEntity savedFolder = folderRepository.save(folderEntity);
        log.info("Folder created successfully with id: {}", savedFolder.getId());

        return folderMapper.toDto(savedFolder);
    }

    /**
     * Получить папку по ID
     */
    public FolderDto getFolderById(Long id) {
        log.info("Getting folder by id: {}", id);
        return commonFileFolderService.getFolderDtoById(id); // через общий сервис
    }

    /**
     * Обновить папку
     */
    @Transactional
    public FolderDto updateFolder(Long id, FolderUpdateDto updateDto) {
        log.info("Updating folder with id: {}", id);

        FolderEntity folderEntity = commonFileFolderService.getFolderEntityById(id); // через общий сервис

        // Проверяем уникальность имени при изменении
        if (updateDto.getName() != null || updateDto.getParentId() != null) {
            String newName = updateDto.getName() != null ? updateDto.getName() : folderEntity.getName();
            Long newParentId = updateDto.getParentId() != null ? updateDto.getParentId() : folderEntity.getParentId();

            if (!newName.equals(folderEntity.getName()) ||
                    !java.util.Objects.equals(newParentId, folderEntity.getParentId())) {

                if (existsByNameAndParentId(newName, newParentId)) {
                    throw new FolderAlreadyExistsException(
                            String.format("Folder with name '%s' already exists in parent folder %s",
                                    newName, newParentId));
                }
            }
        }

        // Проверяем, не пытаемся ли сделать папку родителем самой себя
        if (updateDto.getParentId() != null && updateDto.getParentId().equals(id)) {
            throw new BadRequestException("Folder cannot be parent of itself");
        }

        // Проверяем существование новой родительской папки (через общий сервис)
        if (updateDto.getParentId() != null) {
            commonFileFolderService.getFolderDtoById(updateDto.getParentId());
        }

        folderMapper.updateEntity(folderEntity, updateDto);

        FolderEntity updatedFolder = folderRepository.save(folderEntity);
        log.info("Folder updated successfully with id: {}", id);

        return folderMapper.toDto(updatedFolder);
    }

    /**
     * Удалить папку
     */
    @Transactional
    public void deleteFolder(Long id) {
        log.info("Deleting folder with id: {}", id);

        FolderEntity folderEntity = commonFileFolderService.getFolderEntityById(id); // через общий сервис

        // Проверяем, есть ли файлы в папке (через общий сервис)
        if (commonFileFolderService.hasFilesInFolder(id)) {
            throw new BadRequestException("Cannot delete folder with files. Move or delete files first.");
        }

        // Проверяем, есть ли подпапки
        List<FolderDto> subfolders = getSubfolders(id);
        if (!subfolders.isEmpty()) {
            throw new BadRequestException("Cannot delete folder with subfolders. Delete subfolders first.");
        }

        folderRepository.delete(folderEntity);
        log.info("Folder deleted successfully with id: {}", id);
    }

    /**
     * Получить дерево папок
     */
    public List<FolderTreeDto> getFolderTree() {
        log.info("Building folder tree");

        List<FolderEntity> rootFolders = folderRepository.findRootFolders();
        return buildFolderTree(rootFolders);
    }

    /**
     * Получить содержимое папки (через общий сервис)
     */
    public FolderContentsDto getFolderContents(Long id) {
        log.info("Getting contents for folder id: {}", id);

        FolderDto folder = getFolderById(id);
        List<FileDto> files = commonFileFolderService.getFilesInFolder(id); // через общий сервис
        List<FolderDto> subfolders = getSubfolders(id);

        return FolderContentsDto.builder()
                .folder(folder)
                .files(files)
                .subfolders(subfolders)
                .build();
    }

    /**
     * Получить подпапки (через общий сервис)
     */
    public List<FolderDto> getSubfolders(Long parentId) {
        log.info("Getting subfolders for parent id: {}", parentId);
        return commonFileFolderService.getSubfolders(parentId);
    }

    /**
     * Получить папки пользователя (через CommonUserFolderService)
     */
    public List<FolderDto> getUserFolders(Long userId) {
        log.info("Getting folders for user id: {}", userId);

        // Проверяем существование пользователя
        commonUserFolderService.getUserDtoById(userId);

        return commonUserFolderService.getFoldersByUser(userId);
    }

    /**
     * Проверить существование папки с именем в родительской папке
     */
    public boolean existsByNameAndParentId(String name, Long parentId) {
        return folderRepository.existsByNameAndParentId(name, parentId);
    }

    /**
     * Проверить доступ к папке (через CommonUserFolderService)
     */
    public void validateFolderAccess(Long folderId, Long userId) {
        if (!commonUserFolderService.isFolderCreator(folderId, userId)) {
            throw new UnauthorizedException("User does not have permission to access this folder");
        }
    }

    /**
     * Переместить папку
     */
    @Transactional
    public FolderDto moveFolder(Long id, Long newParentId) {
        log.info("Moving folder id: {} to new parent: {}", id, newParentId);

        FolderUpdateDto updateDto = new FolderUpdateDto();
        updateDto.setParentId(newParentId);

        return updateFolder(id, updateDto);
    }

    /**
     * Переименовать папку
     */
    @Transactional
    public FolderDto renameFolder(Long id, String newName) {
        log.info("Renaming folder id: {} to: {}", id, newName);

        FolderUpdateDto updateDto = new FolderUpdateDto();
        updateDto.setName(newName);

        return updateFolder(id, updateDto);
    }

    /**
     * Получить путь к папке (через общий сервис)
     */
    public List<FolderDto> getFolderPath(Long folderId) {
        log.info("Getting path for folder id: {}", folderId);
        return commonFileFolderService.getFolderPath(folderId);
    }

    /**
     * Получить статистику по папке
     */
    public FolderStatistics getFolderStatistics(Long folderId) {
        log.info("Getting statistics for folder id: {}", folderId);

        FolderDto folder = getFolderById(folderId);
        long filesCount = commonFileFolderService.getFilesCountInFolder(folderId);
        long subfoldersCount = getSubfolders(folderId).size();

        return new FolderStatistics(folder, filesCount, subfoldersCount);
    }

    /**
     * Внутренний класс для статистики папки
     */
    public record FolderStatistics(FolderDto folder, long filesCount, long subfoldersCount) {}

    private List<FolderTreeDto> buildFolderTree(List<FolderEntity> folders) {
        List<FolderTreeDto> tree = new ArrayList<>();

        for (FolderEntity folder : folders) {
            FolderTreeDto node = folderMapper.toTreeDto(folder);

            List<FolderEntity> children = folderRepository.findByParentId(folder.getId());
            if (!children.isEmpty()) {
                node.setChildren(buildFolderTree(children));
            }

            tree.add(node);
        }

        return tree;
    }
}