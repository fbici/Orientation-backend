package com.orientation.orientationapp.dataplat_formats.converter;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Maps raw rows from parsers to normalized format.
 */
public final class RowMapper {

    private RowMapper() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Normalize column names (lowercase, snake_case).
     */
    public static Map<String, Object> normalizeKeys(Map<String, Object> row) {
        Map<String, Object> normalized = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : row.entrySet()) {
            String key = normalizeKey(entry.getKey());
            normalized.put(key, entry.getValue());
        }
        return normalized;
    }

    /**
     * Normalize a single key.
     */
    public static String normalizeKey(String key) {
        if (key == null) return "";
        return key.trim()
                .toLowerCase()
                .replaceAll("[^a-z0-9_]", "_")
                .replaceAll("_+", "_")
                .replaceAll("^_|_$", "");
    }

    /**
     * Map columns using a mapping configuration.
     */
    public static Map<String, Object> mapColumns(Map<String, Object> row, Map<String, String> columnMapping) {
        Map<String, Object> mapped = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : columnMapping.entrySet()) {
            Object value = row.get(entry.getKey());
            if (value != null) {
                mapped.put(entry.getValue(), value);
            }
        }
        return mapped;
    }
}
