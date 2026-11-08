# ORIENTIA — SPRINT F3.2 — INTELLIGENCE AVANCÉE

Date : 6 novembre 2026. Périmètre : backend existant et adaptations administratives Angular. Aucun déploiement, commit, push, changement Flutter ou accès à la base principale.

## A. Résumé exécutif

Les implémentations factices ou indisponibles de simulation, comparaison, assistant et exports ont été remplacées par des calculs et instantanés issus des services métier. `PersistedRecommendationService.evaluate` factorise l'orchestration F3.1 : sélection des programmes vérifiés, règles publiées, éligibilité à trois états, scoring et classement de compatibilité. Il ne crée aucun résultat réel ; seuls les appels de génération explicite conservent les recommandations réelles.

Les simulations, comparaisons, évaluations de bourses et conversations sont conservées séparément dans `decision_records`. Les conversations sont privées à leur auteur, y compris vis-à-vis d'un administrateur pouvant consulter le dossier candidat.

**Sprint terminé et vérifié.** Les preuves chiffrées et contrôles de conservation sont disponibles dans [SPRINT_F3_2_PREUVES.json](SPRINT_F3_2_PREUVES.json).

## B. Simulations

`POST /recommendations/simulate` part du profil académique enregistré et confirmé. Il copie les notes et préférences avant d'appliquer les modifications. Aucun appel à la sauvegarde du candidat ni à la génération persistante des recommandations réelles.

Champs disponibles : `candidateId`, `academicYearId`, `programIds` (maximum 50), `modifiedGrades` (codes de matières configurés), `modifiedGeneralAverage`, `modifiedBacAverage` (alias contrôlé), `modifiedBacType`, `modifiedBacSeries`, `modifiedFields`, `modifiedCountries`, `modifiedCountry` (UUID de destination), `modifiedLanguages`, `modifiedLanguage`, `modifiedBudget`, `description`.

Les notes requièrent un barème explicite et des valeurs comprises dans ce barème. Un scénario sans guide applicable pour son année reçoit HTTP 422. Une destination modifie les préférences et ne transforme pas la nationalité ou le pays du candidat. Les pays doivent être identifiés par leurs UUID.

Réponse : enveloppe d'historique, avec `result.referenceProfile`, `simulatedProfile`, `modifications`, `before`, `after`, `differences`, `isSimulation: true` et avertissement hypothétique. Les différences contiennent les transitions d'éligibilité, le delta de score lorsqu'il est calculable et les critères explicatifs. Les inconnues ne deviennent pas des notes ou des avis favorables.

Exemple :
```json
{"modifiedGeneralAverage":13,"modifiedGrades":{"MATH":14}}
```

## C. Comparaisons

`POST /ai/compare/programs` accepte `candidateId` facultatif et `programIds` : deux à six UUID distincts. Le comparateur réutilise le catalogue et le moteur F3.1, puis évalue les bourses selon chaque programme cible.

Chaque ligne conserve les informations publiées du programme, la hiérarchie université/campus/faculté, les frais dans leur devise originale, l'année, l'éligibilité, les critères, les scores, les bourses et les citations. Les différences sont calculées depuis ces données. Les valeurs inconnues restent nulles. Aucune conversion monétaire, période de frais ou probabilité d'admission n'est supposée.

Les routes historiques `GET /ai/compare/programs?programA=…&programB=…` et `GET /ai/compare/universities?universityA=…&universityB=…` utilisent également les données réelles. Une comparaison universitaire reste factuelle, sans éligibilité globale ni classement officiel.

## D. Bourses

Le service réutilise `Scholarship`, `ScholarshipCriterion`, les faits documentaires validés et `RealEligibilityEngine`.

L'ouverture est distincte de l'éligibilité : vérification humaine, activation, statut, dates de validité, échéance et places restantes connues sont contrôlés. Une échéance est inclusive. Des dates manquantes produisent une disponibilité inconnue ; une bourse expirée, future, inactive, épuisée ou non vérifiée n'est pas présentée comme ouverte.

