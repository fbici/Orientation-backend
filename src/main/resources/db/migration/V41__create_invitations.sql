-- V41: Invitations
CREATE TABLE invitations (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version         BIGINT NOT NULL DEFAULT 0,
    email           VARCHAR(255) NOT NULL,
    tenant_id       UUID NOT NULL,
    role_id         UUID,
    department_id   UUID,
    invited_by      UUID NOT NULL,
    token           VARCHAR(500) NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    expires_at      TIMESTAMPTZ NOT NULL,
    accepted_at     TIMESTAMPTZ,
    revoked_at      TIMESTAMPTZ,
    message         VARCHAR(500),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by      VARCHAR(100),
    updated_by      VARCHAR(100),
    deleted         BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT uk_invitation_token UNIQUE (token),
    CONSTRAINT fk_invitation_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id),
    CONSTRAINT fk_invitation_role FOREIGN KEY (role_id) REFERENCES roles(id),
    CONSTRAINT fk_invitation_department FOREIGN KEY (department_id) REFERENCES departments(id),
    CONSTRAINT fk_invitation_inviter FOREIGN KEY (invited_by) REFERENCES users(id)
);

CREATE INDEX idx_invitation_tenant ON invitations (tenant_id) WHERE deleted = FALSE;
CREATE INDEX idx_invitation_status ON invitations (status) WHERE deleted = FALSE;
