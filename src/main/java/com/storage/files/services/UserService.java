package com.storage.files.services;

import com.storage.files.dto.UserCreateDto;
import com.storage.files.dto.UserDto;
import com.storage.files.dto.UserUpdateDto;
import com.storage.files.models.UserEntity;
import com.storage.files.exceptions.UserAlreadyExistsException;
import com.storage.files.exceptions.UserNotFoundException;
import com.storage.files.exceptions.BadRequestException;
import com.storage.files.mappers.UserMapper;
import com.storage.files.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final CommonFileFolderService commonFileFolderService; // для работы с файлами
    private final CommonUserFolderService commonUserFolderService; // для работы с папками
    // FolderService полностью удален!

    /**
     * Создать нового пользователя
     */
    @Transactional
    public UserDto createUser(UserCreateDto createDto) {
        log.info("Creating new user with username: {}", createDto.getUsername());

        validateUserCreate(createDto);

        if (existsByUsername(createDto.getUsername())) {
            throw new UserAlreadyExistsException("Username already exists: " + createDto.getUsername());
        }

        if (existsByEmail(createDto.getEmail())) {
            throw new UserAlreadyExistsException("Email already exists: " + createDto.getEmail());
        }

        UserEntity userEntity = userMapper.toEntity(createDto);
        userEntity.setPasswordHash(passwordEncoder.encode(createDto.getPassword()));
        userEntity.setIsActive(true);
        userEntity.setCreatedDate(LocalDateTime.now());

        if (userEntity.getRole() == null) {
            userEntity.setRole("USER");
        }

        UserEntity savedUser = userRepository.save(userEntity);
        log.info("User created successfully with id: {}", savedUser.getId());

        return userMapper.toDto(savedUser);
    }

    /**
     * Получить пользователя по ID
     */
    public UserDto getUserById(Long id) {
        log.info("Getting user by id: {}", id);
        return commonUserFolderService.getUserDtoById(id); // через общий сервис
    }

    /**
     * Получить пользователя по username
     */
    public UserDto getUserByUsername(String username) {
        log.info("Getting user by username: {}", username);

        UserEntity userEntity = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found with username: " + username));

        return userMapper.toDto(userEntity);
    }

    /**
     * Получить пользователя по email
     */
    public UserDto getUserByEmail(String email) {
        log.info("Getting user by email: {}", email);

        UserEntity userEntity = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));

        return userMapper.toDto(userEntity);
    }

    /**
     * Обновить информацию о пользователе
     */
    @Transactional
    public UserDto updateUser(Long id, UserUpdateDto updateDto) {
        log.info("Updating user with id: {}", id);

        UserEntity userEntity = commonUserFolderService.getUserEntityById(id); // через общий сервис

        if (updateDto.getEmail() != null && !updateDto.getEmail().equals(userEntity.getEmail())) {
            if (existsByEmail(updateDto.getEmail())) {
                throw new UserAlreadyExistsException("Email already exists: " + updateDto.getEmail());
            }
        }

        userMapper.updateEntity(userEntity, updateDto);

        UserEntity updatedUser = userRepository.save(userEntity);
        log.info("User updated successfully with id: {}", id);

        return userMapper.toDto(updatedUser);
    }

    /**
     * Деактивировать пользователя (soft delete)
     */
    @Transactional
    public void deleteUser(Long id) {
        log.info("Deactivating user with id: {}", id);

        UserEntity userEntity = commonUserFolderService.getUserEntityById(id); // через общий сервис
        userEntity.setIsActive(false);
        userRepository.save(userEntity);

        log.info("User deactivated successfully with id: {}", id);
    }

    /**
     * Полностью удалить пользователя (hard delete)
     */
    @Transactional
    public void permanentlyDeleteUser(Long id) {
        log.info("Permanently deleting user with id: {}", id);

        UserEntity userEntity = commonUserFolderService.getUserEntityById(id); // через общий сервис

        // Проверяем, есть ли у пользователя файлы (через CommonFileFolderService)
        if (commonFileFolderService.userHasFiles(id)) {
            throw new BadRequestException("Cannot delete user with existing files. Delete files first.");
        }

        // Проверяем, есть ли у пользователя папки (через CommonUserFolderService)
        if (commonUserFolderService.getFoldersCountByUser(id) > 0) {
            throw new BadRequestException("Cannot delete user with existing folders. Delete folders first.");
        }

        userRepository.delete(userEntity);
        log.info("User permanently deleted with id: {}", id);
    }

    /**
     * Получить всех пользователей
     */
    public List<UserDto> getAllUsers() {
        log.info("Getting all users");
        return userMapper.toDtoList(userRepository.findAll());
    }

    /**
     * Получить всех активных пользователей
     */
    public List<UserDto> getActiveUsers() {
        log.info("Getting all active users");
        return commonUserFolderService.getActiveUsers(); // через общий сервис
    }

    /**
     * Получить файлы пользователя (через CommonFileFolderService)
     */
    public List<com.storage.files.dto.FileDto> getUserFiles(Long userId) {
        log.info("Getting files for user id: {}", userId);

        // Проверяем существование пользователя
        commonUserFolderService.getUserDtoById(userId);

        return commonFileFolderService.getFilesByUser(userId);
    }

    /**
     * Получить папки пользователя (через CommonUserFolderService)
     */
    public List<com.storage.files.dto.FolderDto> getUserFolders(Long userId) {
        log.info("Getting folders for user id: {}", userId);

        // Проверяем существование пользователя
        commonUserFolderService.getUserDtoById(userId);

        return commonUserFolderService.getFoldersByUser(userId);
    }

    /**
     * Получить статистику пользователя
     */
    public UserStatistics getUserStatistics(Long userId) {
        log.info("Getting statistics for user id: {}", userId);

        commonUserFolderService.getUserDtoById(userId);

        long filesCount = commonFileFolderService.getFilesCountByUser(userId);
        long totalSize = commonFileFolderService.getTotalFileSizeByUser(userId);
        long foldersCount = commonUserFolderService.getFoldersCountByUser(userId);

        return new UserStatistics(filesCount, totalSize, foldersCount);
    }

    /**
     * Проверить существование username
     */
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    /**
     * Проверить существование email
     */
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * Обновить дату последнего входа
     */
    @Transactional
    public void updateLastLoginDate(String username) {
        userRepository.findByUsername(username).ifPresent(user -> {
            user.setLastLoginDate(LocalDateTime.now());
            userRepository.save(user);
            log.info("Updated last login date for user: {}", username);
        });
    }

    /**
     * Сменить пароль
     */
    @Transactional
    public UserDto changePassword(Long id, String oldPassword, String newPassword) {
        log.info("Changing password for user id: {}", id);

        UserEntity userEntity = commonUserFolderService.getUserEntityById(id); // через общий сервис

        if (!passwordEncoder.matches(oldPassword, userEntity.getPasswordHash())) {
            throw new BadRequestException("Old password is incorrect");
        }

        validatePassword(newPassword);

        userEntity.setPasswordHash(passwordEncoder.encode(newPassword));
        UserEntity updatedUser = userRepository.save(userEntity);

        log.info("Password changed successfully for user id: {}", id);
        return userMapper.toDto(updatedUser);
    }

    /**
     * Активировать/деактивировать пользователя
     */
    @Transactional
    public UserDto setUserActiveStatus(Long id, boolean active) {
        log.info("Setting user id: {} active status to: {}", id, active);

        UserEntity userEntity = commonUserFolderService.getUserEntityById(id); // через общий сервис
        userEntity.setIsActive(active);

        UserEntity updatedUser = userRepository.save(userEntity);
        return userMapper.toDto(updatedUser);
    }

    /**
     * Внутренний класс для статистики пользователя
     */
    public record UserStatistics(long filesCount, long totalSize, long foldersCount) {}

    private void validateUserCreate(UserCreateDto createDto) {
        if (createDto.getUsername() == null || createDto.getUsername().trim().isEmpty()) {
            throw new BadRequestException("Username is required");
        }

        if (createDto.getUsername().length() < 3 || createDto.getUsername().length() > 50) {
            throw new BadRequestException("Username must be between 3 and 50 characters");
        }

        if (createDto.getPassword() == null || createDto.getPassword().length() < 6) {
            throw new BadRequestException("Password must be at least 6 characters");
        }

        validatePassword(createDto.getPassword());
    }

    private void validatePassword(String password) {
        if (password.length() < 6) {
            throw new BadRequestException("Password must be at least 6 characters long");
        }
    }
}