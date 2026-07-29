-- V39: Departments
CREATE TABLE departments (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version         BIGINT NOT NULL DEFAULT 0,
    name            VARCHAR(200) NOT NULL,
    code            VARCHAR(50),
    description     VARCHAR(500),
    parent_id       UUID,
    organization_id UUID NOT NULL,
    tenant_id       UUID,
    active          BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order      INTEGER NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by      VARCHAR(100),
    updated_by      VARCHAR(100),
    deleted         BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT fk_department_parent FOREIGN KEY (parent_id) REFERENCES departments(id),
    CONSTRAINT fk_department_organization FOREIGN KEY (organization_id) REFERENCES organizations(id),
    CONSTRAINT fk_department_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id)
);

CREATE INDEX idx_department_organization ON departments (organization_id) WHERE deleted = FALSE;
CREATE INDEX idx_department_tenant ON departments (tenant_id) WHERE tenant_id IS NOT NULL AND deleted = FALSE;
CREATE INDEX idx_department_parent ON departments (parent_id) WHERE parent_id IS NOT NULL AND deleted = FALSE;
