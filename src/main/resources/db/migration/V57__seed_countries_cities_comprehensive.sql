-- V57: Seed comprehensive countries, cities, academic years, grade scales
-- This migration ensures all dropdowns have data

-- ============================================================
-- COUNTRIES (upsert - skip if already exists)
-- ============================================================
INSERT INTO countries (id, version, name, code, official_name, phone_code, currency, active, created_at, updated_at, deleted)
VALUES
    -- Afrique
    ('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a01', 0, 'Bénin', 'BEN', 'République du Bénin', '+229', 'XOF', TRUE, NOW(), NOW(), FALSE),
    ('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a02', 0, 'Burkina Faso', 'BFA', 'Burkina Faso', '+226', 'XOF', TRUE, NOW(), NOW(), FALSE),
    ('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a03', 0, 'Cameroun', 'CMR', 'République du Cameroun', '+237', 'XAF', TRUE, NOW(), NOW(), FALSE),
    ('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a04', 0, 'Congo', 'COG', 'République du Congo', '+242', 'XAF', TRUE, NOW(), NOW(), FALSE),
    ('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a05', 0, 'Gabon', 'GAB', 'République Gabonaise', '+241', 'XAF', TRUE, NOW(), NOW(), FALSE),
    ('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a06', 0, 'Ghana', 'GHA', 'République du Ghana', '+233', 'GHS', TRUE, NOW(), NOW(), FALSE),
    ('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a07', 0, 'Guinée', 'GIN', 'République de Guinée', '+224', 'GNF', TRUE, NOW(), NOW(), FALSE),
    ('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a08', 0, 'Mali', 'MLI', 'République du Mali', '+223', 'XOF', TRUE, NOW(), NOW(), FALSE),
    ('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a09', 0, 'Niger', 'NER', 'République du Niger', '+227', 'XOF', TRUE, NOW(), NOW(), FALSE),
    ('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a10', 0, 'Nigeria', 'NGA', 'République Fédérale du Nigeria', '+234', 'NGN', TRUE, NOW(), NOW(), FALSE),
    ('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a19', 0, 'Togo', 'TGO', 'République Togolaise', '+228', 'XOF', TRUE, NOW(), NOW(), FALSE),
    ('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a20', 0, 'RD Congo', 'COD', 'République Démocratique du Congo', '+243', 'CDF', TRUE, NOW(), NOW(), FALSE),
    ('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a21', 0, 'Madagascar', 'MDG', 'République de Madagascar', '+261', 'MGA', TRUE, NOW(), NOW(), FALSE),
    ('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 0, 'Maurice', 'MUS', 'République de Maurice', '+230', 'MUR', TRUE, NOW(), NOW(), FALSE),
    -- Afrique du Nord
    ('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a23', 0, 'Égypte', 'EGY', 'République Arabe d''Égypte', '+20', 'EGP', TRUE, NOW(), NOW(), FALSE),
    ('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a24', 0, 'Libye', 'LBY', 'État de Libye', '+218', 'LYD', TRUE, NOW(), NOW(), FALSE),
    -- Europe
    ('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a25', 0, 'Allemagne', 'DEU', 'République Fédérale d''Allemagne', '+49', 'EUR', TRUE, NOW(), NOW(), FALSE),
    ('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a26', 0, 'Espagne', 'ESP', 'Royaume d''Espagne', '+34', 'EUR', TRUE, NOW(), NOW(), FALSE),
    ('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a27', 0, 'Italie', 'ITA', 'République Italienne', '+39', 'EUR', TRUE, NOW(), NOW(), FALSE),
    ('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a28', 0, 'Royaume-Uni', 'GBR', 'Royaume-Uni de Grande-Bretagne', '+44', 'GBP', TRUE, NOW(), NOW(), FALSE),
    ('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a29', 0, 'Portugal', 'PRT', 'République Portugaise', '+351', 'EUR', TRUE, NOW(), NOW(), FALSE),
    ('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a30', 0, 'Pologne', 'POL', 'République de Pologne', '+48', 'PLN', TRUE, NOW(), NOW(), FALSE),
    -- Amériques
    ('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a31', 0, 'États-Unis', 'USA', 'États-Unis d''Amérique', '+1', 'USD', TRUE, NOW(), NOW(), FALSE),
    ('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a32', 0, 'Brésil', 'BRA', 'République Fédérative du Brésil', '+55', 'BRL', TRUE, NOW(), NOW(), FALSE),
    -- Asie
    ('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a33', 0, 'Chine', 'CHN', 'République Populaire de Chine', '+86', 'CNY', TRUE, NOW(), NOW(), FALSE),
    ('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a34', 0, 'Japon', 'JPN', 'Japon', '+81', 'JPY', TRUE, NOW(), NOW(), FALSE),
    ('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a35', 0, 'Turquie', 'TUR', 'République de Turquie', '+90', 'TRY', TRUE, NOW(), NOW(), FALSE),
    ('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a36', 0, 'Arabie Saoudite', 'SAU', 'Royaume d''Arabie Saoudite', '+966', 'SAR', TRUE, NOW(), NOW(), FALSE),
    ('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a37', 0, 'Émirats Arabes Unis', 'ARE', 'Émirats Arabes Unis', '+971', 'AED', TRUE, NOW(), NOW(), FALSE)
ON CONFLICT (code) DO NOTHING;

-- ============================================================
-- CITIES (for all countries)
-- ============================================================
INSERT INTO cities (id, version, name, country_id, active, created_at, updated_at, deleted)
VALUES
    -- Bénin
    ('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a01', 0, 'Cotonou', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a01', TRUE, NOW(), NOW(), FALSE),
    ('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a02', 0, 'Porto-Novo', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a01', TRUE, NOW(), NOW(), FALSE),
    ('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a03', 0, 'Abomey-Calavi', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a01', TRUE, NOW(), NOW(), FALSE),
    ('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a04', 0, 'Parakou', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a01', TRUE, NOW(), NOW(), FALSE),
    ('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a05', 0, 'Natitingou', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a01', TRUE, NOW(), NOW(), FALSE),
    ('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a06', 0, 'Lokossa', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a01', TRUE, NOW(), NOW(), FALSE),
    -- Maroc
    ('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a07', 0, 'Casablanca', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', TRUE, NOW(), NOW(), FALSE),
    ('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a08', 0, 'Rabat', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', TRUE, NOW(), NOW(), FALSE),
    ('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a09', 0, 'Fès', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', TRUE, NOW(), NOW(), FALSE),
    ('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a10', 0, 'Marrakech', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', TRUE, NOW(), NOW(), FALSE),
    ('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 0, 'Tanger', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', TRUE, NOW(), NOW(), FALSE),
    ('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 0, 'Meknès', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', TRUE, NOW(), NOW(), FALSE),
    ('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', 0, 'Agadir', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', TRUE, NOW(), NOW(), FALSE),
    -- France
    ('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a14', 0, 'Paris', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', TRUE, NOW(), NOW(), FALSE),
    ('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a15', 0, 'Lyon', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', TRUE, NOW(), NOW(), FALSE),
    ('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a16', 0, 'Marseille', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', TRUE, NOW(), NOW(), FALSE),
    ('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a17', 0, 'Toulouse', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', TRUE, NOW(), NOW(), FALSE),
    ('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a18', 0, 'Bordeaux', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', TRUE, NOW(), NOW(), FALSE),
    ('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a19', 0, 'Lille', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', TRUE, NOW(), NOW(), FALSE),
    ('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a20', 0, 'Strasbourg', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', TRUE, NOW(), NOW(), FALSE),
    ('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a21', 0, 'Nice', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', TRUE, NOW(), NOW(), FALSE),
    ('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 0, 'Montpellier', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', TRUE, NOW(), NOW(), FALSE),
    ('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a23', 0, 'Nantes', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', TRUE, NOW(), NOW(), FALSE),
    -- Sénégal
    ('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a24', 0, 'Dakar', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a17', TRUE, NOW(), NOW(), FALSE),
    ('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a25', 0, 'Saint-Louis', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a17', TRUE, NOW(), NOW(), FALSE),
    ('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a26', 0, 'Thiès', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a17', TRUE, NOW(), NOW(), FALSE),
    -- Côte d'Ivoire
    ('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a27', 0, 'Abidjan', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a18', TRUE, NOW(), NOW(), FALSE),
    ('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a28', 0, 'Yamoussoukro', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a18', TRUE, NOW(), NOW(), FALSE),
    ('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a29', 0, 'Bouaké', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a18', TRUE, NOW(), NOW(), FALSE),
    -- Tunisie
    ('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a30', 0, 'Tunis', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', TRUE, NOW(), NOW(), FALSE),
    ('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a31', 0, 'Sfax', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', TRUE, NOW(), NOW(), FALSE),
    ('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a32', 0, 'Sousse', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', TRUE, NOW(), NOW(), FALSE),
    -- Canada
    ('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a33', 0, 'Montréal', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a16', TRUE, NOW(), NOW(), FALSE),
    ('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a34', 0, 'Toronto', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a16', TRUE, NOW(), NOW(), FALSE),
    ('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a35', 0, 'Ottawa', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a16', TRUE, NOW(), NOW(), FALSE),
    ('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a36', 0, 'Vancouver', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a16', TRUE, NOW(), NOW(), FALSE),
    ('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a37', 0, 'Québec', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a16', TRUE, NOW(), NOW(), FALSE),
    -- Belgique
    ('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a38', 0, 'Bruxelles', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a15', TRUE, NOW(), NOW(), FALSE),
    ('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a39', 0, 'Liège', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a15', TRUE, NOW(), NOW(), FALSE),
    ('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a40', 0, 'Louvain', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a15', TRUE, NOW(), NOW(), FALSE),
    -- Allemagne
    ('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a41', 0, 'Berlin', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a25', TRUE, NOW(), NOW(), FALSE),
    ('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a42', 0, 'Munich', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a25', TRUE, NOW(), NOW(), FALSE),
    ('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a43', 0, 'Hambourg', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a25', TRUE, NOW(), NOW(), FALSE),
    -- Espagne
    ('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a44', 0, 'Madrid', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a26', TRUE, NOW(), NOW(), FALSE),
    ('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a45', 0, 'Barcelone', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a26', TRUE, NOW(), NOW(), FALSE),
    -- Italie
    ('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a46', 0, 'Rome', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a27', TRUE, NOW(), NOW(), FALSE),
    ('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a47', 0, 'Milan', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a27', TRUE, NOW(), NOW(), FALSE),
    -- UK
    ('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a48', 0, 'Londres', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a28', TRUE, NOW(), NOW(), FALSE),
    ('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a49', 0, 'Manchester', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a28', TRUE, NOW(), NOW(), FALSE),
    ('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a50', 0, 'Édimbourg', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a28', TRUE, NOW(), NOW(), FALSE),
    -- USA
    ('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a51', 0, 'New York', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a31', TRUE, NOW(), NOW(), FALSE),
    ('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a52', 0, 'Boston', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a31', TRUE, NOW(), NOW(), FALSE),
    ('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a53', 0, 'San Francisco', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a31', TRUE, NOW(), NOW(), FALSE),
    -- Turquie
    ('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a54', 0, 'Istanbul', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a35', TRUE, NOW(), NOW(), FALSE),
    ('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a55', 0, 'Ankara', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a35', TRUE, NOW(), NOW(), FALSE),
    -- Arabie Saoudite
    ('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a56', 0, 'Riyad', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a36', TRUE, NOW(), NOW(), FALSE),
    ('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a57', 0, 'Djeddah', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a36', TRUE, NOW(), NOW(), FALSE),
    -- Émirats
    ('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a58', 0, 'Dubaï', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a37', TRUE, NOW(), NOW(), FALSE),
    ('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a59', 0, 'Abu Dhabi', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a37', TRUE, NOW(), NOW(), FALSE),
    -- Burkina Faso
    ('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a60', 0, 'Ouagadougou', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a02', TRUE, NOW(), NOW(), FALSE),
    -- Cameroun
    ('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a61', 0, 'Yaoundé', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a03', TRUE, NOW(), NOW(), FALSE),
    ('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a62', 0, 'Douala', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a03', TRUE, NOW(), NOW(), FALSE),
    -- Ghana
    ('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a63', 0, 'Accra', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a06', TRUE, NOW(), NOW(), FALSE),
    -- Mali
    ('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a64', 0, 'Bamako', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a08', TRUE, NOW(), NOW(), FALSE),
    -- Nigeria
    ('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a65', 0, 'Lagos', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a10', TRUE, NOW(), NOW(), FALSE),
    ('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a66', 0, 'Abuja', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a10', TRUE, NOW(), NOW(), FALSE),
    -- Togo
    ('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a67', 0, 'Lomé', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a19', TRUE, NOW(), NOW(), FALSE)
ON CONFLICT (name, country_id) DO NOTHING;

-- ============================================================
-- ACADEMIC YEARS
-- ============================================================
INSERT INTO academic_years (id, version, name, start_date, end_date, active, created_at, updated_at, deleted)
VALUES
    ('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380a01', 0, '2024-2025', '2024-09-01', '2025-06-30', TRUE, NOW(), NOW(), FALSE),
    ('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380a02', 0, '2025-2026', '2025-09-01', '2026-06-30', TRUE, NOW(), NOW(), FALSE),
    ('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380a03', 0, '2026-2027', '2026-09-01', '2027-06-30', TRUE, NOW(), NOW(), FALSE)
ON CONFLICT DO NOTHING;

-- ============================================================
-- SUBJECTS
-- ============================================================
INSERT INTO subjects (id, version, name, code, category, active, created_at, updated_at, deleted)
VALUES
    ('e0eebc99-9c0b-4ef8-bb6d-6bb9bd380a01', 0, 'Mathématiques', 'MATH', 'SCIENCE', TRUE, NOW(), NOW(), FALSE),
    ('e0eebc99-9c0b-4ef8-bb6d-6bb9bd380a02', 0, 'Physique', 'PHYS', 'SCIENCE', TRUE, NOW(), NOW(), FALSE),
    ('e0eebc99-9c0b-4ef8-bb6d-6bb9bd380a03', 0, 'Chimie', 'CHIM', 'SCIENCE', TRUE, NOW(), NOW(), FALSE),
    ('e0eebc99-9c0b-4ef8-bb6d-6bb9bd380a04', 0, 'Sciences de la Vie et de la Terre', 'SVT', 'SCIENCE', TRUE, NOW(), NOW(), FALSE),
    ('e0eebc99-9c0b-4ef8-bb6d-6bb9bd380a05', 0, 'Informatique', 'INFO', 'SCIENCE', TRUE, NOW(), NOW(), FALSE),
    ('e0eebc99-9c0b-4ef8-bb6d-6bb9bd380a06', 0, 'Français', 'FRAN', 'LANGUE', TRUE, NOW(), NOW(), FALSE),
    ('e0eebc99-9c0b-4ef8-bb6d-6bb9bd380a07', 0, 'Anglais', 'ANGL', 'LANGUE', TRUE, NOW(), NOW(), FALSE),
    ('e0eebc99-9c0b-4ef8-bb6d-6bb9bd380a08', 0, 'Arabe', 'ARAB', 'LANGUE', TRUE, NOW(), NOW(), FALSE),
    ('e0eebc99-9c0b-4ef8-bb6d-6bb9bd380a09', 0, 'Espagnol', 'ESPA', 'LANGUE', TRUE, NOW(), NOW(), FALSE),
    ('e0eebc99-9c0b-4ef8-bb6d-6bb9bd380a10', 0, 'Philosophie', 'PHIL', 'HUMANITE', TRUE, NOW(), NOW(), FALSE),
    ('e0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 0, 'Histoire', 'HIST', 'HUMANITE', TRUE, NOW(), NOW(), FALSE),
    ('e0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 0, 'Géographie', 'GEOG', 'HUMANITE', TRUE, NOW(), NOW(), FALSE),
    ('e0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', 0, 'Économie', 'ECON', 'SOCIAL', TRUE, NOW(), NOW(), FALSE),
    ('e0eebc99-9c0b-4ef8-bb6d-6bb9bd380a14', 0, 'Droit', 'DROI', 'SOCIAL', TRUE, NOW(), NOW(), FALSE),
    ('e0eebc99-9c0b-4ef8-bb6d-6bb9bd380a15', 0, 'Biologie', 'BIOL', 'SCIENCE', TRUE, NOW(), NOW(), FALSE),
    ('e0eebc99-9c0b-4ef8-bb6d-6bb9bd380a16', 0, 'Géologie', 'GEOL', 'SCIENCE', TRUE, NOW(), NOW(), FALSE)
ON CONFLICT DO NOTHING;
