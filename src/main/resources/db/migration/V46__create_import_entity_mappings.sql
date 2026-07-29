-- V46: Import Entity Mappings
CREATE TABLE import_entity_mappings (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version             BIGINT NOT NULL DEFAULT 0,
    data_type           VARCHAR(50) NOT NULL,
    entity_class_name   VARCHAR(200) NOT NULL,
    repository_class_name VARCHAR(200) NOT NULL,
    column_mapping_json JSONB,
    active              BOOLEAN NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by          VARCHAR(100),
    updated_by          VARCHAR(100),
    deleted             BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_entity_mapping_data_type ON import_entity_mappings (data_type) WHERE deleted = FALSE;
