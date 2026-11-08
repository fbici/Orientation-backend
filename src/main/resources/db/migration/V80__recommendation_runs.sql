CREATE TABLE recommendation_runs (
 id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
 version bigint NOT NULL DEFAULT 0,
 candidate_id uuid NOT NULL REFERENCES candidates(id),
 candidate_version bigint NOT NULL,
 computed_at timestamptz NOT NULL,
 total_programs integer NOT NULL CHECK(total_programs>=0),
 created_at timestamptz NOT NULL DEFAULT now(), updated_at timestamptz NOT NULL DEFAULT now(),
 created_by varchar(100), updated_by varchar(100), deleted boolean NOT NULL DEFAULT false
);
INSERT INTO recommendation_runs(id,candidate_id,candidate_version,computed_at,total_programs)
 SELECT run_id,candidate_id,COALESCE(candidate_version,0),min(recommended_at),count(*)::integer
 FROM recommendations WHERE run_id IS NOT NULL GROUP BY run_id,candidate_id,candidate_version;
ALTER TABLE recommendations ADD CONSTRAINT fk_recommendation_run FOREIGN KEY(run_id) REFERENCES recommendation_runs(id);
CREATE INDEX idx_recommendation_run_candidate ON recommendation_runs(candidate_id,computed_at DESC) WHERE deleted=false;
