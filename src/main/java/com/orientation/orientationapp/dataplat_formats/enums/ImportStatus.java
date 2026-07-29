package com.orientation.orientationapp.dataplat_formats.enums;

import lombok.Getter;

@Getter
public enum ImportStatus {
    PENDING("Pending"),
    PROCESSING("Processing"),
    VALIDATING("Validating"),
    PARSING("Parsing"),
    TRANSFORMING("Transforming"),
    IMPORTING("Importing"),
    COMPLETED("Completed"),
    FAILED("Failed"),
    PARTIAL("Partially completed"),
    ROLLED_BACK("Rolled back"),
    CANCELLED("Cancelled");

    private final String description;

    ImportStatus(String description) {
        this.description = description;
    }

    public boolean isTerminal() {
        return this == COMPLETED || this == FAILED || this == ROLLED_BACK || this == CANCELLED;
    }

    public boolean isActive() {
        return this == PROCESSING || this == VALIDATING || this == PARSING || this == TRANSFORMING || this == IMPORTING;
    }
}
