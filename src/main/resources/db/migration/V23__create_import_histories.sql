-- V23: Import Histories
CREATE TABLE import_histories (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version             BIGINT NOT NULL DEFAULT 0,
    guide_version_id    UUID,
    import_type         VARCHAR(50) NOT NULL,
    source              VARCHAR(200) NOT NULL,
    file_name           VARCHAR(255),
    file_url            VARCHAR(500),
    status              VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    total_records       INTEGER,
    processed_records   INTEGER,
    success_records     INTEGER,
    failed_records      INTEGER,
    error_log           TEXT,
    imported_by         VARCHAR(255),
    started_at          TIMESTAMPTZ NOT NULL,
    completed_at        TIMESTAMPTZ,
    duration            BIGINT,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by          VARCHAR(100),
    updated_by          VARCHAR(100),
    deleted             BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT fk_import_history_guide_version FOREIGN KEY (guide_version_id) REFERENCES guide_versions(id),
    CONSTRAINT chk_import_history_records CHECK (total_records IS NULL OR total_records >= 0),
    CONSTRAINT chk_import_history_processed CHECK (processed_records IS NULL OR total_records IS NULL OR processed_records <= total_records),
    CONSTRAINT chk_import_history_duration CHECK (duration IS NULL OR duration >= 0)
);

CREATE INDEX idx_import_history_guide_version ON import_histories (guide_version_id) WHERE guide_version_id IS NOT NULL AND deleted = FALSE;
CREATE INDEX idx_import_history_type ON import_histories (import_type) WHERE deleted = FALSE;
CREATE INDEX idx_import_history_status ON import_histories (status) WHERE deleted = FALSE;
CREATE INDEX idx_import_history_started ON import_histories (started_at) WHERE deleted = FALSE;
