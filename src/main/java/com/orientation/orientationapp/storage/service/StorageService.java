package com.orientation.orientationapp.storage.service;

import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;

public interface StorageService {

    /**
     * Store a file in the storage directory
     *
     * @param file the file to store
     * @return the path to the stored file
     */
    String store(MultipartFile file);

    /**
     * Get the path to a stored file
     *
     * @param filename the name of the file
     * @return the path to the file
     */
    Path load(String filename);

    /**
     * Delete a file from storage
     *
     * @param filename the name of the file to delete
     */
    void delete(String filename);

    /**
     * Check if a file exists
     *
     * @param filename the name of the file
     * @return true if the file exists
     */
    boolean exists(String filename);
}
