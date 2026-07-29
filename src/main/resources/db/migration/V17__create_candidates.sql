-- V17: Candidates
CREATE TABLE candidates (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version                 BIGINT NOT NULL DEFAULT 0,
    email                   VARCHAR(255) NOT NULL,
    first_name              VARCHAR(100) NOT NULL,
    last_name               VARCHAR(100) NOT NULL,
    phone                   VARCHAR(20),
    date_of_birth           DATE,
    gender                  VARCHAR(10),
    country_id              UUID NOT NULL,
    city_id                 UUID,
    high_school             VARCHAR(200),
    bac_year                INTEGER NOT NULL,
    bac_type                VARCHAR(30),
    bac_average             NUMERIC(5, 2),
    bac_mention             VARCHAR(30),
    profile_picture_url     VARCHAR(500),
    status                  VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    verified                BOOLEAN NOT NULL DEFAULT FALSE,
    last_login_at           TIMESTAMPTZ,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by              VARCHAR(100),
    updated_by              VARCHAR(100),
    deleted                 BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT uk_candidate_email UNIQUE (email),
    CONSTRAINT fk_candidate_country FOREIGN KEY (country_id) REFERENCES countries(id),
    CONSTRAINT fk_candidate_city FOREIGN KEY (city_id) REFERENCES cities(id),
    CONSTRAINT chk_candidate_bac_year CHECK (bac_year >= 2000)
);

CREATE INDEX idx_candidate_country ON candidates (country_id) WHERE deleted = FALSE;
CREATE INDEX idx_candidate_city ON candidates (city_id) WHERE city_id IS NOT NULL AND deleted = FALSE;
CREATE INDEX idx_candidate_status ON candidates (status) WHERE deleted = FALSE;
CREATE INDEX idx_candidate_bac_year ON candidates (bac_year) WHERE deleted = FALSE;
