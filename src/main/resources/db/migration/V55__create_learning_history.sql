-- V55: Learning History
CREATE TABLE knowledge_learning_history (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version         BIGINT NOT NULL DEFAULT 0,
    candidate_id    UUID NOT NULL,
    program_id      UUID NOT NULL,
    event           VARCHAR(20) NOT NULL,
    score_before    NUMERIC(5, 2),
    score_after     NUMERIC(5, 2),
    context         JSONB,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by      VARCHAR(100),
    updated_by      VARCHAR(100),
    deleted         BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_klh_candidate ON knowledge_learning_history (candidate_id) WHERE deleted = FALSE;
CREATE INDEX idx_klh_program ON knowledge_learning_history (program_id) WHERE deleted = FALSE;
CREATE INDEX idx_klh_event ON knowledge_learning_history (event) WHERE deleted = FALSE;