Les conditions obligatoires sont regroupées par opérateurs AND/OR explicites, selon la même logique à trois états que les admissions. Sans critères publiés applicables, le résultat reste `INSUFFICIENT_DATA`. Une condition connue non satisfaite peut produire `NOT_ELIGIBLE`, même si une autre information manque.

Conditions disponibles selon les règles publiées : moyenne, matières, bac/série, diplôme, niveau, langue, pays, nationalité, programme, université, domaine et revenus. Les références pays/nationalité/programme/université utilisent leurs identifiants canoniques. Les revenus exigent une devise et une période exactes, par exemple `XOF/YEAR` : aucune conversion de devise ou annualisation implicite.

L'extension du profil ajoute `nationalityCountryId`, `householdIncome`, `incomeCurrency`, `incomePeriod`. Ces informations restent déclarées par le candidat, sans certification officielle.

`POST /scholarships/evaluate` :
```json
{"candidateId":"UUID-candidat","programId":"UUID-programme","scholarshipIds":["UUID-bourse"]}
```
Les trois champs sont facultatifs ; en leur absence, le service utilise le candidat connecté et jusqu'à 100 bourses vérifiées du périmètre et de l'année. Cette limite est annoncée dans `selectionLimit`. Un programme cible manquant laisse inconnues les conditions qui l'exigent. Une bourse non vérifiée n'entre pas dans la sélection automatique.

## E. Assistant

Assistant déterministe gratuit, sans clé ni fournisseur d'IA. Les intentions reconnues appellent les services existants : recommandations depuis le profil, explication historique, matières/critères, bourses, comparaison, simulation et recherche documentaire.

`POST /ai/chat/sessions?candidateId=…` crée une session réelle persistée. `POST /ai/chat/sessions/{id}/messages` accepte :
```json
{
  "message":"Comparer ces programmes",
  "programIds":["UUID-programme-1","UUID-programme-2"]
}
```
Autres champs facultatifs : `recommendationId`, `simulation` (même DTO que la simulation). Une simulation requiert des modifications structurées explicites ; l'assistant ne devine pas une note à partir d'une phrase ambiguë. Les UUID contenus dans le texte ne donnent aucun droit d'accès.

Réponse structurée : `id`, `sessionId`, `question`, `content`, `intent`, `status`, `decisionId`, `data`, `sources`, `timestamp`. États : `ANSWERED`, `NEEDS_INPUT`, `INSUFFICIENT_DATA`, `HISTORICAL`, `UNSUPPORTED`. Le résultat métier détaillé et ses citations sont conservés. Un résultat historique périmé demande un recalcul.

Les formulations non reconnues reçoivent une réponse explicite. Les sources sont traitées comme des données ; aucun document n'est exécuté comme instruction. Aucun nom d'établissement, seuil, score, probabilité ou bourse n'est inventé.

## F. Historique

Nouvelle table `decision_records`, reliée au candidat, à sa version et à l'auteur. Types : `SIMULATION`, `COMPARISON`, `SCHOLARSHIP`, `CHAT_SESSION`, `CHAT_MESSAGE`. Les messages référencent leur session par `parent_id`.

Les opérations applicatives ajoutent des instantanés JSONB ; aucune API de modification ou suppression de ces instantanés. `historical` distingue l'instantané d'un résultat recalculé ; `profileChanged` signale la différence de version du dossier candidat. Les références aux sources et versions de guides sont conservées dans le résultat. `sourceCurrent` recalcule à la lecture si les guides et versions documentaires référencés sont encore actifs et dans leurs dates d'application ; sans référence déterminable, il vaut false. L'archivage d'un guide ne réécrit pas l'instantané, mais modifie cet indicateur. Une modification du profil ne réécrit pas davantage les décisions anciennes.

`GET /ai/decisions?candidateId=…&kind=SIMULATION&page=0&size=20` et `GET /ai/decisions/{id}`. Pagination bornée à 100 éléments. Tri stable par date et identifiant. Pour une consultation administrative d'un autre candidat, sélectionner explicitement un type d'historique.

Les sessions et messages sont limités à leur auteur, sans exception de lecture administrative globale.

