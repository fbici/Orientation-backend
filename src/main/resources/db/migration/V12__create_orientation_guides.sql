-- V12: Orientation Guides
CREATE TABLE orientation_guides (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version             BIGINT NOT NULL DEFAULT 0,
    title               VARCHAR(300) NOT NULL,
    country_id          UUID NOT NULL,
    academic_year_id    UUID NOT NULL,
    publisher           VARCHAR(200),
    publication_date    DATE,
    description         TEXT,
    document_url        VARCHAR(500),
    active              BOOLEAN NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by          VARCHAR(100),
    updated_by          VARCHAR(100),
    deleted             BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT uk_orientation_guide_country_year UNIQUE (country_id, academic_year_id),
    CONSTRAINT fk_orientation_guide_country FOREIGN KEY (country_id) REFERENCES countries(id),
    CONSTRAINT fk_orientation_guide_academic_year FOREIGN KEY (academic_year_id) REFERENCES academic_years(id)
);

CREATE INDEX idx_orientation_guide_country ON orientation_guides (country_id) WHERE deleted = FALSE;
CREATE INDEX idx_orientation_guide_year ON orientation_guides (academic_year_id) WHERE deleted = FALSE;
