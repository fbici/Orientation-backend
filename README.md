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
| Messagerie administrative | Envoi via Brevo après commit pour notifications admin et événements documentaires READY_REVIEW/FAILED ; contenu générique et filtrage des comptes/rôles |
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

Java **21** requis. Le profil local doit utiliser une base de développement dédiée et des variables privées ; aucun secret n'est commité. Les tests d'intégration PostgreSQL F1 exigent `-Df1.jdbc.url=jdbc:postgresql://127.0.0.1:PORT/orientia_f1`, un schéma neuf `-Df1.jdbc.schema=SCHEMA_ISOLE` et `F1_TEST_DB_PASSWORD` dans l'environnement. OCR réel : `-Df2.tesseract=CHEMIN_TESSERACT -Df2.tessdata=CHEMIN_TESSDATA`. Les tests Brevo restent simulés ; fixer `BREVO_ENABLED=false` pour la recette isolée. Ne jamais substituer une connexion Render à ces paramètres.

La campagne Brevo précédente a obtenu **262 tests / 40 suites, aucun échec, erreur ou test ignoré** avant le nouveau rendu HTML, avec PostgreSQL privé 18.4 et un schéma vierge. Le registre final des 117 identités, la validation Hibernate/Flyway et les réponses API avec ville inconnue sont couverts. Le serveur PostgreSQL de test a été arrêté proprement après la campagne ; aucune base de production n’a été modifiée manuellement. Angular : **86 tests réussis** et build production lors de la campagne précédente ; la campagne courante est précisée dans son README. Les avertissements connus concernent le budget CSS du layout et le module CommonJS sockjs-client. Le succès local PostgreSQL 18.4 n'est pas une certification de prise en charge par Flyway : sa version annonce une plage testée jusqu'à 17. PostgreSQL **16** est la cible Render et CI.

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
| `GOOGLE_ENABLED` | Profil prod : défaut true après fourniture du client Web ; false désactive explicitement Google. Autres profils : défaut false |
| `GOOGLE_WEB_CLIENT_ID` | Audience publique Application Web ; profil prod préconfiguré avec le client fourni, surcharge possible ; doit correspondre à Flutter |
| `APP_BASE_URL` | `https://orientation-backend-d0ys.onrender.com` |
| `CORS_ALLOWED_ORIGINS` | Origines HTTPS exactes, séparées par virgule, sans joker avec credentials |
| `BREVO_ENABLED` | Défaut true en prod, false en dev/test |
| `BREVO_API_KEY` | Nouvelle clé privée uniquement dans Render, jamais dans Git |
| `BREVO_FROM_EMAIL` | `adminorientia@gmail.com`, expéditeur vérifié confirmé par le propriétaire |
| `BREVO_SENDER_NAME` | Facultatif, défaut `Orientia` |
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

Les migrations existantes V1–V82 incluent V63.1 : **83 scripts historiques**, conservés depuis le déploiement propre. V83 ajoute le registre béninois et rend `universities.city_id` facultatif ; **84 scripts avec V83**. V84 ajoute uniquement les identités Google externes et leurs contraintes : **85 scripts SQL au total**. Sur le schéma isolé créé par Flyway, une entrée technique SCHEMA s’ajoute dans l’historique : compter les entrées de type SQL pour comparer les migrations. V85 ajoute les défis de vérification mobile ; V86 ajoute le registre officiel des entités et rend la ville d’un campus facultative lorsque sa source ne l’établit pas. **87 scripts SQL au total**. Les sept centres de l’UAC sont documentés par l’annuaire ; les autres implantations inconnues restent explicitement à préciser et non validées.

Sur une base vierge, Flyway exécute dans l'ordre, puis Hibernate valide. Sur une base existante, seules les migrations manquantes V83 à V86 s’ajoutent dans l’ordre. Ne pas réappliquer les anciennes migrations. Conserver `flyway.enabled=true`, `baseline-on-migrate=false`, `clean-disabled=true`, `ddl-auto=validate`, `sql.init.mode=never`. V1 exige les droits nécessaires pour uuid-ossp/pgcrypto ; V74 exige PostgreSQL 15 minimum. Ne pas modifier les anciennes migrations, ni utiliser `clean`, `repair`, `baseline` ou `outOfOrder` pour masquer un conflit de checksums.

Le premier administrateur est créé ponctuellement avec bootstrap explicite et mot de passe robuste 16–72 caractères, <=72 octets UTF-8, majuscule/minuscule/chiffre/symbole. Le bootstrap refuse d'écraser un compte existant. Le service actuel est déjà initialisé : laisser bootstrap désactivé et retirer son mot de passe des variables.

## Établissements du Bénin

