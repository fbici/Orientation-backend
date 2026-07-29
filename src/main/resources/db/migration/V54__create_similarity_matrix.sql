-- V54: Similarity Matrix
CREATE TABLE knowledge_similarity_matrix (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version         BIGINT NOT NULL DEFAULT 0,
    entity_type     VARCHAR(50) NOT NULL,
    entity_id_a     UUID NOT NULL,
    entity_id_b     UUID NOT NULL,
    similarity_score NUMERIC(5, 4) NOT NULL,
    algorithm       VARCHAR(50),
    active          BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by      VARCHAR(100),
    updated_by      VARCHAR(100),
    deleted         BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_sm_type ON knowledge_similarity_matrix (entity_type) WHERE deleted = FALSE;
CREATE INDEX idx_sm_entity_a ON knowledge_similarity_matrix (entity_type, entity_id_a) WHERE deleted = FALSE;
CREATE INDEX idx_sm_entity_b ON knowledge_similarity_matrix (entity_type, entity_id_b) WHERE deleted = FALSE;
