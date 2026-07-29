-- V51: Knowledge Relations
CREATE TABLE knowledge_relations (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version         BIGINT NOT NULL DEFAULT 0,
    source_id       UUID NOT NULL,
    target_id       UUID NOT NULL,
    relation_type   VARCHAR(50) NOT NULL,
    weight          NUMERIC(5, 2),
    properties      JSONB,
    active          BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by      VARCHAR(100),
    updated_by      VARCHAR(100),
    deleted         BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT fk_kr_source FOREIGN KEY (source_id) REFERENCES knowledge_nodes(id),
    CONSTRAINT fk_kr_target FOREIGN KEY (target_id) REFERENCES knowledge_nodes(id)
);

CREATE INDEX idx_kr_source ON knowledge_relations (source_id) WHERE deleted = FALSE;
CREATE INDEX idx_kr_target ON knowledge_relations (target_id) WHERE deleted = FALSE;
CREATE INDEX idx_kr_type ON knowledge_relations (relation_type) WHERE deleted = FALSE;
