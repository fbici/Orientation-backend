-- V4: Cities
CREATE TABLE cities (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version         BIGINT NOT NULL DEFAULT 0,
    name            VARCHAR(150) NOT NULL,
    country_id      UUID NOT NULL,
    postal_code     VARCHAR(20),
    latitude        NUMERIC(10, 8),
    longitude       NUMERIC(11, 8),
    active          BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by      VARCHAR(100),
    updated_by      VARCHAR(100),
    deleted         BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT uk_city_name_country UNIQUE (name, country_id),
    CONSTRAINT fk_city_country FOREIGN KEY (country_id) REFERENCES countries(id)
);

CREATE INDEX idx_city_country ON cities (country_id) WHERE deleted = FALSE;
