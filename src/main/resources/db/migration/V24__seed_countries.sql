-- V24: Seed Data - Countries
INSERT INTO countries (id, version, name, code, official_name, phone_code, currency, active, created_at, updated_at, deleted)
VALUES
    ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 0, 'Maroc', 'MAR', 'Royaume du Maroc', '+212', 'MAD', TRUE, NOW(), NOW(), FALSE),
    ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 0, 'France', 'FRA', 'République Française', '+33', 'EUR', TRUE, NOW(), NOW(), FALSE),
    ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', 0, 'Tunisie', 'TUN', 'République Tunisienne', '+216', 'TND', TRUE, NOW(), NOW(), FALSE),
    ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a14', 0, 'Algérie', 'DZA', 'République Algérienne', '+213', 'DZD', TRUE, NOW(), NOW(), FALSE),
    ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a15', 0, 'Belgique', 'BEL', 'Royaume de Belgique', '+32', 'EUR', TRUE, NOW(), NOW(), FALSE),
    ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a16', 0, 'Canada', 'CAN', 'Canada', '+1', 'CAD', TRUE, NOW(), NOW(), FALSE),
    ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a17', 0, 'Sénégal', 'SEN', 'République du Sénégal', '+221', 'XOF', TRUE, NOW(), NOW(), FALSE),
    ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a18', 0, 'Côte d''Ivoire', 'CIV', 'République de Côte d''Ivoire', '+225', 'XOF', TRUE, NOW(), NOW(), FALSE);
