-- V50: Knowledge Nodes
CREATE TABLE knowledge_nodes (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version         BIGINT NOT NULL DEFAULT 0,
    node_type       VARCHAR(100) NOT NULL,
    name            VARCHAR(200) NOT NULL,
    description     TEXT,
    entity_id       UUID NOT NULL,
    entity_type     VARCHAR(50),
    properties      JSONB,
    active          BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by      VARCHAR(100),
    updated_by      VARCHAR(100),
    deleted         BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_kn_type ON knowledge_nodes (node_type) WHERE deleted = FALSE;
CREATE INDEX idx_kn_entity ON knowledge_nodes (entity_type, entity_id) WHERE deleted = FALSE;
CREATE INDEX idx_kn_name ON knowledge_nodes (name) WHERE deleted = FALSE;

CREATE TABLE knowledge_node_relations (
    source_id   UUID NOT NULL,
    target_id   UUID NOT NULL,
    PRIMARY KEY (source_id, target_id),
    CONSTRAINT fk_knr_source FOREIGN KEY (source_id) REFERENCES knowledge_nodes(id),
    CONSTRAINT fk_knr_target FOREIGN KEY (target_id) REFERENCES knowledge_nodes(id)
);
