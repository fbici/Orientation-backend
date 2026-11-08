# ORIENTIA — Sprint F2 : backend métier et intelligence documentaire

Date : 6 novembre 2026. Périmètre : backend Spring Boot et Back Office Angular. F3 et Flutter restent hors modification.

## A. Résumé exécutif

La chaîne existante fonctionne désormais selon le parcours **source stockée → extraction réelle → brouillons persistés → revue humaine → publication transactionnelle → catalogue, graphe et recherche**. Une extraction ne publie plus automatiquement des règles ou des établissements.

Les preuves reposent sur des documents **synthétiques, explicitement marqués non officiels** : aucun guide officiel exploitable n'a été trouvé dans les répertoires inspectés. Aucun contenu synthétique n'a été ajouté au catalogue principal. Les migrations et tests PostgreSQL utilisent exclusivement la base privée `orientia_f1`, sur boucle locale et dans des schémas F2 distincts.

**Suite complète confirmée : 96 tests backend réussis**, sans échec, erreur ni test ignoré, dont le changement de version de guide et la recherche historique. **28 tests Angular réussis** et compilation de production réussie. Les cinq scénarios documentaires ont ensuite été réexécutés avec succès après le contrôle final pays/année des règles.

L'audit npm passe de **66 à 48 alertes**, dont **zéro critique**. Les dépendances de production conservent 12 alertes, dont cinq élevées : ce sprint ne constitue pas une autorisation de mise en production.

## B. Modules corrigés

| Module | Comportement réel |
|---|---|
| Universités | Liste paginée, recherche, pays/ville/type/statut, détail DTO, création, modification et désactivation avec permissions et tenant. |
| Campus et facultés | CRUD, rattachements contrôlés, listes paginées et filtres de parents. |
| Formations | Faculté/campus/université, diplôme, durée, langue, domaine, année, frais et devise ; disponibilité explicite. |
| Bourses | Pays, année, type, organisme, couverture, montant/devise, dates, statut et rattachements facultatifs. Critères publiés accessibles. |
| Guides | Catalogue des guides publiés, versions, dates, document source et empreinte ; filtrage par tenant, pays et année. |
| Documents | Stockage préalable, version et SHA-256, pages, extraction, revue, corrections, rejet, publication et audit. |
| OCR | PDFBox pour le texte PDF ; Tesseract local pour les scans, PNG et JPEG. Aucun décodage des octets PDF comme du texte. |
| Extraction | Détection prudente par libellés, passages et pages conservés ; données absentes laissées inconnues ; ajout manuel à partir d'un passage exact. |
| Graphe/index | Entités existantes `KnowledgeNode`, `KnowledgeRelation`, `SearchIndex` réutilisées ; sources, version et tenant enregistrés. |
| Imports | Parseurs CSV/XLSX existants raccordés ; validation, insertion atomique de brouillons, historique, fichier original et lignes conservés. |
| Angular | Catalogues, documents/revue, imports, guides, règles, recherche et graphe raccordés aux API réelles. |

Les écritures manuelles de catalogue restent **à revoir**. Elles ne constituent pas une validation documentaire et retirent la fiche modifiée de l'index courant. Une publication rétablit la validation avec ses citations. Les données héritées ne sont pas déclarées vérifiées par défaut.

Une bourse avec dates insuffisantes retourne `openForApplications: null`. Avec une période et une échéance connues mais dépassées, elle retourne `false`, même si son ancien statut est `ACTIVE`.

## C. API et contrats

Les chemins ci-dessous sont relatifs au contexte Spring : **/api/v1 en développement**, racine en production selon les profils existants. Aucun déploiement n'a été effectué.

### Catalogue

