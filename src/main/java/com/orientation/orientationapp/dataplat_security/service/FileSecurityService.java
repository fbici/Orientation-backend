package com.orientation.orientationapp.dataplat_security.service;

import com.orientation.orientationapp.dataplat_formats.core.model.ImportContext;

/**
 * Service for file security checks before import.
 */
public interface FileSecurityService {

    /**
     * Validate file MIME type against allowed types.
     *
     * @param fileName the file name
     * @param mimeType the MIME type
     * @return true if the MIME type is allowed
     */
    boolean validateMimeType(String fileName, String mimeType);

    /**
     * Check file size against limits.
     *
     * @param fileSize the file size in bytes
     * @return true if the size is within limits
     */
    boolean validateFileSize(long fileSize);

    /**
     * Scan file for viruses (interface for antivirus integration).
     *
     * @param content the file content
     * @return scan result
     */
    VirusScanResult scanFile(byte[] content);

    /**
     * Verify file integrity using checksum.
     *
     * @param content       the file content
     * @param expectedHash  the expected SHA256 hash
     * @return true if integrity is verified
     */
    boolean verifyIntegrity(byte[] content, String expectedHash);

    /**
     * Validate user permissions for import.
     *
     * @param userId  the user ID
     * @param context the import context
     * @return true if the user has permission
     */
    boolean validatePermissions(String userId, ImportContext context);
}
