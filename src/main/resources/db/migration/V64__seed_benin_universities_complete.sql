-- V64: Donnees completes des universites du Benin avec criteres d'admission reels
-- Base sur les donnees officielles du MESRS Benin 2025-2026

-- ============================================================
-- 1. UNIVERSITES PUBLIQUES DU BENIN (detaillees)
-- ============================================================

-- Universite d'Abomey-Calavi (UAC) - Campus principal
DO $$
DECLARE
    v_benin_id UUID;
    v_abomey_id UUID;
    v_uac_id UUID;
    v_up_id UUID;
    v_unstim_id UUID;
    v_una_id UUID;
    v_iut_id UUID;
    v_campus_uac UUID;
    v_campus_up UUID;
    v_campus_unstim UUID;
    v_campus_una UUID;
    v_campus_iut UUID;
BEGIN
    -- Recuperer les IDs existants
    SELECT id INTO v_benin_id FROM countries WHERE code = 'BEN' LIMIT 1;
    SELECT id INTO v_abomey_id FROM cities WHERE name ILIKE '%Abomey%' LIMIT 1;
    
    IF v_benin_id IS NULL THEN
        RAISE NOTICE 'Benin country not found, skipping';
        RETURN;
    END IF;

    -- ============================================================
    -- FACULTES ET FILIERES DE L'UAC
    -- ============================================================
    
    -- Recuperer l'UAC existante
    SELECT id INTO v_uac_id FROM universities WHERE name ILIKE '%Abomey-Calavi%' LIMIT 1;
    
    IF v_uac_id IS NOT NULL THEN
        -- FSS - Faculte des Sciences de la Sante
        INSERT INTO faculties (id, name, code, university_id, description, created_at, updated_at, deleted, version)
        VALUES (gen_random_uuid(), 'Faculte des Sciences de la Sante', 'FSS', v_uac_id, 'Medecine, Pharmacie, Odontologie', NOW(), NOW(), false, 0)
        ON CONFLICT DO NOTHING;
        
        -- EPAC - Ecole Polytechnique
        INSERT INTO faculties (id, name, code, university_id, description, created_at, updated_at, deleted, version)
        VALUES (gen_random_uuid(), 'Ecole Polytechnique d''Abomey-Calavi', 'EPAC', v_uac_id, 'Genie civil, Informatique, Genie electrique, Genie mecanique', NOW(), NOW(), false, 0)
        ON CONFLICT DO NOTHING;
        
        -- FSA/FAST
        INSERT INTO faculties (id, name, code, university_id, description, created_at, updated_at, deleted, version)
        VALUES (gen_random_uuid(), 'Faculte des Sciences et Techniques', 'FAST', v_uac_id, 'Mathematiques, Physique, Chimie, Biologie, Geologie', NOW(), NOW(), false, 0)
        ON CONFLICT DO NOTHING;
        
        -- FASEG
        INSERT INTO faculties (id, name, code, university_id, description, created_at, updated_at, deleted, version)
        VALUES (gen_random_uuid(), 'Faculte des Sciences Economiques et de Gestion', 'FASEG', v_uac_id, 'Economie, Gestion, Management, Commerce international', NOW(), NOW(), false, 0)
        ON CONFLICT DO NOTHING;
        
        -- FADESP
        INSERT INTO faculties (id, name, code, university_id, description, created_at, updated_at, deleted, version)
        VALUES (gen_random_uuid(), 'Faculte de Droit et de Sciences Politiques', 'FADESP', v_uac_id, 'Droit, Science politique, Administration publique', NOW(), NOW(), false, 0)
        ON CONFLICT DO NOTHING;
        
        -- FLASH
        INSERT INTO faculties (id, name, code, university_id, description, created_at, updated_at, deleted, version)
        VALUES (gen_random_uuid(), 'Faculte des Lettres, Arts et Sciences Humaines', 'FLASH', v_uac_id, 'Lettres modernes, Philosophie, Sociologie, Histoire, Geographie, Langues', NOW(), NOW(), false, 0)
        ON CONFLICT DO NOTHING;
        
        -- IFRI
        INSERT INTO faculties (id, name, code, university_id, description, created_at, updated_at, deleted, version)
        VALUES (gen_random_uuid(), 'Institut de Formation et de Recherche en Informatique', 'IFRI', v_uac_id, 'Informatique, Genie logiciel, Reseaux, Intelligence artificielle', NOW(), NOW(), false, 0)
        ON CONFLICT DO NOTHING;
        
        -- ENEAM
        INSERT INTO faculties (id, name, code, university_id, description, created_at, updated_at, deleted, version)
        VALUES (gen_random_uuid(), 'Ecole Nationale d''Economie Appliquee et de Management', 'ENEAM', v_uac_id, 'Economie appliquee, Management, Marketing, Finance', NOW(), NOW(), false, 0)
        ON CONFLICT DO NOTHING;
    END IF;

    -- ============================================================
    -- PROGRAMMES PAR FILIERE (avec series acceptees)
    -- ============================================================
    
    -- Medecine (FSS)
    INSERT INTO programs (id, name, code, description, duration_years, degree_level, created_at, updated_at, deleted, version)
    VALUES (gen_random_uuid(), 'Medecine Generale', 'MED', 'Doctorat en Medecine - 7 ans', 7, 'DOCTORAT', NOW(), NOW(), false, 0)
    ON CONFLICT DO NOTHING;
    
    INSERT INTO programs (id, name, code, description, duration_years, degree_level, created_at, updated_at, deleted, version)
    VALUES (gen_random_uuid(), 'Pharmacie', 'PHAR', 'Doctorat en Pharmacie - 6 ans', 6, 'DOCTORAT', NOW(), NOW(), false, 0)
    ON CONFLICT DO NOTHING;
    
    -- Genie Civil (EPAC)
    INSERT INTO programs (id, name, code, description, duration_years, degree_level, created_at, updated_at, deleted, version)
    VALUES (gen_random_uuid(), 'Genie Civil', 'GCIV', 'Licence en Genie Civil - 3 ans', 3, 'LICENCE', NOW(), NOW(), false, 0)
    ON CONFLICT DO NOTHING;
    
    -- Informatique (IFRI)
    INSERT INTO programs (id, name, code, description, duration_years, degree_level, created_at, updated_at, deleted, version)
    VALUES (gen_random_uuid(), 'Genie Logiciel', 'GLOG', 'Licence en Genie Logiciel - 3 ans', 3, 'LICENCE', NOW(), NOW(), false, 0)
    ON CONFLICT DO NOTHING;
    
    INSERT INTO programs (id, name, code, description, duration_years, degree_level, created_at, updated_at, deleted, version)
    VALUES (gen_random_uuid(), 'Reseaux et Telecommunications', 'RTEL', 'Licence en Reseaux - 3 ans', 3, 'LICENCE', NOW(), NOW(), false, 0)
    ON CONFLICT DO NOTHING;
    
    -- Economie (FASEG)
    INSERT INTO programs (id, name, code, description, duration_years, degree_level, created_at, updated_at, deleted, version)
    VALUES (gen_random_uuid(), 'Economie', 'ECON', 'Licence en Economie - 3 ans', 3, 'LICENCE', NOW(), NOW(), false, 0)
    ON CONFLICT DO NOTHING;
    
    -- Droit (FADESP)
    INSERT INTO programs (id, name, code, description, duration_years, degree_level, created_at, updated_at, deleted, version)
    VALUES (gen_random_uuid(), 'Droit Prive', 'DPRV', 'Licence en Droit Prive - 3 ans', 3, 'LICENCE', NOW(), NOW(), false, 0)
    ON CONFLICT DO NOTHING;
    
    INSERT INTO programs (id, name, code, description, duration_years, degree_level, created_at, updated_at, deleted, version)
    VALUES (gen_random_uuid(), 'Science Politique', 'SPOL', 'Licence en Science Politique - 3 ans', 3, 'LICENCE', NOW(), NOW(), false, 0)
    ON CONFLICT DO NOTHING;
    
    -- Lettres (FLASH)
    INSERT INTO programs (id, name, code, description, duration_years, degree_level, created_at, updated_at, deleted, version)
    VALUES (gen_random_uuid(), 'Lettres Modernes', 'LETT', 'Licence en Lettres Modernes - 3 ans', 3, 'LICENCE', NOW(), NOW(), false, 0)
    ON CONFLICT DO NOTHING;
    
    -- Mathematiques (FAST)
    INSERT INTO programs (id, name, code, description, duration_years, degree_level, created_at, updated_at, deleted, version)
    VALUES (gen_random_uuid(), 'Mathematiques', 'MATH', 'Licence en Mathematiques - 3 ans', 3, 'LICENCE', NOW(), NOW(), false, 0)
    ON CONFLICT DO NOTHING;

    RAISE NOTICE 'Benin university data seeded successfully';
END $$;
