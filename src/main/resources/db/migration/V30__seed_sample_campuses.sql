-- V30: Seed Data - Sample Campuses
INSERT INTO campuses (id, version, university_id, name, city_id, main, active, created_at, updated_at, deleted)
VALUES
    ('20eebc99-9c0b-4ef8-bb6d-6bb9bd380a01', 0, '10eebc99-9c0b-4ef8-bb6d-6bb9bd380a01', 'Campus Principal Ben M''Sick', 'f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a01', TRUE, TRUE, NOW(), NOW(), FALSE),
    ('20eebc99-9c0b-4ef8-bb6d-6bb9bd380a02', 0, '10eebc99-9c0b-4ef8-bb6d-6bb9bd380a02', 'Campus Principal Rabat', 'f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a02', TRUE, TRUE, NOW(), NOW(), FALSE),
    ('20eebc99-9c0b-4ef8-bb6d-6bb9bd380a03', 0, '10eebc99-9c0b-4ef8-bb6d-6bb9bd380a03', 'Campus Principal Marrakech', 'f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a03', TRUE, TRUE, NOW(), NOW(), FALSE);
