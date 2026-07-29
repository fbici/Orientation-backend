package com.orientation.orientationapp.dataplat_validation.engine.impl;

import com.orientation.orientationapp.dataplat_formats.core.model.ImportContext;
import com.orientation.orientationapp.dataplat_formats.core.model.ValidationIssue;
import com.orientation.orientationapp.dataplat_formats.enums.DataType;
import com.orientation.orientationapp.dataplat_formats.enums.Severity;
import com.orientation.orientationapp.dataplat_formats.enums.ValidationLevel;
import com.orientation.orientationapp.dataplat_validation.service.ValidationEngine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ValidationEngineImpl implements ValidationEngine {

    private final Map<DataType, List<String>> requiredColumns = new HashMap<>();
    private final Map<DataType, List<String>> optionalColumns = new HashMap<>();

    public ValidationEngineImpl() {
        initColumnDefinitions();
    }

    @Override
    public List<ValidationIssue> validate(List<Map<String, Object>> rows, com.orientation.orientationapp.dataplat_validation.strategy.ValidationContext context) {
        List<ValidationIssue> allIssues = new ArrayList<>();

        // 1. Structural validation
        allIssues.addAll(validateStructure(rows, context));

        // 2. Business validation
        allIssues.addAll(validateBusiness(rows, context));

        // 3. Duplicate validation
        allIssues.addAll(validateDuplicates(rows, context));

        // 4. Referential validation
        allIssues.addAll(validateReferential(rows, context));

        log.info("Validation complete: {} issues found", allIssues.size());
        return allIssues;
    }

    @Override
    public List<ValidationIssue> validate(List<Map<String, Object>> rows, com.orientation.orientationapp.dataplat_validation.strategy.ValidationContext context, ValidationLevel level) {
        return switch (level) {
            case STRUCTURAL -> validateStructure(rows, context);
            case BUSINESS -> validateBusiness(rows, context);
            case DUPLICATE -> validateDuplicates(rows, context);
            case REFERENTIAL -> validateReferential(rows, context);
            default -> validate(rows, context);
        };
    }

    @Override
    public List<ValidationIssue> validateRow(Map<String, Object> row, com.orientation.orientationapp.dataplat_validation.strategy.ValidationContext context) {
        List<ValidationIssue> issues = new ArrayList<>();

        // Check required fields
        List<String> required = requiredColumns.getOrDefault(context.getDataType(), List.of());
        for (String col : required) {
            Object value = row.get(col);
            if (value == null || value.toString().trim().isEmpty()) {
                issues.add(ValidationIssue.error(ValidationLevel.STRUCTURAL, "REQUIRED_FIELD_MISSING",
                        "Required field '" + col + "' is missing or empty", 0));
            }
        }

        // Check for unknown columns
        Set<String> knownColumns = new HashSet<>(required);
        knownColumns.addAll(optionalColumns.getOrDefault(context.getDataType(), List.of()));
        for (String key : row.keySet()) {
            if (!knownColumns.contains(key) && !key.startsWith("_")) {
                issues.add(ValidationIssue.warning(ValidationLevel.STRUCTURAL, "UNKNOWN_COLUMN",
                        "Unknown column: '" + key + "'", 0));
            }
        }

        return issues;
    }

    private List<ValidationIssue> validateStructure(List<Map<String, Object>> rows, com.orientation.orientationapp.dataplat_validation.strategy.ValidationContext context) {
        List<ValidationIssue> issues = new ArrayList<>();

        if (rows.isEmpty()) {
            issues.add(ValidationIssue.error(ValidationLevel.STRUCTURAL, "EMPTY_FILE", "File contains no data rows", 0));
            return issues;
        }

        // Check required columns exist
        Set<String> actualColumns = rows.get(0).keySet();
        List<String> required = requiredColumns.getOrDefault(context.getDataType(), List.of());

        for (String col : required) {
            if (!actualColumns.contains(col)) {
                issues.add(ValidationIssue.error(ValidationLevel.STRUCTURAL, "MISSING_COLUMN",
                        "Required column '" + col + "' not found in file", 0));
            }
        }

        // Check for empty rows
        for (int i = 0; i < rows.size(); i++) {
            Map<String, Object> row = rows.get(i);
            boolean isEmpty = row.values().stream()
                    .allMatch(v -> v == null || v.toString().trim().isEmpty());
            if (isEmpty) {
                issues.add(ValidationIssue.warning(ValidationLevel.STRUCTURAL, "EMPTY_ROW",
                        "Row " + (i + 1) + " is empty", i + 1));
            }
        }

        return issues;
    }

    private List<ValidationIssue> validateBusiness(List<Map<String, Object>> rows, com.orientation.orientationapp.dataplat_validation.strategy.ValidationContext context) {
        List<ValidationIssue> issues = new ArrayList<>();

        for (int i = 0; i < rows.size(); i++) {
            Map<String, Object> row = rows.get(i);
            int rowNum = i + 1;

            // Check required fields have values
            List<String> required = requiredColumns.getOrDefault(context.getDataType(), List.of());
            for (String col : required) {
                Object value = row.get(col);
                if (value == null || value.toString().trim().isEmpty()) {
                    issues.add(ValidationIssue.error(ValidationLevel.BUSINESS, "REQUIRED_VALUE_MISSING",
                            "Row " + rowNum + ": Required field '" + col + "' has no value", rowNum));
                }
            }

            // Check for invalid characters in text fields
            for (Map.Entry<String, Object> entry : row.entrySet()) {
                if (entry.getValue() != null) {
                    String val = entry.getValue().toString();
                    if (val.contains("\0") || val.contains("\r")) {
                        issues.add(ValidationIssue.warning(ValidationLevel.BUSINESS, "INVALID_CHARACTERS",
                                "Row " + rowNum + ": Field '" + entry.getKey() + "' contains invalid characters", rowNum));
                    }
                }
            }
        }

        return issues;
    }

    private List<ValidationIssue> validateDuplicates(List<Map<String, Object>> rows, com.orientation.orientationapp.dataplat_validation.strategy.ValidationContext context) {
        List<ValidationIssue> issues = new ArrayList<>();

        // Simple duplicate detection based on all columns
        Set<String> seen = new HashSet<>();
        for (int i = 0; i < rows.size(); i++) {
            String fingerprint = rows.get(i).values().stream()
                    .map(v -> v != null ? v.toString() : "")
                    .collect(Collectors.joining("|"));
            if (!seen.add(fingerprint)) {
                issues.add(ValidationIssue.warning(ValidationLevel.DUPLICATE, "DUPLICATE_ROW",
                        "Row " + (i + 1) + " is a duplicate", i + 1));
            }
        }

        return issues;
    }

    private List<ValidationIssue> validateReferential(List<Map<String, Object>> rows, com.orientation.orientationapp.dataplat_validation.strategy.ValidationContext context) {
        List<ValidationIssue> issues = new ArrayList<>();
        // Referential validation will be implemented when we have cross-table dependencies
        return issues;
    }

    private void initColumnDefinitions() {
        // Countries
        requiredColumns.put(DataType.COUNTRIES, List.of("name", "code"));
        optionalColumns.put(DataType.COUNTRIES, List.of("official_name", "phone_code", "currency", "active"));

        // Universities
        requiredColumns.put(DataType.UNIVERSITIES, List.of("name", "country_code"));
        optionalColumns.put(DataType.UNIVERSITIES, List.of("code", "city", "address", "phone", "email", "website", "ranking", "status", "active"));

        // Programs
        requiredColumns.put(DataType.PROGRAMS, List.of("name", "university_name", "faculty_name"));
        optionalColumns.put(DataType.PROGRAMS, List.of("code", "type", "degree", "duration", "language", "description", "max_students", "tuition_fee", "available"));

        // Subjects
        requiredColumns.put(DataType.SUBJECTS, List.of("name", "country_code"));
        optionalColumns.put(DataType.SUBJECTS, List.of("code", "category", "coefficient", "core", "active"));

        // Scholarships
        requiredColumns.put(DataType.SCHOLARSHIPS, List.of("name", "country_code", "academic_year"));
        optionalColumns.put(DataType.SCHOLARSHIPS, List.of("type", "provider", "description", "amount", "currency", "deadline", "status", "active"));
    }
}
