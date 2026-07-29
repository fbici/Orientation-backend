-- V49: Document Extractions, Classifications, Audits
CREATE TABLE doc_document_extractions (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version             BIGINT NOT NULL DEFAULT 0,
    document_id         UUID NOT NULL,
    full_text           TEXT,
    cleaned_text        TEXT,
    ocr_confidence      NUMERIC(5, 2),
    detected_language   VARCHAR(10),
    block_count         INTEGER,
    paragraph_count     INTEGER,
    table_count         INTEGER,
    image_count         INTEGER,
    tables_json         JSONB,
    structure_json      JSONB,
    extraction_score    NUMERIC(5, 2),
    quality_score       NUMERIC(5, 2),
    ocr_engine          VARCHAR(50),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by          VARCHAR(100),
    updated_by          VARCHAR(100),
    deleted             BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT uk_doc_extraction_document UNIQUE (document_id),
    CONSTRAINT fk_doc_extraction_document FOREIGN KEY (document_id) REFERENCES doc_documents(id)
);

CREATE TABLE doc_document_classifications (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version                 BIGINT NOT NULL DEFAULT 0,
    document_id             UUID NOT NULL,
    primary_type            VARCHAR(30) NOT NULL,
    primary_confidence      NUMERIC(5, 2),
    all_classifications_json JSONB,
    classification_engine   VARCHAR(100),
    features_json           JSONB,
    classification_score    NUMERIC(5, 2),
    created_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by              VARCHAR(100),
    updated_by              VARCHAR(100),
    deleted                 BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT uk_doc_classification_document UNIQUE (document_id),
    CONSTRAINT fk_doc_classification_document FOREIGN KEY (document_id) REFERENCES doc_documents(id)
);

CREATE TABLE doc_document_audits (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version         BIGINT NOT NULL DEFAULT 0,
    document_id     UUID NOT NULL,
    action          VARCHAR(100) NOT NULL,
    user_id         VARCHAR(100),
    ip_address      VARCHAR(255),
    details         TEXT,
    performed_at    TIMESTAMPTZ NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by      VARCHAR(100),
    updated_by      VARCHAR(100),
    deleted         BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT fk_doc_audit_document FOREIGN KEY (document_id) REFERENCES doc_documents(id)
);

CREATE INDEX idx_doc_extraction_document ON doc_document_extractions (document_id) WHERE deleted = FALSE;
CREATE INDEX idx_doc_classification_document ON doc_document_classifications (document_id) WHERE deleted = FALSE;
CREATE INDEX idx_doc_audit_document ON doc_document_audits (document_id) WHERE deleted = FALSE;
