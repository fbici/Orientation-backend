-- V47: Document Intelligence - Documents
CREATE TABLE doc_documents (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version             BIGINT NOT NULL DEFAULT 0,
    title               VARCHAR(500) NOT NULL,
    original_file_name  VARCHAR(200),
    stored_file_name    VARCHAR(255) NOT NULL,
    mime_type           VARCHAR(100) NOT NULL,
    file_size           BIGINT NOT NULL,
    checksum            VARCHAR(500) NOT NULL,
    document_type       VARCHAR(30) NOT NULL DEFAULT 'UNKNOWN',
    status              VARCHAR(50) NOT NULL DEFAULT 'UPLOADED',
    tenant_id           VARCHAR(100),
    organization_id     VARCHAR(100),
    uploaded_by         VARCHAR(100),
    uploaded_at         TIMESTAMPTZ,
    description         VARCHAR(500),
    tags                JSONB,
    language            VARCHAR(10),
    page_count          INTEGER,
    ocr_score           NUMERIC(5, 2),
    quality_score       NUMERIC(5, 2),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by          VARCHAR(100),
    updated_by          VARCHAR(100),
    deleted             BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_doc_documents_type ON doc_documents (document_type) WHERE deleted = FALSE;
CREATE INDEX idx_doc_documents_status ON doc_documents (status) WHERE deleted = FALSE;
CREATE INDEX idx_doc_documents_tenant ON doc_documents (tenant_id) WHERE deleted = FALSE;
CREATE INDEX idx_doc_documents_uploaded ON doc_documents (uploaded_at) WHERE deleted = FALSE;
