-- Only explicit authenticated candidate requests enter this lifecycle.
-- Existing disabled/deleted accounts are deliberately NOT backfilled.
CREATE TABLE candidate_deletion_requests (
 user_id uuid PRIMARY KEY REFERENCES users(id),
 requested_at timestamptz NOT NULL DEFAULT now(),
 purge_after timestamptz NOT NULL DEFAULT (now() + interval '30 days'),
 CHECK (purge_after >= requested_at + interval '30 days')
);
CREATE INDEX idx_candidate_deletion_due ON candidate_deletion_requests(purge_after);