- `GET /universities|campuses|faculties|programs|scholarships` : page Spring `{content,totalElements,totalPages,number,size,...}`, page à partir de 0, taille de 1 à 100.
- `GET /{ressource}/{id}` : DTO explicite, sans graphe JPA.
- `POST /{ressource}` : création (201).
- `PUT /{ressource}/{id}` : modification contrôlée.
- `DELETE /{ressource}/{id}` : **désactivation**, sans suppression physique.
- `GET /{ressource}/{id}/sources` : citations publiées visibles pour la fiche.

Filtres applicables selon la ressource : `search,countryId,cityId,universityId,campusId,facultyId,academicYearId,status,type,studyField,language,active`. Les références de parents doivent appartenir au périmètre accessible. Les DTO exposent `verificationStatus` ; les lecteurs sans permission de gestion ne reçoivent que les fiches vérifiées et leurs parents vérifiés.

Écritures principales :

- Université : `name,countryId,cityId` requis ; `type,status,shortName,address,phone,email,website,foundedYear,description,sourceUrl` facultatifs.
- Campus : `name,universityId,cityId` requis.
- Faculté : `name,campusId` requis.
- Formation : `name,facultyId,type,duration` requis ; langue, domaine, diplôme, année, frais/devise et source facultatifs.
- Bourse : `name,countryId,academicYearId,type,status` requis ; montant exige une devise, dates cohérentes.

### Documents et revue

| Méthode et chemin | Contrat |
|---|---|
| POST /documents/upload | Multipart `file`, métadonnées facultatives ; document réellement stocké, état UPLOADED ; réponse existante 200. |
| POST /documents/{id}/process | Traite exclusivement les octets stockés et vérifie le SHA-256 ; résultat READY_REVIEW ou erreur HTTP réelle. |
| GET /documents/{id}/review | Source/version/empreinte, pages, faits, états et éventuelle erreur de traitement. |
| GET /documents/{id}/pages | Texte page par page et confiance technique d'extraction. |
| GET /documents/{id}/pages/{page}/preview | Aperçu PNG contrôlé, sans cache ; page numérotée à partir de 1. |
| GET /documents/{id}/extractions | Faits avec valeurs initiales et corrigées, passage, page, version optimiste et audit de revue. |
| POST /documents/{id}/extractions | `{kind,pageNumber,passage,values}` ; le passage doit exister littéralement sur la page. |
| PUT /documents/{id}/extractions/{factId} | `{version,status,values,kind?,note?}` ; statuts TO_REVIEW, VALIDATED ou REJECTED. |
| POST /documents/{id}/publish | `{countryId,academicYearId,guideTitle,effectiveDate,expiryDate?,publisher?,factIds[]}`. |

La revue conserve `originalValues`, `originalKind`, le passage immuable, `reviewedBy`, `reviewedAt` et la note. Une version optimiste périmée renvoie 409. Un fait déjà publié est immuable.

Pour une création liée dans le même document, `universityFactId,campusFactId,facultyFactId,programFactId,scholarshipFactId` désignent un autre fait de la même version source. Pour un parent existant, utiliser son `...Id` ; les deux formes sont exclusives. `existingEntityId` permet de choisir explicitement la fiche cible d'une université, d'un campus, d'une faculté, d'une formation ou d'une bourse.

### Guides, critères et références

- `GET /guides`, `GET /guides/{id}`, `GET /guides/{id}/versions`.
- `GET /admission-criteria?programId=&guideVersionId=&page=&size=` et `GET /admission-criteria/{id}`.
- `GET /scholarships/{id}/criteria`.
- `GET /academic-years`, `GET /subjects?countryId=&academicYearId=`, `GET /grade-scales`.
- Référentiels géographiques existants : `/locations/countries`, `/locations/cities`, `/locations/countries/{id}/cities`.

Les règles portent le programme, la version du guide, l'année, le fait source, l'opérateur, les valeurs, le caractère obligatoire, le groupe logique et le barème explicite. La provenance inclut le document et son passage.

### Connaissances et recherche

