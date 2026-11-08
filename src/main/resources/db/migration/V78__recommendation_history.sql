-- Preserve previous recommendations and permit unknown values.
ALTER TABLE recommendations DROP CONSTRAINT uk_recommendation_candidate_guide_program;
ALTER TABLE recommendations ALTER COLUMN transcript_id DROP NOT NULL;
ALTER TABLE recommendations ALTER COLUMN guide_version_id DROP NOT NULL;
ALTER TABLE recommendations ALTER COLUMN match_score DROP NOT NULL;
ALTER TABLE recommendations ADD COLUMN run_id uuid;
ALTER TABLE recommendations ADD COLUMN candidate_version bigint;
ALTER TABLE recommendations ADD COLUMN eligibility_status varchar(30);
ALTER TABLE recommendations ADD COLUMN result_payload jsonb;
ALTER TABLE recommendations ADD CONSTRAINT uk_recommendation_run_program UNIQUE(run_id,program_id);
ALTER TABLE recommendations ADD CONSTRAINT chk_recommendation_eligibility
 CHECK(eligibility_status IS NULL OR eligibility_status IN ('ELIGIBLE','NOT_ELIGIBLE','INSUFFICIENT_DATA'));
CREATE INDEX idx_recommendation_runs ON recommendations(candidate_id,recommended_at DESC,run_id) WHERE deleted=false;
