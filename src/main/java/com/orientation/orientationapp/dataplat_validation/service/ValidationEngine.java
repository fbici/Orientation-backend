package com.orientation.orientationapp.dataplat_validation.service;

import com.orientation.orientationapp.dataplat_formats.core.model.ValidationIssue;
import com.orientation.orientationapp.dataplat_formats.enums.ValidationLevel;
import com.orientation.orientationapp.dataplat_validation.strategy.ValidationContext;

import java.util.List;
import java.util.Map;

/**
 * Engine that orchestrates all validation levels.
 */
public interface ValidationEngine {

    /**
     * Validate data against all levels.
     *
     * @param rows    the data rows
     * @param context the validation context
     * @return list of all validation issues
     */
    List<ValidationIssue> validate(List<Map<String, Object>> rows, ValidationContext context);

    /**
     * Validate data against a specific level.
     *
     * @param rows    the data rows
     * @param context the validation context
     * @param level   the validation level
     * @return list of validation issues for that level
     */
    List<ValidationIssue> validate(List<Map<String, Object>> rows, ValidationContext context, ValidationLevel level);

    /**
     * Validate a single row.
     *
     * @param row     the data row
     * @param context the validation context
     * @return list of validation issues
     */
    List<ValidationIssue> validateRow(Map<String, Object> row, ValidationContext context);
}
