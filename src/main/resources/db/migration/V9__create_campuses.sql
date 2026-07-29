-- V9: Campuses
CREATE TABLE campuses (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version         BIGINT NOT NULL DEFAULT 0,
    university_id   UUID NOT NULL,
    name            VARCHAR(200) NOT NULL,
    city_id         UUID NOT NULL,
    address         VARCHAR(500),
    phone           VARCHAR(20),
    main            BOOLEAN NOT NULL DEFAULT FALSE,
    capacity        INTEGER,
    latitude        NUMERIC(10, 8),
    longitude       NUMERIC(11, 8),
    active          BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by      VARCHAR(100),
    updated_by      VARCHAR(100),
    deleted         BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT uk_campus_name_university UNIQUE (name, university_id),
    CONSTRAINT fk_campus_university FOREIGN KEY (university_id) REFERENCES universities(id),
    CONSTRAINT fk_campus_city FOREIGN KEY (city_id) REFERENCES cities(id),
    CONSTRAINT chk_campus_capacity CHECK (capacity IS NULL OR capacity > 0)
);

CREATE INDEX idx_campus_university ON campuses (university_id) WHERE deleted = FALSE;
CREATE INDEX idx_campus_city ON campuses (city_id) WHERE deleted = FALSE;
