package com.orientation.orientationapp.dataplat_formats.core.model;

import com.orientation.orientationapp.dataplat_formats.enums.Severity;
import com.orientation.orientationapp.dataplat_formats.enums.ValidationLevel;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValidationIssue {

    private ValidationLevel level;
    private Severity severity;
    private String field;
    private String code;
    private String message;
    private Object expectedValue;
    private Object actualValue;
    private int rowIndex;
    private String columnName;

    public static ValidationIssue error(ValidationLevel level, String code, String message, int rowIndex) {
        return ValidationIssue.builder()
                .level(level)
                .severity(Severity.ERROR)
                .code(code)
                .message(message)
                .rowIndex(rowIndex)
                .build();
    }

    public static ValidationIssue warning(ValidationLevel level, String code, String message, int rowIndex) {
        return ValidationIssue.builder()
                .level(level)
                .severity(Severity.WARNING)
                .code(code)
                .message(message)
                .rowIndex(rowIndex)
                .build();
    }
}
