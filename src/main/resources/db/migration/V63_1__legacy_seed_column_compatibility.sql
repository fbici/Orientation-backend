-- Additive compatibility for the immutable V64 legacy seed.
-- Preserve campus/faculty NOT NULL constraints; ambiguous mappings fail instead of fabricating data.
-- A database already beyond V64 requires explicit reviewed out-of-order adoption.
ALTER TABLE faculties ADD COLUMN university_id UUID REFERENCES universities(id);
ALTER TABLE programs ADD COLUMN duration_years INTEGER;
ALTER TABLE programs ADD COLUMN degree_level VARCHAR(30);

CREATE FUNCTION orientia_legacy_faculty_insert() RETURNS trigger LANGUAGE plpgsql AS $$
DECLARE chosen UUID;
BEGIN
 IF NEW.campus_id IS NULL AND NEW.university_id IS NOT NULL THEN
   SELECT id INTO STRICT chosen FROM campuses
   WHERE university_id=NEW.university_id AND main=TRUE AND NOT deleted;
   NEW.campus_id=chosen;
 END IF;
 RETURN NEW;
END $$;
CREATE TRIGGER trg_legacy_faculty_insert BEFORE INSERT ON faculties
 FOR EACH ROW EXECUTE FUNCTION orientia_legacy_faculty_insert();

CREATE FUNCTION orientia_legacy_program_insert() RETURNS trigger LANGUAGE plpgsql AS $$
DECLARE faculty_name TEXT; chosen UUID;
BEGIN
 IF NEW.faculty_id IS NULL AND NEW.duration_years IS NOT NULL AND NEW.degree_level IS NOT NULL THEN
   -- These associations are explicitly described in V64's programme sections.
   faculty_name=CASE NEW.code
     WHEN 'MED' THEN 'Faculte des Sciences de la Sante'
     WHEN 'PHAR' THEN 'Faculte des Sciences de la Sante'
     WHEN 'GCIV' THEN 'Ecole Polytechnique d''Abomey-Calavi'
     WHEN 'GLOG' THEN 'Institut de Formation et de Recherche en Informatique'
     WHEN 'RTEL' THEN 'Institut de Formation et de Recherche en Informatique'
     WHEN 'ECON' THEN 'Faculte des Sciences Economiques et de Gestion'
     WHEN 'DPRV' THEN 'Faculte de Droit et de Sciences Politiques'
     WHEN 'SPOL' THEN 'Faculte de Droit et de Sciences Politiques'
     WHEN 'LETT' THEN 'Faculte des Lettres, Arts et Sciences Humaines'
     WHEN 'MATH' THEN 'Faculte des Sciences et Techniques' END;
   IF faculty_name IS NULL THEN RAISE EXCEPTION 'Legacy programme requires an explicit faculty association'; END IF;
   SELECT f.id INTO STRICT chosen FROM faculties f
     JOIN campuses c ON c.id=f.campus_id JOIN universities u ON u.id=c.university_id
     JOIN countries country ON country.id=u.country_id
     WHERE f.name=faculty_name AND c.main=TRUE AND NOT f.deleted AND NOT c.deleted
       AND country.code='BEN' AND u.name ILIKE '%Abomey-Calavi%';
   NEW.faculty_id=chosen;
   NEW.duration=NEW.duration_years;
   NEW.type=CASE NEW.degree_level WHEN 'LICENCE' THEN 'LICENSE' WHEN 'DOCTORAT' THEN 'DOCTORATE' END;
   IF NEW.type IS NULL THEN RAISE EXCEPTION 'Legacy degree type requires an explicit translation'; END IF;
 END IF;
 RETURN NEW;
END $$;
CREATE TRIGGER trg_legacy_program_insert BEFORE INSERT ON programs
 FOR EACH ROW EXECUTE FUNCTION orientia_legacy_program_insert();
