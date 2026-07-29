package com.orientation.orientationapp.dataplat.validation;

import com.orientation.orientationapp.dataplat_formats.core.model.ImportContext;
import com.orientation.orientationapp.dataplat_formats.core.model.ValidationIssue;
import com.orientation.orientationapp.dataplat_formats.enums.DataFormat;
import com.orientation.orientationapp.dataplat_formats.enums.DataType;
import com.orientation.orientationapp.dataplat_formats.enums.Severity;
import com.orientation.orientationapp.dataplat_validation.engine.impl.ValidationEngineImpl;
import com.orientation.orientationapp.dataplat_validation.strategy.ValidationContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ValidationEngineTest {

    private ValidationEngineImpl validationEngine;

    @BeforeEach
    void setUp() {
        validationEngine = new ValidationEngineImpl();
    }

    @Test
    void shouldValidateSuccessfulImport() {
        List<Map<String, Object>> rows = new ArrayList<>();
        rows.add(Map.of("name", "Université Test", "code", "UT", "country_code", "MAR"));

        ValidationContext context = ValidationContext.builder()
                .dataType(DataType.UNIVERSITIES)
                .build();

        List<ValidationIssue> issues = validationEngine.validate(rows, context);

        assertTrue(issues.stream().noneMatch(i -> i.getSeverity().isError()));
    }

    @Test
    void shouldDetectMissingRequiredField() {
        List<Map<String, Object>> rows = new ArrayList<>();
        rows.add(Map.of("code", "UT", "country_code", "MAR")); // Missing name

        ValidationContext context = ValidationContext.builder()
                .dataType(DataType.UNIVERSITIES)
                .build();

        List<ValidationIssue> issues = validationEngine.validate(rows, context);

        assertTrue(issues.stream().anyMatch(i ->
                i.getSeverity().isError() && i.getCode().contains("REQUIRED")));
    }

    @Test
    void shouldDetectEmptyFile() {
        List<Map<String, Object>> rows = new ArrayList<>();

        ValidationContext context = ValidationContext.builder()
                .dataType(DataType.UNIVERSITIES)
                .build();

        List<ValidationIssue> issues = validationEngine.validate(rows, context);

        assertTrue(issues.stream().anyMatch(i ->
                i.getSeverity().isError() && i.getCode().contains("EMPTY")));
    }

    @Test
    void shouldDetectDuplicates() {
        List<Map<String, Object>> rows = new ArrayList<>();
        rows.add(Map.of("name", "Université Test", "code", "UT", "country_code", "MAR"));
        rows.add(Map.of("name", "Université Test", "code", "UT", "country_code", "MAR"));

        ValidationContext context = ValidationContext.builder()
                .dataType(DataType.UNIVERSITIES)
                .build();

        List<ValidationIssue> issues = validationEngine.validate(rows, context);

        assertTrue(issues.stream().anyMatch(i ->
                i.getSeverity() == Severity.WARNING && i.getCode().contains("DUPLICATE")));
    }

    @Test
    void shouldValidateSingleRow() {
        Map<String, Object> row = Map.of("name", "Université Test", "code", "UT");

        ValidationContext context = ValidationContext.builder()
                .dataType(DataType.UNIVERSITIES)
                .build();

        List<ValidationIssue> issues = validationEngine.validateRow(row, context);

        assertNotNull(issues);
    }
}
