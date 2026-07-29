package com.orientation.orientationapp.dataplat_formats.enums;

import lombok.Getter;

@Getter
public enum Severity {
    INFO("Information"),
    WARNING("Avertissement"),
    ERROR("Erreur"),
    CRITICAL("Critique");

    private final String label;

    Severity(String label) {
        this.label = label;
    }

    public boolean isError() {
        return this == ERROR || this == CRITICAL;
    }
}
