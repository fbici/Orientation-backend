-- Retain every extraction and correction; no destructive replacement of analyses.
ALTER TABLE transcript_analyses DROP CONSTRAINT uk_transcript_analysis_transcript;
ALTER TABLE transcript_analyses ALTER COLUMN normalized_average DROP NOT NULL;
ALTER TABLE transcript_analyses ADD COLUMN review_payload jsonb;
ALTER TABLE transcript_analyses ADD COLUMN confirmed_at timestamptz;
ALTER TABLE transcript_analyses ADD COLUMN parent_analysis_id uuid REFERENCES transcript_analyses(id);
ALTER TABLE transcript_analyses ADD COLUMN source_checksum varchar(64);
CREATE INDEX idx_transcript_analysis_history ON transcript_analyses(transcript_id,analyzed_at DESC);
ALTER TABLE transcripts DROP CONSTRAINT IF EXISTS uk_transcript_candidate_year;
