-- Lecture seule : exécuter sur une copie isolée ou via un accès explicitement autorisé.
BEGIN TRANSACTION READ ONLY;
SELECT COALESCE(institution_type, 'NON_RENSEIGNE') AS type,
       count(*) AS etablissements, count(*) FILTER (WHERE verified) AS verifies,
       count(*) FILTER (WHERE NULLIF(trim(source_url), '') IS NOT NULL) AS sources
FROM universities u JOIN countries c ON c.id=u.country_id
WHERE c.code='BEN' AND NOT u.deleted
GROUP BY institution_type ORDER BY type;
SELECT u.name,u.short_name,ci.name AS ville,u.institution_type,u.verified,u.source_url
FROM universities u JOIN countries c ON c.id=u.country_id JOIN cities ci ON ci.id=u.city_id
WHERE c.code='BEN' AND NOT u.deleted ORDER BY u.name;
COMMIT;
