package com.orientation.orientationapp.common.enums;

import lombok.Getter;

@Getter
public enum AuditAction {
    CREATE("Création"),
    READ("Lecture"),
    UPDATE("Mise à jour"),
    DELETE("Suppression"),
    LOGIN("Connexion"),
    LOGOUT("Déconnexion"),
    FAILED_LOGIN("Échec de connexion"),
    PASSWORD_CHANGE("Changement de mot de passe"),
    EXPORT("Export"),
    IMPORT("Import");

    private final String description;

    AuditAction(String description) {
        this.description = description;
    }
}
