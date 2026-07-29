-- V29: Seed Data - Sample Universities (Morocco)
INSERT INTO universities (id, version, name, short_name, country_id, city_id, status, ranking, active, created_at, updated_at, deleted)
VALUES
    ('10eebc99-9c0b-4ef8-bb6d-6bb9bd380a01', 0, 'Université Hassan II de Casablanca', 'UH2C', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a01', 'ACTIVE', 1, TRUE, NOW(), NOW(), FALSE),
    ('10eebc99-9c0b-4ef8-bb6d-6bb9bd380a02', 0, 'Université Mohammed V de Rabat', 'UM5', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a02', 'ACTIVE', 2, TRUE, NOW(), NOW(), FALSE),
    ('10eebc99-9c0b-4ef8-bb6d-6bb9bd380a03', 0, 'Université Cadi Ayyad de Marrakech', 'UCA', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a03', 'ACTIVE', 3, TRUE, NOW(), NOW(), FALSE),
    ('10eebc99-9c0b-4ef8-bb6d-6bb9bd380a04', 0, 'Université Sidi Mohamed Ben Abdellah de Fès', 'USMBA', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a04', 'ACTIVE', 4, TRUE, NOW(), NOW(), FALSE),
    ('10eebc99-9c0b-4ef8-bb6d-6bb9bd380a05', 0, 'Université Abdelmalek Essaâdi de Tanger', 'UAE', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a05', 'ACTIVE', 5, TRUE, NOW(), NOW(), FALSE);
