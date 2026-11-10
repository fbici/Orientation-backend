-- One current challenge per account; no clear-text verification codes stored.
CREATE TABLE email_verification_challenges (
 id UUID PRIMARY KEY,
 user_id UUID NOT NULL UNIQUE REFERENCES users(id),
 code_hash VARCHAR(64) NOT NULL,
 expires_at TIMESTAMPTZ NOT NULL,
 last_sent_at TIMESTAMPTZ NOT NULL,
 attempts INTEGER NOT NULL DEFAULT 0 CHECK (attempts BETWEEN 0 AND 5),
 send_count INTEGER NOT NULL DEFAULT 1 CHECK (send_count BETWEEN 1 AND 3),
 consumed BOOLEAN NOT NULL DEFAULT FALSE
);
