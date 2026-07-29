package com.orientation.orientationapp.dataplat_formats.core.config;

public final class DataPlatformConstants {

    private DataPlatformConstants() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    // File limits
    public static final long MAX_FILE_SIZE = 50 * 1024 * 1024; // 50MB
    public static final int MAX_ROWS = 100_000;
    public static final int BATCH_SIZE = 1000;

    // Column mappings
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_CODE = "code";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_STATUS = "status";

    // Import sources
    public static final String SOURCE_MANUAL = "MANUAL";
    public static final String SOURCE_API = "API";
    public static final String SOURCE_BATCH = "BATCH";
    public static final String SOURCE_SCHEDULER = "SCHEDULER";

    // Hash algorithms
    public static final String HASH_ALGORITHM = "SHA-256";

    // Async
    public static final String IMPORT_TASK_EXECUTOR = "importTaskExecutor";
    public static final String BATCH_TASK_EXECUTOR = "batchTaskExecutor";
}
