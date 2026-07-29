-- V27: Seed Data - Subjects (Moroccan Baccalaureate)
INSERT INTO subjects (id, version, name, code, grade_scale_id, category, coefficient, core, active, created_at, updated_at, deleted)
VALUES
    -- Scientific Series
    ('e0eebc99-9c0b-4ef8-bb6d-6bb9bd380a01', 0, 'Mathématiques', 'MATH', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a01', 'Scientifique', 3.0, TRUE, TRUE, NOW(), NOW(), FALSE),
    ('e0eebc99-9c0b-4ef8-bb6d-6bb9bd380a02', 0, 'Physique', 'PHYS', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a01', 'Scientifique', 2.0, TRUE, TRUE, NOW(), NOW(), FALSE),
    ('e0eebc99-9c0b-4ef8-bb6d-6bb9bd380a03', 0, 'Chimie', 'CHIM', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a01', 'Scientifique', 1.5, TRUE, TRUE, NOW(), NOW(), FALSE),
    ('e0eebc99-9c0b-4ef8-bb6d-6bb9bd380a04', 0, 'Sciences de la Vie et de la Terre', 'SVT', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a01', 'Scientifique', 1.5, TRUE, TRUE, NOW(), NOW(), FALSE),
    -- Linguistic / Languages
    ('e0eebc99-9c0b-4ef8-bb6d-6bb9bd380a05', 0, 'Arabe', 'ARAB', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a01', 'Linguistique', 2.0, TRUE, TRUE, NOW(), NOW(), FALSE),
    ('e0eebc99-9c0b-4ef8-bb6d-6bb9bd380a06', 0, 'Français', 'FRAN', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a01', 'Linguistique', 1.5, TRUE, TRUE, NOW(), NOW(), FALSE),
    ('e0eebc99-9c0b-4ef8-bb6d-6bb9bd380a07', 0, 'Anglais', 'ANGL', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a01', 'Linguistique', 1.5, TRUE, TRUE, NOW(), NOW(), FALSE),
    -- Human Sciences
    ('e0eebc99-9c0b-4ef8-bb6d-6bb9bd380a08', 0, 'Histoire-Géographie', 'HIST', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a01', 'Humain', 1.0, TRUE, TRUE, NOW(), NOW(), FALSE),
    ('e0eebc99-9c0b-4ef8-bb6d-6bb9bd380a09', 0, 'Philosophie', 'PHIL', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a01', 'Humain', 1.0, TRUE, TRUE, NOW(), NOW(), FALSE),
    -- Technical
    ('e0eebc99-9c0b-4ef8-bb6d-6bb9bd380a10', 0, 'Informatique', 'INFO', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a01', 'Technique', 1.0, FALSE, TRUE, NOW(), NOW(), FALSE),
    ('e0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 0, 'Dessin Technique', 'DESS', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a01', 'Technique', 1.0, FALSE, TRUE, NOW(), NOW(), FALSE),
    ('e0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 0, 'Éducation Islamique', 'EI', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a01', 'Humain', 1.0, TRUE, TRUE, NOW(), NOW(), FALSE);
