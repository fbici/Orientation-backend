package com.orientation.orientationapp.dataplat_formats.core.exception;

import com.orientation.orientationapp.dataplat_formats.core.model.ValidationIssue;
import lombok.Getter;

import java.util.List;

@Getter
public class ValidationException extends DataPlatformException {

    private final List<ValidationIssue> issues;

    public ValidationException(String message, List<ValidationIssue> issues) {
        super(message, "VALIDATION_ERROR");
        this.issues = issues;
    }
}
