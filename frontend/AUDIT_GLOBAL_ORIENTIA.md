# Audit global Orientia

Date : 4 novembre 2026 (Africa/Lagos). Audit des sources locales, sans correction.

## Périmètre, méthode et limites de preuve

- Dossier réellement ouvert : `C:/Users/HP/Documents/projets/Orientation-mobile`, application Flutter existante (pubspec.yaml, lib/core/network/api_client.dart).
- Backend accessible et inspecté : `C:/Users/HP/Documents/projets/orientation-backend`.
- Back Office Angular identifié : `C:/Users/HP/Documents/projets/orientation-backend/frontend`. Le présent rapport est enregistré dans cette racine Angular, conformément à la mission, malgré la différence avec le dossier ouvert.
- Aucun AGENTS.md trouvé dans les racines et sous-arbres recherchés, ni dans les répertoires parents examinés. Aucune délégation à des sous-agents.
- Lecture des contrôleurs, configurations, schémas, moteurs et consommateurs frontend ; inventaires statiques systématiques. Les annexes recensent les routes HTTP annotées et les appels HttpClient repérés. Une correspondance de route ne prouve pas un contrat ni un fonctionnement.
- Les preuves sont des chemins relatifs au backend avec lignes ; les références Flutter commencent par ../Orientation-mobile. Les annexes sont extraites du code de cette session.
- Deux analyses sans émission de fichiers ont réussi : TypeScript et compilateur Angular. Aucun test métier exécuté. Pas de backend démarré, de migration, de modification de données, de déploiement ou de nouvel écran.
- PostgreSQL et Redis : connexions TCP locales acceptées sur 5432 et 6379 ; identité des services, authentification et contenu des bases non vérifiés. Ports 8080 et 4200 fermés au moment du contrôle. Aucun appel au service distant de production.
- Une vérification exhaustive en navigateur de chaque affichage, modal, résolution responsive et bouton n'est donc pas démontrée. L'audit frontend décrit le code des templates et handlers, pas une recette visuelle.
- États Git préexistants constatés : nombreuses modifications Flutter ; backend application-dev.yml modifié. Ces changements n'ont pas été touchés.
- Statuts : **IV** implémentée et vérifiée par exécution ; **INT** implémentée mais non testée ; **P** partielle ; **M** placeholder/mock ; **A** absente du code inspecté ; **D** défectueuse par lecture. Une cause de panne démontrable statiquement est distinguée d'un résultat d'exécution. Aucune fonctionnalité métier n'obtient IV sur la seule présence d'une classe.

## A. Résumé exécutif

**Verdict : backend non prêt pour Flutter ; Back Office non fonctionnel de bout en bout.** Le dépôt contient une base importante, mais beaucoup de capacités annoncées sont des interfaces, des réponses fixes ou des implémentations déconnectées.

Points réutilisables : modèle universitaire relationnel, 64 migrations, repositories Spring Data, JWT signé et BCrypt, validation de plusieurs DTO, parseurs CSV/Excel, composants Angular standalone avec lazy loading, signals et composants partagés. TypeScript et templates Angular passent l'analyse sans émission.

Les obstacles majeurs sont la sécurité des identités et ressources, l'absence de nombreuses API consommées, trois chemins OCR incohérents, un pipeline cassé dès l'ajout de sa première étape, des données obligatoires non renseignées à la persistance, et des recommandations qui ne sélectionnent pas de version de guide et n'appliquent pas le moteur d'éligibilité. Le nombre de fichiers ne mesure pas la maturité métier.

### Les dix problèmes prioritaires

1. Compte super administrateur de production avec mot de passe fixe dans le code et journalisation de ses identifiants.
2. Refresh JWT accepté comme bearer d'accès ; révocation et état du compte insuffisamment contrôlés.
3. Lectures de candidats/relevés/historiques et notifications sans contrôle du propriétaire ; filtrage tenant contourné ou ignoré.
4. Schéma Flyway incomplet pour les entités ; production désactive Flyway et utilise Hibernate update.
5. API universitaires/programmes/campus/facultés manquantes malgré les services Angular ; contrats documents, notifications, knowledge et imports incompatibles.
6. OCR PDF scanné absent : implémentations simulées ou lecture des octets comme texte.
7. Pipeline : steps nul avec Lombok builder, persistance sans programmes/critères, sans validation humaine ni provenance exploitable.
8. Recommandations : version de guide nulle, éligibilité assimilée à un seuil de score, warnings nuls, pas de persistance/historique réel.
9. Imports : fichier non transmis au parseur, parseFile renvoie une liste vide, rollback ne restaure rien.
10. Analytics, monitoring, rapports, comparaison, simulation et explications présentent des données fixes ou simulées sur des chemins de production.

## B. Inventaire technique

| Élément | Constat |
|---|---|
| Backend | Spring Boot 3.4.3 ; cible Java 21 ; Maven Wrapper 3.9.9 |
| Environnement observé | Java exécutable JDK 25 ; JDK 17 également présent ; pas de commande mvn sur PATH |
| Persistance | PostgreSQL, Flyway, Hibernate/JPA ; H2 runtime pour tests |
| Sécurité | Spring Security, JJWT 0.12.6 ; BCrypt |
| Autres dépendances déclarées | MapStruct 1.6.3, Lombok 1.18.38, springdoc 2.8.3, Commons CSV 1.12.0, POI 5.2.5, Redis, Batch, WebSocket |
| Java | 413 fichiers .java ; 54 entités ; 54 repositories JpaRepository/CrudRepository ; 26 contrôleurs REST ; 42 fichiers sous dto |
| Mappers | Aucun @Mapper MapStruct repéré ; mappings principalement manuels ; 3 mappers d'import spécialisés |
| Migrations | V1 à V64 ; inventaire et comptes de contraintes en annexe |
| Tests backend | 14 classes *Test.java, 41 annotations @Test ; 5 classes d'intégration |
| Angular | package.json ^19 ; @angular/core installé 19.2.25 ; TypeScript installé 5.6.3 |
| Frontend | 38 fichiers *.component.ts, composants partagés inclus ; aucun *.spec.ts dans src |
| UI / état | SCSS, Material Symbols, Chart.js, signals, RxJS ; NgRx déclaré, architecture d'état principalement par services/signals |
| Flutter existant | orientia 1.0.0+1 ; contrainte Dart >=3.2.0 <4.0.0 ; Dio, Riverpod, go_router, secure storage |

Les versions déclarées ne constituent pas une analyse de vulnérabilités des dépendances. Aucun inventaire CVE ou accès réseau de registre n'a été exécuté ; aucune absence de CVE n'est affirmée.

### Architecture

Monolithe Spring avec packages modules, recengine, docintel, intelligence, knowledge, ai, backoffice et dataplat_*. Les interfaces ne forment pas des modules Maven indépendants ni des frontières d'accès imposées. Plusieurs contrôleurs accèdent directement aux repositories (candidats, relevés, guides, bourses). Services et DTO existent, mais leur usage est irrégulier et les réponses exposent parfois les entités JPA.

Duplications avérées : recherche/knowledge dans intelligence et knowledge ; deux familles d'interfaces OCR et une façade OcrService ; scoring Default et Real ; implémentations de smart query différentes ; services Angular WebSocketService et RealtimeService. Plusieurs packages modules/* ne contiennent que package-info.java alors que leur logique est ailleurs.

Les champs HistoricalEngine et EligibilityEngine de RecommendationController sont injectés mais non invoqués dans generateRecommendations. ProgramRepository est injecté mais inutilisé dans PersistenceService. DocumentVersion est construit mais jamais enregistré dans DocumentServiceImpl. Les contrats de rollback/version/catalog/history de la plateforme ont des interfaces sans chaîne REST complète. Ces constats justifient une consolidation ; ils ne prouvent pas que toute classe non référencée textuellement est inutilisée, Spring pouvant la découvrir.

Aucun diagnostic global de cycles d'injection n'est démontré sans démarrage. Les relations JPA bidirectionnelles créent un risque de sérialisation récursive/lazy loading quand les contrôleurs renvoient directement les entités et open-in-view est false ; à couvrir par tests de contrat. Ne pas transformer cette réserve en erreur observée.

## C. Audit backend

| Module | Statut | Preuve et état réel |
|---|---|---|
| Login JWT / BCrypt | P | AuthenticationServiceImpl.login et SecurityConfig ; génération réelle, mais sécurité de session et verrouillage défectueux |
| Inscription / vérification email | P | RegisterController:41-165 ; utilisateur et rôle réels, profil créé en best effort, token dans mfaSecret, pas d'expiration |
| Récupération mot de passe | M | AuthenticationServiceImpl.forgotPassword/resetPassword : log/succès sans envoi ni mutation |
| Refresh / logout | P/D | Rotation enregistrée en base ; pas de séparation de type JWT, pas de protection robuste contre rejeu concurrent |
| Profil candidat / onboarding | P/A | CandidateController liste/détail/search/count ; GET/PUT /candidates/me absents |
| Universités / campus / facultés / programmes | P | Entités et repositories présents ; contrôleurs REST correspondants absents, hors catalogue Bénin codé en dur |
| Bourses | INT/P | ScholarshipController GET liste/détail réels ; gestion, validation et éligibilité mobile non exposées complètement |
| Guides / versions / règles | P | GET /guides et /guides/{id} ; versions et critères modélisés mais workflow CRUD/validation/publication non exposé |
| Upload relevés | D | TranscriptController:53-79 : sauvegarde disque puis entité sans candidat ni année, deux relations NOT NULL ; aucune analyse déclenchée |
| OCR documents | M/D | LocalOcrEngine lit le flux brut ; OcrService fait un pseudo-parsing PDF ; TesseractOcrEngine renvoie des messages |
| Pipeline intelligence | D | DocumentProcessingPipeline:45-126,164-174 ; steps non initialisé par builder ; erreurs absorbées dans résultat ERROR |
| Persistance documentaire | D | PersistenceService:40-100 ; universités, matières, bourses incomplètes, programmes et critères non persistés |
| Recommandations | D/P | Programme lu en base, scores heuristiques ; version de guide nulle, moteur éligibilité non invoqué, génération exposée à NPE |
| Simulation | M | DefaultSimulationEngine:73-90 ; 5 programmes statiques |
| Recherche intelligente | P | /intelligence/smart-query consulte des noms de nœuds ; /knowledge/query renvoie des phrases génériques |
| Recherche dite sémantique | P | DefaultSemanticSearchService ; LIKE/contains sur index SQL, pas d'embedding exploité |
| Similarité / learning | P | Feedback SQL + renforcement en HashMap ; pas de branchement dans le score réel |
| Chat | M/D | DefaultChatService FAQ fixe ; ChatController transmet current-user à UUID.fromString |
| Comparaison | M | DefaultComparisonService:15-61 ; valeurs A/B constantes, universités déléguées à comparaison programmes |
| Imports CSV / Excel | P/D | Parseurs concrets ; orchestrateur parseFile renvoie toujours vide, contenu upload non transmis |
| Rollback / versionnement import | M | ImportController:75-79 appelle cancelImport ; aucun retour à l'état antérieur |
| Utilisateurs admin | P/D | UserAdminServiceImpl:26-30 crée toujours une exception ; liste ignore tenantId et search |
| Organisations / tenants / invitations | INT/P | CRUD/services présents ; contrôles d'appartenance tenant et contrats UI incomplets |
| Rôles / équipes / départements | P/A | Entités/services partiels mais contrôleurs /admin/roles, /teams, /departments absents |
| Notifications | P/D | Persistance et marquage réels ; contrat URL/verbe frontend faux, propriétaire fourni par client |
| Dashboard | P/M | Un count candidats et métriques JVM réels ; nombreux KPI fixes et CPU 25 |
| Analytics | M | AnalyticsServiceImpl:20-59 ; universités, scores, taux constants |
| Monitoring | P/M | Mémoire/threads/uptime JVM réels ; santé DB et latence fixes |
| Rapports / exports | M/D | Rapport = métadonnées ; PDF = texte brut, XLSX = CSV renommé ; aucun rapport réel |
| Audit trail | P/M | AuditEvent/service de stockage existent ; AuditCenterServiceImpl renvoie une liste vide |
| Redis | INT/P | CacheManager conditionnel et TTL ; RedisAutoConfiguration exclue en prod |
| WebSocket | P/D | Serveur STOMP/SockJS, clients Angular JSON brut sans distribution vers channels ; pas d'écouteurs pipeline trouvés |

### PostgreSQL / Flyway

1. Les migrations contiennent de vraies FK, contraintes d'unicité, checks et index partiels sur deleted=false. BaseEntity:21-49 apporte UUID, verrou optimiste @Version, timestamps et filtre @Where ; cela ne remplace pas une isolation tenant.
2. Entités **bo_notifications** et **audit_events** sans CREATE TABLE correspondant dans V1-V64. Le champ **KnowledgeNode.embedding** n'apparaît pas dans V50 ni dans les migrations recherchées. Le profil dev utilise ddl-auto=validate : un démarrage propre conforme aux migrations est donc bloqué ou, au minimum, non validable en l'état. Aucune base propre n'a été créée pour confirmer les messages exacts.
3. application-prod.yml:13-28 désactive Flyway et active ddl-auto=update, initialisation SQL et Batch. Le schéma produit peut diverger de celui des migrations ; un démarrage en production ne prouverait pas que V1-V64 est cohérent.
4. Plusieurs attributs JSONB sont mappés en String sans mapping JSON explicite (KnowledgeNode.properties, etc.) ; le pipeline écrit une chaîne source:document:... qui n'est pas un objet JSON. Risque SQL supplémentaire à tester sur PostgreSQL réel.
5. Candidat sans lien user_id/tenant_id ; liaison utilisateur-profil faite par email. Document.tenantId et ImportHistoryRecord.tenantId sont des chaînes libres ; pas de FK tenant. Les tables knowledge et recommandations ne portent pas une stratégie tenant de bout en bout.
6. V53 feedback et V55 learning stockent des UUID sans FK vers candidats/programmes/recommandations ; les UUID fournis par client peuvent devenir orphelins.
7. V29-V32 sont explicitement des seeds sample ; V56 peuple le graphe avec données de démonstration. Les seeds V57-V64 ajoutent d'autres pays/universités/programmes. Aucune provenance officielle, validité des seuils ou recette des données réellement en base n'est établie. Les migrations de seeds ne doivent pas être prises pour des imports de guides.
8. V38 utilise un tenant code DEFAULT ; RegisterController cherche default et peut créer un tenant sans organisation obligatoire si ce tenant n'existe pas. Incohérence à résoudre.
9. Certaines transactions couvrent OCR/persistance longs, avec catch général dans la même méthode ; état partiel ou transaction rollback-only possible. La séparation fichier/SQL dans TranscriptController peut laisser un fichier orphelin.
10. Compilation/démarrage sur PostgreSQL neuf, cohérence des données existantes et application complète de Flyway restent non exécutés. Aucun reset ni réparation de checksum n'a été tenté.

## D. Audit Angular

Architecture standalone, lazy loading par domaine, guards auth et rôle au niveau routing, interceptor bearer et refresh, services injectables, signals et formulaires ngModel. SCSS et composants loading/empty/modal/confirm/toast partagés sont réutilisables. Les handlers utilisent largement any, ce qui masque les écarts DTO malgré la réussite du compilateur.

Dans le tableau, le nom du composant est localisé dans frontend/src/app/<domaine>/... ; les appels et lignes sont détaillés en annexe. **Tous les affichages sont inspectés statiquement et restent non validés en navigateur.** Les guards frontend ne remplacent pas l'autorisation serveur.

| Page / route | Statut | Actions, validation, erreurs et intégration |
|---|---|---|
| /auth/login | INT/P | Formulaire et AuthService.login réels ; erreurs affichées ; sécurité dépend du backend |
| /auth/forgot-password | M | Formulaire branché ; serveur affiche succès sans envoyer de lien |
| /auth/reset-password | M | Confirmation locale ; serveur n'effectue aucune réinitialisation |
| Inscription Angular | A | Pas de page/route dédiée ; inscription backend et Flutter existent |
| /dashboard | P/M | Loading et charts ; count partiel ; fallback CPU/mémoire fixes ; formes de recentActivity/analytics incompatibles |
| /analytics | M/D | Appel réel mais KPI serveur fixes et shape kpis/topUniversities.items non interprétée comme telle |
| /monitoring | M/P | Refresh branché ; lit systemHealth au lieu de systemMetrics ; affiche services UP et chiffres par défaut |
| /universities | D | Modal CRUD, validations name/country/city, toasts et pagination ; endpoints absents ; recherche non transmise |
| /universities/:id | D | Chargement/détail ; endpoint absent |
| Programmes / facultés / campus | A | Services API présents mais pas de pages/routes dédiées |
| Bourses / guides | A | Pas de pages dédiées ; getGuides utilisé par écran règles |
| /recommendations | D | Formulaire bac/moyenne ; POST réel ; résultat ignoré puis liste non paginée reçue comme tableau vide |
| Simulations / comparaisons | A | Pas de pages dédiées ; backend simulation/comparaison mock |
| /documents | D | Sélection fichier, suppression, modal détail et loading ; POST /documents erroné ; /extractions absent |
| /imports | D | Upload/loading/message d'erreur ; GET /imports absent ; dataType obligatoire jamais envoyé |
| /intelligence | D | Pipeline upload branché à /intelligence/process, état et erreurs ; backend bloqué avant extraction |
| /intelligence/knowledge | P | Appel /intelligence/knowledge?q=... cohérent ; recherche texte, pas de validation métier |
| /intelligence/smart-query | P | Appel cohérent ; réponse structurée et absence d'information possibles, sources limitées |
| /knowledge | D | GET /knowledge/nodes inexistant ; recherche envoie q, backend exige query |
| /knowledge/smart-query | D | POST /knowledge/smart-query inexistant ; serveur expose /knowledge/query |
| /rules | M | Catégories vides ; guides lus mais ignorés ; save utilise setTimeout et toast sans API |
| /notifications | D | Actions branchées ; routes /notifications et PUT incompatibles avec /backoffice/notifications et POST |
| /administration | P/D | Onglets réels ; bouton Ajouter utilisateur et menu more_vert sans action ; erreurs secondaires silencieuses |
| /administration/users | D/P | CRUD/modal/loading/toasts ; create échoue systématiquement ; sélection rôle non sauvée par update ; réponse sans roles |
| /administration/roles | D | UI CRUD/permissions ; endpoints absents ; codes users:read différents des codes SQL et prod |
| /administration/organizations | P/D | CRUD réel ; formulaire name/country/description incomplet par rapport au DTO à vérifier avant recette |
| /administration/tenants | INT/P | CRUD relié ; validation UI seulement name ; contrôle tenant serveur insuffisant |
| /administration/teams | D | CRUD/modal présents, endpoints absents |
| /administration/departments | D | CRUD/modal présents, endpoints absents |
| Invitations | A/P | Méthodes ApiService sans page/route ; list sans tenantId requis et DELETE au lieu de POST /{id}/revoke |
| /reports | M | Six cartes et dates fixes ; Nouveau rapport et Exporter sans handlers |
| /settings | M | Toggles locaux ; Sauvegarder sans handler ni persistance |
| /profile | D/P | Formulaire branché sur PUT /auth/me et /auth/me/password absents ; bouton MFA Configurer sans action |
| Audit | A/P | ApiService.getAuditLogs vers /audit inexistant ; pas de page dédiée, backend /backoffice/audit est vide |

### Données fictives et interactions trompeuses