- `GET /knowledge/search?query=&type=&limit=&page=&countryId=&programId=&universityId=&academicYearId=&sourceId=`.
- `GET /knowledge/graph`, `GET /knowledge/statistics`, `GET /knowledge/capabilities`.
- `POST /knowledge/query` : `{query}`, réponse `{query,response,results,searchMode,embeddingsAvailable}`.
- `POST /knowledge/rebuild` : reconstruit réellement l'index du périmètre autorisé.
- Entrées historiques `/intelligence/process`, `/intelligence/knowledge` et recherche assistée : raccordées au même stockage et aux mêmes services.

Le mode annoncé est **TEXT**, `embeddingsAvailable:false`. L'absence de modèle de similarité vérifié produit une réponse 503 explicite ; aucun score vectoriel n'est inventé.

### Imports tabulaires

- `POST /imports` : multipart `file,dataType,countryId?,academicYearId?,dryRun?`.
- Types raccordés : `UNIVERSITIES,FACULTIES,PROGRAMS,SCHOLARSHIPS`.
- CSV UTF-8 ou XLSX, **2 Mio / 1 000 lignes / 100 colonnes**, cellules bornées ; formules Excel refusées.
- `dryRun=true` valide les références et annule toutes les écritures, sans historique de succès fictif.
- `GET /imports`, `/imports/history`, `/imports/versions` : historique paginé et isolé.
- `GET /imports/{id}`, statistiques et qualité : résultats effectifs de l'import.
- `GET /imports/catalog` décrit capacités et limites.
- L'ancien rollback simulé renvoie désormais 409 : il n'annule pas des données déjà enregistrées.

Un import réussi enregistre des **brouillons non officiels**. Il ne crée ni règle publiée ni index de connaissances. La publication officielle reste réservée à une source documentaire revue.

## D. Pipeline documentaire

1. **Autoriser et stocker** : utilisateur réel, permission, tenant, signature, taille, nom serveur, SHA-256 et version persistée.
2. **Marquer PROCESSING** dans une transaction dédiée ; une tentative concurrente est refusée.
3. **Extraire réellement** : PDFBox lit les pages textuelles ; Tesseract traite les pages scannées et les images. Un en-tête textuel court avec corps image déclenche aussi l'OCR.
4. **Persister les brouillons** : pages, texte agrégé et faits dédupliqués par version/type/page/passage ; aucune publication métier.
5. **Revoir** : aperçu, valeurs initiales, correction structurée, rattachement aux fiches ou autres faits, note, rejet ou validation avec auteur réel. Références existantes et URL contrôlées.
6. **Publier** : uniquement les faits validés ; résolution des parents, cohérence de pays/année et permissions, guide/version, écritures métier, relations, graphe, index et métadonnées de publication **dans la même transaction**.
7. **Rechercher** : données vérifiées, tenant et version de guide filtrés ; les citations accompagnent les résultats.

Un échec OCR conserve l'état FAILED et une erreur contrôlée. Une erreur de publication annule les écritures métier, graphe et index du lot. Une sélection partielle laisse PARTIALLY_PUBLISHED tant que tous les faits non rejetés ne sont pas publiés.

Le retraitement de la même source conserve les décisions humaines. Republier les mêmes faits retourne des éléments réutilisés. Une nouvelle source peut produire une nouvelle version du même guide en réutilisant les fiches métier ; l'ancienne version est archivée.

### Installation et configuration OCR

PDFBox **3.0.8** est une dépendance Maven. Tesseract n'était pas installé sur cette machine. Pour les tests, une distribution Windows **5.5.3.20260724** a été extraite dans un répertoire temporaire, sans installation globale ni changement du PATH ; son empreinte de téléchargement a été vérifiée. Les modèles locaux `fra` et `eng` sont présents.

Configurer un exécutable Tesseract et les modèles sur chaque environnement cible ; le chemin temporaire de test n'est pas une configuration de déploiement. Propriétés Spring :

