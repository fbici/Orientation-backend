-- Tabular imports retain bounded original bytes and parsed draft rows.
ALTER TABLE import_history_records ADD COLUMN source_bytes BYTEA;
ALTER TABLE import_history_records ADD COLUMN rows_data JSONB;
CREATE INDEX idx_import_tenant_created ON import_history_records(tenant_id,created_at DESC);
