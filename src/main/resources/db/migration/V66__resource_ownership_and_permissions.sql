-- Legacy candidates with an unambiguous existing account are linked; unmatched rows stay quarantined.
ALTER TABLE candidates ADD COLUMN user_id UUID REFERENCES users(id);
ALTER TABLE candidates ADD COLUMN tenant_id UUID REFERENCES tenants(id);
UPDATE candidates c SET user_id=u.id,tenant_id=u.tenant_id FROM users u
 WHERE lower(c.email)=lower(u.email) AND NOT c.deleted AND NOT u.deleted;
CREATE UNIQUE INDEX uk_candidate_user ON candidates(user_id) WHERE user_id IS NOT NULL;
CREATE INDEX idx_candidate_tenant ON candidates(tenant_id) WHERE NOT deleted;

INSERT INTO permissions(id,version,name,code,description,category,active,deleted,created_at,updated_at)
SELECT gen_random_uuid(),0,x.code,x.code,x.code,'SECURITY',TRUE,FALSE,NOW(),NOW()
FROM (VALUES ('TENANT_ACCESS_ALL'),('CANDIDATE_READ'),('CANDIDATE_MANAGE'),
 ('DOCUMENT_READ'),('DOCUMENT_MANAGE'),('IMPORT_ROLLBACK')) AS x(code)
ON CONFLICT(code) DO NOTHING;
-- No global tenant authority for ADMIN.
INSERT INTO role_permissions(id,version,role_id,permission_id,created_at,updated_at,deleted)
SELECT gen_random_uuid(),0,r.id,p.id,NOW(),NOW(),FALSE FROM roles r CROSS JOIN permissions p
WHERE r.code='SUPER_ADMIN' AND NOT r.deleted AND NOT p.deleted
ON CONFLICT(role_id,permission_id) DO NOTHING;
INSERT INTO role_permissions(id,version,role_id,permission_id,created_at,updated_at,deleted)
SELECT gen_random_uuid(),0,r.id,p.id,NOW(),NOW(),FALSE FROM roles r CROSS JOIN permissions p
WHERE r.code='ADMIN' AND p.code IN ('CANDIDATE_READ','CANDIDATE_MANAGE','DOCUMENT_READ','DOCUMENT_MANAGE','IMPORT_ROLLBACK')
ON CONFLICT(role_id,permission_id) DO NOTHING;
