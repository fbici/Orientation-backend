# Orientia ? Audit et finalisation du backoffice

8 novembre 2026. Branche main, base 019c9b1fd9f2ef4a060a5fda968a411b0d8275fa.
Les corrections ci-dessous sont locales, non commit?es. Aucun push ni d?ploiement effectu?.

## R?sultat et p?rim?tre

Le backoffice existant a ?t? conserv?. Les tableaux partag?s, erreurs, pagination,
notifications et contrats administratifs ont ?t? corrig?s. La compilation et les
suites automatis?es passent. Certaines fonctions restent indisponibles c?t? serveur :
l'interface le signale et d?sactive leurs actions plut?t que simuler une r?ussite.
Ce r?sultat ne constitue pas une validation exhaustive des actions en production.
Le projet Flutter et ses modifications pr?existantes n'ont pas ?t? modifi?s.

## Modules et validation

| Module | Probl?me identifi? | Correction | Test effectu? | Statut |
|---|---|---|---|---|
| Tableaux partag?s | Tri num?rique lexical, z?ro masqu?, filtre vide erron?, navigation pendant chargement | Tri naturel/num?rique, valeurs nulles distingu?es, ?tat vide filtr?, protections et libell?s clavier | Tests DOM et unitaires | Corrig? |
| Modales | Confirmation/fermeture pendant sauvegarde | Verrouillage, r?le dialog, libell?s accessibles | Tests DOM | Corrig? ; focus trap/Escape non valid?s |
| Authentification/r?les | Refus de r?le redirigeait vers login | Retour vers profil pour utilisateur connect? ; login pour anonyme | Guards + tests auth existants, session locale synth?tique | Corrig? ; production non test?e |
| Utilisateurs | Recherche ignor?e, pagination absente, password exig? ? la modification, r?les absents du DTO | Recherche serveur, pagination, DTO UpdateUserRequest distinct sans password/role, vrais codes de r?les | Tests Angular, validation Java, ouverture r?elle de Modifier | Corrig? pour modification ; cr?ation backend indisponible |
| Administration | Boutons d'acc?s sans action, erreurs silencieuses | Navigation vers utilisateurs et erreurs visibles | Compilation, navigation locale | Corrig? ; aper?u limit? aux premi?res lignes |
| Organisations | Code/coordonn?es perdus ? l'?dition, statut toujours actif, pagination absente | Code explicite, conservation coordonn?es, statut et pagination | Tests Angular et Java, ?cran local | Corrig? ; pays non persist? par l'entit?, champ d?sactiv? |
| Tenants | currentUsers mal mapp?, pagination et erreurs absentes | Mapping DTO, statuts, validation, pagination | Tests requ?tes API, build | Corrig? ; CRUD complet non ex?cut? en navigateur |
| R?les/?quipes/d?partements | Aucun contr?leur pour les actions annonc?es | Message permanent et actions d?sactiv?es avec motif | Revue contr?leurs + compilation | Bloqu? c?t? backend |
| Param?tres | Sauvegarde sans API et m?triques invent?es | Champs/actions d?sactiv?s, indisponibilit? explicite, URL r?elle d'environnement | ?cran local, compilation | Bloqu? c?t? backend pour sauvegarde |
| Profil | MFA sans endpoint, validation incompl?te | MFA d?sactiv? avec motif, validation noms et mots de passe | Tests auth existants, ?cran local | Corrig? partiellement ; modification r?elle non ex?cut?e |
| Notifications | Chargement artificiel, erreurs cach?es, compte limit? ? une page | Pagination r?elle, unread-count global, ?tats busy/erreur, protections | 4 tests de service, ?cran local | Corrig? |
| Dashboard | Graphiques incompatibles avec DTO et mesures fictives | S?ries dat?es r?elles, p?riodes actives, inconnus affich?s Non mesur? | 3 tests Angular, ?cran local | Corrig? ; KPI non instrument?s restent indisponibles |
| Monitoring/audit | Mauvaise structure JSON, CPU/DB/latence fictifs | Mapping r?el, CPU JVM si disponible, connexion DB valid?e, audit pagin? | Tests Java DB UP/DOWN, ?cran local | Corrig? ; compteurs API/logs applicatifs non expos?s |
| Universit?s/d?tails | ?dition pays/ville incoh?rente et erreur cach?e | Mapping plat pays/ville, erreur visible, accessibilit? | 2 tests Angular, navigateur bureau et 390 px | Partiel : r?f?rentiel villes indisponible observ? localement |
| Campus/facult?s/programmes/bourses/r?f?rentiels | Contrats existants ? pr?server | Revue routes/services/DTO ; composants et styles partag?s am?lior?s | Build, suites existantes | Revu ; pas de CRUD navigateur exhaustif |
| Guides/documents/revue | Actions m?tier ? pr?server | Revue contrats, am?liorations communes | Tests existants, ?cran Documents local | Revu ; aucun import r?el en production |
| Imports/r?gles | ?tats vides et acc?s ? distinguer des succ?s m?tier | Revue endpoints et comportements existants | Tests existants, ?crans locaux | Partiel : une alerte sur R?gles dans la base isol?e |
| Recommandations/d?cisions/intelligence | Parcours m?tier et exports ? conserver | Revue DTO/routes ; aucun endpoint invent? | Tests existants et build | Revu ; chaque action non ex?cut?e |
| Knowledge/recherche | Pr?servation des parcours existants | Revue des routes, libell?s accessibles cibl?s | Build et tests existants | Revu ; E2E complet restant |
| Analytics/rapports | Agr?gats non disponibles ; service rapports renvoie 503 explicitement | Absence de donn?es fictives ; page rapports utilise les d?cisions existantes | Revue backend et compilation | Limitation backend maintenue |

