-- V59: Seed campuses for each university (1 main campus per university)
INSERT INTO campuses (version, university_id, name, city_id, main, active, created_at, updated_at, deleted)
SELECT 0, u.id, u.name || ' - Campus principal', u.city_id, TRUE, TRUE, NOW(), NOW(), FALSE
FROM universities u
WHERE NOT EXISTS (SELECT 1 FROM campuses c WHERE c.university_id = u.id)
ON CONFLICT DO NOTHING;