## G. Exports

Les routes `GET /ai/export/pdf/{id}`, `/csv/{id}`, `/excel/{id}` acceptent l'identifiant d'une recommandation réelle ou d'une décision simulation/comparaison/bourses autorisée. Les conversations ne sont pas exportables.

PDF réel avec PDFBox, retour à la ligne et pagination ; CSV UTF-8 avec guillemets RFC et neutralisation des cellules commençant par une formule ; XLSX réel avec POI, cellules textuelles et aucune formule interprétée. Les caractères hors du jeu de la police PDF sont représentés explicitement sous forme `[U+XXXX]`.

Les exports contiennent les résultats historiques, critères et données disponibles, années, versions, citations, dates et avertissement non officiel. Les PDF de simulation portent la mention hypothétique sur chaque page. Le nom et l'identité civile complète du candidat ne sont pas nécessaires : son UUID suffit. Aucun bulletin binaire privé ou jeton n'est ajouté.

En-têtes `Cache-Control: no-store` et `X-Content-Type-Options: nosniff`. Un rapport trop volumineux est refusé explicitement plutôt que tronqué ; une cellule dépassant la limite XLSX invite à utiliser PDF ou CSV.

## H. API et permissions

| Méthode et route | Fonction |
| --- | --- |
| POST /recommendations/simulate | Simulation distincte du profil réel |
| POST /ai/compare/programs | Comparaison de 2 à 6 programmes |
| POST /scholarships/evaluate | Évaluation personnalisée de bourses |
| POST /scholarships/{id}/evaluate | Évaluation d'une bourse |
| GET /scholarships/evaluations | Historique paginé des bourses |
| GET /ai/decisions | Historique paginé par candidat/type |
| GET /ai/decisions/{id} | Instantané autorisé |
| POST /ai/chat/sessions | Création d'une conversation |
| GET /ai/chat/sessions | Sessions personnelles paginées |
| GET /ai/chat/sessions/{id} | Session et premiers messages |
| POST /ai/chat/sessions/{id}/messages | Question structurée |
| GET /ai/chat/sessions/{id}/messages | Messages paginés |
| GET /ai/export/{pdf,csv,excel}/{id} | Export des résultats autorisés |

JWT réel requis. Les services réutilisent `ResourceAccess` : propre candidat ou droits de lecture/traitement du candidat dans le tenant autorisé. Un traitement administratif exige `RECOMMENDATION_EXECUTE` ; une lecture administrative exige `RECOMMENDATION_READ`, en plus du droit de dossier candidat. Les conversations restent personnelles à l'auteur.

Erreurs : 400 pour une requête mal formée/DTO invalide, 403 pour une ressource non accessible, 422 pour un scénario métier non calculable, 409 pour un conflit de version du profil via son API existante. Les routes F3.1 de génération, détail, explication, scores et historique réel restent disponibles.

## I. PostgreSQL

Migration ajoutée : `V82__decision_support_history.sql`. Création de l'historique, contraintes de type, clés étrangères candidat/auteur/parent et index de lecture ; ajout des groupes logiques aux critères de bourses existants. Les 83 fichiers du dossier de migrations protégés au début F3.2 sont inchangés, vérifiés par SHA-256 : 82 migrations SQL historiques et le fichier `.gitkeep`. Avec V82, Flyway exécute 83 migrations SQL versionnées ; `.gitkeep` n'est pas une migration.

Validation exclusivement sur le cluster privé PostgreSQL 18.4 : `127.0.0.1:59745/orientia_f1`, schémas `f32_20261106`, `f32_final_20261106`, puis schéma neuf de livraison `f32_release_20261106`. Identifiants privés transmis par environnement, jamais inscrits dans ce rapport. Aucun nettoyage ou suppression de schéma.

Flyway a appliqué les 83 migrations versionnées sur le schéma neuf final et Hibernate validate a réussi. Le cluster privé a ensuite été arrêté, données et schémas conservés. Flyway signale que PostgreSQL 18 est plus récent que sa plage officiellement testée ; cette réserve est conservée.

