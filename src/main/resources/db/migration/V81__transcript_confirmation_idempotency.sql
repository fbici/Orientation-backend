CREATE UNIQUE INDEX uk_transcript_analysis_confirmation ON transcript_analyses(parent_analysis_id) WHERE confirmed_at IS NOT NULL AND parent_analysis_id IS NOT NULL;
