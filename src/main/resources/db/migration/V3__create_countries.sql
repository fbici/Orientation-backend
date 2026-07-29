-- V3: Countries
CREATE TABLE countries (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version         BIGINT NOT NULL DEFAULT 0,
    name            VARCHAR(100) NOT NULL,
    code            VARCHAR(3) NOT NULL,
    official_name   VARCHAR(200),
    phone_code      VARCHAR(10),
    currency        VARCHAR(3),
    active          BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by      VARCHAR(100),
    updated_by      VARCHAR(100),
    deleted         BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT uk_country_code UNIQUE (code),
    CONSTRAINT uk_country_name UNIQUE (name)
);

CREATE INDEX idx_country_active ON countries (active) WHERE deleted = FALSE;
