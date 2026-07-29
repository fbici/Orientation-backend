-- V13: Guide Versions
CREATE TABLE guide_versions (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version                 BIGINT NOT NULL DEFAULT 0,
    orientation_guide_id    UUID NOT NULL,
    version_number          INTEGER NOT NULL,
    version_label           VARCHAR(50),
    status                  VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    effective_date          DATE NOT NULL,
    expiry_date             DATE,
    active                  BOOLEAN NOT NULL DEFAULT TRUE,
    notes                   TEXT,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by              VARCHAR(100),
    updated_by              VARCHAR(100),
    deleted                 BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT uk_guide_version_number UNIQUE (orientation_guide_id, version_number),
    CONSTRAINT fk_guide_version_guide FOREIGN KEY (orientation_guide_id) REFERENCES orientation_guides(id),
    CONSTRAINT chk_guide_version_expiry CHECK (expiry_date IS NULL OR expiry_date > effective_date)
);

CREATE INDEX idx_guide_version_guide ON guide_versions (orientation_guide_id) WHERE deleted = FALSE;
CREATE INDEX idx_guide_version_status ON guide_versions (status) WHERE deleted = FALSE;
CREATE INDEX idx_guide_version_active ON guide_versions (active) WHERE active = TRUE AND deleted = FALSE;
