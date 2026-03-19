package com.storage.files.services;

import com.storage.files.exceptions.FileStorageException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Slf4j
@Service
public class FileStorageService {

    private final Path fileStorageLocation;

    public FileStorageService(@Value("${file.storage.location:./uploads}") String uploadDir) {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
            log.info("File storage directory created/verified at: {}", this.fileStorageLocation);
        } catch (IOException ex) {
            throw new FileStorageException("Could not create the directory for file storage", ex);
        }
    }

    public String storeFile(MultipartFile file, String filename) {
        String normalizedFilename = StringUtils.cleanPath(filename);

        try {
            if (normalizedFilename.contains("..")) {
                throw new FileStorageException("Filename contains invalid path sequence: " + normalizedFilename);
            }

            Path targetLocation = this.fileStorageLocation.resolve(normalizedFilename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            log.info("File stored successfully: {}, size: {}", normalizedFilename, file.getSize());
            return targetLocation.toString();

        } catch (IOException ex) {
            throw new FileStorageException("Could not store file " + normalizedFilename, ex);
        }
    }

    public Resource loadFileAsResource(String filename) {
        try {
            Path filePath = this.fileStorageLocation.resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists()) {
                if (resource.isReadable()) {
                    return resource;
                } else {
                    throw new FileStorageException("File is not readable: " + filename);
                }
            } else {
                throw new FileStorageException("File not found: " + filename);
            }
        } catch (MalformedURLException ex) {
            throw new FileStorageException("File not found: " + filename, ex);
        }
    }

    public void deleteFile(String filename) {
        try {
            Path filePath = this.fileStorageLocation.resolve(filename).normalize();
            boolean deleted = Files.deleteIfExists(filePath);
            if (deleted) {
                log.info("File deleted: {}", filename);
            } else {
                log.warn("File not found for deletion: {}", filename);
            }
        } catch (IOException ex) {
            throw new FileStorageException("Could not delete file: " + filename, ex);
        }
    }

    public boolean fileExists(String filename) {
        Path filePath = this.fileStorageLocation.resolve(filename).normalize();
        return Files.exists(filePath);
    }

    public byte[] readFileContent(String filename) {
        try {
            Path filePath = this.fileStorageLocation.resolve(filename).normalize();
            return Files.readAllBytes(filePath);
        } catch (IOException ex) {
            throw new FileStorageException("Could not read file: " + filename, ex);
        }
    }
}