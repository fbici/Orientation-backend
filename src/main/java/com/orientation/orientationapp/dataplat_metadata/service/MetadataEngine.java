package com.orientation.orientationapp.dataplat_metadata.service;

import com.orientation.orientationapp.dataplat_formats.core.model.ImportMetadata;

import java.util.UUID;

/**
 * Engine for managing import metadata.
 */
public interface MetadataEngine {

    /**
     * Create metadata for a new import.
     *
     * @param fileName the file name
     * @param fileSize the file size
     * @param mimeType the MIME type
     * @param source   the import source
     * @param userId   the user ID
     * @return the created metadata
     */
    ImportMetadata createMetadata(String fileName, long fileSize, String mimeType, String source, String userId);

    /**
     * Update metadata with import results.
     *
     * @param importId the import ID
     * @param result   the import result
     */
    void updateMetadata(UUID importId, com.orientation.orientationapp.dataplat_formats.core.model.ImportResult result);

    /**
     * Verify file integrity using SHA256 hash.
     *
     * @param importId the import ID
     * @param content  the file content
     * @return true if integrity is verified
     */
    boolean verifyIntegrity(UUID importId, byte[] content);

    /**
     * Get metadata for an import.
     *
     * @param importId the import ID
     * @return the metadata
     */
    ImportMetadata getMetadata(UUID importId);

    /**
     * Calculate SHA256 hash of content.
     *
     * @param content the content to hash
     * @return the hash string
     */
    String calculateHash(byte[] content);
}