## J. Angular

L'administration dispose de `/intelligence/decisions`, accessible depuis le menu, et de `/reports` connecté au même diagnostic. Formulaires candidat/programmes, simulation explicite, bourses, assistant, détails/sources, historique paginé et téléchargement réel PDF/CSV/XLSX. Les erreurs d'accès ou données insuffisantes sont visibles et effacent le résultat précédent. Un changement de candidat réinitialise la conversation et empêche l'affichage d'une réponse tardive du dossier précédent.

Les résultats sont présentés sous forme de tableaux : programmes/bourses, éligibilité, compatibilité, conditions, citations et deltas avant/après. Un détail complet conserve les valeurs et la provenance. Les pages catalogue, documents, règles, connaissances et recommandations existantes restent réutilisées. Les cartes et dates de rapports factices ont été retirées. Les statistiques analytiques codées en dur sont remplacées par un état d'indisponibilité explicite et un périmètre tenant validé, sans taux d'admission ou de satisfaction inventé.

Il s'agit d'un écran administratif de diagnostic, sans refonte graphique ni espace candidat complet.

## K. Tests

Validation finale : **178 tests backend, 0 échec, 0 erreur, 0 ignoré**, sur 29 suites. Les tests F1, F2, F3.1 et F3.2 sont inclus. Les assertions portent sur le métier et la persistance, pas uniquement sur HTTP 200.

| Exécution | Tests | Échecs | Erreurs | Ignorés |
| --- | ---: | ---: | ---: | ---: |
| Premier lot ciblé | 20 | 1 | 0 | 0 |
| Reprise ciblée avec règles financières | 58 | 1 | 0 | 0 |
| Parcours ciblé corrigé | 58 | 0 | 0 | 0 |
| Suite complète interrompue | 178 | 1 | 0 | 0 |
| Suite complète finale | 178 | 0 | 0 | 0 |
| Angular final | 42 | 0 | 0 | 0 |

Les deux premiers échecs concernent la fixture : attente HTTP 200 au lieu de 201 pour une création, puis tentative de créer deux faits du même type avec un passage identique. La fixture utilise désormais une citation propre à la condition financière ; la contrainte de traçabilité reste intacte. Le plafond financier publié dans `maxValue` et les plafonds de notes ont aussi reçu des contrôles et tests dédiés.

Le premier lancement complet a subi une interruption de plus de quatre heures dans le parcours F3.1. Son JWT de 15 minutes a expiré, entraînant HTTP 401. La suite a été entièrement relancée avec les mêmes contrôles de sécurité, sans prolonger les jetons ni désactiver de test ; les sept parcours documentaires/candidat passent dans le lancement final.

Commandes backend, avec Java 21, mot de passe privé injecté dans `F1_TEST_DB_PASSWORD` et chemins OCR pris depuis le fichier local `%TEMP%/orientia-f2-tools/ocr-runtime.json` :

```text
mvnw.cmd -q -Df1.jdbc.url=jdbc:postgresql://127.0.0.1:59745/orientia_f1 -Df1.jdbc.schema=f32_release_20261106 -Df2.tesseract=<executable-local> -Df2.tessdata=<tessdata-local> test
```

Commandes Angular depuis `frontend`, avec `CHROME_BIN` pointant vers Chrome :

```text
node node_modules/@angular/cli/bin/ng.js test --watch=false --browsers=ChromeHeadless
npm.cmd run build:prod
```

Compilation Java et TypeScript/templates Angular : réussies. Build Angular de production : réussi. PDFBox et POI ouvrent les vrais exports ; CSV vérifie guillemets, UTF-8 et préfixes de formules, y compris après espaces/BOM. Le parcours réel vérifie les transitions d'éligibilité dans les deux sens, l'égalité du profil avant/après simulation et assistant, le nombre inchangé de recommandations réelles, les bourses éligibles/refusées/inconnues/fermées, les neuf échanges du chat, la pagination, les refus inter-utilisateurs et administratifs des conversations, le XLSX réel et l'archivage des guides.

