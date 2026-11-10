-- Operator review on an isolated restored copy first. NOT executed by Codex.
-- This file cannot migrate, baseline, repair, delete or update data.
BEGIN TRANSACTION READ ONLY;
SELECT current_setting('server_version') AS postgres_version,
       current_setting('server_version_num')::integer >= 150000 AS supports_v74_nulls_not_distinct;
SELECT extname, extversion FROM pg_extension WHERE extname IN ('pgcrypto','uuid-ossp','vector');
SELECT to_regclass('public.flyway_schema_history') IS NOT NULL AS has_flyway_history;
SELECT table_name, column_name, data_type FROM information_schema.columns
 WHERE table_schema='public' AND (
  (table_name='faculties' AND column_name IN ('university_id','campus_id')) OR
  (table_name='programs' AND column_name IN ('duration_years','degree_level','faculty_id')) OR
  (table_name='candidates' AND column_name IN ('user_id','tenant_id','academic_data')) OR
  (table_name='refresh_tokens' AND column_name IN ('family_id','token')))
 ORDER BY table_name,column_name;
SELECT conrelid::regclass AS table_name,conname,pg_get_constraintdef(oid) AS definition
 FROM pg_constraint WHERE conname IN ('uk_university_name_country',
 'uk_orientation_guide_country_year','uk_scholarship_name_country_year',
 'uk_recommendation_candidate_guide_program') ORDER BY conname;
-- Only run the next optional statement if has_flyway_history is true:
-- SELECT version,description,type,success FROM public.flyway_schema_history ORDER BY installed_rank;
-- Only run on a restored copy with the legacy tables present (counts, no personal data):
-- SELECT count(*) AS duplicated_candidate_email_groups FROM
--   (SELECT lower(email) FROM candidates WHERE NOT deleted GROUP BY lower(email) HAVING count(*)>1) d;
-- SELECT count(*) AS active_legacy_refresh_sessions FROM refresh_tokens WHERE NOT revoked;
ROLLBACK;
