-- V44: Import History Records
CREATE TABLE import_history_records (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version             BIGINT NOT NULL DEFAULT 0,
    file_name           VARCHAR(255) NOT NULL,
    file_type           VARCHAR(50) NOT NULL,
    checksum            VARCHAR(500),
    file_size           BIGINT NOT NULL,
    total_rows          INTEGER NOT NULL,
    imported_rows       INTEGER NOT NULL DEFAULT 0,
    rejected_rows       INTEGER NOT NULL DEFAULT 0,
    skipped_rows        INTEGER NOT NULL DEFAULT 0,
    user_id             VARCHAR(100),
    tenant_id           VARCHAR(50),
    organization_id     VARCHAR(50),
    data_type           VARCHAR(50) NOT NULL,
    status              VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    comments            VARCHAR(1000),
    execution_time_ms   BIGINT NOT NULL DEFAULT 0,
    version_number      INTEGER NOT NULL DEFAULT 1,
    parent_version_id   UUID,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by          VARCHAR(100),
    updated_by          VARCHAR(100),
    deleted             BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT fk_import_history_parent FOREIGN KEY (parent_version_id) REFERENCES import_history_records(id)
);

CREATE INDEX idx_import_history_data_type ON import_history_records (data_type) WHERE deleted = FALSE;
CREATE INDEX idx_import_history_records_status ON import_history_records (status) WHERE deleted = FALSE;
CREATE INDEX idx_import_history_user ON import_history_records (user_id) WHERE deleted = FALSE;
CREATE INDEX idx_import_history_created ON import_history_records (created_at) WHERE deleted = FALSE;
