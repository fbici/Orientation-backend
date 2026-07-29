-- V32: Seed Data - Sample Programs
INSERT INTO programs (id, version, faculty_id, name, code, type, degree, duration, language, available, created_at, updated_at, deleted)
VALUES
    -- License programs
    ('40eebc99-9c0b-4ef8-bb6d-6bb9bd380a01', 0, '30eebc99-9c0b-4ef8-bb6d-6bb9bd380a01', 'Licence en Mathématiques', 'L-MATH', 'LICENSE', 'Licence', 3, 'Français', TRUE, NOW(), NOW(), FALSE),
    ('40eebc99-9c0b-4ef8-bb6d-6bb9bd380a02', 0, '30eebc99-9c0b-4ef8-bb6d-6bb9bd380a01', 'Licence en Physique', 'L-PHYS', 'LICENSE', 'Licence', 3, 'Français', TRUE, NOW(), NOW(), FALSE),
    ('40eebc99-9c0b-4ef8-bb6d-6bb9bd380a03', 0, '30eebc99-9c0b-4ef8-bb6d-6bb9bd380a01', 'Licence en Informatique', 'L-INFO', 'LICENSE', 'Licence', 3, 'Français', TRUE, NOW(), NOW(), FALSE),
    ('40eebc99-9c0b-4ef8-bb6d-6bb9bd380a04', 0, '30eebc99-9c0b-4ef8-bb6d-6bb9bd380a01', 'Licence en Chimie', 'L-CHIM', 'LICENSE', 'Licence', 3, 'Français', TRUE, NOW(), NOW(), FALSE),
    ('40eebc99-9c0b-4ef8-bb6d-6bb9bd380a05', 0, '30eebc99-9c0b-4ef8-bb6d-6bb9bd380a02', 'Licence en Lettres Françaises', 'L-FRAN', 'LICENSE', 'Licence', 3, 'Français', TRUE, NOW(), NOW(), FALSE),
    -- Master programs
    ('40eebc99-9c0b-4ef8-bb6d-6bb9bd380a06', 0, '30eebc99-9c0b-4ef8-bb6d-6bb9bd380a01', 'Master en Informatique', 'M-INFO', 'MASTER', 'Master', 2, 'Français', TRUE, NOW(), NOW(), FALSE),
    ('40eebc99-9c0b-4ef8-bb6d-6bb9bd380a07', 0, '30eebc99-9c0b-4ef8-bb6d-6bb9bd380a01', 'Master en Intelligence Artificielle', 'M-IA', 'MASTER', 'Master', 2, 'Français', TRUE, NOW(), NOW(), FALSE),
    -- Engineering
    ('40eebc99-9c0b-4ef8-bb6d-6bb9bd380a08', 0, '30eebc99-9c0b-4ef8-bb6d-6bb9bd380a03', 'Licence en Sciences Juridiques', 'L-Droit', 'LICENSE', 'Licence', 3, 'Français', TRUE, NOW(), NOW(), FALSE);
