-- V20: Transcript Analyses
CREATE TABLE transcript_analyses (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version                 BIGINT NOT NULL DEFAULT 0,
    transcript_id           UUID NOT NULL,
    normalized_average      NUMERIC(5, 2) NOT NULL,
    gpa                     NUMERIC(3, 2),
    percentile              NUMERIC(5, 2),
    national_rank           INTEGER,
    total_score             NUMERIC(10, 2),
    strongest_subjects      JSONB,
    weakest_subjects        JSONB,
    subject_averages        JSONB,
    eligibility_score       NUMERIC(5, 2),
    scholarship_score       NUMERIC(5, 2),
    analysis_version        VARCHAR(20) NOT NULL,
    analyzed_at             TIMESTAMPTZ NOT NULL,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by              VARCHAR(100),
    updated_by              VARCHAR(100),
    deleted                 BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT uk_transcript_analysis_transcript UNIQUE (transcript_id),
    CONSTRAINT fk_transcript_analysis_transcript FOREIGN KEY (transcript_id) REFERENCES transcripts(id),
    CONSTRAINT chk_transcript_analysis_average CHECK (normalized_average >= 0 AND normalized_average <= 20),
    CONSTRAINT chk_transcript_analysis_gpa CHECK (gpa IS NULL OR (gpa >= 0 AND gpa <= 4)),
    CONSTRAINT chk_transcript_analysis_percentile CHECK (percentile IS NULL OR (percentile >= 0 AND percentile <= 100))
);
