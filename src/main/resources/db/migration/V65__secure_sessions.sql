-- Explicit transition: old refresh credentials and old JWTs must be discarded.
-- This migration is prepared only; apply to an existing database after review/backup.
ALTER TABLE refresh_tokens ADD COLUMN family_id UUID;
UPDATE refresh_tokens SET family_id=id, revoked=TRUE,
    token=encode(digest(token,'sha256'),'hex');
ALTER TABLE refresh_tokens ALTER COLUMN family_id SET NOT NULL;
CREATE UNIQUE INDEX uk_refresh_token_digest ON refresh_tokens(token) WHERE revoked=FALSE;
CREATE INDEX idx_refresh_family ON refresh_tokens(family_id);
