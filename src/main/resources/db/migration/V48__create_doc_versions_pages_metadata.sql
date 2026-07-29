-- V48: Document Versions, Pages, Metadata
CREATE TABLE doc_document_versions (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version             BIGINT NOT NULL DEFAULT 0,
    document_id         UUID NOT NULL,
    version_number      INTEGER NOT NULL,
    stored_file_name    VARCHAR(500) NOT NULL,
    file_size           BIGINT NOT NULL,
    checksum            VARCHAR(500) NOT NULL,
    uploaded_by         VARCHAR(100),
    change_description  VARCHAR(500),
    active              BOOLEAN NOT NULL DEFAULT TRUE,
    uploaded_at         TIMESTAMPTZ NOT NULL,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by          VARCHAR(100),
    updated_by          VARCHAR(100),
    deleted             BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT fk_doc_version_document FOREIGN KEY (document_id) REFERENCES doc_documents(id)
);

CREATE TABLE doc_document_pages (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version             BIGINT NOT NULL DEFAULT 0,
    document_id         UUID NOT NULL,
    page_number         INTEGER NOT NULL,
    raw_text            TEXT,
    cleaned_text        TEXT,
    ocr_confidence      NUMERIC(5, 2),
    detected_language   VARCHAR(10),
    blocks_json         JSONB,
    paragraphs_json     JSONB,
    tables_json         JSONB,
    images_json         JSONB,
    width               INTEGER,
    height              INTEGER,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by          VARCHAR(100),
    updated_by          VARCHAR(100),
    deleted             BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT fk_doc_page_document FOREIGN KEY (document_id) REFERENCES doc_documents(id)
);

CREATE TABLE doc_document_metadata (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version             BIGINT NOT NULL DEFAULT 0,
    document_id         UUID NOT NULL,
    title               VARCHAR(500),
    author              VARCHAR(200),
    year                VARCHAR(50),
    version_number      VARCHAR(50),
    university          VARCHAR(200),
    country             VARCHAR(100),
    language            VARCHAR(10),
    page_count          INTEGER,
    created_date        TIMESTAMPTZ,
    keywords            JSONB,
    checksum            VARCHAR(500),
    custom_metadata     JSONB,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by          VARCHAR(100),
    updated_by          VARCHAR(100),
    deleted             BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT uk_doc_metadata_document UNIQUE (document_id),
    CONSTRAINT fk_doc_metadata_document FOREIGN KEY (document_id) REFERENCES doc_documents(id)
);

CREATE INDEX idx_doc_version_document ON doc_document_versions (document_id) WHERE deleted = FALSE;
CREATE INDEX idx_doc_page_document ON doc_document_pages (document_id) WHERE deleted = FALSE;
