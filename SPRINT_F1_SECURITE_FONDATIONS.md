# ORIENTIA — Sprint F1 : sécurité et fondations

Date de validation locale : 5 novembre 2026. Périmètre : backend Spring Boot et Back Office Angular. Référence : `frontend/AUDIT_GLOBAL_ORIENTIA.md`.

## 1. Résumé et périmètre protégé

Le bootstrap administrateur est désormais volontaire et sans identifiants fixes. Les accès JWT sont typés et liés à une famille de sessions révocable ; les refresh tokens sont opaques, aléatoires et stockés sous forme d'empreintes. La rotation et la détection du rejeu sont transactionnelles. Les compteurs de connexion, les tokens de récupération et les changements de mot de passe utilisent des verrous en base.

Les ressources candidat, relevé, notification, utilisateur et document sont contrôlées dans les services à partir du principal et du tenant. Les statistiques du dashboard respectent le tenant. Les uploads utilisent des noms serveur, des signatures de fichiers et un stockage confiné. Les schémas Flyway sont vérifiés sur un PostgreSQL créé exclusivement pour F1. Angular gère les renouvellements concurrents, la déconnexion serveur et le transport STOMP/SockJS authentifié.

Aucune migration, aucun bootstrap et aucune requête de validation n'ont été lancés sur la base principale. Aucun compte administrateur réel n'a été créé. Aucun déploiement, commit ou push. Aucun fichier Flutter modifié par F1. Les modifications mobiles déjà présentes ont été conservées. Le fichier dev préexistant a été préservé ; seule sa ligne de mot de passe a été remplacée par une variable d'environnement.

**État final des tests et verdict : voir sections 6 et 10.** Les limites ci-dessous interdisent de présenter ce travail comme une validation globale de production.

## 2. Anomalies et traitement réel

| Identifiant | Traitement F1 | Preuves et limites |
|---|---|---|
| SEC-01 | Bootstrap désactivé par défaut ; activation explicite, secret fort, tenant existant ; détection des super administrateurs incluant ceux supprimés ; verrou sur le rôle | BootstrapTest ; aucun bootstrap sur la base réelle. Les anciens identifiants potentiellement utilisés doivent être changés par l'exploitant. |
| SEC-02, SEC-10 | Access JWT de 15 minutes maximum : issuer, audience, type, UUID utilisateur, sid, jti et expiration contrôlés. Refresh opaque rt1_, SHA-256 en base ; rotation avec verrou utilisateur puis token ; rejeu révoquant la famille | JwtTokenProviderTest et intégration PostgreSQL, dont deux renouvellements concurrents. Anciennes sessions invalidées à la transition. |
| SEC-03 | Propriété candidat/relevé et portée tenant ; contrôles pour historique, feedback et sélecteur de candidat des recommandations ; DTO explicites | Tests HTTP avec UUID de A et B, également deux candidats du même tenant. Le moteur métier et la cohérence métier de toutes les références d'un feedback relèvent encore de F2. |
| SEC-04 | Notifications personnelles ; userId client ne peut sélectionner autrui ; lectures et marquages contrôlés | Tests de consultation d'une notification d'autrui et du sélecteur userId. Diffusion personnelle au lieu d'un topic global. |
| SEC-05 | Requêtes utilisateurs et documents tenant-scopées ; recherche combinée au scope ; détails, suppression et téléchargement contrôlés | ADMIN A refusé pour B ; suppression directe via service refusée ; recherche et téléchargement testés. Documents historiques sans propriétaire fiable mis hors accès normal. |
| SEC-06, SEC-09 | Vérification compte actif, vérifié, non supprimé, non verrouillé, tenant et organisation actifs ; compteur dans REQUIRES_NEW ; cinq échecs, verrou de quinze minutes ; limite par IP transport | Tests verrouillage, expiration, désactivation/suppression, renouvellement ; test concurrent du compteur. Limite locale au processus : 60 requêtes/15 min, pas une protection distribuée. |
| SEC-07 | Suppression du fallback JWT et du mot de passe DB littéral ; configuration obligatoire | Tests de secret JWT invalide ; configurations relues sans afficher les secrets. .env existant non publié et non réécrit. |
| SEC-08, SEC-15 | /admin accepte ADMIN et SUPER_ADMIN ; permissions actives intégrées aux authorities ; hasPermission invalide remplacé par hasAuthority ; catalogue SQL/Angular canonique | Matrice de rôles testée sur endpoints sensibles. Traversée des tenants exige ROLE_SUPER_ADMIN ET TENANT_ACCESS_ALL. Les modules métier restants ne constituent pas une matrice exhaustive validée. |
| SEC-11 | Tokens vérification distincts du MFA, hashés, expirables et consommables une seule fois ; réponse JSON | Tests expiration, mauvaise finalité, réutilisation et préservation de mfaSecret. Les liens de vérification historiques ne sont plus acceptés. |
| SEC-12 | UUID serveur, extensions imposées, contrôle PDF/PNG/JPEG, taille, confinement, refus traversée et liens symboliques ; nettoyage de rollback | StorageSecurityTest et tests de documents propriétaires. Pas d'antivirus ni de refonte OCR ; anciens fichiers au nom historique à examiner avant reprise. |
| SEC-13 | Origines CORS explicites ; wildcard avec credentials refusé ; origines socket alignées | CorsSecurityTest : preflight autorisé/refus origine étrangère. Hôtes de production conservés. |
| SEC-14 | CONNECT STOMP exige access JWT et famille active ; destinations personnelles et topics globaux explicitement autorisés ; SEND refusé ; livraison sortante recontrôlée | SocketSecurityTest ; trois tests navigateur du client. Pas d'E2E réseau entre navigateur et serveur déployé. |
| SEC-16 | Refresh Angular mutualisé, un seul retry, exclusion des endpoints auth ; stockage rememberMe cohérent, effacement des anciennes valeurs ; logout serveur ; protection contre les réponses tardives | Douze tests AuthService. Tokens encore accessibles au JavaScript : risque XSS résiduel, pas de migration vers cookies HttpOnly dans F1. |
| SEC-17 | Inscription publique sur le seul tenant configuré ; pas de sélection arbitraire ou de création implicite d'organisation | Contrôle ajouté dans RegistrationService ; fournisseur absent testé 503. Livraison réelle et inscription avec fournisseur actif non testées. |
| SEC-18 | Logs bootstrap/auth/JWT/email expurgés ; requête Smart Query et noms de fichiers retirés des logs applicatifs concernés ; erreurs génériques | Inspection des chemins modifiés. Pas d'audit exhaustif de tous les logs du moteur OCR/import/IA. |
| FUN-10, OPS-01 | Migrations additives, mappings JSONB et colonnes corrigés ; prod Flyway activé, baseline automatique interdit, Hibernate validate | Validation PostgreSQL isolée et Hibernate. Adoption sur une base préexistante nécessite impérativement la procédure de section 5. |

