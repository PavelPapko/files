package com.storage.files.controllers;


import com.storage.files.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Пользователи", description = "API для управления пользователями")
@RequestMapping("/api/v1/users")
public interface UserControllerApi {

    @Operation(summary = "Создать пользователя", description = "Создает нового пользователя")
    @PostMapping
    ResponseEntity<UserDto> createUser(@RequestBody UserCreateDto createDto);

    @Operation(summary = "Получить пользователя по ID", description = "Возвращает информацию о пользователе")
    @GetMapping("/{id}")
    ResponseEntity<UserDto> getUserById(@PathVariable Long id);

    @Operation(summary = "Получить пользователя по username", description = "Возвращает информацию о пользователе")
    @GetMapping("/username/{username}")
    ResponseEntity<UserDto> getUserByUsername(@PathVariable String username);

    @Operation(summary = "Обновить пользователя", description = "Обновляет информацию о пользователе")
    @PutMapping("/{id}")
    ResponseEntity<UserDto> updateUser(
            @PathVariable Long id,
            @RequestBody UserUpdateDto updateDto);

    @Operation(summary = "Удалить пользователя", description = "Удаляет пользователя")
    @DeleteMapping("/{id}")
    ResponseEntity<Void> deleteUser(@PathVariable Long id);

    @Operation(summary = "Получить всех пользователей", description = "Возвращает список всех пользователей")
    @GetMapping
    ResponseEntity<List<UserDto>> getAllUsers();

    @Operation(summary = "Получить файлы пользователя", description = "Возвращает список файлов пользователя")
    @GetMapping("/{id}/files")
    ResponseEntity<List<FileDto>> getUserFiles(@PathVariable Long id);
}