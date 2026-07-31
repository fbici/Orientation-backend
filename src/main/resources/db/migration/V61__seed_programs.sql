-- V61: Seed programs linked to faculties
-- Programs require: type (NOT NULL), duration (NOT NULL)

-- Science programs
INSERT INTO programs (version, faculty_id, name, type, degree, duration, language, available, created_at, updated_at, deleted)
SELECT 0, f.id, p.name, p.type, p.degree, p.duration, 'Francais', TRUE, NOW(), NOW(), FALSE
FROM faculties f, (VALUES
    ('Licence en Informatique', 'LICENCE', 'Licence', 3),
    ('Licence en Mathematiques', 'LICENCE', 'Licence', 3),
    ('Licence en Physique', 'LICENCE', 'Licence', 3),
    ('Licence en Chimie', 'LICENCE', 'Licence', 3),
    ('Licence en Biologie', 'LICENCE', 'Licence', 3),
    ('Master en Informatique', 'MASTER', 'Master', 2),
    ('Master en Mathematiques Appliquees', 'MASTER', 'Master', 2)
) AS p(name, type, degree, duration)
WHERE f.name LIKE '%Sciences%' OR f.name LIKE '%Science%'
ON CONFLICT DO NOTHING;

-- Medicine programs
INSERT INTO programs (version, faculty_id, name, type, degree, duration, language, available, created_at, updated_at, deleted)
SELECT 0, f.id, p.name, p.type, p.degree, p.duration, 'Francais', TRUE, NOW(), NOW(), FALSE
FROM faculties f, (VALUES
    ('Doctorat en Medecine', 'DOCTORAT', 'Doctorat', 7),
    ('Doctorat en Pharmacie', 'DOCTORAT', 'Doctorat', 6),
    ('Licence en Sciences Infirmieres', 'LICENCE', 'Licence', 3),
    ('Master en Sante Publique', 'MASTER', 'Master', 2)
) AS p(name, type, degree, duration)
WHERE f.name LIKE '%Medecine%' OR f.name LIKE '%Medicine%'
ON CONFLICT DO NOTHING;

-- Law programs
INSERT INTO programs (version, faculty_id, name, type, degree, duration, language, available, created_at, updated_at, deleted)
SELECT 0, f.id, p.name, p.type, p.degree, p.duration, 'Francais', TRUE, NOW(), NOW(), FALSE
FROM faculties f, (VALUES
    ('Licence en Droit', 'LICENCE', 'Licence', 3),
    ('Licence en Sciences Politiques', 'LICENCE', 'Licence', 3),
    ('Master en Droit Prive', 'MASTER', 'Master', 2),
    ('Master en Droit Public', 'MASTER', 'Master', 2),
    ('Master en Droit International', 'MASTER', 'Master', 2)
) AS p(name, type, degree, duration)
WHERE f.name LIKE '%Droit%'
ON CONFLICT DO NOTHING;

-- Economics programs
INSERT INTO programs (version, faculty_id, name, type, degree, duration, language, available, created_at, updated_at, deleted)
SELECT 0, f.id, p.name, p.type, p.degree, p.duration, 'Francais', TRUE, NOW(), NOW(), FALSE
FROM faculties f, (VALUES
    ('Licence en Sciences Economiques', 'LICENCE', 'Licence', 3),
    ('Licence en Gestion', 'LICENCE', 'Licence', 3),
    ('Licence en Comptabilite', 'LICENCE', 'Licence', 3),
    ('Master en Economie', 'MASTER', 'Master', 2),
    ('Master en Management', 'MASTER', 'Master', 2)
) AS p(name, type, degree, duration)
WHERE f.name LIKE '%Economique%' OR f.name LIKE '%Economic%' OR f.name LIKE '%Gestion%'
ON CONFLICT DO NOTHING;

-- Arts/Humanities programs
INSERT INTO programs (version, faculty_id, name, type, degree, duration, language, available, created_at, updated_at, deleted)
SELECT 0, f.id, p.name, p.type, p.degree, p.duration, 'Francais', TRUE, NOW(), NOW(), FALSE
FROM faculties f, (VALUES
    ('Licence en Lettres Modernes', 'LICENCE', 'Licence', 3),
    ('Licence en Histoire', 'LICENCE', 'Licence', 3),
    ('Licence en Geographie', 'LICENCE', 'Licence', 3),
    ('Licence en Philosophie', 'LICENCE', 'Licence', 3),
    ('Licence en Langues Etrangeres', 'LICENCE', 'Licence', 3),
    ('Master en Education', 'MASTER', 'Master', 2)
) AS p(name, type, degree, duration)
WHERE f.name LIKE '%Lettre%' OR f.name LIKE '%Humanit%'
ON CONFLICT DO NOTHING;

-- Engineering programs
INSERT INTO programs (version, faculty_id, name, type, degree, duration, language, available, created_at, updated_at, deleted)
SELECT 0, f.id, p.name, p.type, p.degree, p.duration, 'Francais', TRUE, NOW(), NOW(), FALSE
FROM faculties f, (VALUES
    ('Genie Informatique', 'INGENIEUR', 'Ingenieur', 5),
    ('Genie Civil', 'INGENIEUR', 'Ingenieur', 5),
    ('Genie Electrique', 'INGENIEUR', 'Ingenieur', 5),
    ('Genie Mecanique', 'INGENIEUR', 'Ingenieur', 5),
    ('Genie des Procedes', 'INGENIEUR', 'Ingenieur', 5)
) AS p(name, type, degree, duration)
WHERE f.name LIKE '%Polytechnique%' OR f.name LIKE '%Ingenieur%' OR f.name LIKE '%Ecole%'
ON CONFLICT DO NOTHING;
