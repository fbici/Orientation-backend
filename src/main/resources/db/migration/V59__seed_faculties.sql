-- V59: Seed faculties for each university
-- Each university gets 3-6 faculties matching real departments

-- BENIN - UAC (Universite d'Abomey-Calavi)
INSERT INTO faculties (version, name, university_id, active, created_at, updated_at, deleted)
SELECT 0, f.name, u.id, TRUE, NOW(), NOW(), FALSE
FROM universities u, (VALUES
    ('Faculte des Sciences et Techniques'),
    ('Faculte de Medecine'),
    ('Faculte de Droit et de Sciences Politiques'),
    ('Faculte des Sciences Economiques'),
    ('Faculte des Lettres, Arts et Sciences Humaines'),
    ('Ecole Polytechnique')
) AS f(name)
WHERE u.name = 'Universite d''Abomey-Calavi'
ON CONFLICT DO NOTHING;

-- BENIN - Universite de Parakou
INSERT INTO faculties (version, name, university_id, active, created_at, updated_at, deleted)
SELECT 0, f.name, u.id, TRUE, NOW(), NOW(), FALSE
FROM universities u, (VALUES
    ('Faculte de Medecine'),
    ('Faculte des Sciences Agronomiques'),
    ('Faculte des Lettres et Sciences Humaines'),
    ('Faculte de Droit et Sciences Politiques')
) AS f(name)
WHERE u.name = 'Universite de Parakou'
ON CONFLICT DO NOTHING;

-- MAROC - Universite Mohammed V
INSERT INTO faculties (version, name, university_id, active, created_at, updated_at, deleted)
SELECT 0, f.name, u.id, TRUE, NOW(), NOW(), FALSE
FROM universities u, (VALUES
    ('Faculte des Sciences'),
    ('Faculte de Medecine et de Pharmacie'),
    ('Faculte de Droit et des Sciences Economiques'),
    ('Ecole Nationale Superieure d''Informatique et d''Analyse des Systemes'),
    ('Faculte des Lettres et Sciences Humaines')
) AS f(name)
WHERE u.name = 'Universite Mohammed V'
ON CONFLICT DO NOTHING;

-- MAROC - Universite Hassan II
INSERT INTO faculties (version, name, university_id, active, created_at, updated_at, deleted)
SELECT 0, f.name, u.id, TRUE, NOW(), NOW(), FALSE
FROM universities u, (VALUES
    ('Faculte des Sciences'),
    ('Faculte de Medecine et de Pharmacie'),
    ('Faculte des Sciences Juridiques Economiques et Sociales'),
    ('Ecole Nationale Superieure d''Arts et Metiers'),
    ('Faculte des Lettres et Sciences Humaines')
) AS f(name)
WHERE u.name = 'Universite Hassan II'
ON CONFLICT DO NOTHING;

-- MAROC - Universite Cadi Ayyad
INSERT INTO faculties (version, name, university_id, active, created_at, updated_at, deleted)
SELECT 0, f.name, u.id, TRUE, NOW(), NOW(), FALSE
FROM universities u, (VALUES
    ('Faculte des Sciences Semlalia'),
    ('Faculte de Medecine et de Pharmacie'),
    ('Faculte des Sciences Juridiques et Economiques'),
    ('Ecole Nationale des Sciences Appliquees')
) AS f(name)
WHERE u.name = 'Universite Cadi Ayyad'
ON CONFLICT DO NOTHING;

-- SENEGAL - UCAD
INSERT INTO faculties (version, name, university_id, active, created_at, updated_at, deleted)
SELECT 0, f.name, u.id, TRUE, NOW(), NOW(), FALSE
FROM universities u, (VALUES
    ('Faculte de Medecine de Pharmacie et d''Odontologie'),
    ('Faculte des Sciences et Technologies'),
    ('Faculte des Lettres et Sciences Humaines'),
    ('Faculte de Droit et de Sciences Politiques'),
    ('Faculte des Sciences Economiques et de Gestion')
) AS f(name)
WHERE u.name = 'Universite Cheikh Anta Diop'
ON CONFLICT DO NOTHING;

-- SENEGAL - UGB
INSERT INTO faculties (version, name, university_id, active, created_at, updated_at, deleted)
SELECT 0, f.name, u.id, TRUE, NOW(), NOW(), FALSE
FROM universities u, (VALUES
    ('Faculte des Sciences et Technologies'),
    ('Faculte de Medecine'),
    ('Faculte des Sciences Juridiques et Politiques'),
    ('UFR de Sciences Economiques')
) AS f(name)
WHERE u.name = 'Universite Gaston Berger'
ON CONFLICT DO NOTHING;

-- COTE D'IVOIRE - UFHB
INSERT INTO faculties (version, name, university_id, active, created_at, updated_at, deleted)
SELECT 0, f.name, u.id, TRUE, NOW(), NOW(), FALSE
FROM universities u, (VALUES
    ('Faculte des Sciences et Technologies'),
    ('Faculte de Medecine'),
    ('Faculte de Droit'),
    ('Faculte des Sciences Economiques'),
    ('Institut National Polytechnique')
) AS f(name)
WHERE u.name = 'Universite Felix Houphouet-Boigny'
ON CONFLICT DO NOTHING;

-- TUNISIE - Universite de Tunis El Manar
INSERT INTO faculties (version, name, university_id, active, created_at, updated_at, deleted)
SELECT 0, f.name, u.id, TRUE, NOW(), NOW(), FALSE
FROM universities u, (VALUES
    ('Faculte des Sciences de Tunis'),
    ('Faculte de Medecine de Tunis'),
    ('Faculte des Sciences Juridiques Politiques et Sociales'),
    ('Ecole Nationale d''Ingenieurs de Tunis')
) AS f(name)
WHERE u.name = 'Universite Tunis El Manar'
ON CONFLICT DO NOTHING;

-- ALGERIE - Universite d'Alger
INSERT INTO faculties (version, name, university_id, active, created_at, updated_at, deleted)
SELECT 0, f.name, u.id, TRUE, NOW(), NOW(), FALSE
FROM universities u, (VALUES
    ('Faculte des Sciences'),
    ('Faculte de Medecine'),
    ('Faculte de Droit'),
    ('Faculte des Sciences Economiques'),
    ('Faculte des Lettres et Sciences Humaines')
) AS f(name)
WHERE u.name = 'Universite d''Alger'
ON CONFLICT DO NOTHING;

-- NIGERIA - University of Lagos
INSERT INTO faculties (version, name, university_id, active, created_at, updated_at, deleted)
SELECT 0, f.name, u.id, TRUE, NOW(), NOW(), FALSE
FROM universities u, (VALUES
    ('Faculty of Science'),
    ('Faculty of Medicine'),
    ('Faculty of Law'),
    ('Faculty of Engineering'),
    ('Faculty of Arts')
) AS f(name)
WHERE u.name = 'University of Lagos'
ON CONFLICT DO NOTHING;

-- GHANA - University of Ghana
INSERT INTO faculties (version, name, university_id, active, created_at, updated_at, deleted)
SELECT 0, f.name, u.id, TRUE, NOW(), NOW(), FALSE
FROM universities u, (VALUES
    ('Faculty of Science'),
    ('School of Medicine and Dentistry'),
    ('Faculty of Law'),
    ('Faculty of Engineering'),
    ('Faculty of Arts')
) AS f(name)
WHERE u.name = 'University of Ghana'
ON CONFLICT DO NOTHING;

-- FRANCE - Sorbonne
INSERT INTO faculties (version, name, university_id, active, created_at, updated_at, deleted)
SELECT 0, f.name, u.id, TRUE, NOW(), NOW(), FALSE
FROM universities u, (VALUES
    ('Faculte des Sciences et Ingenierie'),
    ('Faculte de Medecine'),
    ('Faculte de Droit'),
    ('Faculte des Lettres'),
    ('Institut d''Etudes Politiques')
) AS f(name)
WHERE u.name = 'Sorbonne Universite'
ON CONFLICT DO NOTHING;

-- FRANCE - Paris-Saclay
INSERT INTO faculties (version, name, university_id, active, created_at, updated_at, deleted)
SELECT 0, f.name, u.id, TRUE, NOW(), NOW(), FALSE
FROM universities u, (VALUES
    ('Faculte des Sciences'),
    ('Faculte de Medecine'),
    ('Faculte de Pharmacie'),
    ('Faculte de Droit'),
    ('Ecole Polytechnique')
) AS f(name)
WHERE u.name = 'Universite Paris-Saclay'
ON CONFLICT DO NOTHING;

-- CANADA - Universite de Montreal
INSERT INTO faculties (version, name, university_id, active, created_at, updated_at, deleted)
SELECT 0, f.name, u.id, TRUE, NOW(), NOW(), FALSE
FROM universities u, (VALUES
    ('Faculte des Arts et des Sciences'),
    ('Faculte de Medecine'),
    ('Faculte de Droit'),
    ('Ecole Polytechnique de Montreal'),
    ('HEC Montreal')
) AS f(name)
WHERE u.name = 'Universite de Montreal'
ON CONFLICT DO NOTHING;

-- CANADA - McGill
INSERT INTO faculties (version, name, university_id, active, created_at, updated_at, deleted)
SELECT 0, f.name, u.id, TRUE, NOW(), NOW(), FALSE
FROM universities u, (VALUES
    ('Faculty of Science'),
    ('Faculty of Medicine'),
    ('Faculty of Law'),
    ('Faculty of Engineering'),
    ('Faculty of Arts')
) AS f(name)
WHERE u.name = 'McGill University'
ON CONFLICT DO NOTHING;

-- USA - Harvard
INSERT INTO faculties (version, name, university_id, active, created_at, updated_at, deleted)
SELECT 0, f.name, u.id, TRUE, NOW(), NOW(), FALSE
FROM universities u, (VALUES
    ('Harvard College'),
    ('Harvard Medical School'),
    ('Harvard Law School'),
    ('Harvard Business School'),
    ('School of Engineering and Applied Sciences')
) AS f(name)
WHERE u.name = 'Harvard University'
ON CONFLICT DO NOTHING;

-- USA - MIT
INSERT INTO faculties (version, name, university_id, active, created_at, updated_at, deleted)
SELECT 0, f.name, u.id, TRUE, NOW(), NOW(), FALSE
FROM universities u, (VALUES
    ('School of Science'),
    ('School of Engineering'),
    ('Sloan School of Management'),
    ('School of Architecture and Planning')
) AS f(name)
WHERE u.name = 'MIT'
ON CONFLICT DO NOTHING;

-- UK - Oxford
INSERT INTO faculties (version, name, university_id, active, created_at, updated_at, deleted)
SELECT 0, f.name, u.id, TRUE, NOW(), NOW(), FALSE
FROM universities u, (VALUES
    ('Faculty of Science'),
    ('Faculty of Medicine'),
    ('Faculty of Law'),
    ('Faculty of Arts'),
    ('Said Business School')
) AS f(name)
WHERE u.name = 'University of Oxford'
ON CONFLICT DO NOTHING;

-- UK - Cambridge
INSERT INTO faculties (version, name, university_id, active, created_at, updated_at, deleted)
SELECT 0, f.name, u.id, TRUE, NOW(), NOW(), FALSE
FROM universities u, (VALUES
    ('Faculty of Science'),
    ('School of Clinical Medicine'),
    ('Faculty of Law'),
    ('Faculty of Engineering'),
    ('Judge Business School')
) AS f(name)
WHERE u.name = 'University of Cambridge'
ON CONFLICT DO NOTHING;

-- JAPON - Universite de Tokyo
INSERT INTO faculties (version, name, university_id, active, created_at, updated_at, deleted)
SELECT 0, f.name, u.id, TRUE, NOW(), NOW(), FALSE
FROM universities u, (VALUES
    ('Faculty of Science'),
    ('Faculty of Medicine'),
    ('Faculty of Law'),
    ('Faculty of Engineering'),
    ('Faculty of Letters')
) AS f(name)
WHERE u.name = 'Universite de Tokyo'
ON CONFLICT DO NOTHING;

-- CHINE - Universite de Pekin
INSERT INTO faculties (version, name, university_id, active, created_at, updated_at, deleted)
SELECT 0, f.name, u.id, TRUE, NOW(), NOW(), FALSE
FROM universities u, (VALUES
    ('School of Mathematical Sciences'),
    ('School of Medicine'),
    ('Law School'),
    ('School of Engineering'),
    ('School of Economics')
) AS f(name)
WHERE u.name = 'Universite de Pekin'
ON CONFLICT DO NOTHING;
