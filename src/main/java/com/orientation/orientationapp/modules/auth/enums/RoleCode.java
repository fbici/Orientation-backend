package com.orientation.orientationapp.modules.auth.enums;

import lombok.Getter;

@Getter
public enum RoleCode {
    SUPER_ADMIN("SUPER_ADMIN", "Super Administrateur", true),
    ADMIN("ADMIN", "Administrateur", true),
    MODERATOR("MODERATEUR", "Modérateur", false),
    USER("UTILISATEUR", "Utilisateur", true),
    CANDIDATE("CANDIDAT", "Candidat", true),
    UNIVERSITY_REP("REPRESENTANT_UNIVERSITAIRE", "Représentant universitaire", false);

    private final String code;
    private final String description;
    private final boolean systemRole;

    RoleCode(String code, String description, boolean systemRole) {
        this.code = code;
        this.description = description;
        this.systemRole = systemRole;
    }
}
