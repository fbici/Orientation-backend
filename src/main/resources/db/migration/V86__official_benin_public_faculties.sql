-- Official identities only. Existing programmes, faculty names and review decisions are preserved.
-- Unknown locations remain NULL; the explicit unverified grouping is not a physical campus assertion.
ALTER TABLE campuses ALTER COLUMN city_id DROP NOT NULL;
ALTER TABLE faculties ADD COLUMN registry_source_url VARCHAR(1000);
ALTER TABLE faculties ADD COLUMN registry_source_page INTEGER;
ALTER TABLE faculties ADD COLUMN registry_source_edition VARCHAR(20);
ALTER TABLE faculties ADD COLUMN registry_entity_kind VARCHAR(30);
CREATE FUNCTION pg_temp.orientia_identity(value TEXT) RETURNS TEXT LANGUAGE SQL IMMUTABLE AS $$
 SELECT lower(regexp_replace(translate(value,'ÀÂÄÇÉÈÊËÎÏÔÖÙÛÜŸàâäçéèêëîïôöùûüÿ','AAACEEEEIIOOUUUYaaaceeeeiioouuuy'),'[^a-zA-Z0-9]','','g'))
$$;
DO $$
DECLARE r RECORD; parent_id UUID; country_id_value UUID; city_id_value UUID; campus_id_value UUID; faculty_id_value UUID;
BEGIN
 FOR r IN SELECT * FROM (VALUES
('UAC','CEBELAE','CENTRE BENINOIS DES LANGUES ETRANGERES','Abomey-Calavi','https://recherche-universitaire.uac.bj/wp-content/uploads/2026/01/Annuaire_Statistiques_UAC_2024_2025_en_Redaction_31_12_2025.pdf',9,'2024-2025','CENTRE'),
('UAC','CEFORP','Centre de Formation et de Recherche en matière de Population','Cotonou','https://enseignementsuperieur.gouv.bj/uploads/document_actualite/4fa961e0a40ad936d2999887d6721e15.pdf',39,'2026-2027','CENTRE'),
('UAC','CHAIRE DPHD','CHAIRE UNESCO DES DROITS DE LA PERSONNE HUMAINE ET DE LA DEMOCRATIE','Abomey-Calavi','https://recherche-universitaire.uac.bj/wp-content/uploads/2026/01/Annuaire_Statistiques_UAC_2024_2025_en_Redaction_31_12_2025.pdf',9,'2024-2025','CHAIR'),
('UAC','CIFRED','Centre Inter Facultaire de Formation et de Recherche en Environnement pour le Développement Durable','Abomey-Calavi','https://enseignementsuperieur.gouv.bj/uploads/document_actualite/4fa961e0a40ad936d2999887d6721e15.pdf',25,'2026-2027','CENTRE'),
('UAC','CIPMA','CHAIRE INTERNATIONALE DE PHYSIQUE, MATHEMATIQUES ET APPLICATIONS','Abomey-Calavi','https://recherche-universitaire.uac.bj/wp-content/uploads/2026/01/Annuaire_Statistiques_UAC_2024_2025_en_Redaction_31_12_2025.pdf',9,'2024-2025','CHAIR'),
('UAC','COMCCIDI','CHAIRE OMC COMMERCE INTERNATIONAL ET DEVELOPPEMENT INCLUSIVE','Cotonou','https://recherche-universitaire.uac.bj/wp-content/uploads/2026/01/Annuaire_Statistiques_UAC_2024_2025_en_Redaction_31_12_2025.pdf',9,'2024-2025','CHAIR'),
('UAC','EDPECD','ECOLE DOCTORALE PLURIDISCIPLINAIRE ESPACES, CULTURES ET DÉVELOPPEMENT','Abomey-Calavi','https://recherche-universitaire.uac.bj/wp-content/uploads/2026/01/Annuaire_Statistiques_UAC_2024_2025_en_Redaction_31_12_2025.pdf',9,'2024-2025','DOCTORAL_SCHOOL'),
('UAC','EDPEPSDH','ECOLE DOCTORALE PLURIDISCIPLINAIRE EDUCATION PHYSIQUE, SPORT ET DEVELOPPEMENT HUMAIN','Porto-Novo','https://recherche-universitaire.uac.bj/wp-content/uploads/2026/01/Annuaire_Statistiques_UAC_2024_2025_en_Redaction_31_12_2025.pdf',9,'2024-2025','DOCTORAL_SCHOOL'),
('UAC','EDSAE','ECOLE DOCTORALE DES SCIENCES AGRONOMIQUES ET DE L''EAU','Abomey-Calavi','https://recherche-universitaire.uac.bj/wp-content/uploads/2026/01/Annuaire_Statistiques_UAC_2024_2025_en_Redaction_31_12_2025.pdf',9,'2024-2025','DOCTORAL_SCHOOL'),
('UAC','EDSDI','ECOLE DOCTORALE DES SCIENCES DE L''INGÉNIEUR','Abomey-Calavi','https://recherche-universitaire.uac.bj/wp-content/uploads/2026/01/Annuaire_Statistiques_UAC_2024_2025_en_Redaction_31_12_2025.pdf',9,'2024-2025','DOCTORAL_SCHOOL'),
('UAC','EDSEA','ECOLE DOCTORALE DES SCIENCES EXACTES ET APPLIQUÉES','Abomey-Calavi','https://recherche-universitaire.uac.bj/wp-content/uploads/2026/01/Annuaire_Statistiques_UAC_2024_2025_en_Redaction_31_12_2025.pdf',9,'2024-2025','DOCTORAL_SCHOOL'),
('UAC','EDSEG','ECOLE DOCTORALE DES SCIENCES ECONOMIQUES ET DE GESTION','Cotonou','https://recherche-universitaire.uac.bj/wp-content/uploads/2026/01/Annuaire_Statistiques_UAC_2024_2025_en_Redaction_31_12_2025.pdf',9,'2024-2025','DOCTORAL_SCHOOL'),
('UAC','EDSJPA','ECOLE DOCTORALE DES SCIENCES JURIDIQUES, POLITIQUES ET ADMINISTRATIVES','Abomey-Calavi','https://recherche-universitaire.uac.bj/wp-content/uploads/2026/01/Annuaire_Statistiques_UAC_2024_2025_en_Redaction_31_12_2025.pdf',9,'2024-2025','DOCTORAL_SCHOOL'),
('UAC','EDSS','ECOLE DOCTORALE DES SCIENCES DE LA SANTÉ','Cotonou','https://recherche-universitaire.uac.bj/wp-content/uploads/2026/01/Annuaire_Statistiques_UAC_2024_2025_en_Redaction_31_12_2025.pdf',9,'2024-2025','DOCTORAL_SCHOOL'),
('UAC','EDSVT','ECOLE DOCTORALE DES SCIENCES DE LA VIE ET DE LA TERRE','Abomey-Calavi','https://recherche-universitaire.uac.bj/wp-content/uploads/2026/01/Annuaire_Statistiques_UAC_2024_2025_en_Redaction_31_12_2025.pdf',9,'2024-2025','DOCTORAL_SCHOOL'),
('UAC','ENA','École Nationale d''Administration','Abomey-Calavi','https://enseignementsuperieur.gouv.bj/uploads/document_actualite/4fa961e0a40ad936d2999887d6721e15.pdf',32,'2026-2027','SCHOOL'),
('UAC','ENEAM','Ecole Nationale d’Economie Appliquée et de Management','Cotonou','https://enseignementsuperieur.gouv.bj/uploads/document_actualite/4fa961e0a40ad936d2999887d6721e15.pdf',27,'2026-2027','SCHOOL'),
('UAC','ENS','École Normale Supérieure de Porto-Novo','Porto-Novo','https://enseignementsuperieur.gouv.bj/uploads/document_actualite/4fa961e0a40ad936d2999887d6721e15.pdf',42,'2026-2027','SCHOOL'),
('UAC','ENSTIC','École Nationale des Sciences et Techniques de l''Information et de la Communication','Abomey-Calavi','https://enseignementsuperieur.gouv.bj/uploads/document_actualite/4fa961e0a40ad936d2999887d6721e15.pdf',32,'2026-2027','SCHOOL'),
('UAC','EPA','École du Patrimoine Africain','Porto-Novo','https://enseignementsuperieur.gouv.bj/uploads/document_actualite/4fa961e0a40ad936d2999887d6721e15.pdf',31,'2026-2027','SCHOOL'),
('UAC','EPAC','École Polytechnique d’Abomey-Calavi','Abomey-Calavi','https://enseignementsuperieur.gouv.bj/uploads/document_actualite/4fa961e0a40ad936d2999887d6721e15.pdf',37,'2026-2027','SCHOOL'),
('UAC','FADESP','Faculté de Droit et de Science Politique','Abomey-Calavi','https://enseignementsuperieur.gouv.bj/uploads/document_actualite/4fa961e0a40ad936d2999887d6721e15.pdf',42,'2026-2027','FACULTY'),
('UAC','FASEG','Faculté des Sciences Économiques et de Gestion','Abomey-Calavi','https://enseignementsuperieur.gouv.bj/uploads/document_actualite/4fa961e0a40ad936d2999887d6721e15.pdf',43,'2026-2027','FACULTY'),
('UAC','FASHS','Faculté des Sciences Humaines et Sociales','Abomey-Calavi','https://enseignementsuperieur.gouv.bj/uploads/document_actualite/4fa961e0a40ad936d2999887d6721e15.pdf',31,'2026-2027','FACULTY'),
('UAC','FAST','Faculté des Sciences Techniques','Abomey-Calavi','https://enseignementsuperieur.gouv.bj/uploads/document_actualite/4fa961e0a40ad936d2999887d6721e15.pdf',44,'2026-2027','FACULTY'),
('UAC','FLASH-Adjarra','Faculté des Lettres, Arts et Sciences Humaines-Adjarra','Adjarra','https://enseignementsuperieur.gouv.bj/uploads/document_actualite/4fa961e0a40ad936d2999887d6721e15.pdf',23,'2026-2027','FACULTY'),
('UAC','FLLAC','Faculté des Lettres, Langues, Arts et Communications','Abomey-Calavi','https://enseignementsuperieur.gouv.bj/uploads/document_actualite/4fa961e0a40ad936d2999887d6721e15.pdf',24,'2026-2027','FACULTY'),
('UAC','FSA','Faculté des Sciences Agronomiques','Abomey-Calavi','https://enseignementsuperieur.gouv.bj/uploads/document_actualite/4fa961e0a40ad936d2999887d6721e15.pdf',35,'2026-2027','FACULTY'),
('UAC','FSS','Faculté des Sciences de la Santé','Cotonou','https://enseignementsuperieur.gouv.bj/uploads/document_actualite/4fa961e0a40ad936d2999887d6721e15.pdf',36,'2026-2027','FACULTY'),
('UAC','HERCI','Haute École Régionale de Commerce International','Seme-Kpodji','https://enseignementsuperieur.gouv.bj/uploads/document_actualite/4fa961e0a40ad936d2999887d6721e15.pdf',40,'2026-2027','SCHOOL'),
('UAC','IC','Institut Confucius','Abomey-Calavi','https://enseignementsuperieur.gouv.bj/uploads/document_actualite/4fa961e0a40ad936d2999887d6721e15.pdf',45,'2026-2027','INSTITUTE'),
('UAC','ICaV','Institut de Cadre de Vie','Abomey-Calavi','https://enseignementsuperieur.gouv.bj/uploads/document_actualite/4fa961e0a40ad936d2999887d6721e15.pdf',25,'2026-2027','INSTITUTE'),
('UAC','IFRI','Institut de Formation et de Recherche en Informatique','Abomey-Calavi','https://enseignementsuperieur.gouv.bj/uploads/document_actualite/4fa961e0a40ad936d2999887d6721e15.pdf',33,'2026-2027','INSTITUTE'),
('UAC','ILACI','Institut de Langue Arabe et Culture Islamique','Abomey-Calavi','https://enseignementsuperieur.gouv.bj/uploads/document_actualite/4fa961e0a40ad936d2999887d6721e15.pdf',45,'2026-2027','INSTITUTE'),
('UAC','IMSP','Institut de Mathématiques et de Sciences Physiques','Dangbo','https://enseignementsuperieur.gouv.bj/uploads/document_actualite/4fa961e0a40ad936d2999887d6721e15.pdf',23,'2026-2027','INSTITUTE'),
('UAC','INE','Institut National de l’Eau','Abomey-Calavi','https://enseignementsuperieur.gouv.bj/uploads/document_actualite/4fa961e0a40ad936d2999887d6721e15.pdf',26,'2026-2027','INSTITUTE'),
('UAC','INEPS','Institut National de l’Education Physique et Sportive','Porto-Novo','https://enseignementsuperieur.gouv.bj/uploads/document_actualite/4fa961e0a40ad936d2999887d6721e15.pdf',40,'2026-2027','INSTITUTE'),
('UAC','INMAAC','Institut National des Métiers d’Arts, d’Archéologie et de la Culture','Abomey-Calavi','https://enseignementsuperieur.gouv.bj/uploads/document_actualite/4fa961e0a40ad936d2999887d6721e15.pdf',24,'2026-2027','INSTITUTE'),
('UAC','INMeS','Institut National Médico-Sanitaire','Cotonou','https://enseignementsuperieur.gouv.bj/uploads/document_actualite/4fa961e0a40ad936d2999887d6721e15.pdf',26,'2026-2027','INSTITUTE'),
('UAC','IRSP','Institut Régional de Santé Publique','Ouidah','https://enseignementsuperieur.gouv.bj/uploads/document_actualite/4fa961e0a40ad936d2999887d6721e15.pdf',23,'2026-2027','INSTITUTE'),
('UP','FA','Faculté d''Agronomie',NULL,'https://enseignementsuperieur.gouv.bj/uploads/document_actualite/4fa961e0a40ad936d2999887d6721e15.pdf',47,'2026-2027','FACULTY'),
('UP','FM','Faculté de Médecine',NULL,'https://enseignementsuperieur.gouv.bj/uploads/document_actualite/4fa961e0a40ad936d2999887d6721e15.pdf',47,'2026-2027','FACULTY'),
('UP','ENATSE','École Nationale de formation des Techniciens Supérieurs en Santé Publique et Surveillance Epidémiologique',NULL,'https://enseignementsuperieur.gouv.bj/uploads/document_actualite/4fa961e0a40ad936d2999887d6721e15.pdf',48,'2026-2027','SCHOOL'),
('UP','IFSIO','Institut de Formation en Soins Infirmiers et Obstétricaux',NULL,'https://enseignementsuperieur.gouv.bj/uploads/document_actualite/4fa961e0a40ad936d2999887d6721e15.pdf',48,'2026-2027','INSTITUTE'),
('UP','IUT','Institut Universitaire de Technologie',NULL,'https://enseignementsuperieur.gouv.bj/uploads/document_actualite/4fa961e0a40ad936d2999887d6721e15.pdf',48,'2026-2027','INSTITUTE'),
('UP','ENSPD','École Nationale de Statistique, de Planification et de Démographie',NULL,'https://enseignementsuperieur.gouv.bj/uploads/document_actualite/4fa961e0a40ad936d2999887d6721e15.pdf',50,'2026-2027','SCHOOL'),
('UP','FASEG','Faculté de Sciences Économiques et de Gestion',NULL,'https://enseignementsuperieur.gouv.bj/uploads/document_actualite/4fa961e0a40ad936d2999887d6721e15.pdf',50,'2026-2027','FACULTY'),
('UP','FDSP','Faculté de Droit et Sciences Politiques',NULL,'https://enseignementsuperieur.gouv.bj/uploads/document_actualite/4fa961e0a40ad936d2999887d6721e15.pdf',53,'2026-2027','FACULTY'),
('UP','FLASH','Faculté des Lettres, Arts et Sciences Humaines',NULL,'https://enseignementsuperieur.gouv.bj/uploads/document_actualite/4fa961e0a40ad936d2999887d6721e15.pdf',53,'2026-2027','FACULTY'),
('UNSTIM','ENSET','École Normale Supérieure de l’Enseignement technique',NULL,'https://enseignementsuperieur.gouv.bj/uploads/document_actualite/4fa961e0a40ad936d2999887d6721e15.pdf',56,'2026-2027','SCHOOL'),
('UNSTIM','INSTI','Institut National Supérieur de Technologie Industrielle',NULL,'https://enseignementsuperieur.gouv.bj/uploads/document_actualite/4fa961e0a40ad936d2999887d6721e15.pdf',57,'2026-2027','INSTITUTE'),
('UNSTIM','INSPEI','Institut National Supérieur des Classes Préparatoires aux Études d''Ingénieurs',NULL,'https://enseignementsuperieur.gouv.bj/uploads/document_actualite/4fa961e0a40ad936d2999887d6721e15.pdf',60,'2026-2027','INSTITUTE'),
('UNSTIM','ENS/ Nati','École Normale Supérieure de Natitingou',NULL,'https://enseignementsuperieur.gouv.bj/uploads/document_actualite/4fa961e0a40ad936d2999887d6721e15.pdf',60,'2026-2027','SCHOOL'),
('UNSTIM','ENSBBA','École Nationale Supérieure des Biosciences et Biotechnologies Appliquées',NULL,'https://enseignementsuperieur.gouv.bj/uploads/document_actualite/4fa961e0a40ad936d2999887d6721e15.pdf',60,'2026-2027','SCHOOL'),
('UNSTIM','FAST/ Natitingou','Faculté des Sciences et Techniques de Natitingou',NULL,'https://enseignementsuperieur.gouv.bj/uploads/document_actualite/4fa961e0a40ad936d2999887d6721e15.pdf',63,'2026-2027','FACULTY'),
('UNSTIM','ENSGEP','École Nationale Supérieure de Génie Energétique et Procédés',NULL,'https://enseignementsuperieur.gouv.bj/uploads/document_actualite/4fa961e0a40ad936d2999887d6721e15.pdf',64,'2026-2027','SCHOOL'),
('UNSTIM','ENSTP','École Nationale Supérieure des Travaux Publics',NULL,'https://enseignementsuperieur.gouv.bj/uploads/document_actualite/4fa961e0a40ad936d2999887d6721e15.pdf',66,'2026-2027','SCHOOL'),
('UNA','EAq','École d''Aquaculture',NULL,'https://enseignementsuperieur.gouv.bj/uploads/document_actualite/4fa961e0a40ad936d2999887d6721e15.pdf',70,'2026-2027','SCHOOL'),
('UNA','EHAEV','École d''Horticulture et d''Aménagement des Espaces Verts',NULL,'https://enseignementsuperieur.gouv.bj/uploads/document_actualite/4fa961e0a40ad936d2999887d6721e15.pdf',71,'2026-2027','SCHOOL'),
('UNA','EGPVS','École de Gestion et de Production Végétale et Semencière',NULL,'https://enseignementsuperieur.gouv.bj/uploads/document_actualite/4fa961e0a40ad936d2999887d6721e15.pdf',72,'2026-2027','SCHOOL'),
('UNA','ESTCTPA','École des Sciences et Techniques de Conservation et de Transformation des Produits Agricoles',NULL,'https://enseignementsuperieur.gouv.bj/uploads/document_actualite/4fa961e0a40ad936d2999887d6721e15.pdf',72,'2026-2027','SCHOOL'),
('UNA','EGR','École de Génie Rural',NULL,'https://enseignementsuperieur.gouv.bj/uploads/document_actualite/4fa961e0a40ad936d2999887d6721e15.pdf',74,'2026-2027','SCHOOL'),
('UNA','EGESE','École de Gestion et d''Exploitation des Systèmes d''Elevage',NULL,'https://enseignementsuperieur.gouv.bj/uploads/document_actualite/4fa961e0a40ad936d2999887d6721e15.pdf',77,'2026-2027','SCHOOL'),
('UNA','EAPA','École d’Agrobusiness et de Politiques Agricoles',NULL,'https://enseignementsuperieur.gouv.bj/uploads/document_actualite/4fa961e0a40ad936d2999887d6721e15.pdf',78,'2026-2027','SCHOOL'),
('UNA','ESRVA','École de Sociologie rurale et de Vulgarisation Agricole',NULL,'https://enseignementsuperieur.gouv.bj/uploads/document_actualite/4fa961e0a40ad936d2999887d6721e15.pdf',79,'2026-2027','SCHOOL'),
('UNA','EForT','École de Foresterie Tropicale',NULL,'https://enseignementsuperieur.gouv.bj/uploads/document_actualite/4fa961e0a40ad936d2999887d6721e15.pdf',80,'2026-2027','SCHOOL')
 ) AS source(acronym,code,name,city,source_url,source_page,edition,kind) LOOP
  SELECT u.id,u.country_id INTO parent_id,country_id_value FROM universities u JOIN countries p ON p.id=u.country_id
   WHERE p.code='BEN' AND NOT u.deleted AND u.tenant_id IS NULL AND upper(u.short_name)=r.acronym ORDER BY u.created_at,u.id LIMIT 1;
  IF parent_id IS NULL THEN RAISE EXCEPTION 'Official public university association is missing: %',r.acronym; END IF;
  city_id_value=NULL;campus_id_value=NULL;faculty_id_value=NULL;
  IF r.city IS NOT NULL THEN
   SELECT id INTO city_id_value FROM cities WHERE country_id=country_id_value AND NOT deleted AND pg_temp.orientia_identity(name)=pg_temp.orientia_identity(r.city) ORDER BY created_at,id LIMIT 1;
   IF city_id_value IS NULL THEN INSERT INTO cities(name,country_id) VALUES(r.city,country_id_value) RETURNING id INTO city_id_value; END IF;
   SELECT id INTO campus_id_value FROM campuses WHERE university_id=parent_id AND city_id=city_id_value AND NOT deleted ORDER BY main DESC,created_at,id LIMIT 1;
   IF campus_id_value IS NULL THEN INSERT INTO campuses(university_id,name,city_id,main,verified) VALUES(parent_id,'Centre universitaire de '||r.city,city_id_value,false,false) RETURNING id INTO campus_id_value; END IF;
  ELSE
   SELECT id INTO campus_id_value FROM campuses WHERE university_id=parent_id AND name='Implantation à préciser — rattachement universitaire' AND NOT deleted ORDER BY created_at,id LIMIT 1;
   IF campus_id_value IS NULL THEN INSERT INTO campuses(university_id,name,city_id,main,verified) VALUES(parent_id,'Implantation à préciser — rattachement universitaire',NULL,false,false) RETURNING id INTO campus_id_value; END IF;
  END IF;
  SELECT f.id INTO faculty_id_value FROM faculties f JOIN campuses c ON c.id=f.campus_id
   WHERE c.university_id=parent_id AND NOT f.deleted AND (upper(f.code)=upper(r.code) OR pg_temp.orientia_identity(f.name)=pg_temp.orientia_identity(r.name))
   ORDER BY (upper(f.code)=upper(r.code)) DESC NULLS LAST,f.verified DESC,f.created_at,f.id LIMIT 1;
  IF faculty_id_value IS NULL THEN
   INSERT INTO faculties(campus_id,university_id,name,code,description,verified,registry_source_url,registry_source_page,registry_source_edition,registry_entity_kind)
    VALUES(campus_id_value,parent_id,r.name,r.code,'Identité documentée ; données académiques et implantation à revoir avant publication.',false,r.source_url,r.source_page,r.edition,r.kind);
  ELSE
   UPDATE faculties target SET campus_id=CASE
     WHEN NOT target.verified AND r.city IS NOT NULL AND NOT EXISTS(SELECT 1 FROM faculties other WHERE other.campus_id=campus_id_value AND other.name=target.name AND other.id<>target.id)
     THEN campus_id_value ELSE target.campus_id END,
    registry_source_url=r.source_url,registry_source_page=r.source_page,registry_source_edition=r.edition,registry_entity_kind=r.kind
    WHERE id=faculty_id_value AND registry_source_url IS NULL;
   -- Faculty IDs and programme associations are retained. Reviewed physical locations are preserved.
  END IF;
 END LOOP;
END $$;
