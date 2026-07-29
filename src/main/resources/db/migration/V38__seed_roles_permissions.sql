-- V38: Seed Data - Roles and Permissions
-- Default Organization
INSERT INTO organizations (id, version, name, code, active, created_at, updated_at, deleted)
VALUES ('aa000000-0000-0000-0000-000000000001', 0, 'Orientation Platform', 'ORIENT', TRUE, NOW(), NOW(), FALSE);

-- Default Tenant
INSERT INTO tenants (id, version, name, code, organization_id, active, created_at, updated_at, deleted)
VALUES ('bb000000-0000-0000-0000-000000000001', 0, 'Default Tenant', 'DEFAULT', 'aa000000-0000-0000-0000-000000000001', TRUE, NOW(), NOW(), FALSE);

-- Roles
INSERT INTO roles (id, version, name, code, description, system_role, active, created_at, updated_at, deleted)
VALUES
    ('cc000000-0000-0000-0000-000000000001', 0, 'Super Administrateur', 'SUPER_ADMIN', 'Accès total au système', TRUE, TRUE, NOW(), NOW(), FALSE),
    ('cc000000-0000-0000-0000-000000000002', 0, 'Administrateur', 'ADMIN', 'Administration générale', TRUE, TRUE, NOW(), NOW(), FALSE),
    ('cc000000-0000-0000-0000-000000000003', 0, 'Modérateur', 'MODERATEUR', 'Modération du contenu', FALSE, TRUE, NOW(), NOW(), FALSE),
    ('cc000000-0000-0000-0000-000000000004', 0, 'Utilisateur', 'UTILISATEUR', 'Utilisateur standard', TRUE, TRUE, NOW(), NOW(), FALSE),
    ('cc000000-0000-0000-0000-000000000005', 0, 'Candidat', 'CANDIDAT', 'Candidat à l''orientation', TRUE, TRUE, NOW(), NOW(), FALSE),
    ('cc000000-0000-0000-0000-000000000006', 0, 'Représentant Universitaire', 'REPRESENTANT_UNIVERSITAIRE', 'Représentant d''une université', FALSE, TRUE, NOW(), NOW(), FALSE);

-- Permissions
INSERT INTO permissions (id, version, name, code, description, category, active, created_at, updated_at, deleted)
VALUES
    ('dd000000-0000-0000-0000-000000000001', 0, 'Lire utilisateurs', 'USER_READ', 'Lire les utilisateurs', 'USER_MANAGEMENT', TRUE, NOW(), NOW(), FALSE),
    ('dd000000-0000-0000-0000-000000000002', 0, 'Créer utilisateur', 'USER_CREATE', 'Créer un utilisateur', 'USER_MANAGEMENT', TRUE, NOW(), NOW(), FALSE),
    ('dd000000-0000-0000-0000-000000000003', 0, 'Modifier utilisateur', 'USER_UPDATE', 'Modifier un utilisateur', 'USER_MANAGEMENT', TRUE, NOW(), NOW(), FALSE),
    ('dd000000-0000-0000-0000-000000000004', 0, 'Supprimer utilisateur', 'USER_DELETE', 'Supprimer un utilisateur', 'USER_MANAGEMENT', TRUE, NOW(), NOW(), FALSE),
    ('dd000000-0000-0000-0000-000000000005', 0, 'Lire universités', 'UNIVERSITY_READ', 'Lire les universités', 'UNIVERSITY_MANAGEMENT', TRUE, NOW(), NOW(), FALSE),
    ('dd000000-0000-0000-0000-000000000006', 0, 'Modifier universités', 'UNIVERSITY_UPDATE', 'Modifier une université', 'UNIVERSITY_MANAGEMENT', TRUE, NOW(), NOW(), FALSE),
    ('dd000000-0000-0000-0000-000000000007', 0, 'Lire règles', 'RULE_READ', 'Lire les règles', 'RULE_MANAGEMENT', TRUE, NOW(), NOW(), FALSE),
    ('dd000000-0000-0000-0000-000000000008', 0, 'Modifier règles', 'RULE_EDIT', 'Modifier les règles', 'RULE_MANAGEMENT', TRUE, NOW(), NOW(), FALSE),
    ('dd000000-0000-0000-0000-000000000009', 0, 'Publier règles', 'RULE_PUBLISH', 'Publier les règles', 'RULE_MANAGEMENT', TRUE, NOW(), NOW(), FALSE),
    ('dd000000-0000-0000-0000-000000000010', 0, 'Démarrer import', 'IMPORT_START', 'Démarrer un import', 'IMPORT_MANAGEMENT', TRUE, NOW(), NOW(), FALSE),
    ('dd000000-0000-0000-0000-000000000011', 0, 'Administration système', 'SYSTEM_ADMIN', 'Administration système', 'SYSTEM', TRUE, NOW(), NOW(), FALSE),
    ('dd000000-0000-0000-0000-000000000012', 0, 'Lire statistiques', 'STATISTICS_READ', 'Lire les statistiques', 'STATISTICS', TRUE, NOW(), NOW(), FALSE);

-- Super Admin gets all permissions
INSERT INTO role_permissions (id, version, role_id, permission_id, created_at, updated_at, deleted)
SELECT
    gen_random_uuid(), 0, 'cc000000-0000-0000-0000-000000000001', p.id, NOW(), NOW(), FALSE
FROM permissions p WHERE p.deleted = FALSE;

-- Default admin user (password: admin123)
INSERT INTO users (id, version, email, password, first_name, last_name, tenant_id, status, enabled, created_at, updated_at, deleted)
VALUES
    ('ee000000-0000-0000-0000-000000000001', 0, 'admin@orientation.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjqQeEfzUkGpGvVTQEHX1lK1qQFY.ue', 'Admin', 'System', 'bb000000-0000-0000-0000-000000000001', 'ACTIVE', TRUE, NOW(), NOW(), FALSE);

-- Assign Super Admin role
INSERT INTO user_roles (id, version, user_id, role_id, created_at, updated_at, deleted)
VALUES ('ff000000-0000-0000-0000-000000000001', 0, 'ee000000-0000-0000-0000-000000000001', 'cc000000-0000-0000-0000-000000000001', NOW(), NOW(), FALSE);
