-- V18: Transcripts
CREATE TABLE transcripts (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version             BIGINT NOT NULL DEFAULT 0,
    candidate_id        UUID NOT NULL,
    academic_year_id    UUID NOT NULL,
    title               VARCHAR(200),
    institution         VARCHAR(200),
    average             NUMERIC(5, 2),
    total_subjects      INTEGER,
    total_credits       INTEGER,
    mention             VARCHAR(50),
    status              VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    source              VARCHAR(50),
    original_file_name  VARCHAR(255),
    file_url            VARCHAR(500),
    ocr_confidence      NUMERIC(5, 2),
    validated_at        TIMESTAMPTZ,
    rejected_at         TIMESTAMPTZ,
    rejection_reason    VARCHAR(500),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by          VARCHAR(100),
    updated_by          VARCHAR(100),
    deleted             BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT uk_transcript_candidate_year UNIQUE (candidate_id, academic_year_id),
    CONSTRAINT fk_transcript_candidate FOREIGN KEY (candidate_id) REFERENCES candidates(id),
    CONSTRAINT fk_transcript_academic_year FOREIGN KEY (academic_year_id) REFERENCES academic_years(id),
    CONSTRAINT chk_transcript_average CHECK (average IS NULL OR (average >= 0 AND average <= 20)),
    CONSTRAINT chk_transcript_ocr_confidence CHECK (ocr_confidence IS NULL OR (ocr_confidence >= 0 AND ocr_confidence <= 100))
);

CREATE INDEX idx_transcript_candidate ON transcripts (candidate_id) WHERE deleted = FALSE;
CREATE INDEX idx_transcript_year ON transcripts (academic_year_id) WHERE deleted = FALSE;
CREATE INDEX idx_transcript_status ON transcripts (status) WHERE deleted = FALSE;
