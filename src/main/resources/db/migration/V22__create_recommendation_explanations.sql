-- V22: Recommendation Explanations
CREATE TABLE recommendation_explanations (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version             BIGINT NOT NULL DEFAULT 0,
    recommendation_id   UUID NOT NULL,
    type                VARCHAR(30) NOT NULL,
    criterion           VARCHAR(100) NOT NULL,
    expected_value      VARCHAR(100) NOT NULL,
    actual_value        VARCHAR(100) NOT NULL,
    met                 BOOLEAN NOT NULL DEFAULT FALSE,
    impact              NUMERIC(5, 2),
    sort_order          INTEGER NOT NULL DEFAULT 1,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by          VARCHAR(100),
    updated_by          VARCHAR(100),
    deleted             BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT uk_explanation_recommendation_criterion UNIQUE (recommendation_id, criterion),
    CONSTRAINT fk_explanation_recommendation FOREIGN KEY (recommendation_id) REFERENCES recommendations(id),
    CONSTRAINT chk_explanation_impact CHECK (impact IS NULL OR (impact >= 0 AND impact <= 100)),
    CONSTRAINT chk_explanation_sort_order CHECK (sort_order > 0)
);

CREATE INDEX idx_explanation_recommendation ON recommendation_explanations (recommendation_id) WHERE deleted = FALSE;
CREATE INDEX idx_explanation_type ON recommendation_explanations (type) WHERE deleted = FALSE;
