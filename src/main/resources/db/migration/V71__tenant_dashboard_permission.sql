-- Preserve existing tenant-admin dashboard access with an explicit, tenant-scoped permission.
-- No cross-tenant authority is granted.
INSERT INTO role_permissions(id,version,role_id,permission_id,deleted,created_at,updated_at)
SELECT gen_random_uuid(),0,r.id,p.id,FALSE,NOW(),NOW()
FROM roles r CROSS JOIN permissions p
WHERE r.code='ADMIN' AND p.code='STATISTICS_READ'
AND NOT EXISTS (SELECT 1 FROM role_permissions rp WHERE rp.role_id=r.id AND rp.permission_id=p.id);
