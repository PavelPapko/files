package com.storage.files.services;

import com.storage.files.dto.FolderDto;
import com.storage.files.dto.UserDto;
import com.storage.files.models.FolderEntity;
import com.storage.files.models.UserEntity;
import com.storage.files.exceptions.FolderNotFoundException;
import com.storage.files.exceptions.UserNotFoundException;
import com.storage.files.mappers.FolderMapper;
import com.storage.files.mappers.UserMapper;
import com.storage.files.repository.FolderRepository;
import com.storage.files.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommonUserFolderService {

    private final UserRepository userRepository;
    private final FolderRepository folderRepository;
    private final UserMapper userMapper;
    private final FolderMapper folderMapper;

    /**
     * Получить пользователя по ID
     */
    public UserEntity getUserEntityById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
    }

    /**
     * Получить DTO пользователя по ID
     */
    public UserDto getUserDtoById(Long id) {
        return userMapper.toDto(getUserEntityById(id));
    }

    /**
     * Получить папку по ID
     */
    public FolderEntity getFolderEntityById(Long id) {
        return folderRepository.findById(id)
                .orElseThrow(() -> new FolderNotFoundException("Folder not found with id: " + id));
    }

    /**
     * Получить DTO папки по ID
     */
    public FolderDto getFolderDtoById(Long id) {
        return folderMapper.toDto(getFolderEntityById(id));
    }

    /**
     * Проверить существование пользователя
     */
    public boolean userExists(Long id) {
        return userRepository.existsById(id);
    }

    /**
     * Проверить существование папки
     */
    public boolean folderExists(Long id) {
        return folderRepository.existsById(id);
    }

    /**
     * Получить все папки пользователя
     */
    public List<FolderDto> getFoldersByUser(Long userId) {
        List<FolderEntity> folders = folderRepository.findByCreatedBy(userId);
        return folderMapper.toDtoList(folders);
    }

    /**
     * Получить количество папок пользователя
     */
    public long getFoldersCountByUser(Long userId) {
        return folderRepository.findByCreatedBy(userId).size();
    }

    /**
     * Проверить, является ли пользователь создателем папки
     */
    public boolean isFolderCreator(Long folderId, Long userId) {
        FolderEntity folder = getFolderEntityById(folderId);
        return folder.getCreatedBy() != null && folder.getCreatedBy().equals(userId);
    }

    /**
     * Получить всех активных пользователей
     */
    public List<UserDto> getActiveUsers() {
        return userMapper.toDtoList(userRepository.findAll().stream()
                .filter(UserEntity::getIsActive)
                .toList());
    }

    /**
     * Получить статистику по пользователю
     */
    public UserFolderStatistics getUserFolderStatistics(Long userId) {
        long foldersCount = getFoldersCountByUser(userId);
        return new UserFolderStatistics(userId, foldersCount);
    }

    /**
     * Внутренний класс для статистики
     */
    public record UserFolderStatistics(Long userId, long foldersCount) {}
}