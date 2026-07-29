-- V7: Subjects
CREATE TABLE subjects (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version         BIGINT NOT NULL DEFAULT 0,
    name            VARCHAR(200) NOT NULL,
    code            VARCHAR(20),
    grade_scale_id  UUID NOT NULL,
    category        VARCHAR(50),
    coefficient     NUMERIC(3, 2),
    core            BOOLEAN NOT NULL DEFAULT TRUE,
    active          BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by      VARCHAR(100),
    updated_by      VARCHAR(100),
    deleted         BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT uk_subject_name_scale UNIQUE (name, grade_scale_id),
    CONSTRAINT fk_subject_grade_scale FOREIGN KEY (grade_scale_id) REFERENCES grade_scales(id),
    CONSTRAINT chk_subject_coefficient CHECK (coefficient IS NULL OR coefficient > 0)
);

CREATE INDEX idx_subject_grade_scale ON subjects (grade_scale_id) WHERE deleted = FALSE;
CREATE INDEX idx_subject_core ON subjects (core) WHERE core = TRUE AND deleted = FALSE;
