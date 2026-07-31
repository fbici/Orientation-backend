-- V60: Seed faculties linked to campuses
-- Each campus gets 3-6 faculties matching real departments

-- Science faculties
INSERT INTO faculties (version, campus_id, name, active, created_at, updated_at, deleted)
SELECT 0, c.id, f.name, TRUE, NOW(), NOW(), FALSE
FROM campuses c, (VALUES
    ('Faculte des Sciences et Techniques'),
    ('Faculte des Sciences'),
    ('Ecole des Sciences')
) AS f(name)
WHERE c.main = TRUE
ON CONFLICT DO NOTHING;

-- Medicine faculties
INSERT INTO faculties (version, campus_id, name, active, created_at, updated_at, deleted)
SELECT 0, c.id, f.name, TRUE, NOW(), NOW(), FALSE
FROM campuses c, (VALUES
    ('Faculte de Medecine'),
    ('Faculte de Medecine et de Pharmacie')
) AS f(name)
WHERE c.main = TRUE
ON CONFLICT DO NOTHING;

-- Law faculties
INSERT INTO faculties (version, campus_id, name, active, created_at, updated_at, deleted)
SELECT 0, c.id, f.name, TRUE, NOW(), NOW(), FALSE
FROM campuses c, (VALUES
    ('Faculte de Droit et Sciences Politiques'),
    ('Faculte de Droit')
) AS f(name)
WHERE c.main = TRUE
ON CONFLICT DO NOTHING;

-- Economics faculties
INSERT INTO faculties (version, campus_id, name, active, created_at, updated_at, deleted)
SELECT 0, c.id, f.name, TRUE, NOW(), NOW(), FALSE
FROM campuses c, (VALUES
    ('Faculte des Sciences Economiques'),
    ('Faculte de Gestion')
) AS f(name)
WHERE c.main = TRUE
ON CONFLICT DO NOTHING;

-- Arts faculties
INSERT INTO faculties (version, campus_id, name, active, created_at, updated_at, deleted)
SELECT 0, c.id, f.name, TRUE, NOW(), NOW(), FALSE
FROM campuses c, (VALUES
    ('Faculte des Lettres et Sciences Humaines')
) AS f(name)
WHERE c.main = TRUE
ON CONFLICT DO NOTHING;

-- Engineering schools
INSERT INTO faculties (version, campus_id, name, active, created_at, updated_at, deleted)
SELECT 0, c.id, f.name, TRUE, NOW(), NOW(), FALSE
FROM campuses c, (VALUES
    ('Ecole Polytechnique'),
    ('Ecole d''Ingenieurs')
) AS f(name)
WHERE c.main = TRUE
ON CONFLICT DO NOTHING;
