package com.orientation.orientationapp.modules.auth.enums;

import lombok.Getter;

@Getter
public enum PermissionCode {
    // User management
    USER_READ("USER_READ", "Lire les utilisateurs", "USER_MANAGEMENT"),
    USER_CREATE("USER_CREATE", "Créer un utilisateur", "USER_MANAGEMENT"),
    USER_UPDATE("USER_UPDATE", "Modifier un utilisateur", "USER_MANAGEMENT"),
    USER_DELETE("USER_DELETE", "Supprimer un utilisateur", "USER_MANAGEMENT"),
    USER_READ_ALL("USER_READ_ALL", "Lire tous les utilisateurs", "USER_MANAGEMENT"),

    // Role management
    ROLE_READ("ROLE_READ", "Lire les rôles", "ROLE_MANAGEMENT"),
    ROLE_CREATE("ROLE_CREATE", "Créer un rôle", "ROLE_MANAGEMENT"),
    ROLE_UPDATE("ROLE_UPDATE", "Modifier un rôle", "ROLE_MANAGEMENT"),
    ROLE_DELETE("ROLE_DELETE", "Supprimer un rôle", "ROLE_MANAGEMENT"),
    ROLE_ASSIGN("ROLE_ASSIGN", "Attribuer un rôle", "ROLE_MANAGEMENT"),

    // University management
    UNIVERSITY_READ("UNIVERSITY_READ", "Lire les universités", "UNIVERSITY_MANAGEMENT"),
    UNIVERSITY_CREATE("UNIVERSITY_CREATE", "Créer une université", "UNIVERSITY_MANAGEMENT"),
    UNIVERSITY_UPDATE("UNIVERSITY_UPDATE", "Modifier une université", "UNIVERSITY_MANAGEMENT"),
    UNIVERSITY_DELETE("UNIVERSITY_DELETE", "Supprimer une université", "UNIVERSITY_MANAGEMENT"),

    // Program management
    PROGRAM_READ("PROGRAM_READ", "Lire les programmes", "PROGRAM_MANAGEMENT"),
    PROGRAM_CREATE("PROGRAM_CREATE", "Créer un programme", "PROGRAM_MANAGEMENT"),
    PROGRAM_UPDATE("PROGRAM_UPDATE", "Modifier un programme", "PROGRAM_MANAGEMENT"),
    PROGRAM_DELETE("PROGRAM_DELETE", "Supprimer un programme", "PROGRAM_MANAGEMENT"),

    // Rule management
    RULE_READ("RULE_READ", "Lire les règles", "RULE_MANAGEMENT"),
    RULE_EDIT("RULE_EDIT", "Modifier les règles", "RULE_MANAGEMENT"),
    RULE_PUBLISH("RULE_PUBLISH", "Publier les règles", "RULE_MANAGEMENT"),

    // Import management
    IMPORT_READ("IMPORT_READ", "Lire les imports", "IMPORT_MANAGEMENT"),
    IMPORT_START("IMPORT_START", "Démarrer un import", "IMPORT_MANAGEMENT"),
    IMPORT_CANCEL("IMPORT_CANCEL", "Annuler un import", "IMPORT_MANAGEMENT"),

    // Guide management
    GUIDE_READ("GUIDE_READ", "Lire les guides", "GUIDE_MANAGEMENT"),
    GUIDE_CREATE("GUIDE_CREATE", "Créer un guide", "GUIDE_MANAGEMENT"),
    GUIDE_UPDATE("GUIDE_UPDATE", "Modifier un guide", "GUIDE_MANAGEMENT"),
    GUIDE_PUBLISH("GUIDE_PUBLISH", "Publier un guide", "GUIDE_MANAGEMENT"),

    // Recommendation
    RECOMMENDATION_READ("RECOMMENDATION_READ", "Lire les recommandations", "RECOMMENDATION"),
    RECOMMENDATION_EXECUTE("RECOMMENDATION_EXECUTE", "Exécuter les recommandations", "RECOMMENDATION"),

    // Transcript
    TRANSCRIPT_READ("TRANSCRIPT_READ", "Lire les relevés", "TRANSCRIPT"),
    TRANSCRIPT_UPLOAD("TRANSCRIPT_UPLOAD", "Uploader un relevé", "TRANSCRIPT"),
    TRANSCRIPT_VALIDATE("TRANSCRIPT_VALIDATE", "Valider un relevé", "TRANSCRIPT"),

    // Scholarship
    SCHOLARSHIP_READ("SCHOLARSHIP_READ", "Lire les bourses", "SCHOLARSHIP"),
    SCHOLARSHIP_MANAGE("SCHOLARSHIP_MANAGE", "Gérer les bourses", "SCHOLARSHIP"),

    // Statistics
    STATISTICS_READ("STATISTICS_READ", "Lire les statistiques", "STATISTICS"),

    // System
    SYSTEM_ADMIN("SYSTEM_ADMIN", "Administration système", "SYSTEM"),
    SYSTEM_CONFIG("SYSTEM_CONFIG", "Configuration système", "SYSTEM"),
    AUDIT_READ("AUDIT_READ", "Lire les audits", "AUDIT"),

    // Notification
    NOTIFICATION_SEND("NOTIFICATION_SEND", "Envoyer des notifications", "NOTIFICATION"),
    NOTIFICATION_MANAGE("NOTIFICATION_MANAGE", "Gérer les notifications", "NOTIFICATION");

    private final String code;
    private final String description;
    private final String category;

    PermissionCode(String code, String description, String category) {
        this.code = code;
        this.description = description;
        this.category = category;
    }

    public static PermissionCode fromCode(String code) {
        for (PermissionCode p : values()) {
            if (p.code.equals(code)) return p;
        }
        throw new IllegalArgumentException("Unknown permission code: " + code);
    }
}