| Source | Constat | API réelle ou correction nécessaire |
|---|---|---|
| dashboard.component.ts:238-241 | CPU 34, mémoire 52, disque 45, threads 12 en fallback | /backoffice/dashboard puis métriques mesurées ; afficher indisponible si absentes |
| monitoring.component.ts:105-116 | Services tous UP, versions/chiffres fixes | /backoffice/monitoring, lire systemMetrics et healthChecks réels |
| reports.component.ts:38-44 | Rapports et dates de juillet 2026 fixes | GET /backoffice/reports/generate existe mais génération à implémenter |
| settings.component.ts:62-69 | Paramètres uniquement locaux | /settings absent ; persister uniquement les réglages indispensables |
| rules.component.ts:157-210 | Vides construits localement et faux succès save | API critères/version de guide à exposer ; ne pas annoncer une sauvegarde |
| roles.component.ts:117-125 | Catalogue permissions statique non aligné | API rôle/permissions absente ; unifier avec PermissionCode et seeds |
| AnalyticsServiceImpl:20-59 | Classements, taux et précision OCR constants | /backoffice/analytics doit calculer à partir des faits persistés |
| DefaultExplainableAiServiceV2:20-77 | Notes et raisons constantes | /knowledge/explanations/{id} doit lire la recommandation réelle |
| DefaultSimulationEngine:73-90 | Programmes fixes | /recommendations/simulate doit lire programmes et profil réels |
| BeninOrientationController:29-53,134-138 | Catalogue/seuils/universités codés en dur | /benin/orientation/* existe, mais doit être rattaché à des guides validés |
| DefaultChatService:20-27 | FAQ et seuil moyen générique | /ai/chat existe mais doit lire les sources et refuser de conclure sans preuve |
| DefaultComparisonService / DefaultExportService | Comparaisons et exports fixes | Corriger leurs endpoints existants, éviter une seconde implémentation |

Les fixtures sous src/test, les palettes de couleurs, mois de graphiques, exemples de questions, menus, skeletons et le segment « Aucune donnée » du doughnut ne sont pas des faux faits métier. Les zéros par défaut ne sont pas toujours des mocks, mais doivent être distingués de données indisponibles.

Performance : recherches en mémoire/findAll répétées, requêtes par programme pour le scoring, absence de bornes cohérentes sur size/limit, multiples any et erreurs avalées. Le champ search des universités déclenche un rechargement sans filtrer côté API ; openEdit appelle onCountryChange qui remet cityId à vide (universities.component.ts:199-200,267). Responsive présent dans les styles, mais aucune recette de résolution ni mesure Lighthouse/bundle n'a été exécutée.

## E. Sécurité et anomalies

Les identifiants, mots de passe, valeurs JWT et variables secrètes ne sont pas reproduits. Les gravités expriment l'impact potentiel ; aucune exploitation n'a été exécutée.

| ID | Gravité | Preuve / description | Impact | Correction recommandée |
|---|---|---|---|---|
| SEC-01 | Critique | config/ProdDataInitializer.java:56-78 : super admin et mot de passe fixes, identifiants journalisés ; :82-84 réactive deleted | Prise de contrôle si déployé avec ces valeurs ; réactivation indue | Bootstrap unique hors code, secret injecté, rotation, suppression du log et réactivation automatique |
| SEC-02 | Critique | security/jwt/JwtTokenProvider.java:43-66 et JwtAuthenticationFilter.java:39-57 : même format/signature/claims pour access et refresh, aucun typage vérifié | Refresh bearer valide sur API, même révoqué en base tant que signature/date valides | Discriminer type/audience, vérifier type dans filtre, stratégie de révocation |
| SEC-03 | Élevée | CandidateController:42-47 ; TranscriptController:43-48 ; IntelligenceController:98-120 | IDOR : candidat autorisé à lire UUID d'autrui / modifier feedback et historique | Déduire identité du principal, vérifier propriétaire et tenant en service/repository |
| SEC-04 | Élevée | NotificationController:24-46 ; NotificationServiceImpl:51-55 | userId choisi par client ; notification d'autrui lisible/modifiable | Scope par principal et contrôle d'accès pour chaque notification |
| SEC-05 | Élevée | UserAdminServiceImpl:49-56 ; DocumentServiceImpl:185-200 | Tenant ignoré dans users ; recherche document prioritaire sur filtre tenant ; détails par id non scoped | Requêtes tenant obligatoires et tests de non-régression entre deux tenants |
| SEC-06 | Élevée | JwtAuthenticationFilter:43-57 ; AuthenticationServiceImpl.refreshToken | Filtre crée Authentication sans tester enabled/locked ; refresh sans contrôle état compte | Vérifier état à chaque authentification/refresh ; invalider sessions au changement critique |
| SEC-07 | Élevée | application.yml:73 : fallback de secret JWT ; application-dev.yml:5 valeur DB littérale | Configuration démarre avec secret prévisible si variable manquante | Exiger variables sans fallback en environnement réel, rotation si utilisées ; ne pas afficher valeurs |
| SEC-08 | Élevée | SecurityConfig:73 ; ProdDataInitializer:48-50,73-75 | /admin/** exige ADMIN, super admin n'a que SUPER_ADMIN ; autorisation méthode ne peut pas élargir ce filtre | hasAnyRole cohérent ou hiérarchie explicite, tests ADMIN/SUPER_ADMIN/CANDIDAT |
| SEC-09 | Élevée | AuthenticationServiceImpl.login : catch sauvegarde tentatives puis relance dans @Transactional ; CustomUserDetails.isAccountNonLocked retourne true | Échec login peut annuler compteur/verrouillage ; absence de rate limiter dédié repéré | Compteur transaction indépendante et verrouillage testé ; limitation serveur |
| SEC-10 | Moyenne | RefreshTokenServiceImpl:30-65 ; JwtTokenProvider:59-66 | Pas de jti aléatoire/type, tokens simultanés potentiellement identiques ; rotation sans verrou/rejeu de famille | Jeton opaque aléatoire haché, rotation atomique, détection réutilisation, verrou DB |
| SEC-11 | Moyenne | RegisterController:103-107,137-149,168-201 | Vérification sans expiration, token journalisé, stockage dans mfaSecret ; HTML interpolé sans échappement | Table token hashé à durée courte ; log expurgé ; échappement HTML ; ne pas détourner MFA |
| SEC-12 | Élevée | TranscriptController:53-68 ; StorageServiceImpl:53-59,86-87 | Nom original concaténé au chemin et MIME client ; load sans confinement startsWith | Nom serveur, normalisation/confinement, signature fichier, quotas ; réutiliser stockage validé |
| SEC-13 | Moyenne | application-prod.yml:54-57 et WebConfig:37-40 | allowedOrigins contient * avec allowCredentials=true : configuration CORS incompatible | Liste explicite d'origines HTTPS, tester requêtes et preflight |
| SEC-14 | Moyenne | WebSocketConfig:20-23 ; pas de configuration d'autorisation STOMP repérée | Origines * ; tenant/propriétaire non établis pour abonnements et messages | Auth STOMP et autorisation destinations, portée tenant ; aligner protocole clients |
| SEC-15 | Moyenne | CustomUserDetailsService:33-45 ; ImportController:29 et similaires | Authorities ne contiennent que rôles ; hasPermission à un seul argument sans évaluateur repéré | Permissions canoniques, hasAuthority ou évaluateur correctement signé et tests |
| SEC-16 | Moyenne | auth.service.ts:85-96,147-167 ; auth.interceptor.ts | Tokens JS-accessibles ; refresh concurrent ; refresh persistent écrit en session alors que getToken privilégie local | Stockage adapté, protection XSS, refresh mutualisé, suppression anciennes valeurs et respect rememberMe |
| SEC-17 | Moyenne | RegisterController:54,236-248 | Inscription peut sélectionner tenantCode existant sans invitation/autorisation | Politique explicite pour inscription publique et rattachement tenant |
| SEC-18 | Faible | AuthenticationServiceImpl.login ; IntelligenceController:70 ; RegisterController:129 | Emails, requêtes utilisateur et erreurs internes journalisés/renvoyés ; JWT exception log susceptible de claims | Minimisation PII, redaction, erreurs métier stables et logs à accès restreint |
| FUN-01 | Élevée | DocumentProcessingPipeline.java:45-49,57-60,164-174 | steps initialisé au champ sans @Builder.Default ; addStep déclenche NPE | Initialisation effective, gestion d'échec explicite et test de pipeline |
| FUN-02 | Élevée | PersistenceService.java:40-100 | Programmes/critères jamais sauvegardés ; pays/ville/année/barème/type obligatoires omis | Mapping complet dans zone brouillon, validation avant publication transactionnelle |
| FUN-03 | Élevée | RecommendationController.java:71,206 ; DefaultRankingEngine:114-119 ; Explanation:17 | guideVersionId null ; getWarnings().stream sur null | Sélection version publiée, listes non nulles et tests candidat réel |
| FUN-04 | Élevée | RecommendationController:41-43,71 ; DefaultRankingEngine:26,34 | EligibilityEngine injecté mais non appelé ; UUID programme aléatoire ; eligible si score>=40 | Utiliser vrais IDs et éligibilité officielle ; inclure IDs dans DTO |
| FUN-05 | Élevée | RealScoringEngine:30-38,86-108,206-239,265-276 | Poids bourse absent des composants (somme effective 0,90), historique fixe ; pays/préférences génériques ; confiance 100% malgré manque de faits | Règles testées, scores fondés sur données disponibles, inconnus explicites |
| FUN-06 | Élevée | RealEligibilityEngine:158-187 environ | IN_LIST/NOT_IN_LIST simplifiés true ; SPECIFIC_GRADE utilise moyenne ; groupes logiques non évalués | Évaluer type bac/langue/pays/matière/AND-OR ; inconnu différent de satisfait |
| FUN-07 | Élevée | ImportOrchestratorImpl:199-201 ; ImportController:41-46 | parseFile vide ; contenu fichier perdu ; rollback ne fait que cancel | Transmettre flux, persister et historiser vraies lignes ; rollback réel/idempotent |
| FUN-08 | Élevée | TranscriptController:70-79 et Transcript.java:26-34 | Sauvegarde sans candidat/année NOT NULL, pas d'analyse | Relier principal/profil/année, orchestrer analyse et validation notes |
| FUN-09 | Élevée | ChatController:26,35 et DefaultChatService:34,63 | current-user non UUID ; création session prévisiblement en erreur ; content Flutter != message attendu | Principal réel, propriété session et contrat message commun |
| FUN-10 | Élevée | V1-V64, Notification.java @Table, AuditEvent.java @Table, KnowledgeNode.embedding | Tables bo_notifications/audit_events et colonne embedding absentes du DDL inspecté | Nouvelles migrations additives, PostgreSQL neuf + Hibernate validate |
| FUN-11 | Moyenne | DocumentServiceImpl:65-74,222-227 | Version jamais enregistrée et fichier original jamais stocké ; download exception | Sauvegarde binaire/version effective et téléchargement autorisé |
| FUN-12 | Moyenne | KnowledgeGraphService:82-90 ; SearchIndexService:46-51 | Nœuds sans entityId NOT NULL ; properties non JSON ; indexation duplique nœuds au lieu de SearchIndex | Vrais liens entités/provenance JSON et upserts dans la bonne table |
| FUN-13 | Moyenne | DocumentProcessingPipeline:97-101 ; aucune référence listener hors ce fichier | Événement de recalcul sans consommateur ; progression non publiée vers socket | Brancher traitement après commit et statut durable |
| FUN-14 | Moyenne | ImportOrchestratorImpl:118-120,168-182 | Sans mapper compte succès ; dryRun appelle même chemin d'écriture | Échec explicite type non supporté ; dry run sans mutation |
| UI-01 | Élevée | ApiService + contrôleurs, annexes | Nombreux endpoints absents/verbes erronés ; erreurs transformées en états vides | Aligner contrats et distinguer erreur/vide dans UI |
| UI-02 | Moyenne | rules:204-210, reports:12-44, settings:13-69, profile:87 | Faux succès et boutons non branchés | Brancher capacités indispensables ; rendre indisponibles les autres sans succès fictif |
| OPS-01 | Élevée | application-prod.yml:13-34 ; Dockerfile:17 | update automatique et Flyway off ; package -DskipTests ; Redis exclu malgré compose Redis | Schéma versionné, CI avec tests, profil cohérent sans initialisation implicite |

Précisions : les numéros « environ » désignent une méthode à retrouver par son nom ; les annexes fournissent les chemins complets. Les problèmes d'autorisation sont établis à la lecture, sans accès aux données d'autres utilisateurs. Aucun compte par défaut n'a été utilisé.

### Autres contrôles sécurité

Requêtes JPQL inspectées paramétrées ; aucune concaténation SQL exécutable à partir d'entrée utilisateur repérée dans ces repositories. Cela ne constitue pas un audit dynamique d'injection. Les uploads docintel/pipeline ne passent pas par StorageServiceImpl et ne bénéficient donc pas de toutes ses validations. Le plafond multipart 10 MB existe ; aucun contrôle antivirus, signature binaire ou limites PDF/archives robuste n'est démontré.

Swagger est public selon SecurityConfig ; packages-to-scan limite la documentation aux modules.*.controller, omettant recengine/docintel/intelligence/knowledge/backoffice. Ce n'est pas une cartographie complète pour Flutter. MFA est un champ de modèle, pas un dispositif opérationnel démontré. Le fichier .env existe et n'est pas listé par git ls-files ; aucun contenu n'a été reproduit, aucun historique Git de secrets n'a été audité.

## F. Pipeline intelligent et parcours de bout en bout

### Guide : vérification des 14 étapes

| Étape attendue | Statut | Réalité |
|---|---|---|
| 1. Upload PDF | D/P | Deux entrées distinctes : /documents/upload (docintel) et /intelligence/process ; Angular documents utilise une troisième URL incorrecte |
| 2. Vérification fichier | P | Plafond global multipart ; pas de contrôle binaire partagé entre chemins |
| 3. Texte / OCR | M/D | LocalOcrEngine lit octets et invente confiance 0,85/page 1 ; PDF OcrService cherche stream/endstream sans décompression ; image placeholder |
| 4. Pays / année | P/A | Pays détecté dans liste fixe (Bénin absent de cette liste d'extraction), contexte global ambigu ; année académique non rattachée |
| 5. Universités | P/D | Regex de lignes ; persistance omet pays et ville obligatoires |
| 6. Programmes | P/D | Extraction mots-clés ; aucune sauvegarde dans PersistenceService |
| 7. Critères admission | P/D | Regex moyenne/note/minimum ; aucune sauvegarde ni rattachement guide/programme/matière |
| 8. Bourses | P/D | Regex large incluant « be » ; persistance omet pays, année et type obligatoires |
| 9. Frais / langues | P | Extraction heuristique ; ni frais ni langues structurés dans les entités métier par ce pipeline |
| 10. Validation | A | Aucune approbation humaine ou seuil qualité effectif avant activation ; critère mandatory défini automatiquement |
| 11. PostgreSQL | D | Contraintes non respectées ; pas de transaction métier fiable de publication ; erreur steps précède ces étapes |
| 12. Knowledge Graph | D/P | Nœuds sans entityId ; pas de création de relations dans updateGraph ; provenance chaîne seulement |
| 13. Index | D | SearchIndexService écrit encore KnowledgeNode ; SemanticSearchService lit SearchIndexRepository |
| 14. Recommandations | A/D | Événement sans listener ; programmes/critères non persistés et version officielle non sélectionnée |

Trois voies ne sont pas équivalentes : /documents/upload n'appelle pas DocumentProcessingPipeline ; /intelligence/process crée un UUID aléatoire sans enregistrement Document source ; TesseractOcrEngine n'est pas utilisé par OcrService. Aucun PDF scanné attendu n'est validé. Le commentaire « Tesseract via tess4j » n'est pas une implémentation et aucune dépendance OCR/PDF de ce type ne figure dans pom.xml.

Traçabilité : tables pages, versions, metadata, audit et guide_versions existent, mais le pipeline ne les alimente pas de manière liée aux critères officiels. Il manque une liaison vérifiable document/version/page/année/statut de validation pour chaque fait publié. Un nœud avec source:document:<UUID> ne suffit pas, surtout quand aucun Document correspondant n'est enregistré.

### Recommandation et IA native

ProfileAnalyzer calcule moyennes et catégories de compétences de façon déterministe à partir des notes ; listes de matières fixes. RealScoringEngine lit bien des Program en base mais ignore une partie des contraintes et passe une version nulle à la requête critères. Avec guide_version_id NOT NULL, cette sélection ne retrouve pas des critères officiels versionnés. La génération ne filtre pas avec RealEligibilityEngine. Le RankingEngine fabrique des UUID et une probabilité 85/65/40/15 selon seuils de score : ce ne sont pas des probabilités calibrées sur des admissions.

HistoricalEngine lit quelques attributs du catalogue, pas un historique d'admissions entraîné, et n'est pas branché au calcul réel. DefaultExplanationEngine existe ; generate utilise les explications du RankingEngine. ExplainableAiServiceV2 produit des faits fixes. DefaultSimulationEngine utilise programmes et scoring mock ; profil de référence de simulate construit avec trois arguments null.

KnowledgeEngineService.rebuildIndex ne fait que journaliser. SemanticSearchService est une recherche textuelle SQL/contains, pas un moteur sémantique entraîné. SimilarityEngineService lit une matrice existante ; aucun calcul statistique d'embeddings démontré. LearningEngine persiste feedback et ajuste une HashMap non durable ; getAdjustmentFactor n'est pas consommé dans les recommandations. DefaultRecommendationLearningService calcule des taux simples ; statistics filtre programId=null au lieu d'agréger tous les programmes.

SmartQueryEngine /intelligence sait répondre « Aucune information » et retourne quelques chaînes de provenance : c'est utile, mais insuffisant pour citer page/version ou garantir fiabilité. /knowledge/query utilise un autre service, sans requête catalogue réelle. Le chat est une FAQ fixe non connectée aux moteurs.

Les chemins inspectés fonctionnent conceptuellement sans API IA payante : aucune exigence de LLM payant observée. En revanche, autonomie financière ne signifie pas OCR/IA opérationnels. Aucune admission ni bourse ne doit être garantie : les probabilités actuelles ne sont pas des preuves et les affirmations de la FAQ doivent être sourcées ou retirées.

### Parcours candidat

| Transition | État |
|---|---|
| Inscription -> vérification -> login | Partiel : email désactivé par défaut, token sans expiration, profil créé en best effort |
| Login -> profil académique | Bloqué : /candidates/me et mutation profil absents, lien User/Candidate non robuste |
| Profil -> upload relevé | Bloqué par relations obligatoires non renseignées |
| Upload -> extraction notes | Absent dans TranscriptController ; OCR réel non démontré |
| Extraction -> validation notes | Endpoint/workflow absent |
| Notes validées -> recommandations | Pas de lecture effective des notes validées dans generate ; notes fournies par requête |
| Recommandations -> critères/explications | Version nulle, éligibilité non branchée, NPE warnings ; endpoints détails placeholder |
| Recommandations -> bourses | Liste disponible, sélection fiable et éligibilité non intégrées au score réel |
| Résultat -> feedback -> amélioration | Feedback possible mais IDs et propriétaire non garantis ; score réel n'utilise pas learning |
| Retour utilisateur -> historique/notifications | Historique recommandations non persisté ; contrat notifications incompatible |

## G. Compatibilité Flutter et API nécessaires

Le mobile existe déjà ; aucune création ou modification Flutter n'a été effectuée. Le client actuel doit être aligné sur un contrat stable une fois les blocages corrigés.

Les chemins ci-dessous sont relatifs à la base API : dev /api/v1 ; prod actuellement /. Les API marquées « à fournir » sont des propositions de contrat minimal, pas des endpoints constatés.

| Fonction mobile | API constatée / contrat nécessaire | État |
|---|---|---|
| Inscription / connexion | POST /auth/register, /auth/login ; GET /auth/verify | Partiel ; sécuriser email et rattachement profil |
| Refresh / logout | POST /auth/refresh, /auth/logout | Partiel ; rotation/type/révocation à corriger ; Flutter logout est seulement local |
| Mot de passe oublié/réinitialisé | POST /auth/forgot-password, /auth/reset-password | Routes présentes, métier mock |
| Onboarding | À fournir : état onboarding via profil personnel | Manquant |
| Profil utilisateur | GET /auth/me ; PUT /auth/me et /auth/me/password à fournir | Lecture présente, mutations absentes |
| Profil candidat / académique | À fournir : GET/PUT /candidates/me, notes et préférences persistées | Manquant |
| Upload relevé | POST /transcripts multipart file | Défectueux ; rattachement candidat/année manquant |
| Analyse / validation notes | À fournir : statut d'analyse, résultat structuré, confirmation/correction de lignes | Manquant |
| Recommandations | POST /recommendations/generate ; GET /recommendations | Partiel/défectueux ; persister et paginer par candidat |
| Scores / explications | GET /recommendations/{id}/scores et /explanation | Mock ; fournir vrais faits, IDs et sources |
| Éligibilité | Moteur Java présent ; réponse/API versionnée à brancher | Manquant comme parcours fiable |
| Universités / programmes | /universities et /programs demandés par Flutter mais absents | Manquant ; catalogue paginé et détail nécessaires |
| Bourses | GET /scholarships, /scholarships/{id} | Présent non testé ; éligibilité personnalisée à intégrer |
| Guides | GET /guides, /guides/{id} | Partiel ; version publiée et téléchargement manquants |
| Recherche intelligente | POST /intelligence/smart-query ; GET /intelligence/knowledge?q | Partiel ; résultat textuel, sources à compléter |
| Knowledge | POST /knowledge/query ; GET /knowledge/search?query ; /similar?entityType&entityId | Présent mais simplifié ; Flutter envoie q et nodeId, incorrects |
| Chat | /ai/chat/sessions et /messages | Défectueux current-user ; Flutter envoie content, serveur attend message |
| Comparaison | GET /ai/compare/programs?programA&programB et /universities?universityA&universityB | Mock ; Flutter utilise POST et listes |
| Simulation | POST /recommendations/simulate | Mock ; à baser sur profil courant et vrais programmes |
| Favoris | À fournir : liste/ajout/retrait des favoris du candidat | Absent |
| Historique | /intelligence/history/{candidateId} = feedback seulement ; historique recommandations personnel à fournir | Partiel |
| Notifications | /backoffice/notifications?userId ; POST /{id}/read et /read-all | Contrat à sécuriser par principal ; Flutter appelle autre chemin/verbe |
| Feedback | POST /intelligence/feedback et /knowledge/feedback | Présent partiel ; ownership et vrais IDs requis |
| Paramètres | À fournir : préférences personnelles indispensables | Absent |
| Exports | GET /ai/export/pdf/{id}, /excel/{id}, /csv/{id} | Mock ; Flutter doit aussi recevoir bytes et traiter Content-Disposition |

Défauts additionnels du client existant, ../Orientation-mobile/lib/core/network/api_client.dart : le refresh utilise le même Dio intercepté sans exclusion /auth/refresh, donc un 401 refresh peut récursivement relancer le refresh ; aucune borne de reprise ni mutualisation des 401 ; logout ne révoque pas côté serveur ; téléchargement sans responseType bytes ; routes document, profil, comparaison et notifications incompatibles. AppConstants vise actuellement le serveur distant à racine / ; aucun test n'a ciblé ce serveur.

Avant le démarrage de nouvelles fonctionnalités Flutter : figer auth/refresh, DTO paginés, identité personnelle, catalogue, relevés/analyse/validation, génération persistée, éligibilité/explications/provenance et erreurs HTTP. Favoris et paramètres restent nécessaires au produit cible, mais ne doivent pas détourner les sprints de réparation du parcours essentiel.

## H. Contrats API et cartographie

Les annexes détaillent chaque mapping REST identifié, sa signature de requête, sa réponse déclarée, les délégations visibles, les règles d'accès et les consommateurs Angular repérés. Les URL réelles se déduisent du contexte : /api/v1 + route en dev/test ; route seule en prod. Le préfixe de sécurité /admin s'applique EN PLUS des annotations de méthode.

Principaux écarts vérifiés :

- POST /documents (Angular/Flutter) -> serveur POST /documents/upload ; /documents/{id}/extractions et /classifications absents.
- GET /imports (Angular) absent ; upload attend multipart dataType et non type ; rollback est une annulation en mémoire.
- GET /universities, /programs, /campuses, /faculties, /academic-years, /subjects, /grade-scales : pas de contrôleur correspondant.
- PUT /auth/me et /auth/me/password : absents ; /candidates/me ne correspond pas à /{id} UUID.
- GET /recommendations : objet {programs,message}, pas Page ; génération renvoie recommendations mais UI jette ce résultat avant rechargement.
- GET /knowledge/nodes et POST /knowledge/smart-query : absents ; /search exige query ; /similar exige entityType et entityId.
- /notifications doit être /backoffice/notifications ; marquages POST au lieu de PUT, paramètres userId requis actuellement.
- ApiService reports utilise /backoffice/report et POST /generate ; serveur /backoffice/reports/generate en GET.
- /admin/roles, /admin/teams, /admin/departments, /settings, /backoffice/activity-logs et /audit appelés sans API correspondante.
- Invitations : tenantId obligatoire sur liste mais omis ; annulation DELETE absente, serveur POST /{id}/revoke.
- Responses analytics/monitoring imbriquées différentes des champs UI ; UserResponse ne renvoie pas roles attendu par le formulaire.
- Feedback /knowledge accepte candidateId/programId mais le service ne remplit pas recommendationId NOT NULL.
- Codes permissions divergent entre V38 (USER_READ...), initializer prod (users.read...) et Angular (users:read...).
- BacType, ProgramType et CriterionOperator sont des enums Java ; toute liste UI ou extraction textuelle doit être validée sur ces valeurs et sur le pays, sans traduction implicite.
- Des UUID sont générés à la place des IDs programmes et les RecommendationItem n'exposent pas les IDs nécessaires aux détails/favoris/feedback.
- Les endpoints varient entre DTO, Map, entités et Page ; l'enveloppe BaseResponse n'est utilisée que sur certains chemins d'erreur. GenericExceptionHandler renvoie 500 pour de nombreuses erreurs de validation métier non typées.
- STOMP/SockJS serveur et WebSocket JSON client ne sont pas compatibles ; les clients ne distribuent pas les messages vers leurs Subjects de channels et leur reconnexion conserve la connexion dans la map.

Une route non consommée signifie uniquement « aucun consommateur Angular détecté dans le source inspecté » ; elle peut servir Flutter, Swagger, un script externe ou être inutilisée. Une route similaire ne garantit pas compatibilité des query params, champs, pagination, enum, autorisation, multipart ou téléchargement.

## I. Validation technique réalisée et restant à faire

| Vérification | Résultat réel |
|---|---|
| Lecture backend / Angular / Flutter actif | Réussie |
| Inventaire des sources, routes, migrations, tests | Réalisé ; annexes |
| node .../typescript/bin/tsc -p tsconfig.app.json --noEmit --incremental false | Réussi sans diagnostic |
| node .../@angular/compiler-cli/bundles/src/bin/ngc.js -p tsconfig.app.json --noEmit --incremental false | Réussi sans diagnostic |
| Premier lancement Node dans sandbox | EPERM lstat C:/Users/HP ; analyses relancées avec élévation autorisée automatiquement |
| Build Angular bundles / budgets / styles production | Non exécuté : génère dist/cache ; non couvert par noEmit |
| Compilation backend / tests Maven | Non exécutés : pas de mvn PATH, wrapper nécessitant vérification du cache/outillage ; préserver la consigne de ne créer que le rapport, sans target/logs supplémentaires |
| Tests Angular | Aucun *.spec.ts et aucune target test dans angular.json ; script npm test déclaré sans configuration correspondante |
| Lint / e2e Angular | Scripts déclarés mais targets absentes d'angular.json |
| TCP local PostgreSQL / Redis | Connexion acceptée sur 5432/6379, pas de preuve de schéma ni de health applicatif |
| Démarrage application / migrations propres | Non exécuté ; migrations/profil prod incompatibles et bases existantes à préserver |
| Docker | Commande non trouvée via Get-Command ; aucun conteneur lancé |
| Tests métier / PDF scannés / E2E / responsive | Non exécutés |
| Sécurité dépendances / audit de l'historique secrets | Non exécutés ; versions et configs inspectées seulement |

Les tests présents ne prouvent pas les capacités annoncées : ImportOrchestratorTest.shouldExecuteImport attend explicitement FAILED ; OcrEngineTest traite du texte et non un PDF scanné ; ScoringEngineTest couvre DefaultScoringEngine, pas RealScoringEngine ; UniversityIntegrationTest attend /universities alors que ce contrôleur manque. Les tests Spring utilisent H2/create-drop et Flyway désactivé, donc ne valident pas PostgreSQL/Flyway. Aucun résultat JUnit de cette session n'est revendiqué.

## J. Plan court de finalisation

### Sprint Finalisation 1 — sécurité et fondations

1. Supprimer bootstrap admin fixe, imposer secrets, rotation si déjà utilisés ; rétablir filtrage des logs.
2. Séparer access/refresh, rotation atomique, révocation, statut compte et verrouillage login ; corriger accès SUPER_ADMIN et permissions.
3. Déduire utilisateur/tenant du principal sur profils, relevés, documents, feedback et notifications ; tests à deux comptes/deux tenants.
4. Ajouter migrations additives manquantes et colonnes nécessaires ; remettre Flyway + validate avec une procédure de transition non destructive du schéma existant.
5. Corriger récupération mot de passe/email et stockage upload, CORS et prérequis d'outillage.

Critère de sortie : aucun accès croisé dans les tests ; sessions révoquées refusées ; PostgreSQL temporaire isolé démarre sur migrations ; aucun compte/secret de secours ni mutation implicite au démarrage. Ne jamais modifier les migrations déjà appliquées pour contourner un checksum.

### Sprint Finalisation 2 — backend / Back Office et fin des mocks

1. Figer un seul contrat API et rétablir le catalogue (universités/campus/facultés/programmes), guides/versions et critères indispensables.
2. Unifier le pipeline documentaire existant : stockage/version source, vrai OCR local PDF/images, extraction en brouillon, validation humaine et publication.
3. Réparer null steps, contraintes de persistance, liens entityId/provenance JSON/page/année ; indexer la table de recherche réellement consommée.
4. Réparer imports file/dataType/parse/persist/history et rollback ; ne conserver que les types dont le mapper est opérationnel.
5. Aligner toutes les pages essentielles et champs DTO ; remplacer faux succès et métriques fixes par mesures ou statut indisponible. Débrancher visiblement les capacités secondaires sans métier.
6. Rendre création utilisateurs et configuration des rôles indispensables réellement opérationnelles, sans élargir le périmètre.

Critère de sortie : administrateur importe un guide représentatif, corrige/valide des faits et retrouve des programmes/critères sourcés ; API Angular sans route inexistante sur parcours retenu ; aucune erreur cachée comme liste vide.

### Sprint Finalisation 3 — parcours candidat et préparation Flutter

1. Lier User/Candidate, profil et années ; fiabiliser upload relevé, extraction des notes, correction et validation.
2. Lire le profil validé, sélectionner guide publié pour pays/année, appliquer éligibilité réelle, scores explicables et ID stables ; persister résultats et historique.
3. Supprimer programmes/scénarios/explications fixes du chemin candidat ; considérer données manquantes comme inconnues, ne pas inventer probabilité d'admission.
4. Brancher bourses et feedback sur vrais IDs ; rendre le learning facultatif tant qu'il n'est pas validé.
5. Valider E2E admin puis candidat sur PostgreSQL isolé, PDF texte et scanné, plusieurs pays/bacs, critères contradictoires, données manquantes, 401/403/404/validation et pagination.
6. Produire OpenAPI complet (élargir scan springdoc), exemples et tests de contrat ; aligner le client Flutter existant, notamment refresh, uploads et notifications.

Critère de sortie : le même fait extrait/validé d'un guide est traçable dans l'éligibilité et l'explication du résultat candidat ; recette sans mocks métier et isolation prouvée. Aucun nouveau moteur IA ou service payant nécessaire pour cette finalisation.

## K. Verdict explicite

1. **Backend réellement prêt pour Flutter ? Non.** Des API existent mais les contrats, données et contrôles d'accès fondamentaux ne sont pas fiables.
2. **Back Office fonctionnel de bout en bout ? Non.** Analyse Angular réussie, mais CRUD et workflows essentiels bloqués, pages simulées et routes manquantes.
3. **Recommandations utilisant réellement les guides importés ? Pas de chaîne effective démontrée ; code actuel incompatible avec cette affirmation.** Les programmes sont lus en base, pas une version de guide publiée appliquée aux critères.
4. **OCR fonctionnel sur les documents attendus ? Non pour PDF scannés/images.** Traitement texte limité ; aucun essai de corpus documentaire attendu.
5. **Données isolées et sécurisées ? Non.** Contrôles propriétaire/tenant incomplets et risques critiques d'authentification.
6. **Blocage développement mobile ?** Le mobile est déjà commencé ; l'extension fiable est bloquée par identité personnelle, catalogue, relevés, recommandations persistées, provenance et contrats stables.
7. **Corrections indispensables avant lancement ?** SEC-01 à SEC-09, contrôles propriétaire/tenant, schéma/migrations, OCR réel et validation, persistance/éligibilité/explications, contrats des parcours essentiels, puis preuves de tests et E2E.

Ce rapport est un audit statique étendu avec deux vérifications de compilation sans émission. Il ne certifie ni un déploiement existant, ni une base en service, ni une recette métier complète. Aucune correction n'a été commencée.

## Annexe 1 — Cartographie exhaustive des mappings REST repérés

Extraction statique des annotations Get/Post/Put/Delete/PatchMapping dans les contrôleurs REST. Les contrôleurs de socket sont recensés séparément. Chaque ligne exprime une déclaration Java, pas une réponse observée. Requête = signature complète (DTO, path/query/multipart) ; réponse = type déclaré. Les paramètres au nom implicite doivent être vérifiés avec les options de compilation Java.

108 mappings REST et 128 expressions HttpClient repérés. Les consommateurs sont rapprochés par méthode et patron URL ; cette analyse ne valide ni les query params ni les UUID ni les données. Les absences de consommation ne signifient pas automatiquement code mort.

| Méthode / route relative | Contrôleur et preuve | Délégations visibles | Accès méthode | Requête déclarée | Réponse déclarée | Consommateurs Angular / intégration |
| --- | --- | --- | --- | --- | --- | --- |
| POST /ai/chat/sessions | ChatController.createSession — src/main/java/com/orientation/orientationapp/ai/chat/controller/ChatController.java:23 | chatService.createSession | hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('CANDIDAT') | — | ResponseEntity<ChatSession> | Aucun appel Angular correspondant repéré |
| POST /ai/chat/sessions/{sessionId}/messages | ChatController.sendMessage — src/main/java/com/orientation/orientationapp/ai/chat/controller/ChatController.java:29 | chatService.sendMessage | hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('CANDIDAT') | @PathVariable UUID sessionId, @RequestBody Map<String, String> request | ResponseEntity<ChatMessage> | Aucun appel Angular correspondant repéré |
| GET /ai/chat/sessions/{sessionId} | ChatController.getSession — src/main/java/com/orientation/orientationapp/ai/chat/controller/ChatController.java:39 | chatService.getSession | hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('CANDIDAT') | @PathVariable UUID sessionId | ResponseEntity<ChatSession> | Aucun appel Angular correspondant repéré |
| GET /ai/compare/programs | ComparisonController.comparePrograms — src/main/java/com/orientation/orientationapp/ai/comparator/controller/ComparisonController.java:19 | comparisonService.comparePrograms | hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('CANDIDAT') | @RequestParam UUID programA, @RequestParam UUID programB | ResponseEntity<ComparisonResult> | Aucun appel Angular correspondant repéré |
| GET /ai/compare/universities | ComparisonController.compareUniversities — src/main/java/com/orientation/orientationapp/ai/comparator/controller/ComparisonController.java:27 | comparisonService.compareUniversities | hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('CANDIDAT') | @RequestParam UUID universityA, @RequestParam UUID universityB | ResponseEntity<ComparisonResult> | Aucun appel Angular correspondant repéré |
| GET /ai/export/pdf/{id} | ExportController.exportPdf — src/main/java/com/orientation/orientationapp/ai/export/controller/ExportController.java:21 | exportService.exportToPdf | hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('CANDIDAT') | @PathVariable UUID id | ResponseEntity<byte[]> | Aucun appel Angular correspondant repéré |
| GET /ai/export/excel/{id} | ExportController.exportExcel — src/main/java/com/orientation/orientationapp/ai/export/controller/ExportController.java:31 | exportService.exportToExcel | hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('CANDIDAT') | @PathVariable UUID id | ResponseEntity<byte[]> | Aucun appel Angular correspondant repéré |
| GET /ai/export/csv/{id} | ExportController.exportCsv — src/main/java/com/orientation/orientationapp/ai/export/controller/ExportController.java:41 | exportService.exportToCsv | hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('CANDIDAT') | @PathVariable UUID id | ResponseEntity<byte[]> | Aucun appel Angular correspondant repéré |
| GET /backoffice/analytics | AnalyticsController.getAnalytics — src/main/java/com/orientation/orientationapp/backoffice/analytics/controller/AnalyticsController.java:17 | analyticsService.getAnalytics | hasRole('SUPER_ADMIN') or hasRole('ADMIN') | — | ResponseEntity<AnalyticsResponse> | frontend/src/app/core/services/api.service.ts:37 getAnalytics -> dashboard/components/dashboard.component.ts, analytics/components/analytics.component.ts ; correspondance de route seulement, voir audit métier |
| GET /backoffice/analytics/tenant/{tenantId} | AnalyticsController.getAnalyticsByTenant — src/main/java/com/orientation/orientationapp/backoffice/analytics/controller/AnalyticsController.java:23 | analyticsService.getAnalyticsByTenant | hasRole('SUPER_ADMIN') or hasRole('ADMIN') | @PathVariable String tenantId | ResponseEntity<AnalyticsResponse> | Aucun appel Angular correspondant repéré |
| GET /backoffice/audit | AuditController.getAuditHistory — src/main/java/com/orientation/orientationapp/backoffice/audit_center/controller/AuditController.java:17 | auditCenterService.getAuditHistory | hasRole('SUPER_ADMIN') or hasRole('ADMIN') | @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size | ResponseEntity<AuditResponse> | Aucun appel Angular correspondant repéré |
| GET /backoffice/audit/search | AuditController.searchAudit — src/main/java/com/orientation/orientationapp/backoffice/audit_center/controller/AuditController.java:25 | auditCenterService.searchAudit | hasRole('SUPER_ADMIN') or hasRole('ADMIN') | @RequestParam String q, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size | ResponseEntity<AuditResponse> | Aucun appel Angular correspondant repéré |
| GET /backoffice/dashboard | DashboardController.getDashboard — src/main/java/com/orientation/orientationapp/backoffice/dashboard/controller/DashboardController.java:17 | dashboardService.getExecutiveDashboard | hasRole('SUPER_ADMIN') or hasRole('ADMIN') | — | ResponseEntity<DashboardResponse> | frontend/src/app/core/services/api.service.ts:33 getDashboard -> dashboard/components/dashboard.component.ts ; correspondance de route seulement, voir audit métier |
| GET /backoffice/dashboard/tenant/{tenantId} | DashboardController.getDashboardByTenant — src/main/java/com/orientation/orientationapp/backoffice/dashboard/controller/DashboardController.java:23 | dashboardService.getDashboardByTenant | hasRole('SUPER_ADMIN') or hasRole('ADMIN') | @PathVariable String tenantId | ResponseEntity<DashboardResponse> | frontend/src/app/core/services/api.service.ts:34 getDashboardByTenant -> aucune page directe repérée ; correspondance de route seulement, voir audit métier |
| GET /backoffice/monitoring | MonitoringController.getMonitoring — src/main/java/com/orientation/orientationapp/backoffice/monitoring/controller/MonitoringController.java:17 | monitoringService.getMonitoringData | hasRole('SUPER_ADMIN') or hasRole('ADMIN') | — | ResponseEntity<MonitoringResponse> | frontend/src/app/core/services/api.service.ts:40 getMonitoring -> monitoring/components/monitoring.component.ts ; correspondance de route seulement, voir audit métier |
| GET /backoffice/notifications | NotificationController.getNotifications — src/main/java/com/orientation/orientationapp/backoffice/notification_center/controller/NotificationController.java:22 | notificationService.getByUser | hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('CANDIDAT') | @RequestParam String userId, Pageable pageable | ResponseEntity<Page<Notification>> | Aucun appel Angular correspondant repéré |
| GET /backoffice/notifications/unread-count | NotificationController.getUnreadCount — src/main/java/com/orientation/orientationapp/backoffice/notification_center/controller/NotificationController.java:30 | notificationService.getUnreadCount | hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('CANDIDAT') | @RequestParam String userId | ResponseEntity<Map<String, Long>> | Aucun appel Angular correspondant repéré |
| POST /backoffice/notifications/{id}/read | NotificationController.markAsRead — src/main/java/com/orientation/orientationapp/backoffice/notification_center/controller/NotificationController.java:36 | notificationService.markAsRead | hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('CANDIDAT') | @PathVariable UUID id | ResponseEntity<Map<String, String>> | Aucun appel Angular correspondant repéré |
| POST /backoffice/notifications/read-all | NotificationController.markAllAsRead — src/main/java/com/orientation/orientationapp/backoffice/notification_center/controller/NotificationController.java:43 | notificationService.markAllAsRead | hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('CANDIDAT') | @RequestParam String userId | ResponseEntity<Map<String, String>> | Aucun appel Angular correspondant repéré |
| GET /backoffice/reports/generate | ReportController.generateReport — src/main/java/com/orientation/orientationapp/backoffice/report/controller/ReportController.java:17 | reportService.generateReport | hasRole('SUPER_ADMIN') or hasRole('ADMIN') | @RequestParam String type, @RequestParam(defaultValue = "json") String format | ResponseEntity<ReportResponse> | Aucun appel Angular correspondant repéré |
| POST /imports | ImportController.startImport — src/main/java/com/orientation/orientationapp/dataplat_import/controller/ImportController.java:28 | importOrchestrator.executeImport | hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasPermission('IMPORT_START') | @RequestParam("file") MultipartFile file, @RequestParam("dataType") DataType dataType, @RequestParam(value = "countryId", required = false) UUID countryId, @RequestParam(value = "academicYearId", required = false) UUID academicYearId | ResponseEntity<ImportResponse> | frontend/src/app/core/services/api.service.ts:147 uploadImport -> imports/components/imports.component.ts ; correspondance de route seulement, voir audit métier |
| GET /imports/{id} | ImportController.getImportStatus — src/main/java/com/orientation/orientationapp/dataplat_import/controller/ImportController.java:65 | importOrchestrator.getImportStatus | hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasPermission('IMPORT_READ') | @PathVariable UUID id | ResponseEntity<ImportResult> | frontend/src/app/core/services/api.service.ts:150 getImportDetails -> aucune page directe repérée ; correspondance de route seulement, voir audit métier |
| POST /imports/{id}/rollback | ImportController.rollback — src/main/java/com/orientation/orientationapp/dataplat_import/controller/ImportController.java:75 | importOrchestrator.cancelImport | hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasPermission('IMPORT_ROLLBACK') | @PathVariable UUID id | ResponseEntity<Map<String, String>> | frontend/src/app/core/services/api.service.ts:149 rollbackImport -> aucune page directe repérée ; correspondance de route seulement, voir audit métier |
| GET /imports/catalog | ImportController.getCatalog — src/main/java/com/orientation/orientationapp/dataplat_import/controller/ImportController.java:82 | Logique contrôleur / mapping | hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasPermission('IMPORT_READ') | — | ResponseEntity<Map<String, String>> | Aucun appel Angular correspondant repéré |
| GET /imports/versions | ImportController.getVersions — src/main/java/com/orientation/orientationapp/dataplat_import/controller/ImportController.java:88 | Logique contrôleur / mapping | hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasPermission('IMPORT_READ') | — | ResponseEntity<Map<String, String>> | Aucun appel Angular correspondant repéré |
| GET /imports/history | ImportController.getHistory — src/main/java/com/orientation/orientationapp/dataplat_import/controller/ImportController.java:94 | Logique contrôleur / mapping | hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasPermission('IMPORT_READ') | — | ResponseEntity<Map<String, String>> | Aucun appel Angular correspondant repéré |
| GET /imports/{id}/statistics | ImportController.getStatistics — src/main/java/com/orientation/orientationapp/dataplat_import/controller/ImportController.java:100 | Logique contrôleur / mapping | hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasPermission('IMPORT_READ') | @PathVariable UUID id | ResponseEntity<Map<String, String>> | Aucun appel Angular correspondant repéré |
| GET /imports/{id}/quality | ImportController.getQualityReport — src/main/java/com/orientation/orientationapp/dataplat_import/controller/ImportController.java:106 | Logique contrôleur / mapping | hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasPermission('IMPORT_READ') | @PathVariable UUID id | ResponseEntity<Map<String, String>> | Aucun appel Angular correspondant repéré |
| POST /documents/upload | DocumentController.upload — src/main/java/com/orientation/orientationapp/docintel/document/controller/DocumentController.java:27 | documentService.upload | hasRole('SUPER_ADMIN') or hasRole('ADMIN') | @RequestParam("file") MultipartFile file, @RequestParam(value = "title", required = false) String title, @RequestParam(value = "description", required = false) String description | ResponseEntity<DocumentResponse> | Aucun appel Angular correspondant repéré |
| GET /documents/{id} | DocumentController.getById — src/main/java/com/orientation/orientationapp/docintel/document/controller/DocumentController.java:38 | documentService.getById | hasRole('SUPER_ADMIN') or hasRole('ADMIN') | @PathVariable UUID id | ResponseEntity<DocumentResponse> | frontend/src/app/core/services/api.service.ts:156 getDocument -> aucune page directe repérée ; correspondance de route seulement, voir audit métier |
| GET /documents | DocumentController.list — src/main/java/com/orientation/orientationapp/docintel/document/controller/DocumentController.java:45 | documentService.list | hasRole('SUPER_ADMIN') or hasRole('ADMIN') | @RequestParam(required = false) String search, @RequestParam(required = false) String tenantId, @RequestParam(required = false) Document.DocumentType type, Pageable pageable | ResponseEntity<Page<DocumentResponse>> | frontend/src/app/core/services/api.service.ts:154 getDocuments -> documents/components/documents.component.ts ; correspondance de route seulement, voir audit métier |
| GET /documents/search | DocumentController.search — src/main/java/com/orientation/orientationapp/docintel/document/controller/DocumentController.java:56 | documentService.list | hasRole('SUPER_ADMIN') or hasRole('ADMIN') | @RequestParam String q, Pageable pageable | ResponseEntity<Page<DocumentResponse>> | Aucun appel Angular correspondant repéré |
| GET /documents/statistics | DocumentController.getStatistics — src/main/java/com/orientation/orientationapp/docintel/document/controller/DocumentController.java:65 | documentService.getStatistics | hasRole('SUPER_ADMIN') or hasRole('ADMIN') | — | ResponseEntity<Map<String, Object>> | Aucun appel Angular correspondant repéré |
| DELETE /documents/{id} | DocumentController.delete — src/main/java/com/orientation/orientationapp/docintel/document/controller/DocumentController.java:71 | documentService.delete | hasRole('SUPER_ADMIN') | @PathVariable UUID id | ResponseEntity<MessageResponse> | frontend/src/app/core/services/api.service.ts:162 deleteDocument -> documents/components/documents.component.ts ; correspondance de route seulement, voir audit métier |
| POST /intelligence/process | IntelligenceController.processDocument — src/main/java/com/orientation/orientationapp/intelligence/api/IntelligenceController.java:44 | pipeline.processDocument | hasRole('SUPER_ADMIN') or hasRole('ADMIN') | @RequestParam("file") MultipartFile file | ResponseEntity<DocumentProcessingPipeline.PipelineResult> | frontend/src/app/core/services/intelligence.service.ts:55 processDocument -> intelligence/components/pipeline.component.ts ; correspondance de route seulement, voir audit métier |
| POST /intelligence/smart-query | IntelligenceController.smartQuery — src/main/java/com/orientation/orientationapp/intelligence/api/IntelligenceController.java:67 | smartQueryEngine.query | hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('CANDIDAT') | @Valid @RequestBody SmartQueryRequest request | ResponseEntity<SmartQueryEngine.SmartQueryResult> | frontend/src/app/core/services/intelligence.service.ts:73 smartQuery -> knowledge/components/smart-query.component.ts, intelligence/components/smart-query.component.ts ; correspondance de route seulement, voir audit métier |
| GET /intelligence/knowledge | IntelligenceController.searchKnowledge — src/main/java/com/orientation/orientationapp/intelligence/api/IntelligenceController.java:78 | knowledgeGraphService.search | hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('CANDIDAT') | @RequestParam String q | ResponseEntity<List<KnowledgeNode>> | frontend/src/app/core/services/intelligence.service.ts:80 searchKnowledge -> knowledge/components/knowledge.component.ts, intelligence/components/knowledge-browser.component.ts ; correspondance de route seulement, voir audit métier |
| GET /intelligence/knowledge/{id}/related | IntelligenceController.getRelatedNodes — src/main/java/com/orientation/orientationapp/intelligence/api/IntelligenceController.java:88 | knowledgeGraphService.getRelatedNodes | hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('CANDIDAT') | @PathVariable UUID id | ResponseEntity<List<KnowledgeNode>> | frontend/src/app/core/services/intelligence.service.ts:87 getRelatedNodes -> aucune page directe repérée ; correspondance de route seulement, voir audit métier |
| POST /intelligence/feedback | IntelligenceController.recordFeedback — src/main/java/com/orientation/orientationapp/intelligence/api/IntelligenceController.java:98 | learningEngine.recordAcceptance, learningEngine.recordRejection, learningEngine.recordView | hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('CANDIDAT') | @Valid @RequestBody FeedbackRequest request | ResponseEntity<Void> | frontend/src/app/core/services/intelligence.service.ts:100 recordFeedback -> aucune page directe repérée ; correspondance de route seulement, voir audit métier |
| GET /intelligence/history/{candidateId} | IntelligenceController.getLearningHistory — src/main/java/com/orientation/orientationapp/intelligence/api/IntelligenceController.java:117 | learningEngine.getHistory | hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('CANDIDAT') | @PathVariable UUID candidateId | ResponseEntity<?> | frontend/src/app/core/services/intelligence.service.ts:107 getLearningHistory -> aucune page directe repérée ; correspondance de route seulement, voir audit métier |
| POST /knowledge/rebuild | KnowledgeController.rebuild — src/main/java/com/orientation/orientationapp/knowledge/controller/KnowledgeController.java:35 | knowledgeEngineService.rebuildIndex | hasRole('SUPER_ADMIN') or hasRole('ADMIN') | — | ResponseEntity<Map<String, String>> | Aucun appel Angular correspondant repéré |
| GET /knowledge/statistics | KnowledgeController.getStatistics — src/main/java/com/orientation/orientationapp/knowledge/controller/KnowledgeController.java:42 | knowledgeEngineService.getStatistics | hasRole('SUPER_ADMIN') or hasRole('ADMIN') | — | ResponseEntity<Map<String, Object>> | Aucun appel Angular correspondant repéré |
| GET /knowledge/search | KnowledgeController.search — src/main/java/com/orientation/orientationapp/knowledge/controller/KnowledgeController.java:48 | semanticSearchService.search | hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('CANDIDAT') | @RequestParam String query, @RequestParam(required = false) String type, @RequestParam(defaultValue = "10") int limit | ResponseEntity<?> | frontend/src/app/core/services/api.service.ts:218 searchKnowledge -> knowledge/components/knowledge.component.ts, intelligence/components/knowledge-browser.component.ts ; correspondance de route seulement, voir audit métier |
| GET /knowledge/similar | KnowledgeController.getSimilar — src/main/java/com/orientation/orientationapp/knowledge/controller/KnowledgeController.java:60 | similarityEngineService.findSimilar | hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('CANDIDAT') | @RequestParam String entityType, @RequestParam UUID entityId, @RequestParam(defaultValue = "5") int limit | ResponseEntity<?> | Aucun appel Angular correspondant repéré |
| POST /knowledge/query | KnowledgeController.query — src/main/java/com/orientation/orientationapp/knowledge/controller/KnowledgeController.java:69 | smartQueryService.processQuery | hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('CANDIDAT') | @RequestBody Map<String, String> request | ResponseEntity<Map<String, Object>> | Aucun appel Angular correspondant repéré |
| GET /knowledge/graph | KnowledgeController.getGraph — src/main/java/com/orientation/orientationapp/knowledge/controller/KnowledgeController.java:75 | knowledgeGraphService.getGraph | hasRole('SUPER_ADMIN') or hasRole('ADMIN') | — | ResponseEntity<Map<String, Object>> | Aucun appel Angular correspondant repéré |
| GET /knowledge/explanations/{id} | KnowledgeController.getExplanation — src/main/java/com/orientation/orientationapp/knowledge/controller/KnowledgeController.java:81 | explainableAiService.explain | hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('CANDIDAT') | @PathVariable UUID id | ResponseEntity<Map<String, Object>> | Aucun appel Angular correspondant repéré |
| GET /knowledge/recommendations/improve | KnowledgeController.getImprovementSuggestions — src/main/java/com/orientation/orientationapp/knowledge/controller/KnowledgeController.java:87 | learningService.getImprovementSuggestions | hasRole('SUPER_ADMIN') or hasRole('ADMIN') | @RequestParam UUID programId | ResponseEntity<Map<String, Object>> | Aucun appel Angular correspondant repéré |
| POST /knowledge/feedback | KnowledgeController.recordFeedback — src/main/java/com/orientation/orientationapp/knowledge/controller/KnowledgeController.java:93 | learningService.recordFeedback | hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('CANDIDAT') | @RequestBody Map<String, Object> request | ResponseEntity<Map<String, String>> | Aucun appel Angular correspondant repéré |
| POST /admin/invitations | InvitationController.create — src/main/java/com/orientation/orientationapp/modules/admin/controller/InvitationController.java:25 | invitationService.create | hasRole('SUPER_ADMIN') or hasRole('ADMIN') | @Valid @RequestBody CreateInvitationRequest request | ResponseEntity<Invitation> | frontend/src/app/core/services/api.service.ts:210 sendInvitation -> aucune page directe repérée ; correspondance de route seulement, voir audit métier |
| GET /admin/invitations | InvitationController.list — src/main/java/com/orientation/orientationapp/modules/admin/controller/InvitationController.java:32 | invitationService.list | hasRole('SUPER_ADMIN') or hasRole('ADMIN') | @RequestParam UUID tenantId, Pageable pageable | ResponseEntity<Page<Invitation>> | frontend/src/app/core/services/api.service.ts:209 getInvitations -> aucune page directe repérée ; correspondance de route seulement, voir audit métier |
| POST /admin/invitations/{id}/resend | InvitationController.resend — src/main/java/com/orientation/orientationapp/modules/admin/controller/InvitationController.java:40 | invitationService.resend | hasRole('SUPER_ADMIN') or hasRole('ADMIN') | @PathVariable UUID id | ResponseEntity<MessageResponse> | Aucun appel Angular correspondant repéré |
| POST /admin/invitations/{id}/revoke | InvitationController.revoke — src/main/java/com/orientation/orientationapp/modules/admin/controller/InvitationController.java:47 | invitationService.revoke | hasRole('SUPER_ADMIN') or hasRole('ADMIN') | @PathVariable UUID id | ResponseEntity<MessageResponse> | Aucun appel Angular correspondant repéré |
| POST /admin/invitations/accept | InvitationController.accept — src/main/java/com/orientation/orientationapp/modules/admin/controller/InvitationController.java:54 | invitationService.accept | Authentifié (SecurityConfig), sauf routes publiques auth/locations | @RequestParam String token | ResponseEntity<MessageResponse> | Aucun appel Angular correspondant repéré |
| POST /admin/organizations | OrganizationController.create — src/main/java/com/orientation/orientationapp/modules/admin/controller/OrganizationController.java:24 | organizationService.create | hasRole('SUPER_ADMIN') or hasRole('ADMIN') | @Valid @RequestBody CreateOrganizationRequest request | ResponseEntity<OrganizationResponse> | frontend/src/app/core/services/api.service.ts:185 createOrganization -> administration/components/organizations.component.ts ; correspondance de route seulement, voir audit métier |
| PUT /admin/organizations/{id} | OrganizationController.update — src/main/java/com/orientation/orientationapp/modules/admin/controller/OrganizationController.java:30 | organizationService.update | hasRole('SUPER_ADMIN') or hasRole('ADMIN') | @PathVariable UUID id, @Valid @RequestBody CreateOrganizationRequest request | ResponseEntity<OrganizationResponse> | frontend/src/app/core/services/api.service.ts:186 updateOrganization -> administration/components/organizations.component.ts ; correspondance de route seulement, voir audit métier |
| GET /admin/organizations/{id} | OrganizationController.getById — src/main/java/com/orientation/orientationapp/modules/admin/controller/OrganizationController.java:36 | organizationService.getById | hasRole('SUPER_ADMIN') or hasRole('ADMIN') | @PathVariable UUID id | ResponseEntity<OrganizationResponse> | frontend/src/app/core/services/api.service.ts:184 getOrganization -> aucune page directe repérée ; correspondance de route seulement, voir audit métier |
| GET /admin/organizations | OrganizationController.list — src/main/java/com/orientation/orientationapp/modules/admin/controller/OrganizationController.java:42 | organizationService.list | hasRole('SUPER_ADMIN') or hasRole('ADMIN') | @RequestParam(required = false) String search, Pageable pageable | ResponseEntity<Page<OrganizationResponse>> | frontend/src/app/core/services/api.service.ts:183 getOrganizations -> administration/components/administration.component.ts, administration/components/organizations.component.ts, administration/components/tenants.component.ts ; correspondance de route seulement, voir audit métier |
| PATCH /admin/organizations/{id}/activate | OrganizationController.activate — src/main/java/com/orientation/orientationapp/modules/admin/controller/OrganizationController.java:50 | organizationService.activate | hasRole('SUPER_ADMIN') or hasRole('ADMIN') | @PathVariable UUID id | ResponseEntity<MessageResponse> | Aucun appel Angular correspondant repéré |
| PATCH /admin/organizations/{id}/suspend | OrganizationController.suspend — src/main/java/com/orientation/orientationapp/modules/admin/controller/OrganizationController.java:57 | organizationService.suspend | hasRole('SUPER_ADMIN') or hasRole('ADMIN') | @PathVariable UUID id | ResponseEntity<MessageResponse> | Aucun appel Angular correspondant repéré |
| DELETE /admin/organizations/{id} | OrganizationController.delete — src/main/java/com/orientation/orientationapp/modules/admin/controller/OrganizationController.java:64 | organizationService.delete | hasRole('SUPER_ADMIN') | @PathVariable UUID id | ResponseEntity<MessageResponse> | frontend/src/app/core/services/api.service.ts:187 deleteOrganization -> administration/components/organizations.component.ts ; correspondance de route seulement, voir audit métier |
| POST /admin/tenants | TenantController.create — src/main/java/com/orientation/orientationapp/modules/admin/controller/TenantController.java:24 | tenantService.create | hasRole('SUPER_ADMIN') or hasRole('ADMIN') | @Valid @RequestBody CreateTenantRequest request | ResponseEntity<TenantResponse> | frontend/src/app/core/services/api.service.ts:192 createTenant -> administration/components/tenants.component.ts ; correspondance de route seulement, voir audit métier |
| PUT /admin/tenants/{id} | TenantController.update — src/main/java/com/orientation/orientationapp/modules/admin/controller/TenantController.java:30 | tenantService.update | hasRole('SUPER_ADMIN') or hasRole('ADMIN') | @PathVariable UUID id, @Valid @RequestBody CreateTenantRequest request | ResponseEntity<TenantResponse> | frontend/src/app/core/services/api.service.ts:193 updateTenant -> administration/components/tenants.component.ts ; correspondance de route seulement, voir audit métier |
| GET /admin/tenants/{id} | TenantController.getById — src/main/java/com/orientation/orientationapp/modules/admin/controller/TenantController.java:36 | tenantService.getById | hasRole('SUPER_ADMIN') or hasRole('ADMIN') | @PathVariable UUID id | ResponseEntity<TenantResponse> | frontend/src/app/core/services/api.service.ts:191 getTenant -> aucune page directe repérée ; correspondance de route seulement, voir audit métier |
| GET /admin/tenants | TenantController.list — src/main/java/com/orientation/orientationapp/modules/admin/controller/TenantController.java:42 | tenantService.list | hasRole('SUPER_ADMIN') or hasRole('ADMIN') | @RequestParam(required = false) UUID organizationId, @RequestParam(required = false) String search, Pageable pageable | ResponseEntity<Page<TenantResponse>> | frontend/src/app/core/services/api.service.ts:190 getTenants -> administration/components/administration.component.ts, administration/components/tenants.component.ts, administration/components/users.component.ts ; correspondance de route seulement, voir audit métier |
| PATCH /admin/tenants/{id}/activate | TenantController.activate — src/main/java/com/orientation/orientationapp/modules/admin/controller/TenantController.java:51 | tenantService.activate | hasRole('SUPER_ADMIN') or hasRole('ADMIN') | @PathVariable UUID id | ResponseEntity<MessageResponse> | Aucun appel Angular correspondant repéré |
| PATCH /admin/tenants/{id}/suspend | TenantController.suspend — src/main/java/com/orientation/orientationapp/modules/admin/controller/TenantController.java:58 | tenantService.suspend | hasRole('SUPER_ADMIN') or hasRole('ADMIN') | @PathVariable UUID id | ResponseEntity<MessageResponse> | Aucun appel Angular correspondant repéré |
| DELETE /admin/tenants/{id} | TenantController.delete — src/main/java/com/orientation/orientationapp/modules/admin/controller/TenantController.java:65 | tenantService.delete | hasRole('SUPER_ADMIN') | @PathVariable UUID id | ResponseEntity<MessageResponse> | frontend/src/app/core/services/api.service.ts:194 deleteTenant -> administration/components/tenants.component.ts ; correspondance de route seulement, voir audit métier |
| POST /admin/users | UserController.create — src/main/java/com/orientation/orientationapp/modules/admin/controller/UserController.java:24 | userAdminService.create | hasRole('SUPER_ADMIN') or hasRole('ADMIN') | @Valid @RequestBody CreateUserRequest request | ResponseEntity<UserResponse> | frontend/src/app/core/services/api.service.ts:171 createUser -> administration/components/users.component.ts ; correspondance de route seulement, voir audit métier |
| PUT /admin/users/{id} | UserController.update — src/main/java/com/orientation/orientationapp/modules/admin/controller/UserController.java:30 | userAdminService.update | hasRole('SUPER_ADMIN') or hasRole('ADMIN') | @PathVariable UUID id, @Valid @RequestBody CreateUserRequest request | ResponseEntity<UserResponse> | frontend/src/app/core/services/api.service.ts:172 updateUser -> administration/components/users.component.ts ; correspondance de route seulement, voir audit métier |
| GET /admin/users/{id} | UserController.getById — src/main/java/com/orientation/orientationapp/modules/admin/controller/UserController.java:36 | userAdminService.getById | hasRole('SUPER_ADMIN') or hasRole('ADMIN') | @PathVariable UUID id | ResponseEntity<UserResponse> | frontend/src/app/core/services/api.service.ts:170 getUser -> aucune page directe repérée ; correspondance de route seulement, voir audit métier |
| GET /admin/users | UserController.list — src/main/java/com/orientation/orientationapp/modules/admin/controller/UserController.java:42 | userAdminService.list | hasRole('SUPER_ADMIN') or hasRole('ADMIN') | @RequestParam(required = false) UUID tenantId, @RequestParam(required = false) String search, Pageable pageable | ResponseEntity<Page<UserResponse>> | frontend/src/app/core/services/api.service.ts:168 getUsers -> administration/components/administration.component.ts, administration/components/users.component.ts ; correspondance de route seulement, voir audit métier |
| PATCH /admin/users/{id}/deactivate | UserController.deactivate — src/main/java/com/orientation/orientationapp/modules/admin/controller/UserController.java:51 | userAdminService.deactivate | hasRole('SUPER_ADMIN') or hasRole('ADMIN') | @PathVariable UUID id | ResponseEntity<MessageResponse> | Aucun appel Angular correspondant repéré |
| PATCH /admin/users/{id}/reactivate | UserController.reactivate — src/main/java/com/orientation/orientationapp/modules/admin/controller/UserController.java:58 | userAdminService.reactivate | hasRole('SUPER_ADMIN') or hasRole('ADMIN') | @PathVariable UUID id | ResponseEntity<MessageResponse> | Aucun appel Angular correspondant repéré |
| DELETE /admin/users/{id} | UserController.delete — src/main/java/com/orientation/orientationapp/modules/admin/controller/UserController.java:65 | userAdminService.delete | hasRole('SUPER_ADMIN') | @PathVariable UUID id | ResponseEntity<MessageResponse> | frontend/src/app/core/services/api.service.ts:173 deleteUser -> administration/components/users.component.ts ; correspondance de route seulement, voir audit métier |
| POST /auth/login | AuthController.login — src/main/java/com/orientation/orientationapp/modules/auth/controller/AuthController.java:22 | authenticationService.login | Authentifié (SecurityConfig), sauf routes publiques auth/locations | @Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest | ResponseEntity<LoginResponse> | frontend/src/app/core/services/api.service.ts:23 login -> auth/login/login.component.ts ; frontend/src/app/core/services/auth.service.ts:56 login -> auth/login/login.component.ts ; correspondance de route seulement, voir audit métier |
| POST /auth/refresh | AuthController.refreshToken — src/main/java/com/orientation/orientationapp/modules/auth/controller/AuthController.java:34 | authenticationService.refreshToken | Authentifié (SecurityConfig), sauf routes publiques auth/locations | @Valid @RequestBody RefreshTokenRequest request | ResponseEntity<LoginResponse> | frontend/src/app/core/services/api.service.ts:24 refreshToken -> core/interceptors/auth.interceptor.ts, core/services/auth.service.ts ; frontend/src/app/core/services/auth.service.ts:87 refreshToken -> core/interceptors/auth.interceptor.ts ; correspondance de route seulement, voir audit métier |
| POST /auth/logout | AuthController.logout — src/main/java/com/orientation/orientationapp/modules/auth/controller/AuthController.java:42 | authenticationService.logout | Authentifié (SecurityConfig), sauf routes publiques auth/locations | @RequestBody(required = false) RefreshTokenRequest request | ResponseEntity<MessageResponse> | frontend/src/app/core/services/api.service.ts:25 logout -> layout/layout.component.ts, core/interceptors/auth.interceptor.ts, core/services/auth.service.ts ; frontend/src/app/core/services/auth.service.ts:70 logout -> layout/layout.component.ts, core/interceptors/auth.interceptor.ts ; correspondance de route seulement, voir audit métier |
| POST /auth/forgot-password | AuthController.forgotPassword — src/main/java/com/orientation/orientationapp/modules/auth/controller/AuthController.java:53 | authenticationService.forgotPassword | Authentifié (SecurityConfig), sauf routes publiques auth/locations | @Valid @RequestBody ForgotPasswordRequest request | ResponseEntity<MessageResponse> | frontend/src/app/core/services/api.service.ts:26 forgotPassword -> aucune page directe repérée ; frontend/src/app/core/services/auth.service.ts:123 forgotPassword -> aucune page directe repérée ; frontend/src/app/auth/forgot-password/forgot-password.component.ts:21 onSubmit -> aucune page directe repérée ; correspondance de route seulement, voir audit métier |
| POST /auth/reset-password | AuthController.resetPassword — src/main/java/com/orientation/orientationapp/modules/auth/controller/AuthController.java:61 | authenticationService.resetPassword | Authentifié (SecurityConfig), sauf routes publiques auth/locations | @Valid @RequestBody ResetPasswordRequest request | ResponseEntity<MessageResponse> | frontend/src/app/core/services/api.service.ts:27 resetPassword -> aucune page directe repérée ; frontend/src/app/core/services/auth.service.ts:127 resetPassword -> aucune page directe repérée ; frontend/src/app/auth/reset-password/reset-password.component.ts:21 onSubmit -> aucune page directe repérée ; correspondance de route seulement, voir audit métier |
| GET /auth/me | AuthController.getCurrentUser — src/main/java/com/orientation/orientationapp/modules/auth/controller/AuthController.java:69 | authenticationService.getCurrentUser | Authentifié (SecurityConfig), sauf routes publiques auth/locations | — | ResponseEntity<UserInfo> | frontend/src/app/core/services/api.service.ts:28 getMe -> aucune page directe repérée ; frontend/src/app/core/services/auth.service.ts:102 getCurrentUser -> aucune page directe repérée ; correspondance de route seulement, voir audit métier |
| POST /auth/register | RegisterController.register — src/main/java/com/orientation/orientationapp/modules/auth/controller/RegisterController.java:41 | candidateRepository.save, emailService.sendVerificationEmail, roleRepository.findByCode, userRepository.findByEmail, userRepository.save | Authentifié (SecurityConfig), sauf routes publiques auth/locations | @Valid @RequestBody RegisterRequest request | ResponseEntity<RegisterResponse> | Aucun appel Angular correspondant repéré |
| GET /auth/verify | RegisterController.verifyEmail — src/main/java/com/orientation/orientationapp/modules/auth/controller/RegisterController.java:135 | candidateRepository.findByEmail, candidateRepository.save, tenantRepository.findByCode, tenantRepository.save, userRepository.findAll, userRepository.save | Authentifié (SecurityConfig), sauf routes publiques auth/locations | @RequestParam String token | ResponseEntity<String> | Aucun appel Angular correspondant repéré |
| GET /guides | OrientationGuideController.list — src/main/java/com/orientation/orientationapp/modules/orientation/controller/OrientationGuideController.java:27 | guideRepository.findAll | Authentifié (SecurityConfig), sauf routes publiques auth/locations | @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size | ResponseEntity<Page<OrientationGuide>> | frontend/src/app/core/services/api.service.ts:118 getGuides -> rules/components/rules.component.ts ; correspondance de route seulement, voir audit métier |
| GET /guides/{id} | OrientationGuideController.getById — src/main/java/com/orientation/orientationapp/modules/orientation/controller/OrientationGuideController.java:36 | guideRepository.findById | Authentifié (SecurityConfig), sauf routes publiques auth/locations | @PathVariable UUID id | ResponseEntity<OrientationGuide> | frontend/src/app/core/services/api.service.ts:120 getGuide -> aucune page directe repérée ; correspondance de route seulement, voir audit métier |
| GET /scholarships | ScholarshipController.list — src/main/java/com/orientation/orientationapp/modules/scholarship/controller/ScholarshipController.java:27 | scholarshipRepository.findAll | Authentifié (SecurityConfig), sauf routes publiques auth/locations | @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size | ResponseEntity<Page<Scholarship>> | frontend/src/app/core/services/api.service.ts:112 getScholarships -> aucune page directe repérée ; correspondance de route seulement, voir audit métier |
| GET /scholarships/{id} | ScholarshipController.getById — src/main/java/com/orientation/orientationapp/modules/scholarship/controller/ScholarshipController.java:36 | scholarshipRepository.findById | Authentifié (SecurityConfig), sauf routes publiques auth/locations | @PathVariable UUID id | ResponseEntity<Scholarship> | frontend/src/app/core/services/api.service.ts:114 getScholarship -> aucune page directe repérée ; correspondance de route seulement, voir audit métier |
| GET /transcripts | TranscriptController.list — src/main/java/com/orientation/orientationapp/modules/transcript/controller/TranscriptController.java:29 | transcriptRepository.findAll | hasRole('SUPER_ADMIN') or hasRole('ADMIN') | @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size | ResponseEntity<Page<Transcript>> | frontend/src/app/core/services/api.service.ts:107 getTranscripts -> aucune page directe repérée ; correspondance de route seulement, voir audit métier |
| GET /transcripts/{id} | TranscriptController.getById — src/main/java/com/orientation/orientationapp/modules/transcript/controller/TranscriptController.java:39 | transcriptRepository.findById | hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('CANDIDAT') | @PathVariable UUID id | ResponseEntity<Transcript> | Aucun appel Angular correspondant repéré |
| POST /transcripts | TranscriptController.upload — src/main/java/com/orientation/orientationapp/modules/transcript/controller/TranscriptController.java:47 | transcriptRepository.save | hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('CANDIDAT') | @RequestParam("file") MultipartFile file | ResponseEntity<Transcript> | Aucun appel Angular correspondant repéré |
| GET /benin/orientation/filieres | BeninOrientationController.getFilieres — src/main/java/com/orientation/orientationapp/modules/university/controller/BeninOrientationController.java:57 | Logique contrôleur / mapping | Authentifié (SecurityConfig), sauf routes publiques auth/locations | @RequestParam(required = false) String serie, @RequestParam(required = false) String university | ResponseEntity<List<Map<String, Object>>> | Aucun appel Angular correspondant repéré |
| POST /benin/orientation/simulate | BeninOrientationController.simulate — src/main/java/com/orientation/orientationapp/modules/university/controller/BeninOrientationController.java:71 | Logique contrôleur / mapping | Authentifié (SecurityConfig), sauf routes publiques auth/locations | @RequestBody Map<String, Object> request | ResponseEntity<Map<String, Object>> | Aucun appel Angular correspondant repéré |
| GET /benin/orientation/universites | BeninOrientationController.getUniversites — src/main/java/com/orientation/orientationapp/modules/university/controller/BeninOrientationController.java:131 | Logique contrôleur / mapping | Authentifié (SecurityConfig), sauf routes publiques auth/locations | — | ResponseEntity<List<Map<String, Object>>> | Aucun appel Angular correspondant repéré |
| GET /benin/orientation/series | BeninOrientationController.getSeries — src/main/java/com/orientation/orientationapp/modules/university/controller/BeninOrientationController.java:143 | Logique contrôleur / mapping | Authentifié (SecurityConfig), sauf routes publiques auth/locations | — | ResponseEntity<List<Map<String, Object>>> | Aucun appel Angular correspondant repéré |
| GET /locations/countries | LocationController.getCountries — src/main/java/com/orientation/orientationapp/modules/university/controller/LocationController.java:26 | countryRepository.findAll | Authentifié (SecurityConfig), sauf routes publiques auth/locations | — | ResponseEntity<List<Map<String, Object>>> | frontend/src/app/core/services/api.service.ts:135 getCountries -> universities/components/universities.component.ts ; correspondance de route seulement, voir audit métier |
| GET /locations/countries/{countryId}/cities | LocationController.getCitiesByCountry — src/main/java/com/orientation/orientationapp/modules/university/controller/LocationController.java:47 | cityRepository.findByCountryId | Authentifié (SecurityConfig), sauf routes publiques auth/locations | @PathVariable UUID countryId | ResponseEntity<List<Map<String, Object>>> | frontend/src/app/core/services/api.service.ts:137 getCitiesByCountry -> universities/components/universities.component.ts ; correspondance de route seulement, voir audit métier |
| GET /locations/cities | LocationController.getCities — src/main/java/com/orientation/orientationapp/modules/university/controller/LocationController.java:65 | cityRepository.findAll, cityRepository.findByCountryId | Authentifié (SecurityConfig), sauf routes publiques auth/locations | @RequestParam(required = false) UUID countryId | ResponseEntity<List<Map<String, Object>>> | frontend/src/app/core/services/api.service.ts:136 getCities -> universities/components/universities.component.ts ; correspondance de route seulement, voir audit métier |
| GET /candidates | CandidateController.list — src/main/java/com/orientation/orientationapp/modules/user/controller/CandidateController.java:31 | candidateRepository.findAll | hasRole('SUPER_ADMIN') or hasRole('ADMIN') | @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size | ResponseEntity<Page<Candidate>> | frontend/src/app/core/services/api.service.ts:92 getCandidates -> aucune page directe repérée ; correspondance de route seulement, voir audit métier |
| GET /candidates/{id} | CandidateController.getById — src/main/java/com/orientation/orientationapp/modules/user/controller/CandidateController.java:42 | candidateRepository.findById | hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('CANDIDAT') | @PathVariable UUID id | ResponseEntity<Candidate> | frontend/src/app/core/services/api.service.ts:94 getCandidate -> aucune page directe repérée ; correspondance de route seulement, voir audit métier |
| GET /candidates/search | CandidateController.search — src/main/java/com/orientation/orientationapp/modules/user/controller/CandidateController.java:50 | candidateRepository.findAll | hasRole('SUPER_ADMIN') or hasRole('ADMIN') | @RequestParam String q | ResponseEntity<List<Candidate>> | Aucun appel Angular correspondant repéré |
| GET /candidates/count | CandidateController.count — src/main/java/com/orientation/orientationapp/modules/user/controller/CandidateController.java:61 | candidateRepository.count, candidateRepository.findAll | hasRole('SUPER_ADMIN') or hasRole('ADMIN') | — | ResponseEntity<Map<String, Object>> | Aucun appel Angular correspondant repéré |
| POST /recommendations/generate | RecommendationController.generateRecommendations — src/main/java/com/orientation/orientationapp/recengine/recommendation/controller/RecommendationController.java:47 | profileAnalyzer.analyze, programRepository.findByAvailableTrue, rankingEngine.rankWithExplanations, scoringEngine.computeScore | hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('CANDIDAT') | @Valid @RequestBody GenerateRecommendationRequest request | ResponseEntity<RecommendationResponse> | frontend/src/app/core/services/api.service.ts:100 generateRecommendations -> recommendations/components/recommendations.component.ts ; correspondance de route seulement, voir audit métier |
| POST /recommendations/simulate | RecommendationController.simulate — src/main/java/com/orientation/orientationapp/recengine/recommendation/controller/RecommendationController.java:96 | profileAnalyzer.analyze, programRepository.findByAvailableTrue, rankingEngine.rank, scoringEngine.computeScore, simulationEngine.simulate | hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('CANDIDAT') | @Valid @RequestBody SimulationRequest request | ResponseEntity<SimulationResponse> | frontend/src/app/core/services/api.service.ts:101 simulate -> aucune page directe repérée ; correspondance de route seulement, voir audit métier |
| GET /recommendations | RecommendationController.list — src/main/java/com/orientation/orientationapp/recengine/recommendation/controller/RecommendationController.java:154 | programRepository.findByAvailableTrue | hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('CANDIDAT') | Pageable pageable | ResponseEntity<Map<String, Object>> | frontend/src/app/core/services/api.service.ts:98 getRecommendations -> recommendations/components/recommendations.component.ts ; correspondance de route seulement, voir audit métier |
| GET /recommendations/dashboard | RecommendationController.getDashboard — src/main/java/com/orientation/orientationapp/recengine/recommendation/controller/RecommendationController.java:164 | programRepository.findByAvailableTrue | hasRole('SUPER_ADMIN') or hasRole('ADMIN') | — | ResponseEntity<Map<String, Object>> | Aucun appel Angular correspondant repéré |
| GET /recommendations/dashboard/statistics | RecommendationController.getDashboardStatistics — src/main/java/com/orientation/orientationapp/recengine/recommendation/controller/RecommendationController.java:173 | Logique contrôleur / mapping | hasRole('SUPER_ADMIN') or hasRole('ADMIN') | — | ResponseEntity<Map<String, String>> | Aucun appel Angular correspondant repéré |
| GET /recommendations/{id}/scores | RecommendationController.getScores — src/main/java/com/orientation/orientationapp/recengine/recommendation/controller/RecommendationController.java:179 | Logique contrôleur / mapping | hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('CANDIDAT') | @PathVariable UUID id | ResponseEntity<Map<String, String>> | frontend/src/app/core/services/api.service.ts:102 getRecommendationScores -> aucune page directe repérée ; correspondance de route seulement, voir audit métier |
| GET /recommendations/{id}/explanation | RecommendationController.getExplanation — src/main/java/com/orientation/orientationapp/recengine/recommendation/controller/RecommendationController.java:185 | Logique contrôleur / mapping | hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('CANDIDAT') | @PathVariable UUID id | ResponseEntity<Map<String, String>> | frontend/src/app/core/services/api.service.ts:103 getRecommendationExplanation -> recommendations/components/recommendations.component.ts ; correspondance de route seulement, voir audit métier |

### Appels Angular vers des routes/méthodes sans mapping correspondant

| Appel émis | Preuve / méthode | Consommateurs repérés | Diagnostic |
| --- | --- | --- | --- |
| PUT /auth/me | frontend/src/app/core/services/api.service.ts:29 updateMe | Pas de page directe repérée | Pas de mapping exact dans les contrôleurs REST inspectés ; voir H pour remplacement ou API à compléter |
| PUT /auth/me/password | frontend/src/app/core/services/api.service.ts:30 changePassword | profile/components/profile.component.ts | Pas de mapping exact dans les contrôleurs REST inspectés ; voir H pour remplacement ou API à compléter |
| GET /backoffice/report | frontend/src/app/core/services/api.service.ts:43 getReports | Pas de page directe repérée | Pas de mapping exact dans les contrôleurs REST inspectés ; voir H pour remplacement ou API à compléter |
| POST /backoffice/report/generate | frontend/src/app/core/services/api.service.ts:44 generateReport | Pas de page directe repérée | Pas de mapping exact dans les contrôleurs REST inspectés ; voir H pour remplacement ou API à compléter |
| GET /notifications | frontend/src/app/core/services/api.service.ts:48 getNotifications | Pas de page directe repérée | Pas de mapping exact dans les contrôleurs REST inspectés ; voir H pour remplacement ou API à compléter |
| PUT /notifications/{id}/read | frontend/src/app/core/services/api.service.ts:50 markNotificationRead | Pas de page directe repérée | Pas de mapping exact dans les contrôleurs REST inspectés ; voir H pour remplacement ou API à compléter |
| PUT /notifications/read-all | frontend/src/app/core/services/api.service.ts:51 markAllNotificationsRead | Pas de page directe repérée | Pas de mapping exact dans les contrôleurs REST inspectés ; voir H pour remplacement ou API à compléter |
| DELETE /notifications/{id} | frontend/src/app/core/services/api.service.ts:52 deleteNotification | Pas de page directe repérée | Pas de mapping exact dans les contrôleurs REST inspectés ; voir H pour remplacement ou API à compléter |
| GET /universities | frontend/src/app/core/services/api.service.ts:56 getUniversities | universities/components/universities.component.ts | Pas de mapping exact dans les contrôleurs REST inspectés ; voir H pour remplacement ou API à compléter |
| GET /universities/{id} | frontend/src/app/core/services/api.service.ts:58 getUniversity | universities/components/university-detail.component.ts | Pas de mapping exact dans les contrôleurs REST inspectés ; voir H pour remplacement ou API à compléter |
| POST /universities | frontend/src/app/core/services/api.service.ts:59 createUniversity | universities/components/universities.component.ts | Pas de mapping exact dans les contrôleurs REST inspectés ; voir H pour remplacement ou API à compléter |
| PUT /universities/{id} | frontend/src/app/core/services/api.service.ts:60 updateUniversity | universities/components/universities.component.ts | Pas de mapping exact dans les contrôleurs REST inspectés ; voir H pour remplacement ou API à compléter |
| DELETE /universities/{id} | frontend/src/app/core/services/api.service.ts:61 deleteUniversity | universities/components/universities.component.ts | Pas de mapping exact dans les contrôleurs REST inspectés ; voir H pour remplacement ou API à compléter |
| GET /campuses | frontend/src/app/core/services/api.service.ts:65 getCampuses | Pas de page directe repérée | Pas de mapping exact dans les contrôleurs REST inspectés ; voir H pour remplacement ou API à compléter |
| GET /campuses/{id} | frontend/src/app/core/services/api.service.ts:67 getCampus | Pas de page directe repérée | Pas de mapping exact dans les contrôleurs REST inspectés ; voir H pour remplacement ou API à compléter |
| POST /campuses | frontend/src/app/core/services/api.service.ts:68 createCampus | Pas de page directe repérée | Pas de mapping exact dans les contrôleurs REST inspectés ; voir H pour remplacement ou API à compléter |
| PUT /campuses/{id} | frontend/src/app/core/services/api.service.ts:69 updateCampus | Pas de page directe repérée | Pas de mapping exact dans les contrôleurs REST inspectés ; voir H pour remplacement ou API à compléter |
| DELETE /campuses/{id} | frontend/src/app/core/services/api.service.ts:70 deleteCampus | Pas de page directe repérée | Pas de mapping exact dans les contrôleurs REST inspectés ; voir H pour remplacement ou API à compléter |
| GET /faculties | frontend/src/app/core/services/api.service.ts:74 getFaculties | Pas de page directe repérée | Pas de mapping exact dans les contrôleurs REST inspectés ; voir H pour remplacement ou API à compléter |
| GET /faculties/{id} | frontend/src/app/core/services/api.service.ts:76 getFaculty | Pas de page directe repérée | Pas de mapping exact dans les contrôleurs REST inspectés ; voir H pour remplacement ou API à compléter |
| POST /faculties | frontend/src/app/core/services/api.service.ts:77 createFaculty | Pas de page directe repérée | Pas de mapping exact dans les contrôleurs REST inspectés ; voir H pour remplacement ou API à compléter |
| PUT /faculties/{id} | frontend/src/app/core/services/api.service.ts:78 updateFaculty | Pas de page directe repérée | Pas de mapping exact dans les contrôleurs REST inspectés ; voir H pour remplacement ou API à compléter |
| DELETE /faculties/{id} | frontend/src/app/core/services/api.service.ts:79 deleteFaculty | Pas de page directe repérée | Pas de mapping exact dans les contrôleurs REST inspectés ; voir H pour remplacement ou API à compléter |
| GET /programs | frontend/src/app/core/services/api.service.ts:83 getPrograms | Pas de page directe repérée | Pas de mapping exact dans les contrôleurs REST inspectés ; voir H pour remplacement ou API à compléter |
| GET /programs/{id} | frontend/src/app/core/services/api.service.ts:85 getProgram | Pas de page directe repérée | Pas de mapping exact dans les contrôleurs REST inspectés ; voir H pour remplacement ou API à compléter |
| POST /programs | frontend/src/app/core/services/api.service.ts:86 createProgram | Pas de page directe repérée | Pas de mapping exact dans les contrôleurs REST inspectés ; voir H pour remplacement ou API à compléter |
| PUT /programs/{id} | frontend/src/app/core/services/api.service.ts:87 updateProgram | Pas de page directe repérée | Pas de mapping exact dans les contrôleurs REST inspectés ; voir H pour remplacement ou API à compléter |
| DELETE /programs/{id} | frontend/src/app/core/services/api.service.ts:88 deleteProgram | Pas de page directe repérée | Pas de mapping exact dans les contrôleurs REST inspectés ; voir H pour remplacement ou API à compléter |
| POST /guides | frontend/src/app/core/services/api.service.ts:121 createGuide | Pas de page directe repérée | Pas de mapping exact dans les contrôleurs REST inspectés ; voir H pour remplacement ou API à compléter |
| PUT /guides/{id} | frontend/src/app/core/services/api.service.ts:122 updateGuide | Pas de page directe repérée | Pas de mapping exact dans les contrôleurs REST inspectés ; voir H pour remplacement ou API à compléter |
| DELETE /guides/{id} | frontend/src/app/core/services/api.service.ts:123 deleteGuide | Pas de page directe repérée | Pas de mapping exact dans les contrôleurs REST inspectés ; voir H pour remplacement ou API à compléter |
| GET /academic-years | frontend/src/app/core/services/api.service.ts:126 getAcademicYears | Pas de page directe repérée | Pas de mapping exact dans les contrôleurs REST inspectés ; voir H pour remplacement ou API à compléter |
| GET /subjects | frontend/src/app/core/services/api.service.ts:129 getSubjects | Pas de page directe repérée | Pas de mapping exact dans les contrôleurs REST inspectés ; voir H pour remplacement ou API à compléter |
| GET /grade-scales | frontend/src/app/core/services/api.service.ts:132 getGradeScales | Pas de page directe repérée | Pas de mapping exact dans les contrôleurs REST inspectés ; voir H pour remplacement ou API à compléter |
| GET /imports | frontend/src/app/core/services/api.service.ts:141 getImports | imports/components/imports.component.ts | Pas de mapping exact dans les contrôleurs REST inspectés ; voir H pour remplacement ou API à compléter |
| POST /documents | frontend/src/app/core/services/api.service.ts:160 uploadDocument | documents/components/documents.component.ts | Pas de mapping exact dans les contrôleurs REST inspectés ; voir H pour remplacement ou API à compléter |
| GET /documents/{id}/extractions | frontend/src/app/core/services/api.service.ts:163 getDocumentExtractions | documents/components/documents.component.ts | Pas de mapping exact dans les contrôleurs REST inspectés ; voir H pour remplacement ou API à compléter |
| GET /documents/{id}/classifications | frontend/src/app/core/services/api.service.ts:164 getDocumentClassifications | Pas de page directe repérée | Pas de mapping exact dans les contrôleurs REST inspectés ; voir H pour remplacement ou API à compléter |
| GET /admin/roles | frontend/src/app/core/services/api.service.ts:176 getRoles | administration/components/administration.component.ts, administration/components/roles.component.ts, administration/components/users.component.ts | Pas de mapping exact dans les contrôleurs REST inspectés ; voir H pour remplacement ou API à compléter |
| GET /admin/roles/{id} | frontend/src/app/core/services/api.service.ts:177 getRole | Pas de page directe repérée | Pas de mapping exact dans les contrôleurs REST inspectés ; voir H pour remplacement ou API à compléter |
| POST /admin/roles | frontend/src/app/core/services/api.service.ts:178 createRole | administration/components/roles.component.ts | Pas de mapping exact dans les contrôleurs REST inspectés ; voir H pour remplacement ou API à compléter |
| PUT /admin/roles/{id} | frontend/src/app/core/services/api.service.ts:179 updateRole | administration/components/roles.component.ts | Pas de mapping exact dans les contrôleurs REST inspectés ; voir H pour remplacement ou API à compléter |
| DELETE /admin/roles/{id} | frontend/src/app/core/services/api.service.ts:180 deleteRole | administration/components/roles.component.ts | Pas de mapping exact dans les contrôleurs REST inspectés ; voir H pour remplacement ou API à compléter |
| GET /admin/departments | frontend/src/app/core/services/api.service.ts:197 getDepartments | administration/components/departments.component.ts | Pas de mapping exact dans les contrôleurs REST inspectés ; voir H pour remplacement ou API à compléter |
| POST /admin/departments | frontend/src/app/core/services/api.service.ts:198 createDepartment | administration/components/departments.component.ts | Pas de mapping exact dans les contrôleurs REST inspectés ; voir H pour remplacement ou API à compléter |
| PUT /admin/departments/{id} | frontend/src/app/core/services/api.service.ts:199 updateDepartment | administration/components/departments.component.ts | Pas de mapping exact dans les contrôleurs REST inspectés ; voir H pour remplacement ou API à compléter |
| DELETE /admin/departments/{id} | frontend/src/app/core/services/api.service.ts:200 deleteDepartment | administration/components/departments.component.ts | Pas de mapping exact dans les contrôleurs REST inspectés ; voir H pour remplacement ou API à compléter |
| GET /admin/teams | frontend/src/app/core/services/api.service.ts:203 getTeams | administration/components/teams.component.ts | Pas de mapping exact dans les contrôleurs REST inspectés ; voir H pour remplacement ou API à compléter |
| POST /admin/teams | frontend/src/app/core/services/api.service.ts:204 createTeam | administration/components/teams.component.ts | Pas de mapping exact dans les contrôleurs REST inspectés ; voir H pour remplacement ou API à compléter |
| PUT /admin/teams/{id} | frontend/src/app/core/services/api.service.ts:205 updateTeam | administration/components/teams.component.ts | Pas de mapping exact dans les contrôleurs REST inspectés ; voir H pour remplacement ou API à compléter |
| DELETE /admin/teams/{id} | frontend/src/app/core/services/api.service.ts:206 deleteTeam | administration/components/teams.component.ts | Pas de mapping exact dans les contrôleurs REST inspectés ; voir H pour remplacement ou API à compléter |
| DELETE /admin/invitations/{id} | frontend/src/app/core/services/api.service.ts:211 cancelInvitation | Pas de page directe repérée | Pas de mapping exact dans les contrôleurs REST inspectés ; voir H pour remplacement ou API à compléter |
| GET /knowledge/nodes | frontend/src/app/core/services/api.service.ts:215 getKnowledgeNodes | knowledge/components/knowledge.component.ts | Pas de mapping exact dans les contrôleurs REST inspectés ; voir H pour remplacement ou API à compléter |
| GET /knowledge/nodes/{id} | frontend/src/app/core/services/api.service.ts:217 getKnowledgeNode | Pas de page directe repérée | Pas de mapping exact dans les contrôleurs REST inspectés ; voir H pour remplacement ou API à compléter |
| POST /knowledge/smart-query | frontend/src/app/core/services/api.service.ts:219 smartQuery | knowledge/components/smart-query.component.ts, intelligence/components/smart-query.component.ts | Pas de mapping exact dans les contrôleurs REST inspectés ; voir H pour remplacement ou API à compléter |
| GET /audit | frontend/src/app/core/services/api.service.ts:223 getAuditLogs | Pas de page directe repérée | Pas de mapping exact dans les contrôleurs REST inspectés ; voir H pour remplacement ou API à compléter |
| GET /settings | frontend/src/app/core/services/api.service.ts:227 getSettings | Pas de page directe repérée | Pas de mapping exact dans les contrôleurs REST inspectés ; voir H pour remplacement ou API à compléter |
| PUT /settings | frontend/src/app/core/services/api.service.ts:228 updateSettings | Pas de page directe repérée | Pas de mapping exact dans les contrôleurs REST inspectés ; voir H pour remplacement ou API à compléter |
| GET /backoffice/activity-logs | frontend/src/app/core/services/api.service.ts:232 getActivityLogs | monitoring/components/monitoring.component.ts | Pas de mapping exact dans les contrôleurs REST inspectés ; voir H pour remplacement ou API à compléter |
| PUT /auth/me | frontend/src/app/core/services/auth.service.ts:112 updateProfile | profile/components/profile.component.ts | Pas de mapping exact dans les contrôleurs REST inspectés ; voir H pour remplacement ou API à compléter |
| PUT /auth/me/password | frontend/src/app/core/services/auth.service.ts:119 changePassword | profile/components/profile.component.ts | Pas de mapping exact dans les contrôleurs REST inspectés ; voir H pour remplacement ou API à compléter |
| GET /notifications | frontend/src/app/core/services/notification.service.ts:26 load | layout/layout.component.ts, universities/components/universities.component.ts, rules/components/rules.component.ts, recommendations/components/recommendations.component.ts, notifications/components/notifications.component.ts, monitoring/components/monitoring.component.ts, knowledge/components/knowledge.component.ts, intelligence/components/knowledge-browser.component.ts, documents/components/documents.component.ts, analytics/components/analytics.component.ts, administration/components/departments.component.ts, administration/components/organizations.component.ts, administration/components/roles.component.ts, administration/components/teams.component.ts, administration/components/tenants.component.ts, administration/components/users.component.ts | Pas de mapping exact dans les contrôleurs REST inspectés ; voir H pour remplacement ou API à compléter |
| PUT /notifications/{id}/read | frontend/src/app/core/services/notification.service.ts:37 markAsRead | layout/layout.component.ts, notifications/components/notifications.component.ts | Pas de mapping exact dans les contrôleurs REST inspectés ; voir H pour remplacement ou API à compléter |
| PUT /notifications/read-all | frontend/src/app/core/services/notification.service.ts:48 markAllAsRead | layout/layout.component.ts, notifications/components/notifications.component.ts | Pas de mapping exact dans les contrôleurs REST inspectés ; voir H pour remplacement ou API à compléter |
| GET /search | frontend/src/app/core/services/search.service.ts:42 search | Pas de page directe repérée | Pas de mapping exact dans les contrôleurs REST inspectés ; voir H pour remplacement ou API à compléter |

### Tous les appels HttpClient repérés (y compris méthodes sans page consommatrice)

| Méthode / chemin | Service / méthode / ligne | Consommateurs repérés |
| --- | --- | --- |
| POST /auth/login | frontend/src/app/core/services/api.service.ts:23 login | auth/login/login.component.ts |
| POST /auth/refresh | frontend/src/app/core/services/api.service.ts:24 refreshToken | core/interceptors/auth.interceptor.ts, core/services/auth.service.ts |
| POST /auth/logout | frontend/src/app/core/services/api.service.ts:25 logout | layout/layout.component.ts, core/interceptors/auth.interceptor.ts, core/services/auth.service.ts |
| POST /auth/forgot-password | frontend/src/app/core/services/api.service.ts:26 forgotPassword | — |
| POST /auth/reset-password | frontend/src/app/core/services/api.service.ts:27 resetPassword | — |
| GET /auth/me | frontend/src/app/core/services/api.service.ts:28 getMe | — |
| PUT /auth/me | frontend/src/app/core/services/api.service.ts:29 updateMe | — |
| PUT /auth/me/password | frontend/src/app/core/services/api.service.ts:30 changePassword | profile/components/profile.component.ts |
| GET /backoffice/dashboard | frontend/src/app/core/services/api.service.ts:33 getDashboard | dashboard/components/dashboard.component.ts |
| GET /backoffice/dashboard/tenant/{tenantId} | frontend/src/app/core/services/api.service.ts:34 getDashboardByTenant | — |
| GET /backoffice/analytics | frontend/src/app/core/services/api.service.ts:37 getAnalytics | dashboard/components/dashboard.component.ts, analytics/components/analytics.component.ts |
| GET /backoffice/monitoring | frontend/src/app/core/services/api.service.ts:40 getMonitoring | monitoring/components/monitoring.component.ts |
| GET /backoffice/report | frontend/src/app/core/services/api.service.ts:43 getReports | — |
| POST /backoffice/report/generate | frontend/src/app/core/services/api.service.ts:44 generateReport | — |
| GET /notifications | frontend/src/app/core/services/api.service.ts:48 getNotifications | — |
| PUT /notifications/{id}/read | frontend/src/app/core/services/api.service.ts:50 markNotificationRead | — |
| PUT /notifications/read-all | frontend/src/app/core/services/api.service.ts:51 markAllNotificationsRead | — |
| DELETE /notifications/{id} | frontend/src/app/core/services/api.service.ts:52 deleteNotification | — |
| GET /universities | frontend/src/app/core/services/api.service.ts:56 getUniversities | universities/components/universities.component.ts |
| GET /universities/{id} | frontend/src/app/core/services/api.service.ts:58 getUniversity | universities/components/university-detail.component.ts |
| POST /universities | frontend/src/app/core/services/api.service.ts:59 createUniversity | universities/components/universities.component.ts |
| PUT /universities/{id} | frontend/src/app/core/services/api.service.ts:60 updateUniversity | universities/components/universities.component.ts |
| DELETE /universities/{id} | frontend/src/app/core/services/api.service.ts:61 deleteUniversity | universities/components/universities.component.ts |
| GET /campuses | frontend/src/app/core/services/api.service.ts:65 getCampuses | — |
| GET /campuses/{id} | frontend/src/app/core/services/api.service.ts:67 getCampus | — |
| POST /campuses | frontend/src/app/core/services/api.service.ts:68 createCampus | — |
| PUT /campuses/{id} | frontend/src/app/core/services/api.service.ts:69 updateCampus | — |
| DELETE /campuses/{id} | frontend/src/app/core/services/api.service.ts:70 deleteCampus | — |
| GET /faculties | frontend/src/app/core/services/api.service.ts:74 getFaculties | — |
| GET /faculties/{id} | frontend/src/app/core/services/api.service.ts:76 getFaculty | — |
| POST /faculties | frontend/src/app/core/services/api.service.ts:77 createFaculty | — |
| PUT /faculties/{id} | frontend/src/app/core/services/api.service.ts:78 updateFaculty | — |
| DELETE /faculties/{id} | frontend/src/app/core/services/api.service.ts:79 deleteFaculty | — |
| GET /programs | frontend/src/app/core/services/api.service.ts:83 getPrograms | — |
| GET /programs/{id} | frontend/src/app/core/services/api.service.ts:85 getProgram | — |
| POST /programs | frontend/src/app/core/services/api.service.ts:86 createProgram | — |
| PUT /programs/{id} | frontend/src/app/core/services/api.service.ts:87 updateProgram | — |
| DELETE /programs/{id} | frontend/src/app/core/services/api.service.ts:88 deleteProgram | — |
| GET /candidates | frontend/src/app/core/services/api.service.ts:92 getCandidates | — |
| GET /candidates/{id} | frontend/src/app/core/services/api.service.ts:94 getCandidate | — |
| GET /recommendations | frontend/src/app/core/services/api.service.ts:98 getRecommendations | recommendations/components/recommendations.component.ts |
| POST /recommendations/generate | frontend/src/app/core/services/api.service.ts:100 generateRecommendations | recommendations/components/recommendations.component.ts |
| POST /recommendations/simulate | frontend/src/app/core/services/api.service.ts:101 simulate | — |
| GET /recommendations/{id}/scores | frontend/src/app/core/services/api.service.ts:102 getRecommendationScores | — |
| GET /recommendations/{id}/explanation | frontend/src/app/core/services/api.service.ts:103 getRecommendationExplanation | recommendations/components/recommendations.component.ts |
| GET /transcripts | frontend/src/app/core/services/api.service.ts:107 getTranscripts | — |
| GET /scholarships | frontend/src/app/core/services/api.service.ts:112 getScholarships | — |
| GET /scholarships/{id} | frontend/src/app/core/services/api.service.ts:114 getScholarship | — |
| GET /guides | frontend/src/app/core/services/api.service.ts:118 getGuides | rules/components/rules.component.ts |
| GET /guides/{id} | frontend/src/app/core/services/api.service.ts:120 getGuide | — |
| POST /guides | frontend/src/app/core/services/api.service.ts:121 createGuide | — |
| PUT /guides/{id} | frontend/src/app/core/services/api.service.ts:122 updateGuide | — |
| DELETE /guides/{id} | frontend/src/app/core/services/api.service.ts:123 deleteGuide | — |
| GET /academic-years | frontend/src/app/core/services/api.service.ts:126 getAcademicYears | — |
| GET /subjects | frontend/src/app/core/services/api.service.ts:129 getSubjects | — |
| GET /grade-scales | frontend/src/app/core/services/api.service.ts:132 getGradeScales | — |
| GET /locations/countries | frontend/src/app/core/services/api.service.ts:135 getCountries | universities/components/universities.component.ts |
| GET /locations/cities | frontend/src/app/core/services/api.service.ts:136 getCities | universities/components/universities.component.ts |
| GET /locations/countries/{countryId}/cities | frontend/src/app/core/services/api.service.ts:137 getCitiesByCountry | universities/components/universities.component.ts |
| GET /imports | frontend/src/app/core/services/api.service.ts:141 getImports | imports/components/imports.component.ts |
| POST /imports | frontend/src/app/core/services/api.service.ts:147 uploadImport | imports/components/imports.component.ts |
| POST /imports/{id}/rollback | frontend/src/app/core/services/api.service.ts:149 rollbackImport | — |
| GET /imports/{id} | frontend/src/app/core/services/api.service.ts:150 getImportDetails | — |
| GET /documents | frontend/src/app/core/services/api.service.ts:154 getDocuments | documents/components/documents.component.ts |
| GET /documents/{id} | frontend/src/app/core/services/api.service.ts:156 getDocument | — |
| POST /documents | frontend/src/app/core/services/api.service.ts:160 uploadDocument | documents/components/documents.component.ts |
| DELETE /documents/{id} | frontend/src/app/core/services/api.service.ts:162 deleteDocument | documents/components/documents.component.ts |
| GET /documents/{id}/extractions | frontend/src/app/core/services/api.service.ts:163 getDocumentExtractions | documents/components/documents.component.ts |
| GET /documents/{id}/classifications | frontend/src/app/core/services/api.service.ts:164 getDocumentClassifications | — |
| GET /admin/users | frontend/src/app/core/services/api.service.ts:168 getUsers | administration/components/administration.component.ts, administration/components/users.component.ts |
| GET /admin/users/{id} | frontend/src/app/core/services/api.service.ts:170 getUser | — |
| POST /admin/users | frontend/src/app/core/services/api.service.ts:171 createUser | administration/components/users.component.ts |
| PUT /admin/users/{id} | frontend/src/app/core/services/api.service.ts:172 updateUser | administration/components/users.component.ts |
| DELETE /admin/users/{id} | frontend/src/app/core/services/api.service.ts:173 deleteUser | administration/components/users.component.ts |
| GET /admin/roles | frontend/src/app/core/services/api.service.ts:176 getRoles | administration/components/administration.component.ts, administration/components/roles.component.ts, administration/components/users.component.ts |
| GET /admin/roles/{id} | frontend/src/app/core/services/api.service.ts:177 getRole | — |
| POST /admin/roles | frontend/src/app/core/services/api.service.ts:178 createRole | administration/components/roles.component.ts |
| PUT /admin/roles/{id} | frontend/src/app/core/services/api.service.ts:179 updateRole | administration/components/roles.component.ts |
| DELETE /admin/roles/{id} | frontend/src/app/core/services/api.service.ts:180 deleteRole | administration/components/roles.component.ts |
| GET /admin/organizations | frontend/src/app/core/services/api.service.ts:183 getOrganizations | administration/components/administration.component.ts, administration/components/organizations.component.ts, administration/components/tenants.component.ts |
| GET /admin/organizations/{id} | frontend/src/app/core/services/api.service.ts:184 getOrganization | — |
| POST /admin/organizations | frontend/src/app/core/services/api.service.ts:185 createOrganization | administration/components/organizations.component.ts |
| PUT /admin/organizations/{id} | frontend/src/app/core/services/api.service.ts:186 updateOrganization | administration/components/organizations.component.ts |
| DELETE /admin/organizations/{id} | frontend/src/app/core/services/api.service.ts:187 deleteOrganization | administration/components/organizations.component.ts |
| GET /admin/tenants | frontend/src/app/core/services/api.service.ts:190 getTenants | administration/components/administration.component.ts, administration/components/tenants.component.ts, administration/components/users.component.ts |
| GET /admin/tenants/{id} | frontend/src/app/core/services/api.service.ts:191 getTenant | — |
| POST /admin/tenants | frontend/src/app/core/services/api.service.ts:192 createTenant | administration/components/tenants.component.ts |
| PUT /admin/tenants/{id} | frontend/src/app/core/services/api.service.ts:193 updateTenant | administration/components/tenants.component.ts |
| DELETE /admin/tenants/{id} | frontend/src/app/core/services/api.service.ts:194 deleteTenant | administration/components/tenants.component.ts |
| GET /admin/departments | frontend/src/app/core/services/api.service.ts:197 getDepartments | administration/components/departments.component.ts |
| POST /admin/departments | frontend/src/app/core/services/api.service.ts:198 createDepartment | administration/components/departments.component.ts |
| PUT /admin/departments/{id} | frontend/src/app/core/services/api.service.ts:199 updateDepartment | administration/components/departments.component.ts |
| DELETE /admin/departments/{id} | frontend/src/app/core/services/api.service.ts:200 deleteDepartment | administration/components/departments.component.ts |
| GET /admin/teams | frontend/src/app/core/services/api.service.ts:203 getTeams | administration/components/teams.component.ts |
| POST /admin/teams | frontend/src/app/core/services/api.service.ts:204 createTeam | administration/components/teams.component.ts |
| PUT /admin/teams/{id} | frontend/src/app/core/services/api.service.ts:205 updateTeam | administration/components/teams.component.ts |
| DELETE /admin/teams/{id} | frontend/src/app/core/services/api.service.ts:206 deleteTeam | administration/components/teams.component.ts |
| GET /admin/invitations | frontend/src/app/core/services/api.service.ts:209 getInvitations | — |
| POST /admin/invitations | frontend/src/app/core/services/api.service.ts:210 sendInvitation | — |
| DELETE /admin/invitations/{id} | frontend/src/app/core/services/api.service.ts:211 cancelInvitation | — |
| GET /knowledge/nodes | frontend/src/app/core/services/api.service.ts:215 getKnowledgeNodes | knowledge/components/knowledge.component.ts |
| GET /knowledge/nodes/{id} | frontend/src/app/core/services/api.service.ts:217 getKnowledgeNode | — |
| GET /knowledge/search | frontend/src/app/core/services/api.service.ts:218 searchKnowledge | knowledge/components/knowledge.component.ts, intelligence/components/knowledge-browser.component.ts |
| POST /knowledge/smart-query | frontend/src/app/core/services/api.service.ts:219 smartQuery | knowledge/components/smart-query.component.ts, intelligence/components/smart-query.component.ts |
| GET /audit | frontend/src/app/core/services/api.service.ts:223 getAuditLogs | — |
| GET /settings | frontend/src/app/core/services/api.service.ts:227 getSettings | — |
| PUT /settings | frontend/src/app/core/services/api.service.ts:228 updateSettings | — |
| GET /backoffice/activity-logs | frontend/src/app/core/services/api.service.ts:232 getActivityLogs | monitoring/components/monitoring.component.ts |
| POST /auth/login | frontend/src/app/core/services/auth.service.ts:56 login | auth/login/login.component.ts |
| POST /auth/logout | frontend/src/app/core/services/auth.service.ts:70 logout | layout/layout.component.ts, core/interceptors/auth.interceptor.ts |
| POST /auth/refresh | frontend/src/app/core/services/auth.service.ts:87 refreshToken | core/interceptors/auth.interceptor.ts |
| GET /auth/me | frontend/src/app/core/services/auth.service.ts:102 getCurrentUser | — |
| PUT /auth/me | frontend/src/app/core/services/auth.service.ts:112 updateProfile | profile/components/profile.component.ts |
| PUT /auth/me/password | frontend/src/app/core/services/auth.service.ts:119 changePassword | profile/components/profile.component.ts |
| POST /auth/forgot-password | frontend/src/app/core/services/auth.service.ts:123 forgotPassword | — |
| POST /auth/reset-password | frontend/src/app/core/services/auth.service.ts:127 resetPassword | — |
| POST /intelligence/process | frontend/src/app/core/services/intelligence.service.ts:55 processDocument | intelligence/components/pipeline.component.ts |
| POST /intelligence/smart-query | frontend/src/app/core/services/intelligence.service.ts:73 smartQuery | knowledge/components/smart-query.component.ts, intelligence/components/smart-query.component.ts |
| GET /intelligence/knowledge | frontend/src/app/core/services/intelligence.service.ts:80 searchKnowledge | knowledge/components/knowledge.component.ts, intelligence/components/knowledge-browser.component.ts |
| GET /intelligence/knowledge/{nodeId}/related | frontend/src/app/core/services/intelligence.service.ts:87 getRelatedNodes | — |
| POST /intelligence/feedback | frontend/src/app/core/services/intelligence.service.ts:100 recordFeedback | — |
| GET /intelligence/history/{candidateId} | frontend/src/app/core/services/intelligence.service.ts:107 getLearningHistory | — |
| GET /notifications | frontend/src/app/core/services/notification.service.ts:26 load | layout/layout.component.ts, universities/components/universities.component.ts, rules/components/rules.component.ts, recommendations/components/recommendations.component.ts, notifications/components/notifications.component.ts, monitoring/components/monitoring.component.ts, knowledge/components/knowledge.component.ts, intelligence/components/knowledge-browser.component.ts, documents/components/documents.component.ts, analytics/components/analytics.component.ts, administration/components/departments.component.ts, administration/components/organizations.component.ts, administration/components/roles.component.ts, administration/components/teams.component.ts, administration/components/tenants.component.ts, administration/components/users.component.ts |
| PUT /notifications/{id}/read | frontend/src/app/core/services/notification.service.ts:37 markAsRead | layout/layout.component.ts, notifications/components/notifications.component.ts |
| PUT /notifications/read-all | frontend/src/app/core/services/notification.service.ts:48 markAllAsRead | layout/layout.component.ts, notifications/components/notifications.component.ts |
| GET /search | frontend/src/app/core/services/search.service.ts:42 search | — |
| POST /auth/forgot-password | frontend/src/app/auth/forgot-password/forgot-password.component.ts:21 onSubmit | — |
| POST /auth/reset-password | frontend/src/app/auth/reset-password/reset-password.component.ts:21 onSubmit | — |

### Sockets

- src/main/java/com/orientation/orientationapp/backoffice/notification_center/controller/NotificationSocketController.java:1 : []
- src/main/java/com/orientation/orientationapp/backoffice/monitoring/controller/DashboardSocketController.java:1 : []
- src/main/java/com/orientation/orientationapp/backoffice/monitoring/controller/MonitoringSocketController.java:1 : []

## Annexe 2 — Entités JPA et correspondance des tables

Présence de CREATE TABLE vérifiée dans les migrations ; ne certifie pas toutes les colonnes ni leurs types. Les associations et champs obligatoires doivent rester alignés avec le DDL. BaseEntity fournit version/deleted/timestamps aux entités qui en héritent.

| Entité / table | Preuve | FK JPA / version / tenant | Migration CREATE TABLE |
| --- | --- | --- | --- |
| AuditEvent / audit_events | src/main/java/com/orientation/orientationapp/audit/model/AuditEvent.java:1 | Pas de scope tenant explicite repéré | ABSENTE |
| Notification / bo_notifications | src/main/java/com/orientation/orientationapp/backoffice/notification_center/entity/Notification.java:1 | BaseEntity (@Version, deleted) | ABSENTE |
| ImportHistoryRecord / import_history_records | src/main/java/com/orientation/orientationapp/dataplat_history/entity/ImportHistoryRecord.java:1 | BaseEntity (@Version, deleted); champ tenant; joins: parent_version_id | V44__create_import_history_records.sql |
| ImportEntityMapping / import_entity_mappings | src/main/java/com/orientation/orientationapp/dataplat_import/entity/ImportEntityMapping.java:1 | BaseEntity (@Version, deleted) | V46__create_import_entity_mappings.sql |
| QualityReportRecord / quality_report_records | src/main/java/com/orientation/orientationapp/dataplat_quality/entity/QualityReportRecord.java:1 | BaseEntity (@Version, deleted) | V45__create_quality_report_records.sql |
| Document / doc_documents | src/main/java/com/orientation/orientationapp/docintel/document/entity/Document.java:1 | BaseEntity (@Version, deleted); champ tenant | V47__create_doc_documents.sql |
| DocumentAudit / doc_document_audits | src/main/java/com/orientation/orientationapp/docintel/document/entity/DocumentAudit.java:1 | BaseEntity (@Version, deleted); joins: document_id | V49__create_doc_extractions_classifications_audits.sql |
| DocumentClassification / doc_document_classifications | src/main/java/com/orientation/orientationapp/docintel/document/entity/DocumentClassification.java:1 | BaseEntity (@Version, deleted); joins: document_id | V49__create_doc_extractions_classifications_audits.sql |
| DocumentExtraction / doc_document_extractions | src/main/java/com/orientation/orientationapp/docintel/document/entity/DocumentExtraction.java:1 | BaseEntity (@Version, deleted); joins: document_id | V49__create_doc_extractions_classifications_audits.sql |
| DocumentMetadata / doc_document_metadata | src/main/java/com/orientation/orientationapp/docintel/document/entity/DocumentMetadata.java:1 | BaseEntity (@Version, deleted); joins: document_id | V48__create_doc_versions_pages_metadata.sql |
| DocumentPage / doc_document_pages | src/main/java/com/orientation/orientationapp/docintel/document/entity/DocumentPage.java:1 | BaseEntity (@Version, deleted); joins: document_id | V48__create_doc_versions_pages_metadata.sql |
| DocumentVersion / doc_document_versions | src/main/java/com/orientation/orientationapp/docintel/document/entity/DocumentVersion.java:1 | BaseEntity (@Version, deleted); joins: document_id | V48__create_doc_versions_pages_metadata.sql |
| KnowledgeNode / knowledge_nodes | src/main/java/com/orientation/orientationapp/knowledge/entity/KnowledgeNode.java:1 | BaseEntity (@Version, deleted); joins: source_id, target_id | V50__create_knowledge_nodes.sql |
| KnowledgeRelation / knowledge_relations | src/main/java/com/orientation/orientationapp/knowledge/entity/KnowledgeRelation.java:1 | BaseEntity (@Version, deleted); joins: source_id, target_id | V51__create_knowledge_relations.sql |
| LearningHistory / knowledge_learning_history | src/main/java/com/orientation/orientationapp/knowledge/entity/LearningHistory.java:1 | BaseEntity (@Version, deleted) | V55__create_learning_history.sql |
| RecommendationFeedback / knowledge_recommendation_feedback | src/main/java/com/orientation/orientationapp/knowledge/entity/RecommendationFeedback.java:1 | BaseEntity (@Version, deleted) | V53__create_recommendation_feedback.sql |
| SearchIndex / knowledge_search_index | src/main/java/com/orientation/orientationapp/knowledge/entity/SearchIndex.java:1 | BaseEntity (@Version, deleted) | V52__create_search_index.sql |
| SimilarityMatrix / knowledge_similarity_matrix | src/main/java/com/orientation/orientationapp/knowledge/entity/SimilarityMatrix.java:1 | BaseEntity (@Version, deleted) | V54__create_similarity_matrix.sql |
| ActivityLog / activity_logs | src/main/java/com/orientation/orientationapp/modules/admin/entity/ActivityLog.java:1 | BaseEntity (@Version, deleted); champ tenant; joins: user_id | V43__create_activity_logs.sql |
| Department / departments | src/main/java/com/orientation/orientationapp/modules/admin/entity/Department.java:1 | BaseEntity (@Version, deleted); champ tenant; joins: parent_id, organization_id, tenant_id | V39__create_departments.sql |
| Invitation / invitations | src/main/java/com/orientation/orientationapp/modules/admin/entity/Invitation.java:1 | BaseEntity (@Version, deleted); champ tenant; joins: tenant_id, role_id, department_id, invited_by | V41__create_invitations.sql |
| Team / teams | src/main/java/com/orientation/orientationapp/modules/admin/entity/Team.java:1 | BaseEntity (@Version, deleted); champ tenant; joins: tenant_id, leader_id, team_id, user_id | V42__create_teams.sql |
| UserProfile / user_profiles | src/main/java/com/orientation/orientationapp/modules/admin/entity/UserProfile.java:1 | BaseEntity (@Version, deleted); joins: user_id | V40__create_user_profiles.sql |
| ApiKey / api_keys | src/main/java/com/orientation/orientationapp/modules/auth/entity/ApiKey.java:1 | BaseEntity (@Version, deleted); joins: user_id | V37__create_refresh_tokens_api_keys.sql |
| Organization / organizations | src/main/java/com/orientation/orientationapp/modules/auth/entity/Organization.java:1 | BaseEntity (@Version, deleted) | V33__create_organizations.sql |
| Permission / permissions | src/main/java/com/orientation/orientationapp/modules/auth/entity/Permission.java:1 | BaseEntity (@Version, deleted) | V35__create_roles_permissions.sql |
| RefreshToken / refresh_tokens | src/main/java/com/orientation/orientationapp/modules/auth/entity/RefreshToken.java:1 | BaseEntity (@Version, deleted); joins: user_id, replaced_by_token_id | V37__create_refresh_tokens_api_keys.sql |
| Role / roles | src/main/java/com/orientation/orientationapp/modules/auth/entity/Role.java:1 | BaseEntity (@Version, deleted) | V35__create_roles_permissions.sql |
| RolePermission / role_permissions | src/main/java/com/orientation/orientationapp/modules/auth/entity/RolePermission.java:1 | BaseEntity (@Version, deleted); joins: role_id, permission_id | V35__create_roles_permissions.sql |
| Tenant / tenants | src/main/java/com/orientation/orientationapp/modules/auth/entity/Tenant.java:1 | BaseEntity (@Version, deleted); joins: organization_id | V34__create_tenants.sql |
| User / users | src/main/java/com/orientation/orientationapp/modules/auth/entity/User.java:1 | BaseEntity (@Version, deleted); champ tenant; joins: tenant_id | V36__create_users_user_roles.sql |
| UserRole / user_roles | src/main/java/com/orientation/orientationapp/modules/auth/entity/UserRole.java:1 | BaseEntity (@Version, deleted); joins: user_id, role_id | V36__create_users_user_roles.sql |
| AcademicYear / academic_years | src/main/java/com/orientation/orientationapp/modules/orientation/entity/AcademicYear.java:1 | BaseEntity (@Version, deleted) | V2__create_academic_years.sql |
| AdmissionCriterion / admission_criteria | src/main/java/com/orientation/orientationapp/modules/orientation/entity/AdmissionCriterion.java:1 | BaseEntity (@Version, deleted); joins: guide_version_id, program_id, faculty_id, subject_id | V14__create_admission_criteria.sql |
| GradeScale / grade_scales | src/main/java/com/orientation/orientationapp/modules/orientation/entity/GradeScale.java:1 | BaseEntity (@Version, deleted); joins: country_id, academic_year_id | V5__create_grade_scales.sql |
| GradeScaleItem / grade_scale_items | src/main/java/com/orientation/orientationapp/modules/orientation/entity/GradeScaleItem.java:1 | BaseEntity (@Version, deleted); joins: grade_scale_id | V6__create_grade_scale_items.sql |
| GuideVersion / guide_versions | src/main/java/com/orientation/orientationapp/modules/orientation/entity/GuideVersion.java:1 | BaseEntity (@Version, deleted); joins: orientation_guide_id | V13__create_guide_versions.sql |
| ImportHistory / import_histories | src/main/java/com/orientation/orientationapp/modules/orientation/entity/ImportHistory.java:1 | BaseEntity (@Version, deleted); joins: guide_version_id | V23__create_import_histories.sql |
| OrientationGuide / orientation_guides | src/main/java/com/orientation/orientationapp/modules/orientation/entity/OrientationGuide.java:1 | BaseEntity (@Version, deleted); joins: country_id, academic_year_id | V12__create_orientation_guides.sql |
| Subject / subjects | src/main/java/com/orientation/orientationapp/modules/orientation/entity/Subject.java:1 | BaseEntity (@Version, deleted); joins: grade_scale_id | V7__create_subjects.sql |
| Recommendation / recommendations | src/main/java/com/orientation/orientationapp/modules/recommendation/entity/Recommendation.java:1 | BaseEntity (@Version, deleted); joins: candidate_id, guide_version_id, program_id, transcript_id | V21__create_recommendations.sql |
| RecommendationExplanation / recommendation_explanations | src/main/java/com/orientation/orientationapp/modules/recommendation/entity/RecommendationExplanation.java:1 | BaseEntity (@Version, deleted); joins: recommendation_id | V22__create_recommendation_explanations.sql |
| Scholarship / scholarships | src/main/java/com/orientation/orientationapp/modules/scholarship/entity/Scholarship.java:1 | BaseEntity (@Version, deleted); joins: country_id, academic_year_id | V15__create_scholarships.sql |
| ScholarshipCriterion / scholarship_criteria | src/main/java/com/orientation/orientationapp/modules/scholarship/entity/ScholarshipCriterion.java:1 | BaseEntity (@Version, deleted); joins: guide_version_id, scholarship_id, subject_id | V16__create_scholarship_criteria.sql |
| Transcript / transcripts | src/main/java/com/orientation/orientationapp/modules/transcript/entity/Transcript.java:1 | BaseEntity (@Version, deleted); joins: candidate_id, academic_year_id | V18__create_transcripts.sql |
| TranscriptAnalysis / transcript_analyses | src/main/java/com/orientation/orientationapp/modules/transcript/entity/TranscriptAnalysis.java:1 | BaseEntity (@Version, deleted); joins: transcript_id | V20__create_transcript_analyses.sql |
| TranscriptLine / transcript_lines | src/main/java/com/orientation/orientationapp/modules/transcript/entity/TranscriptLine.java:1 | BaseEntity (@Version, deleted); joins: transcript_id, subject_id | V19__create_transcript_lines.sql |
| Campus / campuses | src/main/java/com/orientation/orientationapp/modules/university/entity/Campus.java:1 | BaseEntity (@Version, deleted); joins: university_id, city_id | V9__create_campuses.sql |
| City / cities | src/main/java/com/orientation/orientationapp/modules/university/entity/City.java:1 | BaseEntity (@Version, deleted); joins: country_id | V4__create_cities.sql |
| Country / countries | src/main/java/com/orientation/orientationapp/modules/university/entity/Country.java:1 | BaseEntity (@Version, deleted) | V3__create_countries.sql |
| Faculty / faculties | src/main/java/com/orientation/orientationapp/modules/university/entity/Faculty.java:1 | BaseEntity (@Version, deleted); joins: campus_id | V10__create_faculties.sql |
| Program / programs | src/main/java/com/orientation/orientationapp/modules/university/entity/Program.java:1 | BaseEntity (@Version, deleted); joins: faculty_id | V11__create_programs.sql |
| University / universities | src/main/java/com/orientation/orientationapp/modules/university/entity/University.java:1 | BaseEntity (@Version, deleted); joins: country_id, city_id | V8__create_universities.sql |
| Candidate / candidates | src/main/java/com/orientation/orientationapp/modules/user/entity/Candidate.java:1 | BaseEntity (@Version, deleted); joins: country_id, city_id | V17__create_candidates.sql |

## Annexe 3 — Migrations et contraintes identifiées

| Migration | Tables créées / nature | FK | Occurrences UNIQUE | Index explicites |
| --- | --- | --- | --- | --- |
| V1__create_extensions.sql | Extensions | 0 | 0 | 0 |
| V2__create_academic_years.sql | academic_years | 0 | 1 | 3 |
| V3__create_countries.sql | countries | 0 | 2 | 1 |
| V4__create_cities.sql | cities | 1 | 1 | 1 |
| V5__create_grade_scales.sql | grade_scales | 2 | 1 | 1 |
| V6__create_grade_scale_items.sql | grade_scale_items | 1 | 2 | 1 |
| V7__create_subjects.sql | subjects | 1 | 1 | 2 |
| V8__create_universities.sql | universities | 2 | 1 | 4 |
| V9__create_campuses.sql | campuses | 2 | 1 | 2 |
| V10__create_faculties.sql | faculties | 1 | 1 | 1 |
| V11__create_programs.sql | programs | 1 | 1 | 3 |
| V12__create_orientation_guides.sql | orientation_guides | 2 | 1 | 2 |
| V13__create_guide_versions.sql | guide_versions | 1 | 1 | 3 |
| V14__create_admission_criteria.sql | admission_criteria | 4 | 0 | 3 |
| V15__create_scholarships.sql | scholarships | 2 | 1 | 4 |
| V16__create_scholarship_criteria.sql | scholarship_criteria | 3 | 0 | 2 |
| V17__create_candidates.sql | candidates | 2 | 1 | 4 |
| V18__create_transcripts.sql | transcripts | 2 | 1 | 3 |
| V19__create_transcript_lines.sql | transcript_lines | 2 | 1 | 2 |
| V20__create_transcript_analyses.sql | transcript_analyses | 1 | 1 | 0 |
| V21__create_recommendations.sql | recommendations | 4 | 1 | 6 |
| V22__create_recommendation_explanations.sql | recommendation_explanations | 1 | 1 | 2 |
| V23__create_import_histories.sql | import_histories | 1 | 0 | 4 |
| V24__seed_countries.sql | Seeds / données | 0 | 0 | 0 |
| V25__seed_academic_years.sql | Seeds / données | 0 | 0 | 0 |
| V26__seed_grade_scales.sql | Seeds / données | 0 | 0 | 0 |
| V27__seed_subjects.sql | Seeds / données | 0 | 0 | 0 |
| V28__seed_scholarship_types.sql | Seeds / données | 0 | 0 | 0 |
| V29__seed_sample_universities.sql | Seeds / données | 0 | 0 | 0 |
| V30__seed_sample_campuses.sql | Seeds / données | 0 | 0 | 0 |
| V31__seed_sample_faculties.sql | Seeds / données | 0 | 0 | 0 |
| V32__seed_sample_programs.sql | Seeds / données | 0 | 0 | 0 |
| V33__create_organizations.sql | organizations | 0 | 0 | 1 |
| V34__create_tenants.sql | tenants | 1 | 1 | 1 |
| V35__create_roles_permissions.sql | roles, permissions, role_permissions | 2 | 3 | 1 |
| V36__create_users_user_roles.sql | users, user_roles | 3 | 2 | 4 |
| V37__create_refresh_tokens_api_keys.sql | refresh_tokens, api_keys | 3 | 1 | 3 |
| V38__seed_roles_permissions.sql | Seeds / données | 0 | 0 | 0 |
| V39__create_departments.sql | departments | 3 | 0 | 3 |
| V40__create_user_profiles.sql | user_profiles | 1 | 1 | 0 |
| V41__create_invitations.sql | invitations | 4 | 1 | 2 |
| V42__create_teams.sql | teams, team_members | 4 | 0 | 1 |
| V43__create_activity_logs.sql | activity_logs | 1 | 0 | 4 |
| V44__create_import_history_records.sql | import_history_records | 1 | 0 | 4 |
| V45__create_quality_report_records.sql | quality_report_records | 0 | 1 | 1 |
| V46__create_import_entity_mappings.sql | import_entity_mappings | 0 | 0 | 1 |
| V47__create_doc_documents.sql | doc_documents | 0 | 0 | 4 |
| V48__create_doc_versions_pages_metadata.sql | doc_document_versions, doc_document_pages, doc_document_metadata | 3 | 1 | 2 |
| V49__create_doc_extractions_classifications_audits.sql | doc_document_extractions, doc_document_classifications, doc_document_audits | 3 | 2 | 3 |
| V50__create_knowledge_nodes.sql | knowledge_nodes, knowledge_node_relations | 2 | 0 | 3 |
| V51__create_knowledge_relations.sql | knowledge_relations | 2 | 0 | 3 |
| V52__create_search_index.sql | knowledge_search_index | 0 | 0 | 3 |
| V53__create_recommendation_feedback.sql | knowledge_recommendation_feedback | 0 | 0 | 3 |
| V54__create_similarity_matrix.sql | knowledge_similarity_matrix | 0 | 0 | 3 |
| V55__create_learning_history.sql | knowledge_learning_history | 0 | 0 | 3 |
| V56__seed_knowledge_nodes.sql | Seeds / données | 0 | 0 | 0 |
| V57__seed_countries_cities_comprehensive.sql | Seeds / données | 0 | 0 | 0 |
| V58__seed_world_countries_cities_universities.sql | Seeds / données | 1 | 0 | 0 |
| V59__seed_campuses.sql | Seeds / données | 0 | 0 | 0 |
| V60__seed_faculties.sql | Seeds / données | 0 | 0 | 0 |
| V61__seed_programs.sql | Seeds / données | 0 | 0 | 0 |
| V62__seed_grade_scales_subjects.sql | Seeds / données | 0 | 0 | 0 |
| V63__make_candidate_country_bac_nullable.sql | ALTER | 0 | 0 | 0 |
| V64__seed_benin_universities_complete.sql | Seeds / données | 0 | 0 | 0 |

## Annexe 4 — DTO et champs explicitement déclarés

Inventaire des classes sous dto ; classes internes aux contrôleurs et modèles métier figurent dans les signatures de l’annexe API. Champs hérités et règles JSON implicites à vérifier lors du gel des contrats. Aucune valeur ni secret n’est reproduit.

| Fichier | Champs / types déclarés |
| --- | --- |
| src/main/java/com/orientation/orientationapp/backoffice/analytics/dto/response/AnalyticsResponse.java:1 | topUniversities: TopEntities; topPrograms: TopEntities; topScholarships: TopEntities; topCountries: TopEntities; evolution: EvolutionData; kpis: KpiData; items: List<EntityStat>; name: String; count: long; score: BigDecimal; daily: Map<String, Long>; monthly: Map<String, Long>; yearly: Map<String, Long>; acceptanceRate: BigDecimal; recommendationSuccessRate: BigDecimal; averageScore: BigDecimal; averageConfidence: BigDecimal; averageProcessingTime: BigDecimal; ocrAccuracy: BigDecimal; importSuccessRate: BigDecimal |
| src/main/java/com/orientation/orientationapp/backoffice/audit_center/dto/response/AuditResponse.java:1 | events: List<AuditEvent>; totalEvents: long; page: int; size: int; id: UUID; action: String; entityType: String; entityId: String; userId: String; details: String; ipAddress: String; timestamp: Instant; oldValues: String; newValues: String |
| src/main/java/com/orientation/orientationapp/backoffice/dashboard/dto/response/DashboardResponse.java:1 | kpis: KpiSummary; recentActivity: RecentActivity; systemHealth: SystemHealth; charts: ChartData; totalCandidates: long; totalRecommendations: long; totalGuidesImported: long; totalDocumentsOcr: long; activeRules: long; totalTenants: long; activeUsers: long; todayRecommendations: long; activeImports: long; recentImports: long; recentRecommendations: long; recentDocuments: long; recentUsers: long; status: String; cpuUsage: double; memoryUsage: double; heapUsed: long; heapMax: long; activeThreads: int; databaseConnections: int; responseTimeMs: double; recommendationsByDay: Map<String, Long>; importsByDay: Map<String, Long>; documentsByType: Map<String, Long> |
| src/main/java/com/orientation/orientationapp/backoffice/monitoring/dto/response/MonitoringResponse.java:1 | systemMetrics: SystemMetrics; applicationMetrics: ApplicationMetrics; healthChecks: List<HealthCheck>; cpuUsage: double; memoryUsage: double; heapUsed: long; heapMax: long; nonHeapUsed: long; activeThreads: int; daemonThreads: int; uptimeMs: long; totalApiCalls: long; averageResponseTime: double; errorCount: long; activeSessions: long; endpointCalls: Map<String, Long>; name: String; status: String; message: String; responseTimeMs: long |
| src/main/java/com/orientation/orientationapp/backoffice/report/dto/response/ReportResponse.java:1 | reportType: String; format: String; data: byte[]; metadata: Map<String, Object> |
| src/main/java/com/orientation/orientationapp/common/dto/BaseResponse.java:1 | success: boolean; message: String; data: T; timestamp: LocalDateTime; responseTimestamp: LocalDateTime |
| src/main/java/com/orientation/orientationapp/common/dto/PageRequest.java:1 | page: int; size: int; sortBy: String; sortDirection: String |
| src/main/java/com/orientation/orientationapp/common/dto/PaginatedResponse.java:1 | content: List<T>; page: int; size: int; totalElements: long; totalPages: int; first: boolean; last: boolean |
| src/main/java/com/orientation/orientationapp/dataplat_import/dto/request/ComparisonRequest.java:1 | versionId1: UUID; versionId2: UUID; dataType: DataType; campaignId: UUID |
| src/main/java/com/orientation/orientationapp/dataplat_import/dto/request/ExportRequest.java:1 | campaignId: UUID; versionId: UUID; dataType: DataType; format: DataFormat |
| src/main/java/com/orientation/orientationapp/dataplat_import/dto/request/ImportRequest.java:1 | campaignId: UUID; academicYearId: UUID; countryId: UUID; dataType: DataType; format: DataFormat; source: String; dryRun: boolean; skipValidation: boolean; options: Map<String, String> |
| src/main/java/com/orientation/orientationapp/dataplat_import/dto/request/RollbackRequest.java:1 | targetVersionId: UUID; campaignId: UUID; reason: String |
| src/main/java/com/orientation/orientationapp/dataplat_import/dto/response/CatalogResponse.java:1 | campaignId: UUID; catalog: Map<DataType, List<VersionInfo>>; activeVersions: Map<DataType, VersionInfo>; statistics: Map<String, Object> |
| src/main/java/com/orientation/orientationapp/dataplat_import/dto/response/ComparisonResponse.java:1 | versionId1: UUID; versionId2: UUID; diffResult: DiffResult; summary: String; totalChanges: int |
| src/main/java/com/orientation/orientationapp/dataplat_import/dto/response/ImportResponse.java:1 | importId: UUID; status: ImportStatus; totalRecords: int; successRecords: int; failedRecords: int; message: String |
| src/main/java/com/orientation/orientationapp/dataplat_import/dto/response/QualityResponse.java:1 | importId: UUID; report: QualityReport; overallScore: double; grade: String |
| src/main/java/com/orientation/orientationapp/docintel/document/dto/request/UploadDocumentRequest.java:1 | title: String; description: String; tenantId: String |
| src/main/java/com/orientation/orientationapp/docintel/document/dto/response/DocumentResponse.java:1 | id: UUID; title: String; originalFileName: String; mimeType: String; fileSize: Long; checksum: String; documentType: String; status: String; tenantId: String; uploadedBy: String; uploadedAt: Instant; description: String; language: String; pageCount: Integer; ocrScore: BigDecimal; qualityScore: BigDecimal; createdAt: Instant |
| src/main/java/com/orientation/orientationapp/docintel/document/dto/response/MessageResponse.java:1 | message: String; success: boolean |
| src/main/java/com/orientation/orientationapp/modules/admin/dto/request/CreateDepartmentRequest.java:1 | name: String; code: String; description: String; organizationId: UUID; tenantId: UUID; parentId: UUID; sortOrder: Integer |
| src/main/java/com/orientation/orientationapp/modules/admin/dto/request/CreateInvitationRequest.java:1 | email: String; tenantId: UUID; roleId: UUID; departmentId: UUID; message: String |
| src/main/java/com/orientation/orientationapp/modules/admin/dto/request/CreateOrganizationRequest.java:1 | name: String; code: String; description: String; email: String; phone: String; website: String; industry: String; country: String |
| src/main/java/com/orientation/orientationapp/modules/admin/dto/request/CreateTenantRequest.java:1 | name: String; code: String; organizationId: UUID; description: String; timezone: String; language: String; country: String; currency: String; subscriptionPlan: String; maxUsers: Integer; maxStorageGb: Integer |
| src/main/java/com/orientation/orientationapp/modules/admin/dto/request/CreateUserRequest.java:1 | email: String; password: String; firstName: String; lastName: String; phone: String; tenantId: UUID; departmentId: UUID; roleId: UUID |
| src/main/java/com/orientation/orientationapp/modules/admin/dto/response/MessageResponse.java:1 | message: String; success: boolean |
| src/main/java/com/orientation/orientationapp/modules/admin/dto/response/OrganizationResponse.java:1 | id: UUID; name: String; code: String; description: String; email: String; phone: String; website: String; logoUrl: String; industry: String; country: String; active: boolean; archived: boolean; tenantCount: int; createdAt: Instant; updatedAt: Instant |
| src/main/java/com/orientation/orientationapp/modules/admin/dto/response/TenantResponse.java:1 | id: UUID; name: String; code: String; organizationId: UUID; organizationName: String; description: String; active: boolean; suspended: boolean; timezone: String; language: String; country: String; currency: String; subscriptionPlan: String; maxUsers: int; currentUsers: int; maxStorageGb: int; createdAt: Instant; updatedAt: Instant |
| src/main/java/com/orientation/orientationapp/modules/admin/dto/response/UserResponse.java:1 | id: UUID; email: String; firstName: String; lastName: String; fullName: String; phone: String; tenantId: UUID; tenantName: String; status: String; emailVerified: boolean; enabled: boolean; mfaEnabled: boolean; lastLoginAt: Instant; createdAt: Instant; roles: List<String> |
| src/main/java/com/orientation/orientationapp/modules/auth/dto/request/ChangePasswordRequest.java:1 | currentPassword: String; newPassword: String |
| src/main/java/com/orientation/orientationapp/modules/auth/dto/request/ForgotPasswordRequest.java:1 | email: String |
| src/main/java/com/orientation/orientationapp/modules/auth/dto/request/LoginRequest.java:1 | email: String; password: String; rememberMe: Boolean |
| src/main/java/com/orientation/orientationapp/modules/auth/dto/request/RefreshTokenRequest.java:1 | refreshToken: String |
| src/main/java/com/orientation/orientationapp/modules/auth/dto/request/RegisterRequest.java:1 | email: String; password: String; firstName: String; lastName: String; phone: String; tenantCode: String |
| src/main/java/com/orientation/orientationapp/modules/auth/dto/request/ResetPasswordRequest.java:1 | token: String; newPassword: String |
| src/main/java/com/orientation/orientationapp/modules/auth/dto/response/LoginResponse.java:1 | accessToken: String; refreshToken: String; expiresIn: long; tokenType: String; user: UserInfo |
| src/main/java/com/orientation/orientationapp/modules/auth/dto/response/MessageResponse.java:1 | message: String; success: boolean |
| src/main/java/com/orientation/orientationapp/modules/auth/dto/response/RegisterResponse.java:1 | userId: UUID; email: String; firstName: String; lastName: String; message: String |
| src/main/java/com/orientation/orientationapp/modules/auth/dto/response/UserInfo.java:1 | id: UUID; email: String; firstName: String; lastName: String; fullName: String; roles: List<String>; permissions: List<String>; tenantId: UUID; tenantName: String |
| src/main/java/com/orientation/orientationapp/recengine/recommendation/dto/request/GenerateRecommendationRequest.java:1 | candidateId: UUID; bacType: String; bacAverage: BigDecimal; subjectGrades: Map<String, BigDecimal>; preferredFields: List<String>; preferredCountries: List<String>; language: String; budget: BigDecimal |
| src/main/java/com/orientation/orientationapp/recengine/recommendation/dto/request/SimulationRequest.java:1 | candidateId: UUID; modifiedGrades: Map<String, BigDecimal>; modifiedBacType: String; modifiedBacAverage: BigDecimal; modifiedCountry: String; modifiedBudget: BigDecimal; modifiedLanguage: String; description: String |
| src/main/java/com/orientation/orientationapp/recengine/recommendation/dto/response/RecommendationResponse.java:1 | recommendations: List<RecommendationItem>; totalPrograms: int; profileSummary: String; rank: Integer; programName: String; universityName: String; facultyName: String; score: BigDecimal; confidence: BigDecimal; admissionProbability: BigDecimal; difficultyLevel: String; eligible: boolean; explanationSummary: String; strengths: List<String>; warnings: List<String> |
| src/main/java/com/orientation/orientationapp/recengine/recommendation/dto/response/SimulationResponse.java:1 | simulatedRecommendations: List<RecommendationItem>; differences: List<SimulationDifference>; summary: String; rank: Integer; programName: String; universityName: String; score: BigDecimal; type: String; programName: String; description: String; rankChange: int |

## Annexe 5 — Tests backend présents

Non exécutés dans cette session. Le nom d’un test ne préjuge pas de ses assertions ni de sa réussite.

| Classe | Méthodes sous @Test ou tests repérés |
| --- | --- |
| src/test/java/com/orientation/orientationapp/dataplat/parser/CsvParserTest.java:1 | shouldParseCsvWithCommaSeparator, shouldParseCsvWithSemicolonSeparator, shouldExtractHeaders, shouldHandleEmptyFile, shouldReturnCorrectFormat, shouldSupportCorrectMimeTypes, shouldHandleUtf8Characters |
| src/test/java/com/orientation/orientationapp/dataplat/validation/ValidationEngineTest.java:1 | shouldValidateSuccessfulImport, shouldDetectMissingRequiredField, shouldDetectEmptyFile, shouldDetectDuplicates, shouldValidateSingleRow |
| src/test/java/com/orientation/orientationapp/dataplat_import/ImportOrchestratorTest.java:1 | shouldExecuteImport, shouldGetImportStatus, shouldCancelImport |
| src/test/java/com/orientation/orientationapp/docintel/classification/ClassificationTest.java:1 | shouldClassifyOrientationGuide, shouldClassifyTranscript, shouldClassifyProgram, shouldReturnClassifierId, shouldHandleEmptyText |
| src/test/java/com/orientation/orientationapp/docintel/ocr/OcrEngineTest.java:1 | shouldPerformOcrOnTextContent, shouldExtractTitles, shouldReturnCorrectEngineId, shouldSupportTextMimeTypes, shouldReturnConfidenceThreshold |
| src/test/java/com/orientation/orientationapp/integration/AuthIntegrationTest.java:1 | getMe_Authenticated, getMe_Unauthenticated |
| src/test/java/com/orientation/orientationapp/integration/IntelligenceIntegrationTest.java:1 | searchKnowledge_ReturnsResults |
| src/test/java/com/orientation/orientationapp/integration/LocationIntegrationTest.java:1 | getCountries_ReturnsList, getCities_ReturnsList |
| src/test/java/com/orientation/orientationapp/integration/RecommendationIntegrationTest.java:1 | listRecommendations_ReturnsList |
| src/test/java/com/orientation/orientationapp/integration/UniversityIntegrationTest.java:1 | listUniversities_ReturnsPage |
| src/test/java/com/orientation/orientationapp/recengine/ProfileAnalyzerTest.java:1 | shouldBuildProfileFromGrades, shouldIdentifyStrongSubjects, shouldIdentifyWeakSubjects |
| src/test/java/com/orientation/orientationapp/recengine/RankingEngineTest.java:1 | shouldRankProgramsByScore, shouldAssignDifficultyLevel |
| src/test/java/com/orientation/orientationapp/recengine/ScoringEngineTest.java:1 | shouldComputeScoreForStrongProfile, shouldComputeScoreForWeakProfile, shouldIncludeAllScoreComponents |
| src/test/java/com/orientation/orientationapp/recengine/SimulationEngineTest.java:1 | shouldRunSimulation |

## Annexe 6 — Couverture statique des composants Angular

Chaque template/class a été parcouru pour routes, formulaires, boutons, états, appels et erreurs. Inventaire mécanique ci-dessous ; le tableau D contient le diagnostic humain. Présence d’un indicateur n’est pas preuve de son fonctionnement. Aucune validation visuelle runtime.

| Composant | Formulaire / modèle | Boutons | Loading / vide / erreurs (indices) | Appels services repérés |
| --- | --- | --- | --- | --- |
| frontend/src/app/administration/components/administration.component.ts:1 | ngModel | 2 | loading, vide, erreur | getOrganizations, getRoles, getTenants, getUsers |
| frontend/src/app/administration/components/departments.component.ts:1 | ngModel | 3 | loading, vide, erreur | createDepartment, deleteDepartment, getDepartments, updateDepartment |
| frontend/src/app/administration/components/organizations.component.ts:1 | ngModel | 3 | loading, vide, erreur | createOrganization, deleteOrganization, getOrganizations, updateOrganization |
| frontend/src/app/administration/components/roles.component.ts:1 | ngModel | 4 | loading, vide, erreur | createRole, deleteRole, getRoles, updateRole |
| frontend/src/app/administration/components/teams.component.ts:1 | ngModel | 3 | loading, vide, erreur | createTeam, deleteTeam, getTeams, updateTeam |
| frontend/src/app/administration/components/tenants.component.ts:1 | ngModel | 3 | loading, vide, erreur | createTenant, deleteTenant, getOrganizations, getTenants, updateTenant |
| frontend/src/app/administration/components/users.component.ts:1 | ngModel | 4 | loading, vide, erreur | createUser, deleteUser, getRoles, getTenants, getUsers, updateUser |
| frontend/src/app/analytics/components/analytics.component.ts:1 | — | 1 | vide, erreur | getAnalytics |
| frontend/src/app/app.component.ts:1 | — | 0 |  | — |
| frontend/src/app/auth/forgot-password/forgot-password.component.ts:1 | ngModel | 1 | loading, erreur | — |
| frontend/src/app/auth/login/login.component.ts:1 | ngModel | 2 | loading, erreur | login |
| frontend/src/app/auth/reset-password/reset-password.component.ts:1 | ngModel | 1 | loading, erreur | — |
| frontend/src/app/dashboard/components/dashboard.component.ts:1 | — | 2 | loading, vide, erreur | getAnalytics, getDashboard, subscribe |
| frontend/src/app/documents/components/documents.component.ts:1 | — | 5 | loading, vide, erreur | deleteDocument, getDocumentExtractions, getDocuments, uploadDocument |
| frontend/src/app/imports/components/imports.component.ts:1 | — | 2 | loading, vide, erreur | getImports, uploadImport |
| frontend/src/app/intelligence/components/knowledge-browser.component.ts:1 | ngModel | 2 | loading, vide, erreur | searchKnowledge |
| frontend/src/app/intelligence/components/pipeline.component.ts:1 | — | 0 | loading, vide, erreur | processDocument |
| frontend/src/app/intelligence/components/smart-query.component.ts:1 | ngModel | 2 | loading, vide, erreur | smartQuery |
| frontend/src/app/knowledge/components/knowledge.component.ts:1 | ngModel | 2 | loading, vide, erreur | getKnowledgeNodes, searchKnowledge |
| frontend/src/app/knowledge/components/smart-query.component.ts:1 | ngModel | 2 | loading, vide, erreur | smartQuery |
| frontend/src/app/layout/layout.component.ts:1 | ngModel | 5 | vide | load, logout, markAllAsRead, markAsRead |
| frontend/src/app/monitoring/components/monitoring.component.ts:1 | — | 2 | loading, vide, erreur | getActivityLogs, getMonitoring |
| frontend/src/app/notifications/components/notifications.component.ts:1 | — | 3 | loading, vide | load, markAllAsRead, markAsRead, notifications |
| frontend/src/app/profile/components/profile.component.ts:1 | ngModel | 2 | erreur | changePassword, updateProfile |
| frontend/src/app/recommendations/components/recommendations.component.ts:1 | ngModel | 3 | loading, vide, erreur | generateRecommendations, getRecommendationExplanation, getRecommendations |
| frontend/src/app/reports/components/reports.component.ts:1 | — | 2 | erreur | — |
| frontend/src/app/rules/components/rules.component.ts:1 | ngModel | 2 | loading, vide, erreur | getGuides |
| frontend/src/app/settings/components/settings.component.ts:1 | — | 1 | erreur | — |
| frontend/src/app/shared/components/confirm-dialog.component.ts:1 | — | 0 | loading | — |
| frontend/src/app/shared/components/data-table.component.ts:1 | ngModel | 3 | loading, vide | — |
| frontend/src/app/shared/components/empty-state.component.ts:1 | — | 0 | vide | — |
| frontend/src/app/shared/components/loading.component.ts:1 | — | 0 | loading | — |
| frontend/src/app/shared/components/modal.component.ts:1 | — | 3 | loading | — |
| frontend/src/app/shared/components/page-header.component.ts:1 | — | 0 |  | — |
| frontend/src/app/shared/components/stat-card.component.ts:1 | — | 0 |  | — |
| frontend/src/app/shared/components/toast-container.component.ts:1 | — | 1 | erreur | — |
| frontend/src/app/universities/components/universities.component.ts:1 | ngModel | 7 | loading, vide, erreur | createUniversity, deleteUniversity, getCities, getCitiesByCountry, getCountries, getUniversities, updateUniversity |
| frontend/src/app/universities/components/university-detail.component.ts:1 | — | 0 | loading, vide, erreur | getUniversity |

### Complements des services HTTP generiques

FileUploadService (frontend/src/app/core/services/file-upload.service.ts:29 et :40) construit aussi deux HttpRequest POST vers un endpoint fourni en argument, puis appelle http.request. Ces deux chemins dynamiques ne sont pas inclus dans les 128 expressions HttpClient get/post/put/delete/patch recensees. Leur cible ne peut pas etre deduite du service seul ; aucun contrat multipart multiple files correspondant n'est etabli. SearchService appelle GET /search, sans controleur correspondant ; searchInstant ignore son argument query et ecoute uniquement le Subject. Le champ de recherche global du layout est lie a q sans declenchement de ce service.

Verification finale : rapport UTF-8 lisible, aucune occurrence du mot de passe bootstrap dans le rapport. Git backend conserve la modification preexistante application-dev.yml et ne montre comme nouvel artefact que frontend/AUDIT_GLOBAL_ORIENTIA.md.
