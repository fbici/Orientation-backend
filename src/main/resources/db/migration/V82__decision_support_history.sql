CREATE TABLE decision_records (
 id uuid PRIMARY KEY DEFAULT gen_random_uuid(), version bigint NOT NULL DEFAULT 0,
 candidate_id uuid NOT NULL REFERENCES candidates(id), candidate_version bigint NOT NULL,
 actor_id uuid NOT NULL REFERENCES users(id),
 kind varchar(32) NOT NULL CHECK(kind IN ('SIMULATION','COMPARISON','SCHOLARSHIP','CHAT_SESSION','CHAT_MESSAGE')),
 parent_id uuid REFERENCES decision_records(id), payload jsonb NOT NULL,
 created_at timestamptz NOT NULL DEFAULT now(), updated_at timestamptz NOT NULL DEFAULT now(),
 created_by varchar(100), updated_by varchar(100), deleted boolean NOT NULL DEFAULT false
);
CREATE INDEX idx_decision_candidate_kind_date ON decision_records(candidate_id,kind,created_at DESC,id DESC) WHERE deleted=false;
CREATE INDEX idx_decision_parent_date ON decision_records(parent_id,created_at,id) WHERE deleted=false;
ALTER TABLE scholarship_criteria ADD COLUMN logical_group varchar(50) NOT NULL DEFAULT 'DEFAULT';
ALTER TABLE scholarship_criteria ADD COLUMN logical_operator varchar(5) NOT NULL DEFAULT 'AND' CHECK(logical_operator IN ('AND','OR'));
