-- V16: Scholarship Criteria
CREATE TABLE scholarship_criteria (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version                 BIGINT NOT NULL DEFAULT 0,
    guide_version_id        UUID NOT NULL,
    scholarship_id          UUID NOT NULL,
    criterion_type          VARCHAR(30) NOT NULL,
    operator                VARCHAR(30) NOT NULL,
    subject_id              UUID,
    min_value               NUMERIC(10, 2),
    max_value               NUMERIC(10, 2),
    string_value            VARCHAR(200),
    description             VARCHAR(500),
    mandatory               BOOLEAN NOT NULL DEFAULT TRUE,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by              VARCHAR(100),
    updated_by              VARCHAR(100),
    deleted                 BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT fk_scholarship_criterion_guide_version FOREIGN KEY (guide_version_id) REFERENCES guide_versions(id),
    CONSTRAINT fk_scholarship_criterion_scholarship FOREIGN KEY (scholarship_id) REFERENCES scholarships(id),
    CONSTRAINT fk_scholarship_criterion_subject FOREIGN KEY (subject_id) REFERENCES subjects(id),
    CONSTRAINT chk_scholarship_criterion_min_max CHECK (min_value IS NULL OR max_value IS NULL OR min_value < max_value)
);

CREATE INDEX idx_scholarship_criterion_guide_version ON scholarship_criteria (guide_version_id) WHERE deleted = FALSE;
CREATE INDEX idx_scholarship_criterion_scholarship ON scholarship_criteria (scholarship_id) WHERE deleted = FALSE;
