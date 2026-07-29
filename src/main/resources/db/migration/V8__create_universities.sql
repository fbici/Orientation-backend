-- V8: Universities
CREATE TABLE universities (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version                 BIGINT NOT NULL DEFAULT 0,
    name                    VARCHAR(200) NOT NULL,
    short_name              VARCHAR(50),
    country_id              UUID NOT NULL,
    city_id                 UUID NOT NULL,
    address                 VARCHAR(500),
    phone                   VARCHAR(20),
    email                   VARCHAR(255),
    website                 VARCHAR(500),
    founded_year            INTEGER,
    status                  VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    ranking                 INTEGER,
    international_ranking   INTEGER,
    student_count           INTEGER,
    acceptance_rate         NUMERIC(5, 2),
    description             TEXT,
    logo_url                VARCHAR(500),
    active                  BOOLEAN NOT NULL DEFAULT TRUE,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by              VARCHAR(100),
    updated_by              VARCHAR(100),
    deleted                 BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT uk_university_name_country UNIQUE (name, country_id),
    CONSTRAINT fk_university_country FOREIGN KEY (country_id) REFERENCES countries(id),
    CONSTRAINT fk_university_city FOREIGN KEY (city_id) REFERENCES cities(id),
    CONSTRAINT chk_university_founded CHECK (founded_year IS NULL OR founded_year > 1000),
    CONSTRAINT chk_university_acceptance CHECK (acceptance_rate IS NULL OR (acceptance_rate >= 0 AND acceptance_rate <= 100))
);

CREATE INDEX idx_university_country ON universities (country_id) WHERE deleted = FALSE;
CREATE INDEX idx_university_city ON universities (city_id) WHERE deleted = FALSE;
CREATE INDEX idx_university_status ON universities (status) WHERE deleted = FALSE;
CREATE INDEX idx_university_ranking ON universities (ranking) WHERE ranking IS NOT NULL AND deleted = FALSE;
