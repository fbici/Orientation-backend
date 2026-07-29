-- V28: Seed Data - Scholarship Types (Reference data via comments, types are in enums)
-- This migration ensures scholarship type reference data is available
-- Scholarship types are managed via Java enums: ScholarshipType

-- Seed sample cities for Morocco
INSERT INTO cities (id, version, name, country_id, postal_code, active, created_at, updated_at, deleted)
VALUES
    ('f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a01', 0, 'Casablanca', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '20000', TRUE, NOW(), NOW(), FALSE),
    ('f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a02', 0, 'Rabat', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '10000', TRUE, NOW(), NOW(), FALSE),
    ('f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a03', 0, 'Marrakech', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '40000', TRUE, NOW(), NOW(), FALSE),
    ('f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a04', 0, 'Fès', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '30000', TRUE, NOW(), NOW(), FALSE),
    ('f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a05', 0, 'Tanger', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '90000', TRUE, NOW(), NOW(), FALSE),
    ('f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a06', 0, 'Meknès', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '50000', TRUE, NOW(), NOW(), FALSE),
    ('f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a07', 0, 'Agadir', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '80000', TRUE, NOW(), NOW(), FALSE),
    ('f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a08', 0, 'Oujda', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '60000', TRUE, NOW(), NOW(), FALSE);
