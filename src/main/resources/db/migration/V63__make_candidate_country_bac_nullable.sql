-- V63: Make country_id and bac_year optional for candidates (registration flow)
-- These fields will be filled later when the candidate completes their profile

ALTER TABLE candidates ALTER COLUMN country_id DROP NOT NULL;
ALTER TABLE candidates ALTER COLUMN bac_year DROP NOT NULL;
ALTER TABLE candidates DROP CONSTRAINT IF EXISTS chk_candidate_bac_year;