## Contrats et fichiers

- Utilisateurs : mise ? jour s?par?e de la cr?ation, sans mot de passe artificiel ni changement de r?le/tenant pr?tendu. Les validations et autorisations serveur existantes sont conserv?es.
- Audit : appels corrig?s vers GET `/backoffice/audit`.
- Rapports : m?thode corrig?e vers GET `/backoffice/reports/generate` avec param?tres type/format ; l'absence d'agr?gateur serveur reste explicite.
- Notifications : liste pagin?e et compteur `/unread-count`, aucune temporisation simul?e.
- Dashboard/monitoring : indicateurs non instrument?s nullables ; m?moire/threads r?els, CPU disponible selon JVM et DB test?e par isValid. Aucun nouveau chemin API.
- Organisation : pays/industrie pr?sents dans le DTO mais absents de l'entit? ; aucune migration invent?e. Le pays est d?sactiv? dans le formulaire.
- Boutons non raccordables : cr?ation administrateur, gestion r?les/?quipes/d?partements, param?tres et MFA d?sactiv?s avec explication. Les m?thodes existantes restent pr?sentes.
- Styles : d?filement horizontal contenu dans les tableaux, actions/formulaires adapt?s aux petites largeurs, focus visible et pr?f?rence de mouvement r?duit.

Inventaire statique des sources, routes et actions : BACKOFFICE_INVENTAIRE_ACTIONS.json.
Cet inventaire ne prouve pas l'ex?cution de chaque bouton.

## Tests et preuves

- Angular : **67 tests r?ussis**, ChromeHeadless ; build production r?ussi.
- Java : **202 tests, 34 suites, z?ro ?chec** sur base PostgreSQL de test isol?e ; Maven verify r?ussi. L'ancien XML MobileFlutterJourneyIT non ex?cut? dans cette campagne est exclu du d?compte.
- Packaging final : Maven package r?ussi apr?s copie du dernier build Angular. Index du JAR identique octet pour octet au build ; **135 fichiers statiques embarqu?s**. Cette ?tape utilise skipTests apr?s le verify complet.
- Navigateur Chrome r?el : 11 routes locales, aucune exception JavaScript, fen?tre Modifier utilisateur ouverte/ferm?e sans sauvegarde, ?cran Universit?s ? 390 ? 844 sans d?bordement de page. Ce contr?le v?rifie chargement/navigation et quelques interactions, pas chaque action m?tier.
- La capture mobile r?v?le une alerte ? R?f?rentiel des villes indisponible ? ; cette anomalie demeure ? diagnostiquer. Le comptage initial des alertes du harness n'a pas captur? ce message retard? : la capture visuelle fait foi. Une alerte est ?galement relev?e sur R?gles ; aucune r?ussite m?tier n'en est d?duite.
- Contr?le SHA-256 : **83 migrations SQL inchang?es**. Aucun changement de configuration de production ou de secrets intentionnel.
- Gitleaks sur la copie des fichiers modifi?s/ajout?s : z?ro d?tection, sortie 0. Ce contr?le ne remplace pas un nouvel audit de tout l'historique.
- Base PostgreSQL priv?e arr?t?e apr?s les tests ; donn?es de test conserv?es.
- Docker : Dockerfile examin?, construction d'image non ex?cut?e car CLI Docker indisponible.
- Avertissements existants : budgets CSS login/layout et CommonJS sockjs ; PostgreSQL local 18.4 plus r?cent que la version officiellement valid?e par Flyway embarqu?. Render PostgreSQL 16 n'a pas ?t? interrog?.
- Aucun e-mail r?el envoy?, aucune base de production modifi?e, aucun identifiant de production utilis?. Tests navigateur avec administrateur synth?tique et base priv?e exclusivement.
- Logs de validation et capture conserv?s dans le r?pertoire temporaire orientia-backoffice-audit et les journaux orientia-bo-*. Aucun jeton ou mot de passe n'est inclus dans ce rapport.

