ALTER TABLE knowledge_nodes ADD COLUMN program_id UUID REFERENCES programs(id);
ALTER TABLE knowledge_nodes ADD COLUMN university_id UUID REFERENCES universities(id);
ALTER TABLE knowledge_search_index ADD COLUMN program_id UUID REFERENCES programs(id);
ALTER TABLE knowledge_search_index ADD COLUMN university_id UUID REFERENCES universities(id);
ALTER TABLE knowledge_search_index ADD COLUMN node_id UUID REFERENCES knowledge_nodes(id);
ALTER TABLE knowledge_search_index ADD COLUMN node_type VARCHAR(100);
CREATE INDEX idx_search_source_filters ON knowledge_search_index(country_id,academic_year_id,source_document_id) WHERE verified AND NOT deleted;
CREATE INDEX idx_search_program ON knowledge_search_index(program_id) WHERE verified AND NOT deleted;
CREATE INDEX idx_search_university ON knowledge_search_index(university_id) WHERE verified AND NOT deleted;
