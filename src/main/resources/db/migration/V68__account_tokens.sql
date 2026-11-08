CREATE TABLE account_tokens (
 id UUID PRIMARY KEY DEFAULT gen_random_uuid(),token_hash VARCHAR(64) NOT NULL UNIQUE,
 user_id UUID NOT NULL REFERENCES users(id),purpose VARCHAR(30) NOT NULL,
 expires_at TIMESTAMPTZ NOT NULL,consumed BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE INDEX idx_account_token_user ON account_tokens(user_id);
-- Old verification values in mfa_secret are deliberately not accepted by this mechanism.
