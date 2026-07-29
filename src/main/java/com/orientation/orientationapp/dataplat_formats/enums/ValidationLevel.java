package com.orientation.orientationapp.dataplat_formats.enums;

import lombok.Getter;

@Getter
public enum ValidationLevel {
    STRUCTURAL("Structure", 1),
    BUSINESS("MÃ©tier", 2),
    REFERENTIAL("RÃ©fÃ©rentiel", 3),
    RELATIONAL("Relationnel", 4),
    COHERENCE("CohÃ©rence", 5),
    DUPLICATE("Doublons", 6),
    FORBIDDEN("Valeurs interdites", 7);

    private final String label;
    private final int order;

    ValidationLevel(String label, int order) {
        this.label = label;
        this.order = order;
    }
}
