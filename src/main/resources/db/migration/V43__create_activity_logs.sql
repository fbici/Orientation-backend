-- V43: Activity Logs
CREATE TABLE activity_logs (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version         BIGINT NOT NULL DEFAULT 0,
    user_id         UUID,
    action          VARCHAR(100) NOT NULL,
    entity_type     VARCHAR(100) NOT NULL,
    entity_id       VARCHAR(50),
    description     TEXT,
    old_values      JSONB,
    new_values      JSONB,
    ip_address      VARCHAR(255),
    user_agent      VARCHAR(500),
    tenant_id       VARCHAR(50),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by      VARCHAR(100),
    updated_by      VARCHAR(100),
    deleted         BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT fk_activity_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_activity_user ON activity_logs (user_id) WHERE user_id IS NOT NULL AND deleted = FALSE;
CREATE INDEX idx_activity_tenant ON activity_logs (tenant_id) WHERE deleted = FALSE;
CREATE INDEX idx_activity_entity ON activity_logs (entity_type, entity_id) WHERE deleted = FALSE;
CREATE INDEX idx_activity_created ON activity_logs (created_at) WHERE deleted = FALSE;
