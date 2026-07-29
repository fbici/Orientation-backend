-- V40: User Profiles
CREATE TABLE user_profiles (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version                 BIGINT NOT NULL DEFAULT 0,
    user_id                 UUID NOT NULL,
    avatar_url              VARCHAR(500),
    job_title               VARCHAR(200),
    department              VARCHAR(200),
    timezone                VARCHAR(50),
    language                VARCHAR(10),
    bio                     VARCHAR(500),
    linkedin_url            VARCHAR(200),
    twitter_url             VARCHAR(200),
    notifications_enabled   BOOLEAN NOT NULL DEFAULT TRUE,
    email_notifications     BOOLEAN NOT NULL DEFAULT TRUE,
    sms_notifications       BOOLEAN NOT NULL DEFAULT FALSE,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by              VARCHAR(100),
    updated_by              VARCHAR(100),
    deleted                 BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT uk_user_profile_user UNIQUE (user_id),
    CONSTRAINT fk_user_profile_user FOREIGN KEY (user_id) REFERENCES users(id)
);
