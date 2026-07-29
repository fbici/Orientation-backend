-- V10: Faculties
CREATE TABLE faculties (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version         BIGINT NOT NULL DEFAULT 0,
    campus_id       UUID NOT NULL,
    name            VARCHAR(200) NOT NULL,
    code            VARCHAR(20),
    description     TEXT,
    dean_name       VARCHAR(100),
    email           VARCHAR(255),
    website         VARCHAR(500),
    capacity        INTEGER,
    active          BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by      VARCHAR(100),
    updated_by      VARCHAR(100),
    deleted         BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT uk_faculty_name_campus UNIQUE (name, campus_id),
    CONSTRAINT fk_faculty_campus FOREIGN KEY (campus_id) REFERENCES campuses(id),
    CONSTRAINT chk_faculty_capacity CHECK (capacity IS NULL OR capacity > 0)
);

CREATE INDEX idx_faculty_campus ON faculties (campus_id) WHERE deleted = FALSE;
