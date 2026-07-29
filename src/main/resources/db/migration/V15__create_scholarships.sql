-- V15: Scholarships
CREATE TABLE scholarships (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version             BIGINT NOT NULL DEFAULT 0,
    name                VARCHAR(200) NOT NULL,
    country_id          UUID NOT NULL,
    academic_year_id    UUID NOT NULL,
    type                VARCHAR(30) NOT NULL,
    provider            VARCHAR(200),
    description         TEXT,
    coverage            VARCHAR(500),
    amount              NUMERIC(12, 2),
    currency            VARCHAR(3),
    duration            INTEGER,
    deadline            DATE,
    application_url     VARCHAR(500),
    total_slots         INTEGER,
    remaining_slots     INTEGER,
    status              VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    government          BOOLEAN NOT NULL DEFAULT FALSE,
    active              BOOLEAN NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by          VARCHAR(100),
    updated_by          VARCHAR(100),
    deleted             BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT uk_scholarship_name_country_year UNIQUE (name, country_id, academic_year_id),
    CONSTRAINT fk_scholarship_country FOREIGN KEY (country_id) REFERENCES countries(id),
    CONSTRAINT fk_scholarship_academic_year FOREIGN KEY (academic_year_id) REFERENCES academic_years(id),
    CONSTRAINT chk_scholarship_amount CHECK (amount IS NULL OR amount >= 0),
    CONSTRAINT chk_scholarship_slots CHECK (total_slots IS NULL OR total_slots >= 0),
    CONSTRAINT chk_scholarship_remaining CHECK (remaining_slots IS NULL OR (total_slots IS NOT NULL AND remaining_slots <= total_slots))
);

CREATE INDEX idx_scholarship_country_year ON scholarships (country_id, academic_year_id) WHERE deleted = FALSE;
CREATE INDEX idx_scholarship_type ON scholarships (type) WHERE deleted = FALSE;
CREATE INDEX idx_scholarship_status ON scholarships (status) WHERE deleted = FALSE;
CREATE INDEX idx_scholarship_deadline ON scholarships (deadline) WHERE deadline IS NOT NULL AND deleted = FALSE;
