-- V56: Seed Knowledge Nodes
INSERT INTO knowledge_nodes (id, version, node_type, name, entity_id, entity_type, properties, active, created_at, updated_at, deleted)
VALUES
    ('a0000000-0000-0000-0000-000000000001', 0, 'UNIVERSITY', 'Université Mohammed V', '10eebc99-9c0b-4ef8-bb6d-6bb9bd380a02', 'University', '{"ranking": 2, "country": "MAR"}', TRUE, NOW(), NOW(), FALSE),
    ('a0000000-0000-0000-0000-000000000002', 0, 'UNIVERSITY', 'Université Hassan II', '10eebc99-9c0b-4ef8-bb6d-6bb9bd380a01', 'University', '{"ranking": 1, "country": "MAR"}', TRUE, NOW(), NOW(), FALSE),
    ('a0000000-0000-0000-0000-000000000003', 0, 'PROGRAM', 'Licence Informatique', '40eebc99-9c0b-4ef8-bb6d-6bb9bd380a03', 'Program', '{"type": "LICENSE", "duration": 3}', TRUE, NOW(), NOW(), FALSE),
    ('a0000000-0000-0000-0000-000000000004', 0, 'PROGRAM', 'Licence Mathématiques', '40eebc99-9c0b-4ef8-bb6d-6bb9bd380a01', 'Program', '{"type": "LICENSE", "duration": 3}', TRUE, NOW(), NOW(), FALSE),
    ('a0000000-0000-0000-0000-000000000005', 0, 'COUNTRY', 'Maroc', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'Country', '{"code": "MAR"}', TRUE, NOW(), NOW(), FALSE);

INSERT INTO knowledge_relations (id, version, source_id, target_id, relation_type, weight, active, created_at, updated_at, deleted)
VALUES
    ('b0000000-0000-0000-0000-000000000001', 0, 'a0000000-0000-0000-0000-000000000002', 'a0000000-0000-0000-0000-000000000003', 'OFFERS', 1.0, TRUE, NOW(), NOW(), FALSE),
    ('b0000000-0000-0000-0000-000000000002', 0, 'a0000000-0000-0000-0000-000000000002', 'a0000000-0000-0000-0000-000000000004', 'OFFERS', 1.0, TRUE, NOW(), NOW(), FALSE),
    ('b0000000-0000-0000-0000-000000000003', 0, 'a0000000-0000-0000-0000-000000000001', 'a0000000-0000-0000-0000-000000000002', 'SIMILAR_TO', 0.8, TRUE, NOW(), NOW(), FALSE);
