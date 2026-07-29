-- V5: Grade Scales
CREATE TABLE grade_scales (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version             BIGINT NOT NULL DEFAULT 0,
    name                VARCHAR(100) NOT NULL,
    country_id          UUID NOT NULL,
    academic_year_id    UUID NOT NULL,
    min_score           NUMERIC(5, 2) NOT NULL,
    max_score           NUMERIC(5, 2) NOT NULL,
    passing_score       NUMERIC(5, 2) NOT NULL,
    normalize_to        NUMERIC(5, 2) NOT NULL,
    description         VARCHAR(500),
    active              BOOLEAN NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by          VARCHAR(100),
    updated_by          VARCHAR(100),
    deleted             BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT uk_grade_scale_country_year UNIQUE (country_id, academic_year_id),
    CONSTRAINT fk_grade_scale_country FOREIGN KEY (country_id) REFERENCES countries(id),
    CONSTRAINT fk_grade_scale_academic_year FOREIGN KEY (academic_year_id) REFERENCES academic_years(id),
    CONSTRAINT chk_grade_scale_scores CHECK (min_score < max_score),
    CONSTRAINT chk_grade_scale_passing CHECK (passing_score >= min_score AND passing_score <= max_score)
);

CREATE INDEX idx_grade_scale_country ON grade_scales (country_id) WHERE deleted = FALSE;
