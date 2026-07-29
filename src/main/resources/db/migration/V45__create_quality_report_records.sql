-- V45: Quality Report Records
CREATE TABLE quality_report_records (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version                 BIGINT NOT NULL DEFAULT 0,
    import_history_id       UUID NOT NULL,
    total_rows              INTEGER NOT NULL,
    valid_rows              INTEGER NOT NULL DEFAULT 0,
    invalid_rows            INTEGER NOT NULL DEFAULT 0,
    duplicate_rows          INTEGER NOT NULL DEFAULT 0,
    missing_values_count    INTEGER NOT NULL DEFAULT 0,
    overall_score           NUMERIC(5, 2),
    validation_score        NUMERIC(5, 2),
    transformation_score    NUMERIC(5, 2),
    warnings_json           JSONB,
    errors_json             JSONB,
    missing_values_json     JSONB,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by              VARCHAR(100),
    updated_by              VARCHAR(100),
    deleted                 BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT uk_quality_report_import UNIQUE (import_history_id)
);

CREATE INDEX idx_quality_report_import ON quality_report_records (import_history_id) WHERE deleted = FALSE;