Journaux locaux : `%TEMP%/orientia-f32-release-verification.log`, `orientia-f32-angular-tests-final.log`, `orientia-f32-angular-build-final.log`. Les totaux par suite et les preuves PostgreSQL sont conservés dans `SPRINT_F3_2_PREUVES.json`.

Avertissements sans échec : budgets CSS login/layout et CommonJS SockJS préexistants, découverte Commons Logging, agent Mockito dynamique, plage de versions Flyway officiellement testée inférieure à PostgreSQL 18. ChromeHeadless a également signalé un délai de fermeture après ses 42 succès ; la commande est sortie avec le code 0.

## L. Sécurité

Le contrôle d'accès précède les calculs, lectures d'historique et exports. Les identifiants étrangers ou inconnus ne permettent pas de franchir le périmètre candidat/tenant. Les messages sont liés à une session existante autorisée ; aucune création implicite depuis un UUID fourni.

La simulation n'enregistre pas les notes dans le candidat et ne crée pas de recommandations réelles. Les DTO renvoyés contiennent des instantanés et références publiques contrôlées, pas de graphe JPA ni de document scolaire binaire. Les défenses F1 et la publication humaine F2 sont réutilisées.

La suite complète finale F1/F2/F3.1/F3.2 est réussie, sans test ignoré. Les 82 migrations SQL historiques et le fichier `.gitkeep` sont identiques au relevé initial. L'état Git Flutter est inchangé par rapport aux 35 entrées préexistantes ; aucun fichier Flutter n'a été écrit. Aucun schéma ou fichier métier n'a été supprimé.

## M. Préparation Flutter

Contrats disponibles pour le futur client : simulation avant/après et delta, comparaison multi-programmes, évaluation de bourses, assistant structuré avec citations, historique paginé et téléchargements binaires authentifiés. Les trois états d'éligibilité doivent rester distincts côté client ; la compatibilité ne constitue pas une probabilité d'admission.

Aucun fichier Flutter modifié. F3.3 n'est pas commencé.

## N. Problèmes restants

Limites connues, sans calcul favorable inventé :

1. Assistant déterministe : les formulations ambiguës nécessitent des paramètres structurés ; aucun modèle payant n'est requis.
2. Sélection automatique des bourses limitée explicitement à 100 par évaluation ; une sélection par identifiants permet de traiter un autre sous-ensemble.
3. Les critères non pris en charge, les dates, frais, langues ou conditions non documentés restent inconnus. Une préférence ou un budget copié dans une simulation n'a d'effet chiffré que si le moteur F3.1 ou une règle publiée l'utilise ; aucune nouvelle pondération budgétaire n'a été inventée.
4. Les taux d'admission, de satisfaction et les classements non observés restent indisponibles. Les anciens rapports agrégés renvoient explicitement HTTP 422 et orientent vers l'export d'une décision enregistrée, au lieu d'annoncer un faux rapport généré.
5. PDF : représentation explicite des caractères hors police ; XLSX : limite standard de longueur d'une cellule ; gros exports refusés au lieu d'être tronqués. L'export administratif utilise des champs de diagnostic et conserve les entrées académiques/financières nécessaires à l'explication, sous les mêmes droits d'accès.
6. Les avertissements techniques du build et des outils sont recensés en K. L'écran Angular reste administratif ; l'expérience candidat Flutter appartient au sprint ultérieur.

Aucun déploiement ni validation en production n'a été effectué.

## O. Verdict

**Sprint terminé et vérifié.**

Les 178 tests backend et les 42 tests Angular passent ; le build de production Angular, les 83 migrations sur schéma neuf et Hibernate validate sont validés. Le parcours documentaire → profil confirmé → recommandations → simulations sans mutation → comparaison → bourses → assistant sourcé → exports → historique privé est démontré sur PostgreSQL isolé, avec refus d'accès et archivage des sources.

Les données de test restent marquées synthétiques et non officielles. La base principale n'a pas été consultée ni modifiée, Flutter est inchangé et le cluster privé est arrêté sans suppression de données. Aucun commit, push ou déploiement. F3.3 n'est pas commencé.
