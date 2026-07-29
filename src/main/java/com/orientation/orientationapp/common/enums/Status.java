package com.orientation.orientationapp.common.enums;

import lombok.Getter;

@Getter
public enum Status {
    ACTIVE("Actif"),
    INACTIVE("Inactif"),
    PENDING("En attente"),
    SUSPENDED("Suspendu"),
    DELETED("Supprimé");

    private final String description;

    Status(String description) {
        this.description = description;
    }
}