## 3. Fichiers importants et impacts

| Chemin (sous src/main/java/com/orientation/orientationapp sauf indication) | Changement / justification | Impact |
|---|---|---|
| config/ProdDataInitializer.java ; H.java ; HashGen.java | Retrait des mots de passe fixes ; outils de hash avec saisie console | Aucun compte créé par défaut. Les éventuels .class locaux anciens ne sont pas des sources corrigées et ne doivent pas être redistribués. |
| security/jwt/JwtTokenProvider.java ; security/filter/JwtAuthenticationFilter.java | Validation stricte des access tokens et de l'état de session | Les anciens tokens cessent de fonctionner. Une vérification de session en base est nécessaire à chaque requête. |
| modules/auth/entity/RefreshToken.java ; repository/RefreshTokenRepository.java ; service/impl/RefreshTokenServiceImpl.java | Hash et famille ; verrous dans un ordre cohérent ; révocation de famille/utilisateur/tenant | Rejeu refusé et révocation immédiatement prise en compte par HTTP et STOMP. |
| modules/auth/service/impl/AuthenticationServiceImpl.java ; service/LoginSessionIssuer.java ; service/impl/LoginAttemptService.java | Émission sous verrou avec nouvelle vérification du mot de passe ; compteur indépendant et sérialisé | Un rollback de login n'annule pas les échecs. Une modification concurrente de sécurité ne valide pas un ancien mot de passe par cache. |
| security/service/AccountState.java ; CustomUserDetailsService.java ; ResourceAccess.java | État de compte et scope centralisés ; permissions effectives | Le principal, jamais le tenant envoyé seul par le client, établit les droits. |
| modules/auth/entity/AccountToken.java ; repository/AccountTokenRepository.java ; service/AccountRecoveryService.java | Récupération et vérification séparées du MFA ; lecture scalaire du propriétaire avant verrouillage | Empêche la réutilisation concurrente d'un token à travers un cache JPA périmé. |
| modules/auth/service/EmailService.java ; RegistrationService.java ; controller/RegisterController.java | Fournisseur configurable, erreurs réelles, inscription transactionnelle et tenant public imposé | Provider absent : 503 ; pas de faux succès. Réinscription d'un compte non vérifié éligible : nouveau lien, sans changer son mot de passe. |
| modules/auth/service/AccountProfileService.java ; controller/AuthController.java ; dto/request/* | Profil limité aux champs autorisés ; changement de mot de passe avec ancien mot de passe et révocation | Email, rôles et tenant ne sont pas modifiables via le profil personnel. |
| modules/user/controller/CandidateController.java ; service/CandidateAccessService.java ; dto/CandidateResponse.java | Sélection propriétaire/tenant et DTO aplati | Pas de graphe JPA utilisateur/tenant exposé. |
| modules/transcript/controller/TranscriptController.java ; service/TranscriptAccessService.java ; dto/TranscriptResponse.java | Relevés contrôlés et upload via StorageService | Le chemin interne et les graphes lazy ne font plus partie du DTO. |
| storage/service/StorageServiceImpl.java ; docintel/document/service/impl/DocumentServiceImpl.java ; repository/DocumentRepository.java ; controller/DocumentController.java | Stockage confiné ; scope combiné à recherche ; téléchargement autorisé | Les identifiants legacy default/current-user ne prouvent aucun droit et sont refusés. |
| backoffice/notification_center/controller/* ; service/impl/NotificationServiceImpl.java | Inbox et destinations personnelles | Ne pas utiliser un userId d'un autre compte. |
| modules/admin/service/impl/UserAdminServiceImpl.java ; TenantServiceImpl.java ; OrganizationServiceImpl.java ; InvitationServiceImpl.java | Contrôles fins dans les services ; scopes d'administration ; révocation sur changements sensibles | Les services complémentaires ont été relus ; tous leurs scénarios métier n'ont pas de test HTTP dédié. |
| backoffice/dashboard/service/impl/DashboardServiceImpl.java ; backoffice/monitoring/controller/DashboardSocketController.java | Comptage candidat tenant-scopé ; diffusion d'invalidation sans données | Les autres KPI déjà factices restent hors périmètre ; ne pas les présenter comme des métriques métier vérifiées. |
| backoffice/config/WebSocketConfig.java ; SocketAuthenticationInterceptor.java | Origines explicites ; authentification et autorisations STOMP ; contrôle de livraison | Le simple handshake SockJS ne donne aucun accès aux destinations. |
| knowledge/entity/* ; docintel/document/entity/* ; autres entités JSONB | Mapping JSON natif ; version_number et entity_id_a/b alignés au SQL existant | Évite ddl-auto=update et préserve les colonnes historiques. |
| src/main/resources/application*.yml ; config/web/WebConfig.java ; security/config/SecurityConfig.java | Secrets, CORS, Flyway/validate, console H2 inaccessible par l'API | Le passage prod exige les variables et la préparation du schéma. |
| mvnw.cmd ; src/test/resources/application*.yml ; src/test/java/** | Wrapper Windows réparé ; PostgreSQL F1 isolé ; H2 de compatibilité pour anciens tests ; vraies fixtures auth | Aucune désactivation de sécurité pour faire passer les tests. |
| frontend/src/app/core/services/auth.service.ts ; core/interceptors/auth.interceptor.ts ; auth/* | Contrats auth, storage, refresh, erreurs et retour login | Déconnexion locale immédiate avec signalement si révocation réseau échoue. |
| frontend/src/app/core/services/websocket.service.ts ; realtime.service.ts ; src/sockjs-polyfills.ts | STOMP/SockJS réel, destinations reçues dispatchées, renouvellement/retrait du token suivis | Alias global de compatibilité SockJS chargé avant le client ; erreur navigateur reproduite puis corrigée. |
| frontend/src/app/core/services/notification.service.ts ; api.service.ts ; administration/components/roles.component.ts | URLs/verbres notifications et permissions canoniques | Alignement avec les endpoints backend existants. |
| frontend/angular.json ; tsconfig*.json ; package*.json ; src/index.html | Cible de tests ; séparation des specs ; dépendances STOMP ; no-referrer | Build et tests exécutables ; audits npm détaillés plus bas. |

L'inventaire exact des fichiers de travail est annexé en fin de document. L'audit global préexistant n'a pas été réécrit.

## 4. Contrats API à prendre en compte

Les chemins ci-dessous sont relatifs au contexte backend existant, conservé.

| Endpoint | Contrat actuel / changement |
|---|---|
| POST /auth/login | email/password ; accessToken, refreshToken opaque, expiresIn en secondes, tokenType Bearer et user. Échecs génériques ; verrou temporaire ; limite IP. |
| POST /auth/refresh | {refreshToken} ; rotation à chaque succès, conserver immédiatement les deux nouveaux tokens. Un rejeu révoque toute la famille. |
| POST /auth/logout | Access bearer et {refreshToken} du même utilisateur ; révocation de sa famille, y compris avec un refresh venant juste d'être tourné. |
| GET /auth/me | Principal réel, compte actif et famille active exigés. |
| PUT /auth/me | firstName, lastName, phone seulement. |
| PUT /auth/me/password | currentPassword, newPassword ; alias oldPassword conservé ; succès : se reconnecter, toutes les sessions du compte sont révoquées. |
| POST /auth/forgot-password | {email} ; réponse générique pour compte éligible ; 503 si aucun envoi n'est possible faute de fournisseur. |
| POST /auth/reset-password | {token,newPassword} ; alias password accepté ; mot de passe 12 caractères minimum, au plus 72 octets UTF-8 ; usage unique et révocation des sessions. |
| POST /auth/register | 202 générique, tenant public configuré uniquement ; vérification email préalable à la connexion. Fournisseur absent : 503. |
| GET /auth/verify?token=... | Token aléatoire de vérification, finalité et expiration contrôlées ; JSON, pas de HTML interpolé ; sans utilisation de mfaSecret. |
| /candidates et /transcripts | Accès par UUID contrôlé ; réponses DTO aplaties, pas de graphe JPA ni de chemin de stockage. |
| /documents ; GET /documents/{id}/download | Tenant et propriétaire établis côté serveur ; recherche ne contourne pas le tenant ; téléchargement avec type autorisé et nom de pièce jointe sûr. |
| GET /backoffice/notifications ; /unread-count | userId facultatif, limité à soi. |
| POST /backoffice/notifications/{id}/read ; /read-all | Marquage personnel ; POST conservé et Angular aligné. |
| GET /backoffice/dashboard ; /tenant/{tenantId} | STATISTICS_READ ; tenant courant par défaut ; global uniquement si SUPER_ADMIN + TENANT_ACCESS_ALL. |
| STOMP CONNECT /ws | Authorization: Bearer accessToken dans les headers STOMP, pas dans l'URL. Les URLs et le contexte de production n'ont pas été réinventés. |
| STOMP SUBSCRIBE | /user/queue/notifications et /user/queue/pipeline ; /topic/dashboard et /topic/monitoring uniquement pour le super administrateur ayant TENANT_ACCESS_ALL et STATISTICS_READ. |
| STOMP SEND | Refusé en l'absence de handler métier autorisé. |

La durée absolue de la famille refresh n'est pas repoussée indéfiniment par la rotation. Le logout d'un appareil n'invalide pas les autres familles ; reset, changement de mot de passe et désactivation révoquent les sessions concernées.

## 5. Migrations et transition vers une base existante

Aucune des 64 migrations historiques V1 à V64 n'a été modifiée : empreintes SHA-256 comparées à l'état initial. Les fichiers suivants sont ajoutés dans cette branche :

| Version | Objet |
|---|---|
| 63.1 | Compatibilité additive avec le seed historique V64 incohérent : colonnes legacy et triggers vers campus/faculté/type/durée obligatoires. Associations tirées des sections explicites de V64 ; mapping ambigu refusé par SELECT INTO STRICT. |
| 65 | Familles de refresh ; révocation et empreinte des tokens hérités ; index d'unicité actif. |
| 66 | Liaisons user/tenant des candidats, backfill par email et contraintes ; permissions de ressources et accès global explicite. |
| 67 | bo_notifications, audit_events, KnowledgeNode.embedding et index. |
| 68 | account_tokens : hash, finalité, expiration et consommation. |
| 69 | SearchIndex.embedding manquant, révélé par Hibernate validate sur PostgreSQL. |
| 70 | Catalogue canonique des permissions et conservation des grants par alias ; droits globaux réservés au super administrateur. |
| 71 | Permission STATISTICS_READ pour le dashboard tenant de l'ADMIN, sans accès global. |

**Validation isolée :** schéma neuf créé par Flyway puis validation Hibernate et tests de persistance/autorisation. Aucun Hibernate update utilisé pour masquer les écarts. PostgreSQL 18.4 émet un avertissement : la version de Flyway intégrée annonce une couverture testée jusqu'à PostgreSQL 17. Les tests réalisés sur 18.4 ne constituent pas une certification générale de cette combinaison.

**Procédure obligatoire avant toute intervention sur la base principale :**

1. Obtenir une autorisation distincte, sauvegarder la base et vérifier une restauration sur clone.
2. Relever le catalogue réel et flyway_schema_history sans modifier la base : Flyway était désactivé et Hibernate update a pu créer des objets absents de l'historique. Comparer les types, contraintes, tables, colonnes et checksums.
3. Examiner les doublons et ambiguïtés de lower(email) dans users/candidates, les comptes supprimés/non vérifiés et le rattachement réel des documents/candidats. Le backfill d'ownership par email exige une correspondance métier validée, unique et fiable ; interrompre l'adoption si elle n'est pas prouvée. Aucun réattributage automatique des anciens documents default/current-user.
4. V63.1 doit précéder V64 sur une base vierge. Sur une base déjà au-delà de V64, l'appliquer nécessite une décision explicite d'adoption hors ordre, après comparaison des colonnes/triggers et rehearsal sur clone. **Ne pas activer out-of-order globalement, ni baseliner arbitrairement une base non vide.**
5. En cas d'objets déjà créés par Hibernate mais non suivis par Flyway, préparer une réconciliation dédiée et revue. Les migrations de cette branche ne promettent pas une adoption automatique de tous les schémas divergents.
6. Rejouer exactement le chemin choisi sur clone, contrôler Hibernate validate, contraintes, permissions et données existantes. Ne pas utiliser Flyway repair pour effacer une divergence inexpliquée.
7. Préparer une déconnexion générale : anciens JWT/refresh et anciens liens de vérification ne sont pas compatibles. Les comptes non vérifiés du tenant public éligible peuvent demander un nouveau lien via réinscription ; traiter séparément les cas administratifs.
8. Renseigner les secrets et les paramètres, désactiver le bootstrap après toute initialisation volontaire, puis demander l'autorisation de migration/déploiement. Cette étape n'a pas été exécutée.

Configuration à fournir sans committer les valeurs : JWT_SECRET (au moins 32 octets de matériau fort), DB_PASSWORD, paramètres de connexion existants, CORS_ALLOWED_ORIGINS explicites si nécessaire. Bootstrap volontaire : APP_BOOTSTRAP_ENABLED=true, APP_BOOTSTRAP_EMAIL, APP_BOOTSTRAP_PASSWORD fort de 16 à 72 caractères avec classes variées, APP_BOOTSTRAP_TENANT_CODE d'un tenant actif existant ; aucun super administrateur ne doit déjà exister, même supprimé.

Email : activer explicitement app.sendgrid.enabled et injecter app.sendgrid.api-key (SENDGRID_API_KEY), configurer l'expéditeur, app.base-url et app.password-reset-url. Aucun fournisseur payant n'a été activé et aucun email réel n'a été envoyé pendant F1. Les erreurs de transport ne sont pas présentées comme un succès.

## 6. Validation exécutée

| Vérification finale | Réussis | Échecs | Erreurs | Ignorés | Résultat |
|---|---:|---:|---:|---:|---|
| Suite backend complète, JDK 21 + URL PostgreSQL isolée + schéma neuf | 84 | 1 | 0 | 0 | 85 tests exécutés ; Maven exit 1 |
| Dont groupe F1 (inclus dans la ligne précédente) | 44 | 0 | 0 | 0 | 25 intégration PostgreSQL + 19 tests ciblés |
| Dont tests historiques (inclus dans le total) | 40 | 1 | 0 | 0 | Auth et Knowledge passent après correction des fixtures/config H2 |
| Angular ChromeHeadless | 15 | 0 | 0 | 0 | 12 auth + 3 STOMP ; exit 0 |
| TypeScript et templates Angular | — | 0 | — | — | tsc puis ngc --noEmit : exit 0 |
| Build Angular production final | — | 0 | — | — | Exit 0 ; avertissements CSS et CommonJS détaillés section 8 |
| Flyway + Hibernate sur schéma neuf | — | 0 | — | — | 72 migrations SQL, 73 entrées d'historique avec création de schéma ; version finale 71 ; 58 tables ; Hibernate validate accepté |
| Contraintes de sécurité lues sur ce schéma | — | 0 | — | — | refresh_tokens.family_id NOT NULL ; unicité account_tokens présente |
| git diff --check | — | 0 | — | — | Exit 0 |

**Échec restant :** `UniversityIntegrationTest.listUniversities_ReturnsPage`, GET /universities : 200 attendu, 500 reçu. Aucun contrôleur n'expose cette route ; le traitement de ressource absente finit dans l'erreur générique. L'endpoint métier manquant était déjà relevé dans l'audit. Il n'a pas été ajouté ni le test désactivé ou assoupli pour faire passer F1. **La suite complète n'est donc pas verte.**

Les rapports Surefire XML dans `target/surefire-reports` fournissent le détail. La sortie Maven finale a également été conservée hors dépôt sous `%TEMP%/orientia-f1-final-tests.log`. Les fichiers de connexion du PostgreSQL de test sont privés, hors dépôt ; ne pas publier leur contenu. Le schéma final, distinct des premiers essais, est `f1_final_20261105`.

Le cluster PostgreSQL temporaire F1 a ensuite été arrêté proprement (pg_ctl stop : exit 0 ; status : exit 3). Ses fichiers de test sont conservés hors dépôt ; aucun cluster PostgreSQL préexistant n'a été arrêté.

Détail du groupe F1 : FoundationsIntegrationTest 25, JwtTokenProviderTest 5, BootstrapTest 3, StorageSecurityTest 4, SocketSecurityTest 4, CorsSecurityTest 2, AuthRateLimitTest 1. Ces nombres ne sont pas additionnés une seconde fois au total backend.

Environnement : Spring Boot 3.4.3, Maven Wrapper 3.9.9 (wrapper 3.3.2), Microsoft JDK 21.0.13.0.2 pour la validation finale ; Java 25 présent initialement. Angular 19.2.25 installé, TypeScript 5.6.3, ChromeHeadless installé sur cette machine. PostgreSQL temporaire 18.4, loopback uniquement, port distinct, mot de passe généré conservé hors dépôt. Docker indisponible, donc instance locale initdb isolée. La base principale n'est jamais une cible de ces tests.

Commandes exécutées dans leurs projets respectifs (URL/password de test fournis uniquement par configuration privée temporaire) :

```powershell
# JAVA_HOME fixé sur le JDK 21 temporaire
.\mvnw.cmd -q "-Df1.jdbc.url=jdbc:postgresql://127.0.0.1:<port-isole>/orientia_f1" test
# Schéma neuf distinct pour le contrôle final
.\mvnw.cmd -q "-Df1.jdbc.schema=f1_final_20261105" "-Df1.jdbc.url=jdbc:postgresql://127.0.0.1:<port-isole>/orientia_f1" test
# Le groupe F1 a aussi été exécuté séparément avec -Dtest=FoundationsIntegrationTest,JwtTokenProviderTest,BootstrapTest,StorageSecurityTest,AuthRateLimitTest,SocketSecurityTest,CorsSecurityTest
npx.cmd tsc --noEmit -p tsconfig.app.json
npx.cmd ngc --noEmit -p tsconfig.app.json
npm.cmd run build -- --configuration production
npm.cmd test -- --watch=false --browsers=ChromeHeadless --progress=false
npm.cmd audit --json
npm.cmd audit --omit=dev --json
git diff --check
```

Le test PostgreSQL est protégé par une condition : sans propriété f1.jdbc.url autorisée sur loopback/base orientia_f1, il est ignoré. Il utilise le profil f1-test, Flyway activé, Hibernate validate, bootstrap/email/broadcasts planifiés désactivés. Ne pas interpréter un simple mvnw test sans la propriété comme une exécution de l'intégration PostgreSQL.

Les fixtures sont exclusivement des comptes/données synthétiques dans les bases de test ; aucune donnée fictive n'est utilisée pour annoncer qu'une fonction métier de production fonctionne.

### Contrôles couverts

- Typage access/refresh, claims invalides et expiration ; absence de refresh en clair en base.
- Rotation, rejeu, expiration/finalité, famille révoquée, sessions simultanées et logout avec token venant d'être tourné.
- Verrouillage temporaire persistant malgré échec ; échecs concurrents.
- A/B, deux tenants, deux propriétaires du même tenant, ADMIN, SUPER_ADMIN, MODERATEUR, CANDIDAT, anonyme ; accès global explicitement autorisé.
- Documents : lecture, suppression via service, recherche tenant et téléchargement.
- Tokens de reset/vérification hashés, à usage unique, expiration, concurrence et MFA inchangé ; changement de mot de passe révoquant les sessions parallèles.
- Upload : MIME/signature/taille, nom original hostile, traversée, rollback.
- CORS et STOMP : CONNECT/abonnements/SEND, principal UUID et abandon d'une livraison après révocation.
- Angular : refresh partagé, absence de boucle 401, pas de bearer vers un autre domaine, rememberMe, logout réseau et réponses tardives, récupération 503, dispatch des trames STOMP et arrêt au logout.

Les premières tentatives ont révélé et permis de corriger les incohérences du seed V64, les mappings JSONB/colonnes, les graphes lazy exposés par les relevés, la séparation des specs TypeScript, la signature beforeConnect et l'absence de global dans SockJS sous navigateur. Les résultats finaux ci-dessus remplacent ces essais intermédiaires ; ils ne sont pas ajoutés artificiellement au nombre de tests réussis.

## 7. Risques résiduels et vérifications impossibles

- **Dépendances npm : 66 alertes au total (2 faibles, 32 modérées, 28 élevées, 4 critiques).** Les quatre critiques portent sur piscina, proxy-addr, shell-quote et tar dans l'outillage transitif. Audit production seul : **12 alertes, dont 5 élevées et 7 modérées, aucune critique**. Angular et NgRx sont concernés ; les corrections proposées impliquent notamment des versions majeures. Aucun npm audit fix --force exécuté. Une migration de dépendances et sa non-régression restent nécessaires avant de conclure à une plateforme sécurisée en production.
- Profil prod relu mais aucun démarrage vers la base de production exécuté.
- Pas d'envoi SendGrid réel, pas de test sur hôte déployé, pas de test navigateur/serveur STOMP de bout en bout et pas de test de charge.
- Stockage des tokens Angular encore accessible au JavaScript ; protection XSS/CSP à compléter selon le déploiement. no-referrer réduit la diffusion des liens mais ne remplace pas ces protections.
- Le rate limiter est local à une instance et s'appuie sur l'IP transport. Derrière un proxy, configurer la protection réseau et les proxys de confiance séparément ; le client ne peut pas imposer X-Forwarded-For.
- Isolation F1 démontrée pour les ressources et opérations énumérées. Ceci n'est pas une certification de tous les modules IA, imports, analytics et exports. Les stubs et chemins métier relevés dans l'audit, dont imports, chat et exports, restent à traiter en F2 ; ne pas y brancher des données sensibles en supposant ce travail déjà fait.
- Les KPI factices du dashboard et les résultats métier OCR/recommandation/import n'ont pas été validés comme réels. F2 n'a pas commencé.
- Pas de migration/validation de la base principale : risques de schéma issu de Hibernate update, ownership historique et migration 63.1 documentés en section 5. La validation sur schéma neuf ne prouve pas la compatibilité de toutes les données existantes.
- Les fichiers locaux ignorés, anciens .class, anciens logs et secrets déjà distribués ne sont pas effacés par une modification des sources. Rotation/examen opérationnel à planifier sans supprimer de données pendant F1.
- Les contrôles supplémentaires d'organisations, tenants, invitations et inscription sont présents dans les services, mais n'ont pas tous une matrice d'intégration HTTP dédiée. Ne pas assimiler une relecture à un test exécuté.

## 8. Compatibilité Angular

Connexion et durée de session suivent le contrat backend. Refresh mutualisé et stockage respectent rememberMe. Un refresh tardif ou une réponse /me tardive ne réauthentifie pas un utilisateur déconnecté. Le logout tente la révocation serveur et signale un échec réseau. Les formulaires utilisent newPassword et les contraintes nouvelles ; le token de reset est retiré de la query string après lecture.

Le transport utilise @stomp/stompjs et sockjs-client : reconnexion STOMP, headers d'authentification, destinations personnelles, réinitialisation après changement du token. Les topics globaux sont réservés aux permissions globales. Les ADMIN de tenant ne reçoivent pas de flux global de dashboard ; leurs données HTTP restent scopées et peuvent être rafraîchies normalement. Aucun abonnement inter-tenant n'a été ajouté.

Build final : avertissements CSS layout (5,07 kB) et login (6,51 kB) au-dessus du seuil d'avertissement de 4 kB, sous le seuil d'erreur ; CommonJS SockJS. Les budgets n'ont pas été relevés pour masquer ces avertissements.

## 9. Compatibilité Flutter à traiter en F3

Aucune modification Flutter dans F1. Le client devra :

1. Traiter expiresIn en secondes et mémoriser immédiatement le refresh tourné ; mutualiser les renouvellements, éviter tout rejeu automatique d'un ancien refresh et ne pas utiliser le refresh comme bearer.
2. Envoyer le refresh courant à POST /auth/logout avec l'access bearer ; gérer la reconnexion après changement/reset du mot de passe ou révocation.
3. Gérer 401, 403, 429/Retry-After et 503 email ; respecter le verrou temporaire et l'absence de faux succès.
4. Envoyer newPassword pour reset et currentPassword/newPassword pour changement ; respecter 12 caractères minimum et 72 octets UTF-8 maximum.
5. Utiliser le tenant public autorisé pour inscription et le nouveau lien de vérification ; ne pas dépendre des anciens tokens MFA détournés.
6. Adapter les modèles candidats/relevés aux DTO aplatis, utiliser le téléchargement autorisé plutôt qu'un chemin interne et les endpoints de notifications alignés.
7. Si le temps réel est consommé, parler STOMP/SockJS avec headers CONNECT et destinations personnelles ; ne pas envoyer le JWT dans l'URL.
8. Effacer proprement l'identité après révocation et vérifier le stockage sécurisé mobile pendant F3.

## 10. Verdict

**Sprint partiellement terminé.**

Les corrections des fondations décrites ici sont livrées et les **44 tests F1 passent**, avec **15 tests Angular**, TypeScript, templates, build production et validation PostgreSQL/Hibernate sur schéma neuf réussis. Les 64 migrations historiques et la configuration dev préexistante ont été contrôlées contre la sauvegarde initiale : aucune migration historique modifiée ; seule la ligne de mot de passe dev a changé.

La validation globale demeure partielle : **un test historique échoue sur l'endpoint /universities absent**, des vulnérabilités npm de production et d'outillage restent ouvertes, et l'adoption Flyway sur la base principale exige une réconciliation revue puis une autorisation distincte. L'envoi email réel et l'intégration STOMP sur un environnement déployé n'ont pas été testés. Aucun de ces points n'est masqué par les tests ciblés réussis.

Aucune autorisation de déploiement ou de migration de la base principale n'est implicite dans ce rapport. Le Sprint F2 n'a pas été engagé.

## Annexe — fichiers modifiés ou ajoutés dans le sprint

- `H.java`
- `HashGen.java`
- `SPRINT_F1_SECURITE_FONDATIONS.md`
- `frontend/.angular/cache/19.2.27/orientation-backoffice/.tsbuildinfo`
- `frontend/angular.json`
- `frontend/package-lock.json`
- `frontend/package.json`
- `frontend/src/app/administration/components/roles.component.ts`
- `frontend/src/app/auth/forgot-password/forgot-password.component.ts`
- `frontend/src/app/auth/login/login.component.ts`
- `frontend/src/app/auth/reset-password/reset-password.component.ts`
- `frontend/src/app/core/interceptors/auth.interceptor.ts`
- `frontend/src/app/core/services/api.service.ts`
- `frontend/src/app/core/services/auth.service.spec.ts`
- `frontend/src/app/core/services/auth.service.ts`
- `frontend/src/app/core/services/notification.service.ts`
- `frontend/src/app/core/services/realtime.service.ts`
- `frontend/src/app/core/services/websocket.service.spec.ts`
- `frontend/src/app/core/services/websocket.service.ts`
- `frontend/src/index.html`
- `frontend/src/sockjs-polyfills.ts`
- `frontend/tsconfig.app.json`
- `frontend/tsconfig.spec.json`
- `mvnw.cmd`
- `src/main/java/com/orientation/orientationapp/backoffice/config/SocketAuthenticationInterceptor.java`
- `src/main/java/com/orientation/orientationapp/backoffice/config/WebSocketConfig.java`
- `src/main/java/com/orientation/orientationapp/backoffice/dashboard/service/impl/DashboardServiceImpl.java`
- `src/main/java/com/orientation/orientationapp/backoffice/monitoring/controller/DashboardSocketController.java`
- `src/main/java/com/orientation/orientationapp/backoffice/monitoring/controller/MonitoringSocketController.java`
- `src/main/java/com/orientation/orientationapp/backoffice/notification_center/controller/NotificationController.java`
- `src/main/java/com/orientation/orientationapp/backoffice/notification_center/controller/NotificationSocketController.java`
- `src/main/java/com/orientation/orientationapp/backoffice/notification_center/service/impl/NotificationServiceImpl.java`
- `src/main/java/com/orientation/orientationapp/config/ProdDataInitializer.java`
- `src/main/java/com/orientation/orientationapp/config/web/WebConfig.java`
- `src/main/java/com/orientation/orientationapp/dataplat_import/controller/ImportController.java`
- `src/main/java/com/orientation/orientationapp/dataplat_import/entity/ImportEntityMapping.java`
- `src/main/java/com/orientation/orientationapp/dataplat_quality/entity/QualityReportRecord.java`
- `src/main/java/com/orientation/orientationapp/docintel/document/controller/DocumentController.java`
- `src/main/java/com/orientation/orientationapp/docintel/document/entity/Document.java`
- `src/main/java/com/orientation/orientationapp/docintel/document/entity/DocumentClassification.java`
- `src/main/java/com/orientation/orientationapp/docintel/document/entity/DocumentExtraction.java`
- `src/main/java/com/orientation/orientationapp/docintel/document/entity/DocumentMetadata.java`
- `src/main/java/com/orientation/orientationapp/docintel/document/entity/DocumentPage.java`
- `src/main/java/com/orientation/orientationapp/docintel/document/service/impl/DocumentServiceImpl.java`
- `src/main/java/com/orientation/orientationapp/exception/handler/GlobalExceptionHandler.java`
- `src/main/java/com/orientation/orientationapp/intelligence/api/IntelligenceController.java`
- `src/main/java/com/orientation/orientationapp/knowledge/controller/KnowledgeController.java`
- `src/main/java/com/orientation/orientationapp/knowledge/entity/KnowledgeNode.java`
- `src/main/java/com/orientation/orientationapp/knowledge/entity/KnowledgeRelation.java`
- `src/main/java/com/orientation/orientationapp/knowledge/entity/LearningHistory.java`
- `src/main/java/com/orientation/orientationapp/knowledge/entity/SearchIndex.java`
- `src/main/java/com/orientation/orientationapp/knowledge/entity/SimilarityMatrix.java`
- `src/main/java/com/orientation/orientationapp/modules/admin/entity/ActivityLog.java`
- `src/main/java/com/orientation/orientationapp/modules/admin/entity/Invitation.java`
- `src/main/java/com/orientation/orientationapp/modules/admin/service/impl/InvitationServiceImpl.java`
- `src/main/java/com/orientation/orientationapp/modules/admin/service/impl/OrganizationServiceImpl.java`
- `src/main/java/com/orientation/orientationapp/modules/admin/service/impl/TenantServiceImpl.java`
- `src/main/java/com/orientation/orientationapp/modules/admin/service/impl/UserAdminServiceImpl.java`
- `src/main/java/com/orientation/orientationapp/modules/auth/controller/AuthController.java`
- `src/main/java/com/orientation/orientationapp/modules/auth/controller/RegisterController.java`
- `src/main/java/com/orientation/orientationapp/modules/auth/dto/request/ChangePasswordRequest.java`
- `src/main/java/com/orientation/orientationapp/modules/auth/dto/request/RegisterRequest.java`
- `src/main/java/com/orientation/orientationapp/modules/auth/dto/request/ResetPasswordRequest.java`
- `src/main/java/com/orientation/orientationapp/modules/auth/dto/request/UpdateProfileRequest.java`
- `src/main/java/com/orientation/orientationapp/modules/auth/entity/AccountToken.java`
- `src/main/java/com/orientation/orientationapp/modules/auth/entity/RefreshToken.java`
- `src/main/java/com/orientation/orientationapp/modules/auth/entity/User.java`
- `src/main/java/com/orientation/orientationapp/modules/auth/repository/AccountTokenRepository.java`
- `src/main/java/com/orientation/orientationapp/modules/auth/repository/RefreshTokenRepository.java`
- `src/main/java/com/orientation/orientationapp/modules/auth/repository/RoleRepository.java`
- `src/main/java/com/orientation/orientationapp/modules/auth/repository/UserRepository.java`
- `src/main/java/com/orientation/orientationapp/modules/auth/service/AccountProfileService.java`
- `src/main/java/com/orientation/orientationapp/modules/auth/service/AccountRecoveryService.java`
- `src/main/java/com/orientation/orientationapp/modules/auth/service/EmailService.java`
- `src/main/java/com/orientation/orientationapp/modules/auth/service/LoginSessionIssuer.java`
- `src/main/java/com/orientation/orientationapp/modules/auth/service/RegistrationService.java`
- `src/main/java/com/orientation/orientationapp/modules/auth/service/impl/AuthenticationServiceImpl.java`
- `src/main/java/com/orientation/orientationapp/modules/auth/service/impl/InvalidRefreshException.java`
- `src/main/java/com/orientation/orientationapp/modules/auth/service/impl/LoginAttemptService.java`
- `src/main/java/com/orientation/orientationapp/modules/auth/service/impl/RefreshTokenServiceImpl.java`
- `src/main/java/com/orientation/orientationapp/modules/transcript/controller/TranscriptController.java`
- `src/main/java/com/orientation/orientationapp/modules/transcript/dto/TranscriptResponse.java`
- `src/main/java/com/orientation/orientationapp/modules/transcript/entity/Transcript.java`
- `src/main/java/com/orientation/orientationapp/modules/transcript/entity/TranscriptAnalysis.java`
- `src/main/java/com/orientation/orientationapp/modules/transcript/entity/TranscriptLine.java`
- `src/main/java/com/orientation/orientationapp/modules/transcript/repository/TranscriptRepository.java`
- `src/main/java/com/orientation/orientationapp/modules/transcript/service/TranscriptAccessService.java`
- `src/main/java/com/orientation/orientationapp/modules/user/controller/CandidateController.java`
- `src/main/java/com/orientation/orientationapp/modules/user/dto/CandidateResponse.java`
- `src/main/java/com/orientation/orientationapp/modules/user/entity/Candidate.java`
- `src/main/java/com/orientation/orientationapp/modules/user/repository/CandidateRepository.java`
- `src/main/java/com/orientation/orientationapp/modules/user/service/CandidateAccessService.java`
- `src/main/java/com/orientation/orientationapp/recengine/recommendation/controller/RecommendationController.java`
- `src/main/java/com/orientation/orientationapp/security/config/SecurityConfig.java`
- `src/main/java/com/orientation/orientationapp/security/filter/AuthRateLimitFilter.java`
- `src/main/java/com/orientation/orientationapp/security/filter/JwtAuthenticationFilter.java`
- `src/main/java/com/orientation/orientationapp/security/jwt/JwtTokenProvider.java`
- `src/main/java/com/orientation/orientationapp/security/service/AccountState.java`
- `src/main/java/com/orientation/orientationapp/security/service/CustomUserDetails.java`
- `src/main/java/com/orientation/orientationapp/security/service/CustomUserDetailsService.java`
- `src/main/java/com/orientation/orientationapp/security/service/ResourceAccess.java`
- `src/main/java/com/orientation/orientationapp/storage/service/StorageService.java`
- `src/main/java/com/orientation/orientationapp/storage/service/StorageServiceImpl.java`
- `src/main/resources/application-dev.yml`
- `src/main/resources/application-prod.yml`
- `src/main/resources/application.yml`
- `src/main/resources/db/migration/V63_1__legacy_seed_column_compatibility.sql`
- `src/main/resources/db/migration/V65__secure_sessions.sql`
- `src/main/resources/db/migration/V66__resource_ownership_and_permissions.sql`
- `src/main/resources/db/migration/V67__missing_schema_objects.sql`
- `src/main/resources/db/migration/V68__account_tokens.sql`
- `src/main/resources/db/migration/V69__search_embedding.sql`
- `src/main/resources/db/migration/V70__canonical_permission_grants.sql`
- `src/main/resources/db/migration/V71__tenant_dashboard_permission.sql`
- `src/test/java/com/orientation/orientationapp/integration/AuthIntegrationTest.java`
- `src/test/java/com/orientation/orientationapp/security/AuthRateLimitTest.java`
- `src/test/java/com/orientation/orientationapp/security/BootstrapTest.java`
- `src/test/java/com/orientation/orientationapp/security/CorsSecurityTest.java`
- `src/test/java/com/orientation/orientationapp/security/FoundationsIntegrationTest.java`
- `src/test/java/com/orientation/orientationapp/security/JwtTokenProviderTest.java`
- `src/test/java/com/orientation/orientationapp/security/SocketSecurityTest.java`
- `src/test/java/com/orientation/orientationapp/security/StorageSecurityTest.java`
- `src/test/resources/application-f1-test.yml`
- `src/test/resources/application-test.yml`
