-- V52: Search Index
CREATE TABLE knowledge_search_index (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version         BIGINT NOT NULL DEFAULT 0,
    entity_type     VARCHAR(100) NOT NULL,
    entity_id       UUID NOT NULL,
    name            VARCHAR(200) NOT NULL,
    content         TEXT,
    metadata        JSONB,
    active          BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by      VARCHAR(100),
    updated_by      VARCHAR(100),
    deleted         BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_si_entity ON knowledge_search_index (entity_type, entity_id) WHERE deleted = FALSE;
CREATE INDEX idx_si_name ON knowledge_search_index (name) WHERE deleted = FALSE;

CREATE INDEX idx_si_search ON knowledge_search_index USING gin(to_tsvector('french', coalesce(name, '') || ' ' || coalesce(content, ''))) WHERE deleted = FALSE;
