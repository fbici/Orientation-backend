-- V21: Recommendations
CREATE TABLE recommendations (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version                 BIGINT NOT NULL DEFAULT 0,
    candidate_id            UUID NOT NULL,
    guide_version_id        UUID NOT NULL,
    program_id              UUID NOT NULL,
    transcript_id           UUID NOT NULL,
    match_score             NUMERIC(5, 2) NOT NULL,
    confidence_score        NUMERIC(5, 2) NOT NULL,
    rank                    INTEGER NOT NULL,
    status                  VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    eligible                BOOLEAN NOT NULL DEFAULT TRUE,
    admission_probability   NUMERIC(5, 2),
    recommended_at          TIMESTAMPTZ NOT NULL,
    expires_at              TIMESTAMPTZ,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by              VARCHAR(100),
    updated_by              VARCHAR(100),
    deleted                 BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT uk_recommendation_candidate_guide_program UNIQUE (candidate_id, guide_version_id, program_id),
    CONSTRAINT fk_recommendation_candidate FOREIGN KEY (candidate_id) REFERENCES candidates(id),
    CONSTRAINT fk_recommendation_guide_version FOREIGN KEY (guide_version_id) REFERENCES guide_versions(id),
    CONSTRAINT fk_recommendation_program FOREIGN KEY (program_id) REFERENCES programs(id),
    CONSTRAINT fk_recommendation_transcript FOREIGN KEY (transcript_id) REFERENCES transcripts(id),
    CONSTRAINT chk_recommendation_match_score CHECK (match_score >= 0 AND match_score <= 100),
    CONSTRAINT chk_recommendation_confidence CHECK (confidence_score >= 0 AND confidence_score <= 100),
    CONSTRAINT chk_recommendation_rank CHECK (rank > 0),
    CONSTRAINT chk_recommendation_probability CHECK (admission_probability IS NULL OR (admission_probability >= 0 AND admission_probability <= 100)),
    CONSTRAINT chk_recommendation_expires CHECK (expires_at IS NULL OR recommended_at IS NULL OR expires_at > recommended_at)
);

CREATE INDEX idx_recommendation_candidate ON recommendations (candidate_id) WHERE deleted = FALSE;
CREATE INDEX idx_recommendation_guide_version ON recommendations (guide_version_id) WHERE deleted = FALSE;
CREATE INDEX idx_recommendation_program ON recommendations (program_id) WHERE deleted = FALSE;
CREATE INDEX idx_recommendation_transcript ON recommendations (transcript_id) WHERE deleted = FALSE;
CREATE INDEX idx_recommendation_status ON recommendations (status) WHERE deleted = FALSE;
CREATE INDEX idx_recommendation_score ON recommendations (match_score DESC) WHERE deleted = FALSE;
