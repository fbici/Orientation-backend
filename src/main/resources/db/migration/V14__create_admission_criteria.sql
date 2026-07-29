-- V14: Admission Criteria
CREATE TABLE admission_criteria (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version                 BIGINT NOT NULL DEFAULT 0,
    guide_version_id        UUID NOT NULL,
    program_id              UUID,
    faculty_id              UUID,
    criterion_type          VARCHAR(30) NOT NULL,
    operator                VARCHAR(30) NOT NULL,
    subject_id              UUID,
    min_value               NUMERIC(10, 2),
    max_value               NUMERIC(10, 2),
    string_value            VARCHAR(200),
    logical_group           VARCHAR(50) NOT NULL DEFAULT 'DEFAULT',
    logical_operator        VARCHAR(5) NOT NULL DEFAULT 'AND',
    priority                INTEGER NOT NULL DEFAULT 1,
    weight                  NUMERIC(5, 2),
    description             VARCHAR(500),
    mandatory               BOOLEAN NOT NULL DEFAULT TRUE,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by              VARCHAR(100),
    updated_by              VARCHAR(100),
    deleted                 BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT fk_admission_criterion_guide_version FOREIGN KEY (guide_version_id) REFERENCES guide_versions(id),
    CONSTRAINT fk_admission_criterion_program FOREIGN KEY (program_id) REFERENCES programs(id),
    CONSTRAINT fk_admission_criterion_faculty FOREIGN KEY (faculty_id) REFERENCES faculties(id),
    CONSTRAINT fk_admission_criterion_subject FOREIGN KEY (subject_id) REFERENCES subjects(id),
    CONSTRAINT chk_admission_criterion_min_max CHECK (min_value IS NULL OR max_value IS NULL OR min_value < max_value),
    CONSTRAINT chk_admission_criterion_priority CHECK (priority > 0),
    CONSTRAINT chk_admission_criterion_weight CHECK (weight IS NULL OR (weight >= 0 AND weight <= 1)),
    CONSTRAINT chk_admission_criterion_logical CHECK (logical_operator IN ('AND', 'OR'))
);

CREATE INDEX idx_admission_criterion_guide_version ON admission_criteria (guide_version_id) WHERE deleted = FALSE;
CREATE INDEX idx_admission_criterion_program ON admission_criteria (program_id) WHERE program_id IS NOT NULL AND deleted = FALSE;
CREATE INDEX idx_admission_criterion_faculty ON admission_criteria (faculty_id) WHERE faculty_id IS NOT NULL AND deleted = FALSE;
