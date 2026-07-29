package com.orientation.orientationapp.storage.service;

import com.orientation.orientationapp.exception.BusinessException;
import com.orientation.orientationapp.storage.config.StorageConfig;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class StorageServiceImpl implements StorageService {

    private final StorageConfig storageConfig;
    private Path uploadLocation;

    @PostConstruct
    public void init() {
        this.uploadLocation = Paths.get(storageConfig.getUploadDir()).toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.uploadLocation);
            log.info("Storage directory created at: {}", this.uploadLocation);
        } catch (IOException ex) {
            throw new BusinessException("Could not create storage directory", "STORAGE_ERROR", ex);
        }
    }

    @Override
    public String store(MultipartFile file) {
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());

        if (file.isEmpty()) {
            throw new BusinessException("Cannot store empty file", "EMPTY_FILE");
        }

        if (originalFilename.contains("..")) {
            throw new BusinessException("Cannot store file with relative path outside current directory", "INVALID_PATH");
        }

        // Validate file type
        String contentType = file.getContentType();
        String[] allowedTypes = storageConfig.getAllowedTypes().split(",");
        boolean isAllowed = Arrays.stream(allowedTypes)
                .anyMatch(type -> type.trim().equals(contentType));

        if (!isAllowed) {
            throw new BusinessException("File type not allowed: " + contentType, "INVALID_FILE_TYPE");
        }

        // Validate file size
        if (file.getSize() > storageConfig.getMaxSize()) {
            throw new BusinessException("File size exceeds maximum allowed size", "FILE_TOO_LARGE");
        }

        // Generate unique filename
        String extension = "";
        if (originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String uniqueFilename = UUID.randomUUID().toString() + extension;

        try {
            Path targetLocation = this.uploadLocation.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            log.info("File stored successfully: {}", uniqueFilename);
            return uniqueFilename;
        } catch (IOException ex) {
            throw new BusinessException("Could not store file: " + originalFilename, "STORAGE_ERROR", ex);
        }
    }

    @Override
    public Path load(String filename) {
        return uploadLocation.resolve(filename).normalize();
    }

    @Override
    public void delete(String filename) {
        try {
            Path file = load(filename);
            Files.deleteIfExists(file);
            log.info("File deleted successfully: {}", filename);
        } catch (IOException ex) {
            log.error("Could not delete file: {}", filename, ex);
            throw new BusinessException("Could not delete file", "STORAGE_ERROR", ex);
        }
    }

    @Override
    public boolean exists(String filename) {
        return Files.exists(load(filename));
    }
}
