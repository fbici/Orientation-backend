-- V60: Seed programs linked to faculties
-- Each faculty gets 3-8 programs matching real formations

-- Helper: create programs for ALL science faculties
INSERT INTO programs (version, name, faculty_id, available, created_at, updated_at, deleted)
SELECT 0, p.name, f.id, TRUE, NOW(), NOW(), FALSE
FROM faculties f, (VALUES
    ('Licence en Informatique'),
    ('Licence en Mathematiques'),
    ('Licence en Physique'),
    ('Licence en Chimie'),
    ('Licence en Biologie'),
    ('Master en Informatique'),
    ('Master en Mathematiques Appliquees')
) AS p(name)
WHERE f.name LIKE '%Sciences%' OR f.name LIKE '%Science%' OR f.name LIKE '%Polytechnique%'
ON CONFLICT DO NOTHING;

-- Programs for medicine faculties
INSERT INTO programs (version, name, faculty_id, available, created_at, updated_at, deleted)
SELECT 0, p.name, f.id, TRUE, NOW(), NOW(), FALSE
FROM faculties f, (VALUES
    ('Doctorat en Medecine'),
    ('Doctorat en Pharmacie'),
    ('Licence en Sciences Infirmieres'),
    ('Master en Sante Publique')
) AS p(name)
WHERE f.name LIKE '%Medecine%' OR f.name LIKE '%Medicine%' OR f.name LIKE '%Sante%'
ON CONFLICT DO NOTHING;

-- Programs for law faculties
INSERT INTO programs (version, name, faculty_id, available, created_at, updated_at, deleted)
SELECT 0, p.name, f.id, TRUE, NOW(), NOW(), FALSE
FROM faculties f, (VALUES
    ('Licence en Droit'),
    ('Licence en Sciences Politiques'),
    ('Master en Droit Prive'),
    ('Master en Droit Public'),
    ('Master en Droit International')
) AS p(name)
WHERE f.name LIKE '%Droit%' OR f.name LIKE '%Law%' OR f.name LIKE '%Juridique%'
ON CONFLICT DO NOTHING;

-- Programs for economics faculties
INSERT INTO programs (version, name, faculty_id, available, created_at, updated_at, deleted)
SELECT 0, p.name, f.id, TRUE, NOW(), NOW(), FALSE
FROM faculties f, (VALUES
    ('Licence en Sciences Economiques'),
    ('Licence en Gestion'),
    ('Licence en Comptabilite'),
    ('Master en Economie'),
    ('Master en Management')
) AS p(name)
WHERE f.name LIKE '%Economique%' OR f.name LIKE '%Economic%' OR f.name LIKE '%Gestion%' OR f.name LIKE '%Management%'
ON CONFLICT DO NOTHING;

-- Programs for arts/humanities faculties
INSERT INTO programs (version, name, faculty_id, available, created_at, updated_at, deleted)
SELECT 0, p.name, f.id, TRUE, NOW(), NOW(), FALSE
FROM faculties f, (VALUES
    ('Licence en Lettres Modernes'),
    ('Licence en Histoire'),
    ('Licence en Geographie'),
    ('Licence en Philosophie'),
    ('Licence en Langues Etrangeres'),
    ('Master en Education')
) AS p(name)
WHERE f.name LIKE '%Lettre%' OR f.name LIKE '%Arts%' OR f.name LIKE '%Art%' OR f.name LIKE '%Humanit%'
ON CONFLICT DO NOTHING;

-- Programs for engineering schools
INSERT INTO programs (version, name, faculty_id, available, created_at, updated_at, deleted)
SELECT 0, p.name, f.id, TRUE, NOW(), NOW(), FALSE
FROM faculties f, (VALUES
    ('Genie Informatique'),
    ('Genie Civil'),
    ('Genie Electrique'),
    ('Genie Mecanique'),
    ('Genie des Procedes')
) AS p(name)
WHERE f.name LIKE '%Polytechnique%' OR f.name LIKE '%Ingenieur%' OR f.name LIKE '%Engineering%' OR f.name LIKE '%Arts et Metiers%'
ON CONFLICT DO NOTHING;

-- Programs for business schools
INSERT INTO programs (version, name, faculty_id, available, created_at, updated_at, deleted)
SELECT 0, p.name, f.id, TRUE, NOW(), NOW(), FALSE
FROM faculties f, (VALUES
    ('Bachelor en Administration des Affaires'),
    ('Master en Business Administration (MBA)'),
    ('Master en Finance'),
    ('Master en Marketing')
) AS p(name)
WHERE f.name LIKE '%Business%' OR f.name LIKE '%HEC%' OR f.name LIKE '%Said%' OR f.name LIKE '%Judge%'
ON CONFLICT DO NOTHING;