```properties
app.ocr.tesseract-command=C:/outils/Tesseract-OCR/tesseract.exe
app.ocr.tessdata-dir=C:/outils/Tesseract-OCR/tessdata
app.ocr.languages=fra+eng
app.ocr.timeout-seconds=45
app.ocr.max-pages=200
app.ocr.render-dpi=200
app.ocr.max-page-pixels=16000000
```

L'OCR est gratuit et local, sans API payante obligatoire. Limites : 10 Mio par document, nombre de pages et dimensions bornés, sous-processus sans shell et avec timeout. Une confiance de lecture technique n'est pas un score de vérité académique ; aucun score global de qualité métier fictif n'est enregistré.

Sources techniques : [PDFBox](https://pdfbox.apache.org/download.cgi), [sécurité PDFBox](https://pdfbox.apache.org/security.html), [installation Tesseract](https://tesseract-ocr.github.io/tessdoc/Installation.html), [usage Tesseract](https://tesseract-ocr.github.io/tessdoc/Command-Line-Usage.html), [distribution Windows](https://github.com/UB-Mannheim/tesseract/wiki).

## E. PostgreSQL

Les migrations historiques sont conservées. **73 fichiers de migration préexistants, jusqu'à V71 inclus, sont inchangés par comparaison SHA-256 avec l'instantané F2.**

| Nouvelle migration | Objet |
|---|---|
| V72__academic_catalog_scope.sql | Tenant, champs de catalogue et de bourses, année/source, indicateurs de vérification. |
| V73__document_review_provenance.sql | Faits extraits, revue humaine, provenance et publication ; liens vers critères, documents et versions ; portée du graphe/index. |
| V74__academic_uniqueness_per_tenant.sql | Unicité université/guide/bourse par tenant ; remplace les anciennes contraintes, **sans supprimer de lignes**. Utilise NULLS NOT DISTINCT, donc PostgreSQL 15+ requis. |
| V75__knowledge_relationship_filters.sql | Liens programme/université et nœud/type dans l'index pour les filtres réels. |
| V76__tabular_import_evidence.sql | Octets originaux et lignes JSON des imports bornés ; index d'historique tenant/date. |

Les versions sources ont une empreinte et un identifiant réels. Les faits publiés référencent leurs entités et la version du guide. Les contraintes refusent notamment une publication sans validation/auteur ou sans référence d'entité/version.

Validation effectuée avec **PostgreSQL 18.4**, Flyway et Hibernate `ddl-auto=validate`, dans les schémas privés F2. Aucune migration n'a été exécutée sur la base principale. Flyway avertit que cette version de PostgreSQL est plus récente que sa plage officiellement testée ; le passage local des migrations et de Hibernate ne remplace pas une vérification de compatibilité sur l'environnement cible.

La transition d'une base déjà migrée reste à répéter sur un clone avec sauvegarde : examiner notamment l'historique F1 et sa migration V63.1 avant toute intervention réelle. Aucun `clean`, `repair`, effacement de données ou changement de migration historique n'a été effectué.

## F. Angular

Les routes existantes sont conservées et complétées : universités, campus, facultés, formations, bourses, guides, documents et revue, imports, règles, connaissances et recherche assistée. Les anciennes entrées Intelligence réutilisent ces écrans actifs.

Fonctions raccordées : recherche serveur, filtres et pagination, création/modification/désactivation, upload vers le vrai endpoint, extraction explicite, aperçu PNG et texte page par page, correction et choix des rattachements, association à une fiche existante, décisions versionnées, sélection et publication, consultation des sources et des guides, imports réels avec prévalidation.

Les composants réutilisent les classes du design existant, ses modales et confirmations. Ils affichent les chargements, listes vides et erreurs réelles. Une publication refusée n'affiche pas de succès. Les aperçus précédents sont annulés lors d'un changement de page et les URL Blob sont libérées. Le WebSocket de développement pointe maintenant sur `/api/v1/ws`.

Les écrans F2 ne présentent plus de tableaux métier statiques comme résultats officiels. Le catalogue Bénin codé en dur a été remplacé par les données vérifiées. Sa simulation de décision candidat renvoie explicitement 501, en attendant F3.

Limite de preuve : tests Angular dans Chrome Headless et compilation de production ; pas de recette humaine exhaustive ni de scénario navigateur complet contre un backend déployé.

## G. Tests

### Résultats confirmés

- Backend : **96 tests réussis**, 0 échec, 0 erreur, 0 ignoré ; inclut les tests F1 et l'OCR réel avec exécutable Tesseract.
- Angular : **28 tests réussis**, Chrome Headless 156.
- Build Angular production : réussi ; avertissements préexistants de budgets CSS login/layout et CommonJS SockJS.
- Changement de version de guide : **réussi**, fiches réutilisées et citations de la version archivée retrouvées par filtre source.
- Vérification ciblée supplémentaire après contrôle pays/année des règles : **5 scénarios réussis**, sans échec, erreur ni test ignoré ; refus inter-pays et rollback vérifiés.
- `git diff --check` : réussi ; seuls avertissements Git sur la normalisation LF/CRLF.

Les tests du parseur vide et de l'orchestrateur ont été adaptés au contrat réel : fichier vide refusé à l'import, parseur vide retournant une liste vide ; aucune annulation fictive. Une régression intermédiaire sur ce contrat CSV a été détectée puis corrigée. Aucun test de sécurité F1 n'a été supprimé ou désactivé.

### Reproduction isolée

Configurer une base de test locale dédiée `orientia_f1`, un utilisateur de test et un schéma neuf ; fournir son mot de passe via `F1_TEST_DB_PASSWORD`, sans l'inscrire dans une commande ou dans Git. Utiliser Java 21 et le Maven Wrapper.

```powershell
.\mvnw.cmd "-Df1.jdbc.url=jdbc:postgresql://127.0.0.1:59745/orientia_f1" "-Df1.jdbc.schema=f2_reproduction" "-Df2.tesseract=C:/outils/Tesseract-OCR/tesseract.exe" "-Df2.tessdata=C:/outils/Tesseract-OCR/tessdata" test
```

Le port ci-dessus est celui de l'instance privée utilisée ici ; adapter au port de sa propre instance isolée. Les tests PostgreSQL refusent une URL qui ne cible pas `127.0.0.1` et `orientia_f1`. Sans les propriétés de test isolé/OCR, certains tests sont conditionnels : un lancement sans paramètres ne prouve pas leur exécution.

```powershell
cd frontend
npm run build:prod
$env:CHROME_BIN='C:\Program Files (x86)\Google\Chrome\Application\chrome.exe'
node node_modules/@angular/cli/bin/ng.js test --watch=false --browsers=ChromeHeadless
npm audit
npm audit --omit=dev
```

Journaux de travail : `%TEMP%/orientia-f2-*.log`. Les rapports Surefire sont dans `target/surefire-reports`. Les journaux de tests peuvent contenir des sessions synthétiques : ils ne doivent pas être publiés comme pièces publiques. La synthèse de clôture, sans secret ni session de test, est enregistrée dans `SPRINT_F2_PREUVES.json`.

## H. Sécurité

Permissions métier précises en plus de l'authentification et des gardes HTTP : gestion documentaire, lecture des documents, création/mise à jour/publication de guides, édition/publication de règles, gestion des universités/formations/bourses, imports. Le rôle administrateur ne remplace pas ces permissions.

Les tenants proviennent de l'utilisateur ou du document source, jamais d'un choix arbitraire du client. Les parents existants, sources, aperçus, imports, critères, graphe et résultats de recherche sont contrôlés. Les tests vérifient refus inter-tenant, refus candidat en écriture, passage fabriqué, données incomplètes et immutabilité après publication.

Les fondations F1 restent testées : sessions et refresh, révocation, comptes inactifs, verrouillage, bootstrap, stockage, CORS et STOMP. Les notifications de progression documentaire sont personnelles.

### Audit npm

| Périmètre | Avant F2 | Après correctifs compatibles |
|---|---:|---:|
| Total | 66 | 48 |
| Critiques | 4 | 0 |
| Élevées | 28 | 15 |
| Modérées | 32 | 31 |
| Faibles | 2 | 2 |
| Production, total | 12 | 12 |
| Production, élevées | 5 | 5 |

Actions : `npm audit fix --ignore-scripts`, puis overrides correctifs **piscina ^4.9.4** et **pacote ^20.0.1**. Pacote 20.0.1 utilise la branche corrigée de tar ; aucune substitution directe de tar 6 par une API majeure différente n'est conservée. Arbre final vérifié avec tar 7.5.22. Ni `--force`, ni montée majeure d'Angular.

Les cinq paquets runtime élevés signalés par l'audit sont `@angular/common,compiler,core,router,service-worker` dans leur branche 19. Le correctif proposé par npm passe à une majeure ultérieure. Prévoir une migration Angular planifiée, avec alignement CLI/CDK/Material/NgRx/TypeScript/Node et tests complets, dans un chantier distinct ; ne pas appliquer automatiquement la proposition npm.

## I. Démonstration et preuves

`AcademicWorkflowIntegrationTest` utilise de vrais comptes synthétiques, mots de passe chiffrés et login JWT, sans remplacer les services métier par des mocks.

Le PDF de démonstration porte la mention **F2 SYNTHETIC TEST GUIDE — NOT OFFICIAL**. Il décrit une université, un campus, une faculté, trois licences, une moyenne minimale de 12,5/20 et une bourse historique de 1 000 XOF.

Le scénario confirmé :

1. Connexion d'un administrateur autorisé.
2. Upload réel et conservation d'une version source avec SHA-256.
3. Lecture PDF, pages et faits persistés ; aucun programme créé à ce stade.
4. Revue avec pays, ville, parents, année, caractère obligatoire et barème explicitement renseignés.
5. Refus d'une validation incomplète et d'un passage absent ; refus d'un autre tenant et d'un candidat en publication.
6. Publication ; trois formations supplémentaires exactement, critères liés au bon guide et relations du graphe persistées.
7. Recherche des trois formations par texte, année et source ; passages, pages et statut de revue présents ; résultat vide pour l'autre tenant.
8. Aperçu PNG réel et consultation des versions/provenances.
9. Republication et retraitement sans ajout de formations ; faits publiés non modifiables.
10. Échec volontaire d'une dépendance : aucun ajout métier, graphe, index ni marqueur de publication partiel.
11. Imports CSV et XLSX réels ; prévalidation sans écriture, conservation de l'original, erreur de référence annulant tout le lot.
12. PDF invalide : état FAILED persisté, erreur explicite et aucune publication.
13. Bourse historique : candidatures fermées malgré son ancien statut ACTIVE.
14. Critère publié récupéré par le moteur existant : moyenne 13 acceptée, 11 refusée pour le seuil 12,5.

15. Nouvelle source du même guide : version 2 créée, anciennes versions archivées, aucun programme ajouté à nouveau, association explicite à la fiche universitaire existante et recherche des trois formations dans l'ancienne source réussie.

16. Publication d'un critère dans un guide d'un autre pays : refus 422 et rollback, puis publication dans le guide correct réussie.

## J. Préparation Flutter

Aucun fichier Flutter n'a été modifié. Les API métier disponibles pour F3/mobile sont les catalogues paginés, détails et citations, bourses et validité, guides et versions, critères publiés par programme/guide, référentiels et recherche textuelle filtrée.

Utiliser les UUID réels, respecter la portée tenant et traiter `null` comme inconnu. Ne pas transformer une fiche non vérifiée, un critère isolé, un résultat de recherche ou une ancienne bourse en décision d'admission/financement.

Les valeurs d'énumérations et contraintes sont celles des DTO `CatalogRequests`, `AdmissionRuleWrite` et `ScholarshipRuleWrite`. Les réponses d'erreur gardent un statut HTTP réel ; ne pas afficher de succès sur 409/422/503.

Le langage de règles existant est constitué d'énumérations, valeurs et groupes stockés en base, pas d'un DSL général complet. Les publications alimentent les repositories du moteur d'éligibilité existant. Ses approximations de règles de liste et de notes par matière ne passent plus automatiquement ; les champs/combinaisons non pris en charge restent à finaliser en F3. Le test du seuil numérique ne valide pas l'ensemble du moteur candidat.

## K. Problèmes restants et priorités

**P1 — avant production**

- Traiter les cinq vulnérabilités élevées runtime via une migration Angular planifiée ; les 48 alertes restantes ne sont pas neutralisées par le succès des builds.
- Répéter la transition Flyway sur un clone représentatif de la base cible ; vérifier version PostgreSQL, sauvegarde, historique F1 et V63.1.
- Installer/configurer durablement Tesseract et les modèles sur l'hôte cible. Le runtime temporaire prouve les tests locaux, pas un déploiement.
- Recette sur de vrais guides autorisés et représentatifs : tables complexes, scans dégradés, mises en page, abréviations, règles multi-lignes. L'extraction prudente ne prétend pas tout reconnaître ; la revue permet l'ajout de passages omis.
- Recette utilisateur complète des écrans et de leurs droits sur environnement isolé.

**P2 — exploitation documentaire**

- L'OCR reste synchrone et borné par page ; prévoir file de tâches, reprise d'un processus interrompu et suivi opérationnel pour les gros volumes.
- Les limites de pages/graphe/listes sont explicites ; les sélecteurs de référence chargent les premiers 100 résultats et proposent une recherche pour les catalogues.
- Les imports tabulaires ne publient pas officiellement. Ils ne couvrent pas tous les types historiques de DataType ; les types non raccordés sont refusés au lieu de prétendre importer.
- Le téléchargement du document original reste soumis à DOCUMENT_READ et au tenant. Aucune politique de partage public des fichiers privés n'a été inventée.

**Hors F2 — inventaire à reprendre en F3 ou chantier dédié**

- Recommandation candidat complète : règles par matière, diplômes/documents, listes, logique complexe, normalisation des barèmes, scores et explications.
- Services de chat (`ai/chat`), apprentissage, comparaison/export et certaines statistiques administratives (`backoffice/analytics`, `backoffice/monitoring`) hors écrans F2 comportent encore des implémentations partielles décrites dans l'audit global ; ne pas les présenter comme validés par ce sprint.
- Les anciens composants `intelligence/components/pipeline`, `knowledge-browser` et `smart-query` restent dans le dépôt ; leurs routes actives réutilisent désormais les écrans documentaires et de recherche réels. Ils ne constituent pas un second parcours validé.
- Pas de modèle vectoriel ni d'embeddings vérifiés ; recherche TEXT fonctionnelle et limite annoncée.
- Avertissements CSS et CommonJS déjà présents ; optimisation front distincte des corrections de fond.

## L. Verdict

**Sprint terminé et vérifié sur le périmètre F2, en environnement isolé.**

Le parcours documentaire, les API métier, les imports, le graphe, la recherche et le versionnement historique disposent de preuves d'exécution sur environnement isolé. La suite complète compte 96 tests réussis ; les cinq scénarios documentaires ont été réexécutés après le contrôle final pays/année, avec succès. Les 28 tests Angular et le build de production sont réussis.

Cette clôture technique ne signifie pas que la plateforme est prête pour la production : les points P1 ci-dessus restent nécessaires. F3 n'a pas été démarré.

L’instance PostgreSQL temporaire démarrée pour ces tests a été arrêtée après les vérifications ; ses données n’ont pas été supprimées.

Aucun commit, push, déploiement, migration sur base principale ou changement Flutter n'a été effectué.