Source : [guide officiel MESRS 2025–2026](https://enseignementsuperieur.gouv.bj/uploads/document_actualite/07ccfec01c103dab63756ab2b15455be.pdf). Registre technique : `src/main/resources/catalogue/benin_etablissements_2025_2026.json`, avec identité, édition, section et page PDF par fiche privée.

**117 identités documentées : 4 publiques, 112 privées et 1 type juridique à revoir (IUEP Govié, OTHER).** Les 4 universités publiques sont UAC, Université de Parakou, UNSTIM et UNA. Le registre inclut aussi écoles et instituts : ces catégories ne sont pas toutes des universités au sens juridique.

La migration ne duplique pas les fiches globales déjà présentes quand nom ou sigle correspond, complète seulement type/source manquants, préserve leurs autres données et ne force pas leur validation. Les nouvelles fiches ont `verified=false` : visibles aux gestionnaires autorisés pour revue, **pas publiées automatiquement aux candidats**. Cette distinction évite de présenter l'annexe 2025 comme un agrément actuel de toutes les formations. Les villes, coordonnées, frais, critères et programmes absents ne sont pas inventés. L'API et le backoffice acceptent une ville inconnue, avec contrôle d'appartenance au pays si elle est renseignée.

À terminer éditorialement : contrôle des appellations, statut public/privé lorsque douteux, villes/campus, contacts et sites officiels, programmes/agréments en vigueur, validité annuelle et publication selon le workflow existant. Le registre couvre la source consultée ; il ne prouve pas l'exhaustivité de tous les établissements ouverts en 2026–2027. Aucune SQL n'a été exécutée manuellement sur la production ; l'application de V83 sur Render doit être confirmée après déploiement.

## Brevo, e-mails Orientia et notifications

SendGrid a été remplacé après le blocage de l’offre confirmé par le propriétaire. Transport HTTPS `https://api.brevo.com/v3/smtp/email`, en-tête `api-key`, JSON `sender`, `to`, `subject`, `htmlContent`. L’acceptation exige **HTTP 201 et un `messageId` texte non vide** ; réponse bornée à 16 Kio, connexion 5 s et requête 15 s. Un accusé 201 ne prouve pas la livraison finale. Aucun corps fournisseur, e-mail destinataire, jeton ou clé n’est journalisé.

Le layout partagé `EmailTemplateRenderer` reprend le vrai logo transparent, fond lavande, carte blanche, texte bleu nuit, bouton violet et pied de page Orientia. Les textes dynamiques et attributs sont échappés ; les URL d’action utilisent HTTP(S), sans identifiants. Le logo existe aussi sous `/assets/email/orientia-logo.png`. Pour les e-mails, `EMAIL_LOGO_URL` utilise désormais par défaut le PNG public du dépôt, figé au commit `4d87fee35be9e550924d797d2279b8923197eff3`, indépendamment du réveil Render. Une URL HTTPS de médiathèque Brevo/CDN peut le remplacer. Le contrôle du 10 novembre a confirmé le PNG GitHub (HTTP 200, 422 169 octets) alors que l’URL Render a expiré ; cela ne certifie pas la cause de chaque ancien message. Les images distantes peuvent toujours être bloquées par le logiciel de messagerie. Brevo transactionnel ne prend pas en charge les images CID ([réponse officielle](https://community.brevo.com/t/does-transactional-email-support-embedded-image/6665)). Texte alternatif conservé ; aucun e-mail réel envoyé pour cette correction. Les tables à largeur contenue et les liens de secours restent lisibles sans images. Le corps transmis est **HTML seul**, conformément au [guide Brevo](https://developers.brevo.com/docs/send-a-transactional-email) : aucune alternative MIME texte envoyée ne doit être revendiquée. Le texte visible reste complet. Les aperçus locaux navigateur ne certifient pas le rendu de chaque version Gmail/Outlook.

Scénarios : vérification d’adresse (24 heures), récupération (une heure), notification administrative et méthode de bienvenue préservée. Cette dernière n’a toujours aucun appel actif : elle ne constitue pas un nouvel e-mail automatique. L’inscription, le renvoi conditionnel et la récupération utilisent le service existant. Aucun changement de migration ni d’endpoint.

Notifications centrales : après commit lorsqu’une notification est créée pour un ADMIN/SUPER_ADMIN actif, ou lors des événements documentaires READY_REVIEW/FAILED. Comptes candidats/verrouillés et tenants suspendus exclus. Contenu générique et lien vers le backoffice ; aucun relevé, fichier, jeton de vérification ou reset recopié. File bornée en mémoire (1–2 threads, 100 tâches), sans garantie de persistance après crash ni de livraison exactement une fois.

Expéditeur vérifié **adminorientia@gmail.com** et nouvelle configuration privée Render confirmés par le propriétaire, ainsi que la rotation de la clé précédemment divulguée. Destinataire central : **adminorientia@gmail.com** ; contact public distinct : **lestudioviral@gmail.com**. Aucune valeur de clé n’est conservée ici. Les anciennes variables `SENDGRID_*` ne sont plus lues et peuvent être retirées de Render après déploiement du code Brevo.

Après ajout du design, **46 tests ciblés réussis** (42 transport/configuration et 4 renderer), sans échec ni test ignoré. Les quatre aperçus ont été contrôlés dans un viewport mobile réel de 390 px : images chargées et largeur de contenu de 390 px, sans débordement. Un aperçu bureau à 900 px a également été observé. Ces contrôles locaux ne remplacent pas un envoi autorisé vers les clients e-mail cibles.

Le JAR final a été recompilé avec `mvn package -DskipTests` après les tests ci-dessus ; présence du renderer et du vrai logo original vérifiée dans l’archive. Aucun nouveau démarrage de ce JAR en production ni envoi réel n’est revendiqué.

Les tests utilisent exclusivement un client HTTP simulé : rejet des statuts hors 201, accusés invalides/trop grands, contrôle des en-têtes, transport/timeout/interruption et confidentialité des logs. Le test PostgreSQL d’inscription vérifie le rollback utilisateur/candidat/jeton après échec fournisseur, puis un retry accepté 202 sans ouverture de session. Le rollback DB n’annule pas un e-mail déjà accepté à distance si une panne de commit survient ensuite ; aucune atomicité absolue réseau/DB n’est promise. **Aucun e-mail réel envoyé, aucun compte de production créé, aucune délivrabilité Brevo déclarée validée.**

## Google Android et identité serveur

Le flux natif est implémenté : Flutter utilise `google_sign_in`, transmet son jeton d'identité par POST `/auth/google` avec `{idToken}`, et reçoit la paire JWT/refresh et le profil Orientia habituels. L'endpoint est public mais soumis au limiteur des tentatives d'authentification. Après fourniture du client Web, le profil **prod** active le flux avec cette audience publique par défaut. `GOOGLE_ENABLED=false` le désactive ; les autres profils restent désactivés par défaut. Une configuration absente/invalide produit une réponse 503 explicite. Aucun e-mail transactionnel n'est envoyé par la création Google.

`GoogleIdentityVerifier` utilise la bibliothèque officielle Google Java 2.9.2 : signature RSA avec clés Google, issuer Google, audience `GOOGLE_WEB_CLIENT_ID`, présence et validité de l'expiration/date d'émission, e-mail vérifié et subject non vide. Les jetons malformés sont refusés ; une indisponibilité de vérification ne crée pas de session. Les jetons Google, mots de passe et corps fournisseur ne sont pas journalisés ni enregistrés.

L'identité stable est le couple issuer/subject dans `auth_external_identities` (migration additive V84). Aucun rattachement par simple égalité d'e-mail : si un compte existe déjà sans cette identité, réponse 409 `GOOGLE_ACCOUNT_EXISTS` et connexion par mot de passe. Une nouvelle identité crée uniquement un utilisateur CANDIDAT et son profil, avec mot de passe aléatoire inconnu ; aucune élévation de rôle. Comptes, tenant et organisation doivent rester actifs. Les JWT et refresh reprennent les contrôles existants. La liaison explicite à un compte existant n'est pas implémentée.

Client **Android** fourni : `1048361062873-9p9jrn2umec435bddqen2epn6fur93d8.apps.googleusercontent.com`, package `com.orientia.orientia`, associé au SHA-1 du certificat dans Google Cloud. **Client Application Web fourni : `1048361062873-j7dmfajmue27jhbs1ms7jmbpnpa5pcp3.apps.googleusercontent.com`.** Même pour l'application Android, son identifiant Web sert d'audience serveur (`serverClientId`) ; il ne remplace pas le client Android package/certificat. Le profil prod et Flutter en production utilisent désormais cet identifiant public. Les surcharges `GOOGLE_WEB_CLIENT_ID` (Render et compilation Flutter) doivent rester identiques ; `GOOGLE_ENABLED=true` active le flux si une ancienne variable Render le désactivait. Le nouveau code et V84 doivent réellement être déployés. Aucun fichier téléchargé `client_secret_*.json` n’est utilisé ou publié. Aucun secret OAuth client ni URI de redirection Web n'est demandé pour ce flux natif.

Tests : signatures cryptographiques éphémères, refus de signature étrangère/audience/issuer/expiration/claims manquants/e-mail non vérifié ; sur PostgreSQL isolé, nouvelle identité, rôle candidat, session réelle/refresh, subject stable, refus de liaison par e-mail et compte désactivé. L'identité fournisseur est simulée dans les tests d'intégration ; ils ne prouvent pas une connexion Google réelle sur appareil ou Render.

Le bouton Google du **backoffice Web** reste désactivé : le parcours navigateur Authorization Code/PKCE/state/nonce et son callback ne sont pas implémentés. Ne pas annoncer `/login/oauth2/code/google` comme disponible. Références : [plugin Google Android](https://pub.dev/packages/google_sign_in_android), [vérification Google côté serveur](https://developers.google.com/identity/sign-in/android/backend-auth).

## Numéros internationaux

Inscription et modification du compte passent par `InternationalPhoneService` (Google libphonenumber 9.0.43). Numéro facultatif ; s'il est fourni, indicatif international `+` obligatoire, validation des métadonnées de tous les pays et normalisation E.164. Numéros nationaux ambigus, extensions et formats invalides sont refusés avec une erreur française. Les valeurs antérieures ne sont pas converties en masse. Les autres numéros de catalogue ne sont pas arbitrairement réécrits.

Au Bénin, dix chiffres nationaux avec préfixe `01` depuis le 30 novembre 2024, donc `+229 01 XX XX XX XX`. Les tests couvrent le plan actuel et refusent l'ancien numéro à huit chiffres, ainsi que France, États-Unis, Nigeria, Brésil et Inde. La validation ne vérifie ni existence ni propriété de la ligne ; aucun OTP SMS n'est ajouté. [ARCEP Bénin](https://web.arcep.bj/passage-des-numero-a-10-chiffre/), [métadonnées Google](https://github.com/google/libphonenumber/releases).

## Sécurité, vérifications historiques et limites

Les audits précédents ont retrouvé d'anciennes valeurs JWT/seed dans l'historique Git. Elles ont été retirées du code courant et ne doivent jamais être réutilisées ; leur présence historique n'est pas effacée par un push normal. Aucun force-push ni purge d'historique n'a été effectué. Les scans Gitleaks automatiques ne remplacent pas ce constat manuel. Les alertes historiques sur des SHA-256 de migrations ont été identifiées comme empreintes, pas comme clés API. Aucun ancien secret n'est reproduit ici.

Les campagnes successives ont validé 181, puis 195, 202 et 218 tests backend ; 218 tests/37 suites pour les formulaires et notifications, puis 219 avec le registre. Les premières validations Flyway ont testé les 83 scripts, le JAR prod depuis un dossier sans `.env`, santé UP et pages Angular HTTP 200 sans compte administrateur seed. Ces résultats sont historiques ; ils ne prouvent pas chaque déploiement actuel.

Les limitations conservées incluent des écrans sans API d'administration complète (équipes/rôles/départements, certains paramètres/MFA), métriques non instrumentées explicitement indisponibles et revue métier de données requise avant publication. Aucun test E2E exhaustif de toutes les actions administrateur en production, aucun e-mail réel et aucun accès/modification directe de base de production pendant les audits. La recette authentifiée candidate attend un compte de test autorisé.

## Diagnostic Render et inscription

Le journal transmis le 10 novembre montre un build réussi (01:39 UTC) puis une expiration du déploiement sans port détecté (01:54 UTC). Le dépôt configure déjà `server.port=${PORT:8080}` et `server.address=0.0.0.0` ; aucune correction spéculative de port ou de délai PostgreSQL n'a été ajoutée. La journalisation prod passe à INFO pour rendre visibles Spring, Hikari, Flyway et Tomcat ; les logs SQL et sécurité restent à WARN. Aucun secret, jeton ou corps fournisseur n'est ajouté aux logs.

Le service public a répondu UP au contrôle, mais OPTIONS `/auth/google` n'annonçait que GET/HEAD/OPTIONS. La documentation publique `/api-docs` retournait un objet OpenAPI sans chemins, donc ne permettait pas de certifier les routes déployées. Un état UP ne prouve ni le dernier SHA Live, ni la disponibilité des e-mails. Confirmer le commit dans Render > Deploys et utiliser Deploy latest commit si nécessaire ; ne pas confondre un build réussi et un déploiement réussi. [Ports Render](https://render.com/docs/web-services#port-binding), [déploiements Render](https://render.com/docs/deploys).

Reproduction exécutée : JAR Java 21 en profil prod depuis un répertoire temporaire sans `.env`, sur un second schéma PostgreSQL privé vierge. Les 85 migrations SQL sont appliquées, HTTP 200 UP après 106,18 s, port dynamique effectif ; Starting/Started, Flyway et Tomcat sont visibles dans les logs. Aucune variable GOOGLE/APP_GOOGLE n'est fournie : les valeurs prod embarquées sont utilisées. OPTIONS `/auth/google` annonce POST/OPTIONS ; un jeton malformé synthétique renvoie 401 INVALID_GOOGLE_TOKEN, sans appel Google réel. Ce contraste avec GET/HEAD sur l'instance publique établit une différence du contrat déployé ; il ne prouve pas la cause du 503 d'inscription. Le JAR et PostgreSQL privés ont été arrêtés proprement, SendGrid/admin/bootstrap désactivés pendant la recette. Cette reproduction locale ne certifie pas les ressources ou variables du service Render.

Pour un échec d'inscription, relever les WARN à l'heure exacte de la tentative. Le motif `Email delivery unavailable; no delivery is confirmed` peut indiquer configuration incomplète/désactivée, rejet Brevo ou transport/timeout (connexion 5 s, requête 15 s). Aucun de ces motifs n'apparaissait dans le journal fourni : la cause de cette tentative n'est pas établie. `RegistrationService` annule la transaction sur l'échec d'envoi ; aucun succès fictif n'est retourné. Une acceptation d'envoi ne certifie pas la réception finale.

Vérifications de configuration, sans copier les secrets :

- `BREVO_ENABLED=true`, nouvelle `BREVO_API_KEY` présente uniquement dans Render et `BREVO_FROM_EMAIL=adminorientia@gmail.com`, vérifié dans Brevo ; états confirmés par le propriétaire. Le nouveau code doit aussi être déployé.
- `APP_BASE_URL=https://orientation-backend-d0ys.onrender.com`, sans paramètres ni fragment ; `PASSWORD_RESET_URL` facultative et valide si fournie.
- Même audience Google publique côté serveur et mobile ; supprimer ou corriger une ancienne surcharge désactivant le flux voulu.
- Contrôler démarrage Flyway, puis Tomcat/Started et `/actuator/health` avant une recette autorisée. Aucun envoi réel ou compte de production n'est créé par les tests locaux.

## Livraison et documentation

Les précédents lots d'authentification/notifications ont été poussés jusqu'à `20db871f9a21d5c121830e234a59adb7a4aa1c37`. La présence de changements locaux, un commit, un push, une compilation Render et une migration appliquée sont des états distincts : vérifier chaque étape, sans force-push ni écrasement de commits distants. L'auto-déploiement Render et son dernier SHA doivent être vérifiés au moment de livrer.

## Corrections critiques — mobile, accès et catalogue

**Validation finale : 278 tests / 42 suites réussis, zéro échec, erreur ou test ignoré**, avec `mvn clean verify`, Java 21 et PostgreSQL privé sur un schéma neuf. Les 87 migrations SQL et Hibernate ont été exécutés ; le cluster privé a été arrêté proprement ensuite. La première exécution a détecté le mauvais nom de colonne `acronym` dans la nouvelle V86 : corrigé en `short_name` avant cette validation. Aucune migration ancienne modifiée, aucun `repair`/`clean` Flyway et aucune requête manuelle sur une base de production.

### Vérification mobile par code

| Contrat public | Résultat |
|---|---|
| POST `/auth/mobile/register` | Même inscription candidat et contrôles existants ; 202 avec `challengeId`, durée et délai de renvoi, jamais le code ni une session |
| POST `/auth/mobile/verify-email` | `{challengeId, code}` ; six chiffres, validation du compte et du profil candidat, aucun JWT émis |
| POST `/auth/mobile/resend-code` | `{challengeId}` ; réponse neutre, renvoi uniquement si éligible |

Code aléatoire cryptographique, dix minutes, cinq essais maximum persistés, trois envois maximum par période et délai de 60 secondes. HMAC-SHA256 du couple défi/code avec le secret serveur JWT ; pas de code en clair en base ou dans les logs. Les verrous utilisateur/défi sérialisent la consommation ; les essais erronés sont conservés malgré la réponse 400, ce que vérifie le test PostgreSQL. Un nouvel envoi remplace le code antérieur sans remettre les essais à zéro durant la période. L’échec du fournisseur annule la transaction de création/envoi du défi. Une acceptation Brevo ne garantit toujours pas la réception finale ni une atomicité réseau/DB absolue.

Le template Brevo du mobile affiche les six chiffres, leur validité et les instructions de retour dans l’application : aucun bouton ou lien de vérification Web. Le reset et les anciens liens sont conservés pour compatibilité. Les variables Brevo et Google existantes restent valables ; aucune nouvelle clé de service n’est nécessaire. Une rotation de `JWT_SECRET` invalide aussi les codes en attente : refaire une demande.

### Backoffice et compteurs

Le layout Angular et ses routes enfants exigent un rôle d’équipe autorisé (`SUPER_ADMIN`, `ADMIN`, `MODERATEUR`, `REPRESENTANT_UNIVERSITAIRE`). Un candidat ne conserve pas de session Web dans ce backoffice ; le client demande la révocation de la session nouvellement délivrée sans enregistrer ses jetons ; une panne réseau peut empêcher de confirmer cette révocation. Les anciens liens de vérification conduisent vers `/mobile-access`, qui invite à utiliser l’application, au lieu de proposer le dashboard. La frontière HTTP `/backoffice/**` refuse les candidats en plus des contrôles de permissions/tenant propres à chaque API. Les routes `/admin/**` restent réservées aux administrateurs. Les tests utilisent un vrai JWT candidat sur la base isolée pour vérifier les refus 403 ; le rôle réel du propriétaire en production n’a pas été modifié.

Le dashboard ne renvoie plus systématiquement `null` pour ses compteurs métier : candidats liés à un compte, recommandations, universités, documents traités, guides, règles actives, tenants, utilisateurs actifs et imports sont comptés en PostgreSQL avec le scope approprié. Les séries datées et la répartition documentaire sont réelles. `SUPER_ADMIN` avec `TENANT_ACCESS_ALL` voit le scope global ; l’administrateur ordinaire reste dans son tenant et le catalogue partagé. Les mesures système non instrumentées restent absentes ; aucun pourcentage de progression ni historique fictif ne les remplace.

### Facultés, écoles et pays

`catalogue/benin_public_entities_sources.json` et la migration additive V86 documentent **66 entités distinctes** : **UAC 40, UP 9, UNSTIM 8, UNA 9**. Le [guide officiel MESRS 2026–2027](https://enseignementsuperieur.gouv.bj/actualite/show/ACT-Z5JpZRp8-B29C1C2) fournit les entités du premier cycle ; l’[annuaire officiel UAC 2024–2025, tableau 2, page PDF 9](https://recherche-universitaire.uac.bj/wp-content/uploads/2026/01/Annuaire_Statistiques_UAC_2024_2025_en_Redaction_31_12_2025.pdf) complète ses écoles doctorales, centres et chaires. Les variantes INeMES/INMeS et FASHS-Calavi/FASHS sont fusionnées. Les données incluent URL, édition, page et empreinte des sources. Les catégories ne sont pas toutes des facultés au sens strict.

Les lignes existantes sont réutilisées par université et code/nom normalisé ; leurs IDs et associations de programmes sont conservés. Une implantation non revue peut être corrigée lorsqu’un centre de l’UAC est établi par la source ; une localisation déjà validée n’est pas écrasée. Une implantation inconnue conserve une ville NULL sous un rattachement clairement nommé « Implantation à préciser ». Cette ligne non validée ne constitue pas l’affirmation d’un campus physique. Les nouvelles entités restent à revoir avant publication aux candidats. Le catalogue administrateur propose par défaut « Entités documentées uniquement » ; désactiver ce filtre permet de retrouver les anciennes données, sans les supprimer.

Les filtres pays/université restent génériques et les choix d’université chargent toutes les pages au lieu de se limiter à 100. **L’inventaire des facultés des établissements privés et des autres pays n’est pas déclaré exhaustif** : il exige leurs sources officielles et la revue éditoriale. Le workflow d’import, de rattachement et de publication existant reste disponible pour chaque pays ; aucune faculté générique inventée n’est ajoutée en masse.

Déploiement : publier d’abord le backend contenant V85/V86, laisser Flyway appliquer uniquement les migrations manquantes, vérifier le bon commit Live et `/actuator/health`, puis utiliser le mobile 1.4.0. Aucun e-mail réel, compte de production, test administrateur connecté de production ni modification de base de production n’a été effectué pour cette campagne. Le succès de la suite locale ne prouve pas une recette exhaustive de toutes les fonctionnalités en production.

Après la correction du logo e-mail indépendant de Render : **57 tests ciblés e-mail/code réussis**, zéro échec, après la campagne complète de 278 tests. Le PNG public figé a été vérifié HTTP 200 et signature PNG ; aucune livraison ni aucun rendu Gmail/Outlook réel n’est revendiqué. L’APK 1.4.0 a été observé sur le Samsung : animation du logo puis connexion.


### Diagnostic du timeout Render Free — 10 novembre 2026

Le propriétaire confirme Docker, instance Free, sans `JAVA_TOOL_OPTIONS` ni `JDK_JAVA_OPTIONS`. Le journal fourni confirme la connexion PostgreSQL 16, la validation des 87 migrations et l’application réussie de V85/V86. La dernière étape visible est Hibernate ; aucune ligne Tomcat démarré/Started ni erreur explicite de mémoire ne précède le timeout. Le port `${PORT}` et l’adresse `0.0.0.0` sont déjà configurés : aucune modification de port ou réparation Flyway n’est justifiée par ces traces.

Le [plan Free Render](https://render.com/docs/compute-plans) dispose de 0,1 CPU et 512 MB. Java 21 local, avec `-XX:MaxRAM=512m -XX:ActiveProcessorCount=1`, choisit un tas maximum de 128 Mio et Serial GC. Cette mesure locale ne relève pas les options effectives du conteneur Render.

Comparaison du JAR publié en profil prod, Java 21, PostgreSQL privé sur loopback et schéma neuf dédié, e-mails/Google/bootstrap désactivés :

| Tas maximum | Santé HTTP 200 UP | Pauses GC consignées | Erreur mémoire |
| --- | --- | --- | --- |
| 128 Mio | 66,0 s | 214 | Aucune |
| 256 Mio | 51,0 s | 194 | Aucune |

Les deux processus ont terminé l’initialisation Hibernate. Le premier passage a appliqué les migrations, le second les a seulement validées ; les temps ne constituent donc pas un benchmark strict. Windows local, un processeur déclaré à Java : ni quota CPU de 0,1 ni limite globale du conteneur à 512 MB reproduits. La mémoire native totale et la charge OCR n’ont pas été mesurées. Aucune garantie de fonctionnement dans 512 MB ne peut être déduite de ces tests. Java et le cluster PostgreSQL privés ont été arrêtés après contrôle.

Proposition expliquée, **non appliquée** : essai Render avec `JAVA_TOOL_OPTIONS=-Xms16m -Xmx256m -XX:+ExitOnOutOfMemoryError -Xlog:gc`. Le tas maximum serait doublé, sans allouer toute la mémoire du conteneur à Java ; la mémoire native et l’OCR doivent encore tenir dans le budget restant. L’option de sortie rend une saturation du tas explicite ; les journaux GC permettent de repérer des collectes répétées. Contrôler CPU/RAM, Tomcat/Started et la santé avant de conclure. En cas de dépassement mémoire ou de CPU durablement saturé, un plan doté de davantage de ressources doit être envisagé avec le propriétaire. Aucun changement JVM, Dockerfile, variable Render, déploiement ou push effectué pendant ce diagnostic.


## Fonctions mobiles et cycle de suppression — 11 novembre 2026

Lot local non publié. Aucune base de production, variable Render ou configuration JVM modifiée. Les écrans Flutter validés restent inchangés ; le rapport mobile et sa matrice de 30 modules sont dans le README du dépôt mobile.

### Contrats corrigés

- `GET /auth/me` et les réponses `UserInfo` incluent `phone`, même nul, pour éviter l’effacement involontaire du numéro lors de l’édition du profil.
- Les erreurs `BusinessException` exposent leur `code` en plus du message. L’inscription mobile refuse explicitement les adresses désactivées (`ACCOUNT_INACTIVE`) et en attente d’effacement (`ACCOUNT_DELETION_PENDING`), avant tout envoi de code. L’issue neutre des adresses déjà actives est conservée. Cette distinction explicite de disponibilité est un choix produit demandé ; aucune identité ni raison de suspension n’est renvoyée.
- Les critères d’admission sont ordonnés par priorité puis identifiant afin de parcourir plusieurs pages sans ordre instable.
- L’action administrative historique `DELETE /admin/users/{id}` conserve sa sémantique de suspension. Le backoffice dit désormais « Désactiver l’accès » et « compte conservé », plutôt que « utilisateur supprimé ». Les demandes personnelles affichent leur date d’effacement ; la modification/réactivation ordinaire de ces comptes est refusée.

### Suppression personnelle après 30 jours

La durée de **30 jours a été explicitement validée**. Nouvelle migration **V87** : `candidate_deletion_requests(user_id, requested_at, purge_after)`, avec contrainte de délai minimal. Aucun compte inactif existant n’est ajouté automatiquement à cette table ; aucun effacement rétroactif n’est déduit d’une désactivation.

`POST /auth/me/deletion-request` exige le rôle candidat, un dossier propre au compte, `confirmation: "SUPPRIMER"` et soit `currentPassword`, soit `googleIdToken`. Un jeton Google est vérifié puis comparé à l’identité fournisseur déjà liée au même utilisateur ; l’e-mail seul n’autorise jamais cette opération. Un compte ayant aussi un rôle de personnel est exclu. La route est incluse dans la limitation des tentatives d’authentification. Réponse : `purgeAfter` et message de confirmation.

La transaction réserve une seule échéance (un nouvel essai ne repart pas à zéro), désactive le compte, révoque les sessions et les clés API, et consomme les jetons de récupération/vérification. La date n’est pas une promesse de disponibilité du service à cette minute : l’adresse n’est libérée qu’après l’effacement réussi.

`CandidateErasureJob` traite au plus 25 demandes échues par passage, une minute après démarrage puis chaque heure. `app.account-erasure.enabled` / `APP_ACCOUNT_ERASURE_ENABLED` permet de désactiver le job (activé par défaut ; désactivé dans les tests d’intégration). Sur Render Free, le traitement reprend au réveil du service. Surveiller les journaux `Candidate erasure completed` et `needs retry/review` ; ils donnent seulement l’identifiant technique et la catégorie d’erreur, sans e-mail, mot de passe ni jeton.

`CandidateErasureService` verrouille une demande échue, vérifie que le compte est désactivé et non administratif, puis traite les lignes personnelles, y compris celles masquées par le soft delete : dossier, relevés/analyses, recommandations, décisions/conversations, documents privés et leurs enfants, sessions, identités Google, jetons, profil utilisateur, appartenances et traces structurées liées. Les fichiers sont validés par le service de stockage et contrôlés contre des références partagées. Les documents publiés/partagés ou en traitement bloquent la purge automatique pour revue ; aucune donnée de catalogue partagée n’est supprimée pour satisfaire cette demande.

Les contraintes SQL sont vérifiées avant la suppression des fichiers. Si un fichier ou une contrainte bloque l’opération, la transaction ne libère pas l’adresse. La suppression de fichiers est idempotente pour permettre une reprise après un échec partiel ; elle ne bénéficie pas d’un rollback physique. Ne pas présenter une demande en erreur comme effacée. Les sauvegardes externes, copies d’e-mails déjà livrées et éventuelles archives hors stockage applicatif nécessitent leur propre politique de conservation ; ce lot ne purge pas ces systèmes.

### Validation et déploiement ultérieur

Campagne locale complète : **289 tests recensés, 286 réussis et 3 OCR ignorés**, aucun échec, Flyway et Hibernate validés sur un schéma PostgreSQL privé neuf. Elle inclut le refus des comptes inactifs sans défi fictif, l’absence de purge avant 30 jours, la révocation des sessions, l’effacement de fichiers/documents/transcripts/conversations, la préservation d’un autre candidat et le refus d’un fichier partagé. Aucun e-mail réel ni accès à une base de production.

Backoffice : build production réussi et **96 tests réussis**, dont les libellés de suspension, l’absence de double envoi et le blocage d’une nouvelle désactivation pendant une demande personnelle. Les avertissements CSS du layout et CommonJS de `sockjs-client` préexistent à ce changement.

Avant publication : relire la migration et la politique de conservation, publier backend/backoffice avant l’APK mobile 1.4.1+8, puis recetter avec un compte de test autorisé. Ne pas lancer de purge manuelle sur les anciens comptes inactifs : ils ne portent pas de consentement/date de demande. Les commandes de test existantes restent disponibles ; les tests PostgreSQL exigent `f1.jdbc.url` strictement en boucle locale vers `orientia_f1`, un schéma neuf `f1.jdbc.schema` et `F1_TEST_DB_PASSWORD` fourni uniquement dans l’environnement local. Aucun push ni déploiement effectué par ce lot.

Dernière revalidation après les ajustements administratifs, le tri des critères et les contrôles Google : **54 tests réussis, zéro échec ni test ignoré** (`FoundationsIntegrationTest` 38, `AcademicWorkflowIntegrationTest` 7, `BackofficeContractsTest` 5, `RegistrationAvailabilityTest` 3, `AuthRateLimitTest` 1). Nouveau schéma privé distinct, cluster arrêté à la fin.

Contrôle final de confidentialité : `CandidateDeletionRequestTest`, **1 test réussi**, vérifie le masquage du mot de passe et du jeton Google dans la représentation de diagnostic de la nouvelle requête. L’APK mobile **1.4.1+8** est compilé ; package et version vérifiés, sans installation ni recette de compte production.


## Interfaces métier — livraison du 12 novembre 2026

Cette livraison regroupe les changements fonctionnels du 11 novembre et les corrections de présentation du 12 novembre. Les mentions « lot local non publié » des sections précédentes décrivent leur état avant cette livraison.

### Mobile 1.4.2+9

- Les cinq onglets Accueil, Explorer, Parcours, Assistant et Profil sont conservés. Une surface visuelle commune aux écrans métier harmonise fonds, cartes, champs, boutons, barres de titre et navigation ; les parcours d’entrée validés ne sont pas retouchés. Les 38 empreintes contrôlées de démarrage/authentification/thème d’entrée/assets sont identiques à leur état validé.
- Explorer et Parcours disposent de cartes avec icônes, titres et descriptions. Les catalogues, recommandations, relevés, versions d’analyse et historique de décisions chargent progressivement leurs résultats. Les pages déjà reçues restent consultables ; les filtres/recherches repartent de la première page. Les étapes du formulaire académique conservent leurs boutons de progression.
- Paramètres donne accès au compte, à la sécurité, à l’aide et aux informations légales. À propos mentionne « Créé par Le Studio Viral ». Éditeur confirmé : **SALAOU Souleyman**, propriétaire de la marque, sans numéro d’immatriculation déclaré. Contact : **lestudioviral@gmail.com**.
- Les pages de confidentialité, conditions d’utilisation et informations sur l’éditeur décrivent les données, le service d’orientation, Render, Brevo, Google facultatif, la suppression après 30 jours et les demandes au support. **L’adresse professionnelle, les durées des journaux/sauvegardes et les modalités des transferts internationaux restent à compléter** ; aucun agrément ni conformité juridique n’est revendiqué. Référence consultée pour les droits : [APDP Bénin — protéger](https://archive.apdp.bj/proteger/).

### Backoffice

- Les recommandations et décisions utilisent une recherche de candidat par nom/e-mail (`GET /candidates/search`) plutôt qu’une saisie d’UUID. Les comparaisons sélectionnent les formations par leur nom depuis le catalogue existant, avec une limite de six.
- Rapports affiche un mode dédié à l’historique et aux exports, sans formulaire de simulation. La navigation ne sélectionne plus simultanément Intelligence et Aide à la décision.
- Les espacements des formulaires/actions, zones d’import, états vides et pagination sont harmonisés. Les noms des types d’import sont traduits. La pagination ne propose pas de navigation lorsqu’une seule page ou aucun résultat est disponible. Les outils de calcul utilisent la pagination partagée.
- Les indicateurs non mesurés restent explicitement indisponibles. Aucune statistique, notification, fonctionnalité de favori ou capacité de modèle IA externe n’est inventée.

### Vérifications

- Flutter : **87 tests réussis** dans la suite complète finale ; **10 tests métier ciblés réussis**. Couverture ajoutée : accumulation des résultats sans pagination visible, remise à zéro lors d’une recherche, lecture des informations à 360 px avec texte agrandi, accès aux rubriques après défilement.
- Angular : **98 tests réussis**, build production réussi. Deux avertissements conservés : budget CSS du layout et CommonJS `sockjs-client`.
- Backend Java : aucun nouveau changement Java dans la phase de présentation. Les validations du lot fonctionnel restent celles documentées plus haut : 289 recensés dont 286 réussis/3 OCR ignorés, puis 54 tests ciblés et 1 test de masquage réussis.
- Aperçus backoffice Rapports, Aide à la décision et Imports contrôlés dans Chrome local avec un compte synthétique et des réponses simulées. Ce contrôle ne valide pas les données ni les droits d’un compte de production.
- Recherche de signatures de secrets sur les fichiers publiables : aucune clé Brevo/SendGrid/AWS ni clé privée détectée. Cette recherche par motifs ne constitue pas une preuve d’absence de tout secret possible.
- Aucun e-mail réel, aucune modification de base de production, aucun changement JVM/variables Render. La recette métier avec un compte candidat autorisé et le contrôle sur téléphone restent distincts des tests locaux.

Commandes mobile :

```powershell
flutter run --release --dart-define=APP_ENV=production --dart-define=ORIENTIA_API_URL=https://orientation-backend-d0ys.onrender.com
flutter build apk --release --dart-define=APP_ENV=production --dart-define=ORIENTIA_API_URL=https://orientation-backend-d0ys.onrender.com
```

L’installation d’un nouvel APK est nécessaire pour voir les changements mobiles ; un push GitHub ne met pas à jour l’application déjà installée. Pour le backoffice, le push doit être suivi d’un déploiement Render réussi. Après publication, vérifier le commit Live et la santé du serveur avant de recetter les nouveaux parcours.
