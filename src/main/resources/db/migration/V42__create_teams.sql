-- V42: Teams
CREATE TABLE teams (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version         BIGINT NOT NULL DEFAULT 0,
    name            VARCHAR(200) NOT NULL,
    description     VARCHAR(500),
    tenant_id       UUID NOT NULL,
    leader_id       UUID,
    active          BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by      VARCHAR(100),
    updated_by      VARCHAR(100),
    deleted         BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT fk_team_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id),
    CONSTRAINT fk_team_leader FOREIGN KEY (leader_id) REFERENCES users(id)
);

CREATE TABLE team_members (
    team_id     UUID NOT NULL,
    user_id     UUID NOT NULL,
    PRIMARY KEY (team_id, user_id),
    CONSTRAINT fk_team_member_team FOREIGN KEY (team_id) REFERENCES teams(id),
    CONSTRAINT fk_team_member_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_team_tenant ON teams (tenant_id) WHERE deleted = FALSE;
