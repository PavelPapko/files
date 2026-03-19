package com.storage.files.controllers;

import com.storage.files.controllers.UserControllerApi;
import com.storage.files.dto.FileDto;
import com.storage.files.dto.UserCreateDto;
import com.storage.files.dto.UserDto;
import com.storage.files.dto.UserUpdateDto;
import com.storage.files.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class UserController implements UserControllerApi {

    private final UserService userService;

    @Override
    public ResponseEntity<UserDto> createUser(UserCreateDto createDto) {
        UserDto user = userService.createUser(createDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @Override
    public ResponseEntity<UserDto> getUserById(Long id) {
        UserDto user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @Override
    public ResponseEntity<UserDto> getUserByUsername(String username) {
        UserDto user = userService.getUserByUsername(username);
        return ResponseEntity.ok(user);
    }

    @Override
    public ResponseEntity<UserDto> updateUser(Long id, UserUpdateDto updateDto) {
        UserDto user = userService.updateUser(id, updateDto);
        return ResponseEntity.ok(user);
    }

    @Override
    public ResponseEntity<Void> deleteUser(Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<UserDto> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @Override
    public ResponseEntity<List<FileDto>> getUserFiles(Long id) {
        List<FileDto> files = userService.getUserFiles(id);
        return ResponseEntity.ok(files);
    }
}