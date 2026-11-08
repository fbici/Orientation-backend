-- Facts are reviewable children of the existing document/extraction/version workflow.
CREATE TABLE doc_extracted_facts (
 id UUID PRIMARY KEY DEFAULT gen_random_uuid(),version BIGINT NOT NULL DEFAULT 0,
 created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
 created_by VARCHAR(100),updated_by VARCHAR(100),deleted BOOLEAN NOT NULL DEFAULT FALSE,
 document_id UUID NOT NULL REFERENCES doc_documents(id),
 document_version_id UUID NOT NULL REFERENCES doc_document_versions(id),
 page_number INTEGER NOT NULL CHECK(page_number>0),
 source_key VARCHAR(64) NOT NULL,source_passage TEXT NOT NULL CHECK(length(source_passage)>0),
 kind VARCHAR(40) NOT NULL,payload JSONB NOT NULL,original_payload JSONB NOT NULL,
 academic_year_id UUID REFERENCES academic_years(id),
 confidence NUMERIC(5,2),status VARCHAR(20) NOT NULL DEFAULT 'EXTRACTED'
  CHECK(status IN ('EXTRACTED','TO_REVIEW','VALIDATED','REJECTED')),
 reviewed_by UUID REFERENCES users(id),reviewed_at TIMESTAMPTZ,review_note TEXT,
 published_entity_id UUID,published_entity_type VARCHAR(50),published_at TIMESTAMPTZ,
 guide_version_id UUID REFERENCES guide_versions(id),
 CONSTRAINT uk_document_fact_source UNIQUE(document_version_id,source_key),
 CONSTRAINT ck_validated_fact_reviewer CHECK(status<>'VALIDATED' OR (reviewed_by IS NOT NULL AND reviewed_at IS NOT NULL)),
 CONSTRAINT ck_published_fact_validated CHECK(published_at IS NULL OR (status='VALIDATED' AND published_entity_id IS NOT NULL AND guide_version_id IS NOT NULL))
);
CREATE INDEX idx_fact_document ON doc_extracted_facts(document_id,status) WHERE NOT deleted;
CREATE INDEX idx_fact_entity ON doc_extracted_facts(published_entity_type,published_entity_id) WHERE NOT deleted;
ALTER TABLE knowledge_nodes ADD COLUMN tenant_id UUID REFERENCES tenants(id);
ALTER TABLE knowledge_nodes ADD COLUMN guide_version_id UUID REFERENCES guide_versions(id);
ALTER TABLE knowledge_nodes ADD COLUMN verified BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE knowledge_search_index ADD COLUMN tenant_id UUID REFERENCES tenants(id);
ALTER TABLE knowledge_search_index ADD COLUMN guide_version_id UUID REFERENCES guide_versions(id);
ALTER TABLE knowledge_search_index ADD COLUMN verified BOOLEAN NOT NULL DEFAULT FALSE;
CREATE UNIQUE INDEX uk_verified_knowledge_entity_version ON knowledge_nodes(entity_type,entity_id,guide_version_id) WHERE verified AND NOT deleted;
CREATE UNIQUE INDEX uk_verified_search_entity_version ON knowledge_search_index(entity_type,entity_id,guide_version_id) WHERE verified AND NOT deleted;
CREATE INDEX idx_verified_knowledge_scope ON knowledge_nodes(tenant_id,verified);
CREATE INDEX idx_verified_search_scope ON knowledge_search_index(tenant_id,verified);
ALTER TABLE doc_documents ADD COLUMN processing_error VARCHAR(1000);
ALTER TABLE doc_documents ADD COLUMN processed_at TIMESTAMPTZ;
ALTER TABLE admission_criteria ADD COLUMN source_fact_id UUID REFERENCES doc_extracted_facts(id);
ALTER TABLE admission_criteria ADD COLUMN field_name VARCHAR(50);
ALTER TABLE admission_criteria ADD COLUMN value_scale_max NUMERIC(10,2);
ALTER TABLE scholarship_criteria ADD COLUMN source_fact_id UUID REFERENCES doc_extracted_facts(id);
ALTER TABLE scholarship_criteria ADD COLUMN field_name VARCHAR(50);
ALTER TABLE scholarship_criteria ADD COLUMN value_scale_max NUMERIC(10,2);
ALTER TABLE doc_extracted_facts ADD COLUMN original_kind VARCHAR(40);
CREATE UNIQUE INDEX uk_guide_source_document_version ON guide_versions(document_version_id) WHERE document_version_id IS NOT NULL AND NOT deleted;
ALTER TABLE knowledge_nodes ADD COLUMN country_id UUID REFERENCES countries(id);
ALTER TABLE knowledge_nodes ADD COLUMN academic_year_id UUID REFERENCES academic_years(id);
ALTER TABLE knowledge_nodes ADD COLUMN source_document_id UUID REFERENCES doc_documents(id);
ALTER TABLE knowledge_search_index ADD COLUMN country_id UUID REFERENCES countries(id);
ALTER TABLE knowledge_search_index ADD COLUMN academic_year_id UUID REFERENCES academic_years(id);
ALTER TABLE knowledge_search_index ADD COLUMN source_document_id UUID REFERENCES doc_documents(id);
