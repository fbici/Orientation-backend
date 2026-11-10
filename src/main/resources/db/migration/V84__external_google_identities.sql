-- Native Android Google sign-in: stable provider subject, never an email-only link.
CREATE TABLE auth_external_identities (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    issuer VARCHAR(100) NOT NULL,
    subject VARCHAR(255) NOT NULL,
    user_id UUID NOT NULL REFERENCES users(id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_external_identity_subject UNIQUE(issuer,subject),
    CONSTRAINT uk_external_identity_user UNIQUE(user_id,issuer)
);
