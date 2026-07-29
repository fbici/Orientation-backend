-- V31: Seed Data - Sample Faculties
INSERT INTO faculties (id, version, campus_id, name, code, active, created_at, updated_at, deleted)
VALUES
    ('30eebc99-9c0b-4ef8-bb6d-6bb9bd380a01', 0, '20eebc99-9c0b-4ef8-bb6d-6bb9bd380a01', 'Faculté des Sciences', 'FS', TRUE, NOW(), NOW(), FALSE),
    ('30eebc99-9c0b-4ef8-bb6d-6bb9bd380a02', 0, '20eebc99-9c0b-4ef8-bb6d-6bb9bd380a01', 'Faculté des Lettres et des Sciences Humaines', 'FLSH', TRUE, NOW(), NOW(), FALSE),
    ('30eebc99-9c0b-4ef8-bb6d-6bb9bd380a03', 0, '20eebc99-9c0b-4ef8-bb6d-6bb9bd380a02', 'Faculté des Sciences', 'FS-RABAT', TRUE, NOW(), NOW(), FALSE),
    ('30eebc99-9c0b-4ef8-bb6d-6bb9bd380a04', 0, '20eebc99-9c0b-4ef8-bb6d-6bb9bd380a02', 'Faculté de Droit et des Sciences Économiques', 'FDSE', TRUE, NOW(), NOW(), FALSE);
