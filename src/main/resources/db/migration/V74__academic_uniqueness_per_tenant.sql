-- Evolve natural keys for the additive tenant ownership columns. No rows are removed.
-- NULL remains the shared legacy/public catalogue scope.
ALTER TABLE universities DROP CONSTRAINT uk_university_name_country;
ALTER TABLE universities ADD CONSTRAINT uk_university_name_country_tenant UNIQUE NULLS NOT DISTINCT(name,country_id,tenant_id);
ALTER TABLE orientation_guides DROP CONSTRAINT uk_orientation_guide_country_year;
ALTER TABLE orientation_guides ADD CONSTRAINT uk_guide_country_year_tenant UNIQUE NULLS NOT DISTINCT(country_id,academic_year_id,tenant_id);
ALTER TABLE scholarships DROP CONSTRAINT uk_scholarship_name_country_year;
ALTER TABLE scholarships ADD CONSTRAINT uk_scholarship_name_country_year_tenant UNIQUE NULLS NOT DISTINCT(name,country_id,academic_year_id,tenant_id);
