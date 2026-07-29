package com.orientation.orientationapp.dataplat_validation.strategy;

import com.orientation.orientationapp.dataplat_formats.core.model.ValidationIssue;
import com.orientation.orientationapp.dataplat_formats.enums.ValidationLevel;

import java.util.List;
import java.util.Map;

/**
 * Strategy interface for validating individual fields/rows.
 * Each validator handles a specific type of validation.
 */
public interface FieldValidator {

    /**
     * @return the validation level this validator handles
     */
    ValidationLevel getLevel();

    /**
     * Validate a single row of data.
     *
     * @param row     the data row
     * @param context validation context
     * @return list of validation issues found
     */
    List<ValidationIssue> validate(Map<String, Object> row, ValidationContext context);

    /**
     * Validate the entire dataset (for cross-row validations).
     *
     * @param rows    all data rows
     * @param context validation context
     * @return list of validation issues found
     */
    default List<ValidationIssue> validateAll(List<Map<String, Object>> rows, ValidationContext context) {
        return List.of();
    }

    /**
     * @return the priority of this validator (lower = runs first)
     */
    default int getPriority() {
        return getLevel().getOrder() * 100;
    }

    /**
     * @return whether this validator can run in parallel
     */
    default boolean isParallelizable() {
        return true;
    }
}
