package com.orientation.orientationapp.common.enums;

import lombok.Getter;

@Getter
public enum UserRole {
    STUDENT("Étudiant", "Can access student features"),
    ADMIN("Administrateur", "Full system access"),
    MODERATOR("Modérateur", "Can moderate content"),
    UNIVERSITY_REP("Représentant universitaire", "Can manage university info");

    private final String description;
    private final String permissions;

    UserRole(String description, String permissions) {
        this.description = description;
        this.permissions = permissions;
    }
}
