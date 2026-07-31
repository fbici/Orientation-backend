-- V61: Seed grade scales and subjects for key countries
-- Grade scales for Benin, Maroc, Senegal, France, Canada

-- Grade Scale: Benin (0-20 scale)
INSERT INTO grade_scales (version, name, country_id, academic_year_id, min_score, max_score, passing_score, normalize_to, description, active, created_at, updated_at, deleted)
SELECT 0, 'Bac Benin - Note sur 20', c.id, ay.id, 0, 20, 10, 100, 'Systeme de notation beninois sur 20', TRUE, NOW(), NOW(), FALSE
FROM countries c, academic_years ay
WHERE c.code = 'BEN' AND ay.label = '2025-2026'
ON CONFLICT (country_id, academic_year_id) DO NOTHING;

-- Grade Scale: Maroc (0-20 scale)
INSERT INTO grade_scales (version, name, country_id, academic_year_id, min_score, max_score, passing_score, normalize_to, description, active, created_at, updated_at, deleted)
SELECT 0, 'Bac Maroc - Note sur 20', c.id, ay.id, 0, 20, 10, 100, 'Systeme de notation marocain sur 20', TRUE, NOW(), NOW(), FALSE
FROM countries c, academic_years ay
WHERE c.code = 'MAR' AND ay.label = '2025-2026'
ON CONFLICT (country_id, academic_year_id) DO NOTHING;

-- Grade Scale: Senegal (0-20 scale)
INSERT INTO grade_scales (version, name, country_id, academic_year_id, min_score, max_score, passing_score, normalize_to, description, active, created_at, updated_at, deleted)
SELECT 0, 'Bac Senegal - Note sur 20', c.id, ay.id, 0, 20, 10, 100, 'Systeme de notation senegalais sur 20', TRUE, NOW(), NOW(), FALSE
FROM countries c, academic_years ay
WHERE c.code = 'SEN' AND ay.label = '2025-2026'
ON CONFLICT (country_id, academic_year_id) DO NOTHING;

-- Grade Scale: France (0-20 scale)
INSERT INTO grade_scales (version, name, country_id, academic_year_id, min_score, max_score, passing_score, normalize_to, description, active, created_at, updated_at, deleted)
SELECT 0, 'Bac France - Note sur 20', c.id, ay.id, 0, 20, 10, 100, 'Systeme de notation francais sur 20', TRUE, NOW(), NOW(), FALSE
FROM countries c, academic_years ay
WHERE c.code = 'FRA' AND ay.label = '2025-2026'
ON CONFLICT (country_id, academic_year_id) DO NOTHING;

-- Grade Scale: Cote d'Ivoire (0-20 scale)
INSERT INTO grade_scales (version, name, country_id, academic_year_id, min_score, max_score, passing_score, normalize_to, description, active, created_at, updated_at, deleted)
SELECT 0, 'Bac Cote d''Ivoire - Note sur 20', c.id, ay.id, 0, 20, 10, 100, 'Systeme de notation ivoirien sur 20', TRUE, NOW(), NOW(), FALSE
FROM countries c, academic_years ay
WHERE c.code = 'CIV' AND ay.label = '2025-2026'
ON CONFLICT (country_id, academic_year_id) DO NOTHING;

-- Grade Scale: Tunisie (0-20 scale)
INSERT INTO grade_scales (version, name, country_id, academic_year_id, min_score, max_score, passing_score, normalize_to, description, active, created_at, updated_at, deleted)
SELECT 0, 'Bac Tunisie - Note sur 20', c.id, ay.id, 0, 20, 10, 100, 'Systeme de notation tunisien sur 20', TRUE, NOW(), NOW(), FALSE
FROM countries c, academic_years ay
WHERE c.code = 'TUN' AND ay.label = '2025-2026'
ON CONFLICT (country_id, academic_year_id) DO NOTHING;

-- SUBJECTS for Benin grade scale
INSERT INTO subjects (version, name, code, grade_scale_id, category, coefficient, core, active, created_at, updated_at, deleted)
SELECT 0, s.name, s.code, gs.id, s.cat, s.coef, TRUE, TRUE, NOW(), NOW(), FALSE
FROM grade_scales gs, countries c, (VALUES
    ('Mathematiques', 'MATH', 'SCIENCE', 4.0),
    ('Physique', 'PHYS', 'SCIENCE', 3.0),
    ('Chimie', 'CHIM', 'SCIENCE', 3.0),
    ('SVT', 'SVT', 'SCIENCE', 3.0),
    ('Francais', 'FRAN', 'LANGUE', 3.0),
    ('Anglais', 'ANGL', 'LANGUE', 2.0),
    ('Philosophie', 'PHIL', 'HUMANITE', 2.0),
    ('Histoire-Geographie', 'HGEO', 'HUMANITE', 2.0),
    ('Informatique', 'INFO', 'SCIENCE', 2.0)
) AS s(name, code, cat, coef)
WHERE gs.name LIKE '%Benin%' AND c.code = 'BEN' AND gs.country_id = c.id
ON CONFLICT DO NOTHING;

-- SUBJECTS for Maroc grade scale
INSERT INTO subjects (version, name, code, grade_scale_id, category, coefficient, core, active, created_at, updated_at, deleted)
SELECT 0, s.name, s.code, gs.id, s.cat, s.coef, TRUE, TRUE, NOW(), NOW(), FALSE
FROM grade_scales gs, countries c, (VALUES
    ('Mathematiques', 'MATH', 'SCIENCE', 5.0),
    ('Physique', 'PHYS', 'SCIENCE', 4.0),
    ('Chimie', 'CHIM', 'SCIENCE', 3.0),
    ('Sciences de la Vie et de la Terre', 'SVT', 'SCIENCE', 3.0),
    ('Francais', 'FRAN', 'LANGUE', 3.0),
    ('Arabe', 'ARAB', 'LANGUE', 3.0),
    ('Anglais', 'ANGL', 'LANGUE', 2.0),
    ('Philosophie', 'PHIL', 'HUMANITE', 2.0)
) AS s(name, code, cat, coef)
WHERE gs.name LIKE '%Maroc%' AND c.code = 'MAR' AND gs.country_id = c.id
ON CONFLICT DO NOTHING;

-- SUBJECTS for France grade scale
INSERT INTO subjects (version, name, code, grade_scale_id, category, coefficient, core, active, created_at, updated_at, deleted)
SELECT 0, s.name, s.code, gs.id, s.cat, s.coef, TRUE, TRUE, NOW(), NOW(), FALSE
FROM grade_scales gs, countries c, (VALUES
    ('Mathematiques', 'MATH', 'SCIENCE', 5.0),
    ('Physique-Chimie', 'PHCH', 'SCIENCE', 5.0),
    ('Sciences de la Vie et de la Terre', 'SVT', 'SCIENCE', 3.0),
    ('Francais', 'FRAN', 'LANGUE', 4.0),
    ('Philosophie', 'PHIL', 'HUMANITE', 4.0),
    ('Histoire-Geographie', 'HGEO', 'HUMANITE', 3.0),
    ('Anglais', 'ANGL', 'LANGUE', 3.0),
    ('Specialite', 'SPEC', 'SCIENCE', 4.0)
) AS s(name, code, cat, coef)
WHERE gs.name LIKE '%France%' AND c.code = 'FRA' AND gs.country_id = c.id
ON CONFLICT DO NOTHING;
