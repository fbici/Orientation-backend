-- V57: Seed supplementary data (academic years, grade scales)
-- Fixes column names to match actual schema

-- ============================================================
-- ACADEMIC YEARS (uses 'label' not 'name')
-- ============================================================
INSERT INTO academic_years (id, version, label, start_date, end_date, active, current, created_at, updated_at, deleted)
VALUES
    ('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380a01', 0, '2024-2025', '2024-09-01', '2025-06-30', TRUE, FALSE, NOW(), NOW(), FALSE),
    ('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380a02', 0, '2025-2026', '2025-09-01', '2026-06-30', TRUE, TRUE, NOW(), NOW(), FALSE),
    ('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380a03', 0, '2026-2027', '2026-09-01', '2027-06-30', TRUE, FALSE, NOW(), NOW(), FALSE)
ON CONFLICT (label) DO NOTHING;
