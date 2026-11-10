# Orientia — backend et backoffice

Plateforme d'orientation universitaire et de recherche de bourses, d'abord pour le Bénin, puis ouverte aux autres pays. Spring Boot 3.4.3, Java 21, PostgreSQL, Flyway/JPA, JWT ; backoffice Angular 19 dans `frontend/`. Dépôt : [Orientation-backend](https://github.com/fbici/Orientation-backend), branche `main`. Documentation consolidée le 10 novembre 2026.

**Ce README est la documentation de référence backend.** Le README de `frontend/` couvre Angular ; celui du dépôt mobile couvre Flutter. Toute évolution met à jour ces fichiers : ne plus créer de rapports de sprint, récapitulatifs ou fichiers PREUVES. Les anciens rapports ont été consolidés puis supprimés ; Git conserve leur historique. Les scripts et les données sources utiles restent des fichiers techniques.

## Architecture et travaux réalisés

| Lot | Résultat implémenté |
|---|---|
| Audit global | Inventaire des modules et contrats, détection des mocks, fausses métriques et risques de sécurité ; diagnostic séparé des résultats exécutés |
| F1 — fondations | Suppression des valeurs par défaut d'administration/JWT, séparation des tenants et permissions, stockage protégé, sessions/refresh révoquables, récupération/vérification par jetons expirables, bootstrap explicite, Flyway et Hibernate validate |
| F2 — métier et documents | Catalogue/CRUD et filtres, guides/version/source/revue/publication, pipeline d'upload/PDF/OCR Tesseract et extraction structurée, imports tabulaires traçables, revue humaine et provenance avant publication |
| F3.1 — candidat | Profil académique versionné, confirmation des relevés, normalisation de notes, éligibilité à trois états, recommandations et explications fondées sur des critères disponibles, historique des calculs |
| F3.2 — intelligence | Simulations, comparaisons, bourses, assistant documentaire, décisions historisées et exports ; aucune probabilité ni donnée académique arbitraire présentée comme réelle |
| F3.3 — contrats mobiles | Vérification de l'intégration Flutter, sécurité de session, multipart/refresh et parcours isolés ; aucun test authentifié de production sans compte de test autorisé |
| Backoffice | Contrats API, utilisateurs/organisations/notifications/dashboard/monitoring, pagination, erreurs et droits ; retrait des indicateurs fictifs et désactivation explicite des fonctions sans API |
| Auth Experience | Pages Web connexion/inscription/vérification/récupération/reset animées, logo existant, validation et états réels ; la version actuellement observée sur Render est blanche et centrée |
| Messagerie administrative | Réutilisation de SendGrid après commit pour notifications admin et événements documentaires READY_REVIEW/FAILED ; contenu générique et filtrage des comptes/rôles |
| Catalogue Bénin | Migration additive V83, registre MESRS 2025–2026, gestion d'une ville inconnue sans localisation inventée ; revue éditoriale conservée |

Les accès tenant, permissions, ressources et fichiers sont contrôlés côté serveur. Les données non confirmées restent inconnues ; un résultat de recherche documentaire ne vaut pas une décision d'admission. Les anciennes données de démonstration ne constituent pas des références officielles publiées.

## Développement et tests

```powershell
java -version
.\mvnw.cmd verify
cd frontend
npm ci
npm run build:prod
npm test -- --watch=false --browsers=ChromeHeadless
```

Java **21** requis. Le profil local doit utiliser une base de développement dédiée et des variables privées ; aucun secret n'est commité. Les tests d'intégration PostgreSQL F1 exigent `-Df1.jdbc.url=jdbc:postgresql://127.0.0.1:PORT/orientia_f1`, un schéma neuf `-Df1.jdbc.schema=SCHEMA_ISOLE` et `F1_TEST_DB_PASSWORD` dans l'environnement. OCR réel : `-Df2.tesseract=CHEMIN_TESSERACT -Df2.tessdata=CHEMIN_TESSDATA`. Les tests SendGrid restent simulés ; fixer `SENDGRID_ENABLED=false` pour la recette isolée. Ne jamais substituer une connexion Render à ces paramètres.

La suite complète finale a obtenu **219 tests / 37 suites, aucun échec, erreur ou test ignoré**, avec PostgreSQL privé 18.4 et un schéma vierge. Le registre final des 117 identités, la validation Hibernate/Flyway et les réponses API avec ville inconnue sont couverts. Le serveur PostgreSQL de test a été arrêté proprement après la campagne ; aucune base de production n’a été modifiée manuellement. Angular : **86 tests réussis**, build production réussi. Les avertissements connus concernent le budget CSS du layout et le module CommonJS sockjs-client. Le succès local PostgreSQL 18.4 n'est pas une certification de prise en charge par Flyway : sa version annonce une plage testée jusqu'à 17. PostgreSQL **16** est la cible Render et CI.

## Production Render

URL communiquée par le propriétaire : **https://orientation-backend-d0ys.onrender.com**. Santé : `/actuator/health`. Pages publiques : `/auth/login`, `/auth/register`, `/auth/verify-email`, `/auth/forgot-password`, `/auth/reset-password`. Le profil `prod` utilise le contexte **racine**, sans `/api/v1`. Angular et Spring sont servis ensemble ; WebSocket sur `/ws` à la même origine. Les GET publics ne certifient pas les opérations d'administration ni l'état de toutes les données.

Le propriétaire confirme Java 21, PostgreSQL 16, les 83 migrations initiales et le bootstrap désactivé après création administrateur. Un navigateur neuf a confirmé le 10 novembre le formulaire Web blanc avec logo Orientia et vrai G Google, aux formats ordinateur et mobile, sans débordement. Aucun formulaire de production n'a été soumis pendant ce contrôle. Un push Git ne prouve pas que Render a terminé son déploiement.

Service : **Web Service Docker**, dépôt ci-dessus, branche `main`, racine du dépôt, `Dockerfile` racine. Render construit le Dockerfile ; laisser vides les commandes natives et la surcharge Docker Command pour conserver l'ENTRYPOINT.

```sh
# Équivalent de build local
docker build -t orientia .
# Commande de démarrage effective, WORKDIR /app
java -Djava.awt.headless=true -jar app.jar --spring.profiles.active=prod
```

Le Dockerfile compile Angular avec Node 22 (`npm ci`, `npx ng build --configuration=production`) puis Java 21 (`mvn clean package -DskipTests -B`). Les tests sont exécutés séparément. `PORT` est fourni par Render, bind `0.0.0.0`. Le build d'image Docker local n'a pas été exécuté faute de moteur Docker ; le JAR et ses assets Angular ont été validés séparément.

### Variables d'environnement — aucune valeur secrète dans Git

| Variable | Usage |
|---|---|
| `SPRING_PROFILES_ACTIVE` | `prod` |
| `SPRING_DATASOURCE_URL` | JDBC PostgreSQL réel, sans identifiants dans l'URL |
| `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD` | Identifiants de la nouvelle base, hors Git |
| `JWT_SECRET` | Nouvelle valeur aléatoire d'au moins 32 octets ; ne jamais réutiliser l'ancien défaut |
| `APP_BASE_URL` | `https://orientation-backend-d0ys.onrender.com` |
| `CORS_ALLOWED_ORIGINS` | Origines HTTPS exactes, séparées par virgule, sans joker avec credentials |
| `SENDGRID_ENABLED`, `SENDGRID_API_KEY`, `SENDGRID_FROM_EMAIL` | Activation, nouvelle clé privée Mail Send et expéditeur vérifié |
| `PASSWORD_RESET_URL` | Facultative ; repli `APP_BASE_URL/auth/reset-password` |
| `ADMIN_NOTIFICATION_EMAIL` | Destinataire central, défaut `adminorientia@gmail.com` |
| `ADMIN_NOTIFICATIONS_EMAIL_ENABLED` | Défaut true ; false désactive les alertes centrales |
| `REGISTRATION_TENANT_CODE` | `DEFAULT`, tenant actif |
| `STORAGE_DIR` | `/app/uploads`, chemin persistant inscriptible UID/GID 10001 |
| `TESSERACT_COMMAND`, `OCR_LANGUAGES` | `tesseract`, `fra+eng` |
| `TESSDATA_DIR`, `OCR_TIMEOUT_SECONDS` | Répertoire facultatif et timeout OCR, défaut 45 s |
| `BOOTSTRAP_ENABLED` | **false** en fonctionnement normal |
| `BOOTSTRAP_EMAIL`, `BOOTSTRAP_PASSWORD`, `BOOTSTRAP_TENANT_CODE` | Amorçage initial uniquement, secret neuf hors Git |
| `PORT` | Géré par Render |

JDBC attendu : `jdbc:postgresql://HOST_INTERNE:5432/BASE?sslmode=require`, avec les vrais champs Render et TLS conservé. Ne pas fournir une URI `postgresql://` non convertie comme URL JDBC. Conserver la même région pour le service et PostgreSQL. Le stockage des relevés nécessite un disque persistant ou une solution pérenne ; le disque Render implique un service compatible. Tester permissions et sauvegardes, sans confondre disponibilité de la base et persistance des fichiers.

### Flyway et bootstrap

Les migrations existantes V1–V82 incluent V63.1 : **83 scripts historiques**, conservés depuis le déploiement propre. V83 ajoute le registre béninois et rend `universities.city_id` facultatif ; **84 scripts au total**. Les campus conservent leur ville obligatoire.

Sur une base vierge, Flyway exécute dans l'ordre, puis Hibernate valide. Sur la base existante à 83 migrations, seul V83 doit s'ajouter. Conserver `flyway.enabled=true`, `baseline-on-migrate=false`, `clean-disabled=true`, `ddl-auto=validate`, `sql.init.mode=never`. V1 exige les droits nécessaires pour uuid-ossp/pgcrypto ; V74 exige PostgreSQL 15 minimum. Ne pas modifier les anciennes migrations, ni utiliser `clean`, `repair`, `baseline` ou `outOfOrder` pour masquer un conflit de checksums.

Le premier administrateur est créé ponctuellement avec bootstrap explicite et mot de passe robuste 16–72 caractères, <=72 octets UTF-8, majuscule/minuscule/chiffre/symbole. Le bootstrap refuse d'écraser un compte existant. Le service actuel est déjà initialisé : laisser bootstrap désactivé et retirer son mot de passe des variables.

## Établissements du Bénin

Source : [guide officiel MESRS 2025–2026](https://enseignementsuperieur.gouv.bj/uploads/document_actualite/07ccfec01c103dab63756ab2b15455be.pdf). Registre technique : `src/main/resources/catalogue/benin_etablissements_2025_2026.json`, avec identité, édition, section et page PDF par fiche privée.

**117 identités documentées : 4 publiques, 112 privées et 1 type juridique à revoir (IUEP Govié, OTHER).** Les 4 universités publiques sont UAC, Université de Parakou, UNSTIM et UNA. Le registre inclut aussi écoles et instituts : ces catégories ne sont pas toutes des universités au sens juridique.

La migration ne duplique pas les fiches globales déjà présentes quand nom ou sigle correspond, complète seulement type/source manquants, préserve leurs autres données et ne force pas leur validation. Les nouvelles fiches ont `verified=false` : visibles aux gestionnaires autorisés pour revue, **pas publiées automatiquement aux candidats**. Cette distinction évite de présenter l'annexe 2025 comme un agrément actuel de toutes les formations. Les villes, coordonnées, frais, critères et programmes absents ne sont pas inventés. L'API et le backoffice acceptent une ville inconnue, avec contrôle d'appartenance au pays si elle est renseignée.

À terminer éditorialement : contrôle des appellations, statut public/privé lorsque douteux, villes/campus, contacts et sites officiels, programmes/agréments en vigueur, validité annuelle et publication selon le workflow existant. Le registre couvre la source consultée ; il ne prouve pas l'exhaustivité de tous les établissements ouverts en 2026–2027. Aucune SQL n'a été exécutée manuellement sur la production ; l'application de V83 sur Render doit être confirmée après déploiement.

## SendGrid et notifications

Intégration existante : HTTPS `https://api.sendgrid.com/v3/mail/send`, authentification Bearer. Inscription/demande de vérification, renvoi conditionnel et récupération de mot de passe utilisent ce service. La consommation du jeton de vérification ne renvoie pas un e-mail ; une méthode de bienvenue sans appel actif ne doit pas être annoncée comme parcours livré.

Notifications centrales : événement après commit lorsqu'une notification est créée pour un ADMIN/SUPER_ADMIN actif, ou pipeline documentaire READY_REVIEW/FAILED. Les comptes candidats/verrouillés et tenants suspendus ne déclenchent pas le transfert administratif. Le mail contient un texte générique et un lien vers `/auth/login` avec returnUrl interne ; aucun contenu de relevé, fichier, destinataire utilisateur, jeton de vérification ou reset n'est copié.

Destinataire : **adminorientia@gmail.com**. Contact public : **lestudioviral@gmail.com**. L'adresse de contact ne remplace pas automatiquement un expéditeur SendGrid vérifié. File asynchrone bornée (1–2 threads, 100 tâches), erreurs fournisseur/saturation journalisées de façon générique, sans secrets ni corps fournisseur. La file mémoire n'offre ni persistance après crash, ni livraison exactement une fois ; une outbox durable serait nécessaire pour ces garanties.

Les tests couvrent 202, 400/401/429/500, IO/interruption, paramètres, liens, après commit/rollback, rôle/compte/tenant, désactivation et saturation. Ils n'envoient aucun e-mail réel. Une acceptation SendGrid ne prouve pas une réception finale. Une clé neuve, un expéditeur vérifié et une recette explicitement autorisée restent nécessaires pour confirmer la délivrabilité.

## Google OAuth

Identifiant Android public fourni : `1048361062873-9p9jrn2umec435bddqen2epn6fur93d8.apps.googleusercontent.com`. Il n'est pas utilisé artificiellement comme client Web. **Aucun flux Google fonctionnel n'est livré.** Un client Application Web et une implémentation serveur testée restent à ajouter.

Architecture retenue : Authorization Code + PKCE S256, state/nonce, validation OIDC serveur (signature, issuer, audience, expiration), identité stable issuer/sub, aucune liaison automatique par seul e-mail, consentement et réauthentification pour lier un compte, rôle CANDIDAT sans élévation, contrôle des comptes désactivés/tenants et URI exactes. Aucun token dans l'URL : échange court à usage unique puis POST, cookie de corrélation HttpOnly/Secure/SameSite=Lax et expiration du handshake. La chaîne JWT reste indépendante.

Variables proposées pour une implémentation future : `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`, `GOOGLE_REDIRECT_URI`, `GOOGLE_ENABLED` ; **elles ne sont pas consommées actuellement**. URI proposée `/login/oauth2/code/google` non encore implémentée : ne pas la présenter comme prête. Références : [Google OIDC](https://developers.google.com/identity/openid-connect/openid-connect), [Spring OAuth2 Login](https://docs.spring.io/spring-security/reference/servlet/oauth2/login/core.html).

## Sécurité, vérifications historiques et limites

Les audits précédents ont retrouvé d'anciennes valeurs JWT/seed dans l'historique Git. Elles ont été retirées du code courant et ne doivent jamais être réutilisées ; leur présence historique n'est pas effacée par un push normal. Aucun force-push ni purge d'historique n'a été effectué. Les scans Gitleaks automatiques ne remplacent pas ce constat manuel. Les alertes historiques sur des SHA-256 de migrations ont été identifiées comme empreintes, pas comme clés API. Aucun ancien secret n'est reproduit ici.

Les campagnes successives ont validé 181, puis 195, 202 et 218 tests backend ; 218 tests/37 suites pour les formulaires et notifications, puis 219 avec le registre. Les premières validations Flyway ont testé les 83 scripts, le JAR prod depuis un dossier sans `.env`, santé UP et pages Angular HTTP 200 sans compte administrateur seed. Ces résultats sont historiques ; ils ne prouvent pas chaque déploiement actuel.

Les limitations conservées incluent des écrans sans API d'administration complète (équipes/rôles/départements, certains paramètres/MFA), métriques non instrumentées explicitement indisponibles et revue métier de données requise avant publication. Aucun test E2E exhaustif de toutes les actions administrateur en production, aucun e-mail réel et aucun accès/modification directe de base de production pendant les audits. La recette authentifiée candidate attend un compte de test autorisé.

## Livraison et documentation

Les précédents lots d'authentification/notifications ont été poussés jusqu'à `20db871f9a21d5c121830e234a59adb7a4aa1c37`. La présence de changements locaux, un commit, un push, une compilation Render et une migration appliquée sont des états distincts : vérifier chaque étape, sans force-push ni écrasement de commits distants. L'auto-déploiement Render et son dernier SHA doivent être vérifiés au moment de livrer.
