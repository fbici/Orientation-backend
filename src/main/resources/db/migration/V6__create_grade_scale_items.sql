-- V6: Grade Scale Items
CREATE TABLE grade_scale_items (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version             BIGINT NOT NULL DEFAULT 0,
    grade_scale_id      UUID NOT NULL,
    raw_grade           VARCHAR(10) NOT NULL,
    raw_value           NUMERIC(5, 2),
    normalized_value    NUMERIC(5, 2) NOT NULL,
    normalized_gpa      NUMERIC(3, 2),
    label               VARCHAR(100),
    sort_order          INTEGER NOT NULL DEFAULT 0,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by          VARCHAR(100),
    updated_by          VARCHAR(100),
    deleted             BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT uk_grade_scale_item_raw UNIQUE (grade_scale_id, raw_grade),
    CONSTRAINT uk_grade_scale_item_normalized UNIQUE (grade_scale_id, normalized_value),
    CONSTRAINT fk_grade_scale_item_scale FOREIGN KEY (grade_scale_id) REFERENCES grade_scales(id)
);

CREATE INDEX idx_grade_scale_item_scale ON grade_scale_items (grade_scale_id) WHERE deleted = FALSE;
