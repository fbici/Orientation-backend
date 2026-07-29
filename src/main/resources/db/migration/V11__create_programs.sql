-- V11: Programs
CREATE TABLE programs (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version         BIGINT NOT NULL DEFAULT 0,
    faculty_id      UUID NOT NULL,
    name            VARCHAR(200) NOT NULL,
    code            VARCHAR(30),
    type            VARCHAR(30) NOT NULL,
    degree          VARCHAR(100),
    duration        INTEGER NOT NULL,
    language        VARCHAR(50),
    description     TEXT,
    objectives      TEXT,
    prerequisites   TEXT,
    max_students    INTEGER,
    tuition_fee     NUMERIC(12, 2),
    available       BOOLEAN NOT NULL DEFAULT TRUE,
    website         VARCHAR(500),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by      VARCHAR(100),
    updated_by      VARCHAR(100),
    deleted         BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT uk_program_name_faculty UNIQUE (name, faculty_id),
    CONSTRAINT fk_program_faculty FOREIGN KEY (faculty_id) REFERENCES faculties(id),
    CONSTRAINT chk_program_duration CHECK (duration > 0),
    CONSTRAINT chk_program_max_students CHECK (max_students IS NULL OR max_students > 0),
    CONSTRAINT chk_program_tuition CHECK (tuition_fee IS NULL OR tuition_fee >= 0)
);

CREATE INDEX idx_program_faculty ON programs (faculty_id) WHERE deleted = FALSE;
CREATE INDEX idx_program_type ON programs (type) WHERE deleted = FALSE;
CREATE INDEX idx_program_available ON programs (available) WHERE available = TRUE AND deleted = FALSE;
