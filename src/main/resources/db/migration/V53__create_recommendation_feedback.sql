-- V53: Recommendation Feedback
CREATE TABLE knowledge_recommendation_feedback (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version         BIGINT NOT NULL DEFAULT 0,
    recommendation_id UUID NOT NULL,
    candidate_id    UUID NOT NULL,
    program_id      UUID NOT NULL,
    feedback_type   VARCHAR(20) NOT NULL,
    rating          NUMERIC(5, 2),
    comment         VARCHAR(500),
    helpful         BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by      VARCHAR(100),
    updated_by      VARCHAR(100),
    deleted         BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_krf_candidate ON knowledge_recommendation_feedback (candidate_id) WHERE deleted = FALSE;
CREATE INDEX idx_krf_program ON knowledge_recommendation_feedback (program_id) WHERE deleted = FALSE;
CREATE INDEX idx_krf_type ON knowledge_recommendation_feedback (feedback_type) WHERE deleted = FALSE;
