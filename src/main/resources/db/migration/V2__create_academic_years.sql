-- V2: Academic Years
CREATE TABLE academic_years (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version         BIGINT NOT NULL DEFAULT 0,
    label           VARCHAR(20) NOT NULL,
    start_date      DATE NOT NULL,
    end_date        DATE NOT NULL,
    active          BOOLEAN NOT NULL DEFAULT TRUE,
    current         BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by      VARCHAR(100),
    updated_by      VARCHAR(100),
    deleted         BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT uk_academic_year_label UNIQUE (label),
    CONSTRAINT chk_academic_year_dates CHECK (end_date > start_date)
);

CREATE INDEX idx_academic_year_active ON academic_years (active) WHERE deleted = FALSE;
CREATE INDEX idx_academic_year_current ON academic_years (current) WHERE current = TRUE AND deleted = FALSE;
CREATE INDEX idx_academic_year_dates ON academic_years (start_date, end_date);