## Restes et recommandations avant publication

1. Diagnostiquer le r?f?rentiel villes et l'alerte R?gles ? partir d'une session de test autoris?e, avec statuts HTTP et journal serveur ; ne pas les consid?rer valid?s.
2. Impl?menter s?par?ment les fonctions serveur manquantes (cr?ation admin, r?les, ?quipes, d?partements, param?tres, MFA) avant d'activer les boutons correspondants.
3. D?finir et instrumenter les KPI manquants. Conserver Non mesur? tant que la mesure n'existe pas.
4. Valider les CRUD/imports/exports et les restrictions de chaque r?le sur une pr?production avec donn?es et comptes autoris?s. La session locale administrateur ne valide pas les autres r?les.
5. V?rifier les acc?s directs/rafra?chissements des routes Angular et les parcours clavier complets ; les tests navigateur r?alis?s utilisent la navigation SPA depuis la racine.
6. Examiner ces diff?rences avant tout commit/push ou red?ploiement. Aucun push automatique n'a ?t? effectu?.

## Liste des fichiers modifi?s ou ajout?s

- `frontend/AUDIT_BACKOFFICE_AVANT_CORRECTIONS.md`
- `frontend/AUDIT_BACKOFFICE_FINALISATION.md`
- `frontend/BACKOFFICE_INVENTAIRE_ACTIONS.json`
- `frontend/BACKOFFICE_PREUVES.json`
- `frontend/src/app/administration/components/administration.component.ts`
- `frontend/src/app/administration/components/departments.component.ts`
- `frontend/src/app/administration/components/organizations.component.spec.ts`
- `frontend/src/app/administration/components/organizations.component.ts`
- `frontend/src/app/administration/components/roles.component.ts`
- `frontend/src/app/administration/components/teams.component.ts`
- `frontend/src/app/administration/components/tenants.component.ts`
- `frontend/src/app/administration/components/users.component.spec.ts`
- `frontend/src/app/administration/components/users.component.ts`
- `frontend/src/app/core/guards/access.guards.spec.ts`
- `frontend/src/app/core/guards/role.guard.ts`
- `frontend/src/app/core/services/api.service.ts`
- `frontend/src/app/core/services/backoffice-api.spec.ts`
- `frontend/src/app/core/services/notification.service.spec.ts`
- `frontend/src/app/core/services/notification.service.ts`
- `frontend/src/app/dashboard/components/dashboard.component.spec.ts`
- `frontend/src/app/dashboard/components/dashboard.component.ts`
- `frontend/src/app/intelligence/components/knowledge-browser.component.ts`
- `frontend/src/app/monitoring/components/monitoring.component.ts`
- `frontend/src/app/notifications/components/notifications.component.ts`
- `frontend/src/app/profile/components/profile.component.ts`
- `frontend/src/app/settings/components/settings.component.ts`
- `frontend/src/app/shared/components/data-table.component.ts`
- `frontend/src/app/shared/components/functional-ui.spec.ts`
- `frontend/src/app/shared/components/modal.component.ts`
- `frontend/src/app/shared/components/toast-container.component.ts`
- `frontend/src/app/universities/components/universities.component.spec.ts`
- `frontend/src/app/universities/components/universities.component.ts`
- `frontend/src/styles.scss`
- `src/main/java/com/orientation/orientationapp/backoffice/dashboard/dto/response/DashboardResponse.java`
- `src/main/java/com/orientation/orientationapp/backoffice/dashboard/service/impl/DashboardServiceImpl.java`
- `src/main/java/com/orientation/orientationapp/backoffice/monitoring/dto/response/MonitoringResponse.java`
- `src/main/java/com/orientation/orientationapp/backoffice/monitoring/service/impl/MonitoringServiceImpl.java`
- `src/main/java/com/orientation/orientationapp/modules/admin/controller/UserController.java`
- `src/main/java/com/orientation/orientationapp/modules/admin/dto/request/UpdateUserRequest.java`
- `src/main/java/com/orientation/orientationapp/modules/admin/service/UserAdminService.java`
- `src/main/java/com/orientation/orientationapp/modules/admin/service/impl/UserAdminServiceImpl.java`
- `src/test/java/com/orientation/orientationapp/security/BackofficeContractsTest.java`
- `src/test/java/com/orientation/orientationapp/security/BackofficeOrganizationTest.java`
