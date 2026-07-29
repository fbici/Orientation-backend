-- V19: Transcript Lines
CREATE TABLE transcript_lines (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version             BIGINT NOT NULL DEFAULT 0,
    transcript_id       UUID NOT NULL,
    subject_id          UUID NOT NULL,
    raw_grade           VARCHAR(20) NOT NULL,
    raw_value           NUMERIC(5, 2),
    normalized_value    NUMERIC(5, 2),
    coefficient         NUMERIC(3, 2),
    credits             INTEGER,
    semester            INTEGER,
    passed              BOOLEAN NOT NULL DEFAULT FALSE,
    comment             VARCHAR(500),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by          VARCHAR(100),
    updated_by          VARCHAR(100),
    deleted             BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT uk_transcript_line_subject_semester UNIQUE (transcript_id, subject_id, semester),
    CONSTRAINT fk_transcript_line_transcript FOREIGN KEY (transcript_id) REFERENCES transcripts(id),
    CONSTRAINT fk_transcript_line_subject FOREIGN KEY (subject_id) REFERENCES subjects(id),
    CONSTRAINT chk_transcript_line_raw_value CHECK (raw_value IS NULL OR raw_value >= 0),
    CONSTRAINT chk_transcript_line_normalized CHECK (normalized_value IS NULL OR normalized_value >= 0),
    CONSTRAINT chk_transcript_line_coefficient CHECK (coefficient IS NULL OR coefficient > 0),
    CONSTRAINT chk_transcript_line_credits CHECK (credits IS NULL OR credits >= 0),
    CONSTRAINT chk_transcript_line_semester CHECK (semester IS NULL OR semester IN (1, 2))
);

CREATE INDEX idx_transcript_line_transcript ON transcript_lines (transcript_id) WHERE deleted = FALSE;
CREATE INDEX idx_transcript_line_subject ON transcript_lines (subject_id) WHERE deleted = FALSE;
