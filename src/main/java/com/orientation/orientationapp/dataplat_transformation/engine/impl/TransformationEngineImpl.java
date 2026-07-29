package com.orientation.orientationapp.dataplat_transformation.engine.impl;

import com.orientation.orientationapp.dataplat_formats.core.model.ImportContext;
import com.orientation.orientationapp.dataplat_formats.converter.RowMapper;
import com.orientation.orientationapp.dataplat_transformation.service.TransformationEngine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.regex.Pattern;

@Slf4j
@Service
public class TransformationEngineImpl implements TransformationEngine {

    private static final Pattern ACCENT_PATTERN = Pattern.compile("[\\p{InCombining_Diacritical_Marks}]");
    private static final Pattern MULTIPLE_SPACES = Pattern.compile("\\s+");

    @Override
    public List<Map<String, Object>> transform(List<Map<String, Object>> rows, ImportContext context) {
        List<Map<String, Object>> transformed = new ArrayList<>();

        for (int i = 0; i < rows.size(); i++) {
            Map<String, Object> row = rows.get(i);
            Map<String, Object> transformedRow = transformRow(row, context);
            transformed.add(transformedRow);
        }

        log.info("Transformation complete: {} rows transformed", transformed.size());
        return transformed;
    }

    @Override
    public List<Map<String, Object>> transform(List<Map<String, Object>> rows, ImportContext context, String transformerId) {
        return switch (transformerId) {
            case "normalize" -> normalizeRows(rows);
            case "enrich" -> enrichRows(rows, context);
            case "all" -> {
                List<Map<String, Object>> result = normalizeRows(rows);
                yield enrichRows(result, context);
            }
            default -> transform(rows, context);
        };
    }

    private Map<String, Object> transformRow(Map<String, Object> row, ImportContext context) {
        Map<String, Object> result = new LinkedHashMap<>();

        for (Map.Entry<String, Object> entry : row.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (value == null) {
                result.put(key, null);
                continue;
            }

            String strValue = value.toString().trim();

            // Normalize string values
            if (!isNumericField(key)) {
                strValue = normalizeString(strValue);
            }

            result.put(key, strValue);
        }

        // Add metadata
        result.put("_import_id", context.getImportId().toString());
        result.put("_imported_at", Instant.now().toString());
        result.put("_row_number", 0); // Will be set by orchestrator

        return result;
    }

    private List<Map<String, Object>> normalizeRows(List<Map<String, Object>> rows) {
        List<Map<String, Object>> normalized = new ArrayList<>();

        for (Map<String, Object> row : rows) {
            Map<String, Object> normalizedRow = new LinkedHashMap<>();

            for (Map.Entry<String, Object> entry : row.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();

                if (value == null) {
                    normalizedRow.put(key, null);
                    continue;
                }

                String strValue = value.toString();

                // Trim
                strValue = strValue.trim();

                // Normalize spaces
                strValue = MULTIPLE_SPACES.matcher(strValue).replaceAll(" ");

                // Remove control characters
                strValue = strValue.replaceAll("[\\p{Cntrl}]", "");

                normalizedRow.put(key, strValue);
            }

            normalized.add(normalizedRow);
        }

        return normalized;
    }

    private List<Map<String, Object>> enrichRows(List<Map<String, Object>> rows, ImportContext context) {
        Instant now = Instant.now();
        String importId = context.getImportId().toString();

        for (int i = 0; i < rows.size(); i++) {
            Map<String, Object> row = rows.get(i);

            // Add metadata
            row.putIfAbsent("_import_id", importId);
            row.putIfAbsent("_imported_at", now.toString());
            row.put("_row_number", i + 1);

            // Generate UUID if not present
            row.putIfAbsent("_uuid", java.util.UUID.randomUUID().toString());
        }

        return rows;
    }

    private String normalizeString(String value) {
        if (value == null || value.isEmpty()) return value;

        // Trim
        value = value.trim();

        // Normalize spaces
        value = MULTIPLE_SPACES.matcher(value).replaceAll(" ");

        return value;
    }

    private boolean isNumericField(String fieldName) {
        return fieldName.contains("amount") || fieldName.contains("fee") ||
                fieldName.contains("count") || fieldName.contains("rank") ||
                fieldName.contains("coefficient") || fieldName.contains("duration") ||
                fieldName.contains("max_") || fieldName.contains("min_");
    }
}
