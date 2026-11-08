> Historique de la mission precedente. Pour le service retire et le nouveau deploiement sur base vierge, consulter [le nouveau guide](RENDER_NOUVEAU_DEPLOIEMENT.md).

# Orientia - Validation du backend Render existant

Ce document prepare une mise a jour du service existant. Il ne cree aucun service,
ne lance aucune migration et ne donne aucune autorisation de deploiement.

## Publication suspendue

Le service connu est https://orientation-backend-4lcp.onrender.com.
Le profil prod utilise le contexte / : les API sont /auth/login, /transcripts,
/candidates/me/academic-profile, etc. Le contexte /api/v1 est celui du profil dev.
L'etat de l'auto-deploiement, des previews, de la base et du disque Render reste
inconnu faute d'acces au tableau de bord. Aucun push backend, meme de validation,
n'est effectue tant que l'absence de deploiement et les conditions de securite ne
sont pas confirmees. La CI de ce depot ne contient aucun job de deploiement.

L'historique public contient une ancienne cle JWT de repli dans application.yml
et un ancien mot de passe d'amorcage dans H.java/HashGen.java et le seed V38.
Les valeurs ne sont pas reproduites. Le code courant n'utilise plus ces valeurs.
Faire confirmer la rotation des secrets et du compte concerne s'ils ont ete
utilises sur Render. Ne jamais reemployer ces valeurs. Aucun historique Git n'est
reecrit, aucune migration historique n'est modifiee.

## Configuration a verifier dans le service existant

| Variable | Attendu |
|---|---|
| SPRING_PROFILES_ACTIVE | prod (egalement fixe par l'ENTRYPOINT du Dockerfile) |
| SPRING_DATASOURCE_URL | URL JDBC existante, jdbc:postgresql://HOST:PORT/BASE?sslmode=require |
| SPRING_DATASOURCE_USERNAME | utilisateur Render existant |
| SPRING_DATASOURCE_PASSWORD | secret Render ; aucune valeur dans Git |
| JWT_SECRET | valeur aleatoire renouvelee, au moins 32 octets ; aucun repli public |
| PORT | valeur fournie par Render ; application-prod.yml la reprend |
| APP_BASE_URL | https://orientation-backend-4lcp.onrender.com, sans /api/v1 |
| CORS_ALLOWED_ORIGINS | origines Web explicites, sans joker avec credentials |
| SENDGRID_ENABLED | true seulement si fournisseur et expediteur verifies |
| SENDGRID_API_KEY | secret du fournisseur, hors Git |
| SENDGRID_FROM_EMAIL | expediteur effectivement autorise par le fournisseur |
| REGISTRATION_TENANT_CODE | code d'un tenant public deja configure et valide |
| STORAGE_DIR | chemin du disque persistant existant, accessible a UID/GID 10001 |
| TESSERACT_COMMAND | tesseract, installe dans l'image runtime |
| OCR_LANGUAGES | fra+eng, paquets installes dans l'image |
| TESSDATA_DIR | facultatif si installation Debian standard |
| OCR_TIMEOUT_SECONDS | 45 par defaut |
| BOOTSTRAP_ENABLED | false ; aucune creation silencieuse de compte administrateur |

L'URL Render DATABASE_URL au format postgresql:// n'est pas directement une URL
JDBC. Aucun repli vers PostgreSQL localhost n'est permis en prod.
Le Dockerfile conserve Java 21, utilise Node 22 pour Angular 19 et installe
Tesseract fra+eng. Le runtime est non privilegie (UID/GID 10001), headless, avec
uploads/logs prives. Confirmer les permissions du disque monte avant deploiement.
Un systeme de fichiers ephemere ne garantit pas la conservation des documents.
Aucun changement de plan Render ni montage de disque n'a ete effectue.

Node 20 est en fin de vie ; Node 22 est compatible avec Angular 19.2 :
- https://nodejs.org/en/about/eol
- https://angular.dev/reference/versions

## Migrations : arret obligatoire avant la production

83 fichiers SQL sont presents, jusqu'a V82 (V63.1 compris). Les migrations
historiques restent intactes. Ne pas lancer Flyway migrate, repair, baseline,
clean ou outOfOrder sur Render dans le cadre de cette synchronisation.

| Point | Risque sur une base existante |
|---|---|
| Ancien Hibernate update / Flyway desactive | Schema peuple sans historique Flyway : le demarrage fail-fast peut refuser de demarrer ; ne pas activer baseline-on-migrate aveuglement |
| V63.1 avant V64 | Si V64 est deja enregistree, la migration intercalee exige un plan d'adoption revu sur copie ; les triggers doivent obtenir une association unique |
| V65 | pgcrypto requis ; revocation des anciennes sessions et changement du format des tokens ; reconnexion des utilisateurs |
| V66 | Rattachement candidats/comptes par email ; ambiguite ou doublons peuvent empecher l'index unique ; lignes non rattachees a examiner |
| V67-V71 | Objets manquants, colonnes et permissions : confirmer le schema reel et les noms de contraintes |
| V72-V76 | Provenance et perimetre tenant ; ancien catalogue non automatiquement certifie |
| V74 | UNIQUE NULLS NOT DISTINCT exige PostgreSQL >=15 ; contraintes historiques nommees attendues ; risque de doublons ou de schema divergent |
| V77-V82 | Nouvelles colonnes, FK et unicites ; verifier la reprise des recommandations et les doublons de confirmations |

PostgreSQL 16 dans docker-compose/CI est une intention, pas une preuve de la
version Render. Les tests locaux PostgreSQL 18.4 sur schema vierge ne prouvent pas
la compatibilite d'une base peuplee. La version Render reelle reste a confirmer.
Reference SQL : https://www.postgresql.org/docs/15/sql-createtable.htm

Procedure a faire approuver : sauvegarde verifiee et restaurable ; restauration
isolee ; revue en lecture seule (render-preflight-readonly.sql) ; comparaison de
l'historique et des contraintes ; repetition de l'upgrade complet sur copie avec
volumetrie et cas existants ; sauvegarde des documents ; plan de retour incluant
la base ; rotation des secrets ; fenetre de maintenance et reconnexion ; puis
seulement autorisation explicite de mise a jour de la branche qui deploie.
Ne pas utiliser ces scripts pour interroger la production sans autorisation.

## Compatibilite avec le mobile

Le mobile F3.3 attend les routes et contrats F1-F3.2. La publication mobile seule
ne met pas le backend a jour. Tant que le backend valide n'est pas deploye, un
ancien service Render peut refuser les nouveaux parcours. Ne pas masquer cette
incompatibilite avec des donnees fictives.

Les reglages Render externes restent prioritaires : la suppression du job deploy
GitHub ne desactive pas un auto-deploiement configure dans Render.
Reference : https://render.com/docs/deploys
