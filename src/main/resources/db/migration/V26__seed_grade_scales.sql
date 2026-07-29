-- V26: Seed Data - Grade Scales (Maroc 0-20)
INSERT INTO grade_scales (id, version, name, country_id, academic_year_id, min_score, max_score, passing_score, normalize_to, description, active, created_at, updated_at, deleted)
VALUES
    ('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a01', 0, 'Système Marocain 0-20', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a02', 0, 20, 10, 20, 'Système de notation marocain standard 0-20', TRUE, NOW(), NOW(), FALSE);

-- Grade Scale Items for Moroccan system
INSERT INTO grade_scale_items (id, version, grade_scale_id, raw_grade, raw_value, normalized_value, normalized_gpa, label, sort_order, created_at, updated_at, deleted)
VALUES
    ('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380a01', 0, 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a01', '0', 0, 0, 0.0, 'Très Insuffisant', 1, NOW(), NOW(), FALSE),
    ('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380a02', 0, 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a01', '1', 1, 1, 0.2, 'Très Insuffisant', 2, NOW(), NOW(), FALSE),
    ('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380a03', 0, 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a01', '2', 2, 2, 0.4, 'Très Insuffisant', 3, NOW(), NOW(), FALSE),
    ('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380a04', 0, 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a01', '3', 3, 3, 0.6, 'Insuffisant', 4, NOW(), NOW(), FALSE),
    ('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380a05', 0, 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a01', '4', 4, 4, 0.8, 'Insuffisant', 5, NOW(), NOW(), FALSE),
    ('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380a06', 0, 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a01', '5', 5, 5, 1.0, 'Insuffisant', 6, NOW(), NOW(), FALSE),
    ('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380a07', 0, 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a01', '6', 6, 6, 1.2, 'Passable', 7, NOW(), NOW(), FALSE),
    ('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380a08', 0, 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a01', '7', 7, 7, 1.4, 'Passable', 8, NOW(), NOW(), FALSE),
    ('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380a09', 0, 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a01', '8', 8, 8, 1.6, 'Passable', 9, NOW(), NOW(), FALSE),
    ('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380a10', 0, 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a01', '9', 9, 9, 1.8, 'Passable', 10, NOW(), NOW(), FALSE),
    ('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 0, 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a01', '10', 10, 10, 2.0, 'Assez Bien', 11, NOW(), NOW(), FALSE),
    ('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 0, 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a01', '11', 11, 11, 2.2, 'Assez Bien', 12, NOW(), NOW(), FALSE),
    ('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', 0, 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a01', '12', 12, 12, 2.4, 'Bien', 13, NOW(), NOW(), FALSE),
    ('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380a14', 0, 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a01', '13', 13, 13, 2.6, 'Bien', 14, NOW(), NOW(), FALSE),
    ('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380a15', 0, 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a01', '14', 14, 14, 2.8, 'Très Bien', 15, NOW(), NOW(), FALSE),
    ('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380a16', 0, 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a01', '15', 15, 15, 3.0, 'Très Bien', 16, NOW(), NOW(), FALSE),
    ('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380a17', 0, 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a01', '16', 16, 16, 3.2, 'Excellent', 17, NOW(), NOW(), FALSE),
    ('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380a18', 0, 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a01', '17', 17, 17, 3.4, 'Excellent', 18, NOW(), NOW(), FALSE),
    ('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380a19', 0, 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a01', '18', 18, 18, 3.6, 'Excellent', 19, NOW(), NOW(), FALSE),
    ('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380a20', 0, 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a01', '19', 19, 19, 3.8, 'Excellent', 20, NOW(), NOW(), FALSE),
    ('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380a21', 0, 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a01', '20', 20, 20, 4.0, 'Excellent', 21, NOW(), NOW(), FALSE);
