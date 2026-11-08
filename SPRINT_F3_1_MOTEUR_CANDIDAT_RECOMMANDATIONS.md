# ORIENTIA — Sprint F3.1 : moteur candidat et recommandations

Date : 6 novembre 2026. Périmètre : backend Spring Boot et diagnostic Angular existant.
Validation finale réussie : 155 tests backend dans la suite complète, puis 36 tests ciblés après l'ajout de bacSeries, soit 156 cas distincts vérifiés ; 32 tests Angular et build de production réussis.

## A. Résumé exécutif

Le parcours relie désormais le candidat authentifié à son profil académique persistant, aux analyses réelles de ses relevés et aux programmes vérifiés publiés en F2. L'éligibilité utilise les critères du guide applicable et distingue ELIGIBLE, NOT_ELIGIBLE et INSUFFICIENT_DATA. Le score mesure une compatibilité documentée ; il ne constitue pas une probabilité d'admission.

La génération conserve ses entrées académiques, critères, sources, conversions, pondérations, résultats et identifiants réels. Les calculs précédents restent consultables. Une confirmation personnelle ne modifie jamais la certification officielle du candidat.

Flutter, la base principale, les migrations antérieures au sprint, les commits, le push et le déploiement sont hors de cette intervention.

## B. Profil candidat

L'entité Candidate reste le propriétaire des données. Deux colonnes ajoutées par V77 conservent le document académique JSONB et sa date de confirmation. Le verrouillage optimiste existant protège les modifications concurrentes : une version périmée produit HTTP 409.

AcademicProfileRequest porte le pays, l'année d'admission, l'année des résultats, le barème, l'analyse de relevé sélectionnée, le niveau, le diplôme, la spécialisation, l'établissement, la date de naissance, le type, la série nationale et l'année du bac, la moyenne, les notes par code matière, les intérêts, les destinations, les langues et le budget avec devise.

Les références sont validées en base. Le barème doit correspondre au pays et à l'année des résultats. Les valeurs hors barème et les budgets sans devise sont refusés. Les notes manquantes restent absentes ; elles ne deviennent pas zéro. bacType désigne la catégorie générale, tandis que bacSeries conserve le code national déclaré ; une catégorie scientifique ne permet pas de deviner la série C ou D. Les matières déclarées doivent utiliser les codes du référentiel pour correspondre aux règles publiées.

Les modifications peuvent retirer la confirmation. La génération requiert un profil confirmé et une année d'admission explicite. Les champs historiques de Candidate ne constituent pas une deuxième source de notes pour le calcul.

DefaultProfileAnalyzer.analyze(candidateId) charge ce profil après contrôle d'accès. Son ancien adaptateur sans barème ne calcule plus de moyenne normalisée ou de compétences implicites. L'adaptateur avec barème explicite demeure disponible pour les calculs purs et les tests.

## C. Bulletins

Le stockage sécurisé F1 et PdfImageOcrEngine de F2 sont réutilisés : PDF texte avec PDFBox, PDF scannés et images PNG/JPEG avec Tesseract local. Aucun fournisseur payant n'est nécessaire.

Chaque analyse conserve les pages, le texte, le moteur utilisé, le contrôle SHA-256 du fichier, la période déclarée et les lignes extraites. Le parseur des notes est volontairement conservateur : libellé, deux-points, note et dénominateur explicites, avec coefficient facultatif. Une lettre ne se convertit que par une correspondance configurée.

Les matières sont résolues uniquement par nom ou code exact dans le barème. Les matières inconnues et valeurs illisibles restent nulles. Les coefficients absents ne sont pas inventés et aucune moyenne globale n'est reconstruite à partir de coefficients inconnus.

Une correction crée une analyse enfant ; une confirmation crée une version SELF_CONFIRMED. Les versions antérieures ne sont ni écrasées ni supprimées. La confirmation est idempotente et protégée par verrou et index unique. Chaque ligne corrigée conserve un passage exact d'une page originale. Une analyse confirmée est immuable ; une réanalyse ouvre un nouveau brouillon.

Plusieurs relevés peuvent appartenir à la même année. Les périodes ne sont pas fusionnées automatiquement : le candidat sélectionne explicitement une analyse confirmée dans son profil. Les notes actives et leurs sources sont conservées dans TranscriptAnalysis.reviewPayload ; les anciennes colonnes de score de cette entité restent compatibles avec les données historiques et ne reçoivent pas de valeurs simulées.

## D. Normalisation

GradeNormalizer utilise les bornes et normalizeTo des barèmes configurés. Une conversion numérique exige deux barèmes actifs, complets, compatibles et une valeur dans les bornes. Le moteur vérifie séparément les pays et années applicables. L'année d'un relevé peut précéder l'année d'admission.

La conversion linéaire est :
`(valeur - minimumSource) × (maximumCible - minimumCible) / (maximumSource - minimumSource) + minimumCible`.

L'éligibilité compare les produits croisés exacts, sans arrondi préalable. Une valeur juste sous un seuil ne le franchit donc pas par affichage. Les conversions de lettres exigent un GradeScaleItem exact ; une lettre ou une conversion inconnue reste indéterminée.

L'explication conserve la valeur originale, les identifiants et bornes des deux barèmes, leur normalizeTo, pays et années, la valeur convertie et la méthode EXACT_CROSS_MULTIPLICATION. Le résultat persistant conserve également le contexte du barème source.

## E. Éligibilité

RealEligibilityEngine est conservé et corrigé ; aucun deuxième moteur d'éligibilité n'a été créé.

- Comparaisons numériques : égalité, exclusion, >, >=, <, <= et intervalle inclusif.
- Notes : moyenne et notes des matières explicitement référencées.
- Règles textuelles CUSTOM reconnues : bacType/bacSeries, diplôme, niveau, langue et pays.
- Listes : égalité exacte ou appartenance à une liste explicitement délimitée par virgule, point-virgule ou barre verticale ; aucune recherche par sous-chaîne.
- Groupes : opérateur AND ou OR publié, puis AND entre les groupes obligatoires. Des opérateurs contradictoires rendent le groupe indéterminé.
- Une alternative vraie rend OR vrai ; faux OR inconnu reste inconnu. Une obligation fausse rend AND faux ; vrai AND inconnu reste inconnu.
- Une condition optionnelle non remplie produit un avertissement, pas un blocage.

Un guide absent, futur, expiré, archivé ou d'une autre année ne produit pas un avis favorable. Les programmes et leur hiérarchie doivent être vérifiés et actifs. Un guide ou programme d'un autre tenant est exclu. Le fait source doit être validé, publié, lié au critère et au guide, et sa version documentaire active.

Aucun critère publié, aucune condition obligatoire, une matière requise absente, un barème inconnu ou un opérateur non pris en charge ne deviennent une éligibilité. Les règles non implémentées, notamment âge justifié, classement, percentile, crédits et coefficient comme condition autonome, restent explicitement INSUFFICIENT_DATA.

Chaque critère expose résultat, observation, attendu, caractère obligatoire, groupe, opérateur logique, motif, identifiant du fait, document, version documentaire, page, passage, année et version du guide.

## F. Scoring

RealScoringEngine devient l'adaptateur des programmes et critères persistés vers DefaultScoringEngine. Les deux anciennes formules divergentes sont consolidées.

| Dimension | Poids par défaut | Donnée réellement utilisée |
|---|---:|---|
| Académique | 0,50 | Moyenne déclarée/confirmée rapportée au barème explicite |
| Matières | 0,20 | Matières des critères publiés, si toutes les valeurs nécessaires sont connues |
| Intérêts | 0,15 | Correspondance exacte du domaine du programme |
| Destination | 0,10 | Identifiant réel du pays préféré |
| Langue | 0,05 | Correspondance exacte de la langue déclarée/préférée |

Configuration : `app.recommendations.weights.academic`, `.subject`, `.preference`, `.country`, `.language`. Les poids négatifs ou tous nuls sont refusés.

Le score est la moyenne pondérée des seules dimensions calculables, bornées entre 0 et 100. Sans dimension calculable, il est null. Les composantes absentes portent computed=false et score=null. Le champ historique confidence représente exclusivement la couverture pondérée des données, pas une confiance statistique.

Les compétences non configurées, les données de cohortes inexistantes et les comparaisons de budget sans période fiable ne sont pas inventées. HistoricalEngine ne retourne plus de score historique ou probabilité fictive. Les bourses ouvertes et vérifiées sont présentées séparément avec leurs critères et échéances ; elles ne gonflent pas artificiellement la compatibilité.

## G. Recommandations

La génération lit le profil confirmé en base. Les notes et préférences transitoires envoyées à /generate sont refusées avec HTTP 422.

Les programmes sont sélectionnés dans le catalogue vérifié du tenant du candidat et pour son année. Une seule version de guide active, applicable au pays et à l'année doit être déterminable. Une absence ou ambiguïté reste indéterminée. Les règles évaluées ne sont jamais remplacées par un score.

Le classement place les programmes éligibles avant les cas insuffisants, puis les refus. Dans chaque catégorie : compatibilité décroissante, puis identifiant du programme pour départager les égalités. Aucun UUID aléatoire ne remplace un programme réel.

RecommendationRun conserve aussi les calculs sans résultat. Recommendation conserve chaque résultat par couple calcul/programme, avec contrainte unique, rang, état, couverture, date, version du candidat et snapshot JSONB des entrées et explications. Les générations suivantes ne remplacent pas l'historique.

Le champ stale signale une modification du profil. sourceCurrent distingue un résultat historique dont le guide, programme ou fait source n'est plus applicable. Les anciennes décisions restent datées et traçables. Les bourses expirées, sans dates suffisantes, épuisées, non vérifiées ou hors périmètre ne sont pas proposées comme ouvertes.

## H. Contrats API

Toutes les routes ci-dessous nécessitent une identité réelle authentifiée.

| Méthode et route | Contrat |
|---|---|
| GET /candidates/me/academic-profile | Profil personnel, version de modification, confirmation et état de vérification officielle |
| PUT /candidates/me/academic-profile | AcademicProfileRequest complet avec version courante ; confirmed explicite |
| GET/PUT /candidates/{id}/academic-profile | Même contrat, avec contrôle de tenant et autorisations administratives |
| POST /transcripts | Multipart file et academicYearId facultatif ; année courante seulement pour compatibilité de l'ancien upload |
| GET /transcripts, GET /transcripts/{id} | Liste paginée ou DTO d'un relevé autorisé |
| POST /transcripts/{id}/analyses | gradeScaleId et period ; nouvelle extraction/réanalyse |
| GET /transcripts/{id}/analyses | Historique paginé des analyses |
| PUT /transcripts/{id}/analyses/{analysisId} | TranscriptReview : barème, période, moyenne et notes corrigées |
| POST /transcripts/{id}/analyses/{analysisId}/confirm | Confirmation personnelle idempotente |
| POST /recommendations/generate | {} pour le candidat ; {candidateId} pour l'administrateur autorisé |
| GET /recommendations | Page de résultats ; candidateId, runId, page et size facultatifs |
| GET /recommendations/history | Page des calculs, y compris ceux sans résultat |
| GET /recommendations/{id} | Résultat persistant et état de fraîcheur |
| GET /recommendations/{id}/scores | Composantes du score enregistré |
| GET /recommendations/{id}/explanation | Entrées, règles, sources et conversions du calcul |
| POST /recommendations/simulate | HTTP 501 explicite : F3.2 non commencé |

L'administrateur doit posséder CANDIDATE_READ pour lire un autre candidat, CANDIDATE_MANAGE pour modifier ses données/relevés, RECOMMENDATION_EXECUTE pour générer et RECOMMENDATION_READ pour consulter ses recommandations. Ces permissions ne contournent pas l'isolation des tenants.

Les pages sont bornées à 100 éléments. Les DTO ne sérialisent pas les graphes JPA ou chemins de stockage. Erreurs : 400 pour les requêtes invalides, 401 pour une identité inexistante/invalide, 403 pour une ressource interdite, 409 pour un profil modifié, 422 pour un contexte académique inexploitable.

## I. PostgreSQL

Migrations ajoutées, sans modifier les anciennes :

| Migration | Objet |
|---|---|
| V77 | Profil académique JSONB et confirmation |
| V78 | Résultats triétats, snapshots et unicité par calcul/programme |
| V79 | Analyses de relevés versionnées, moyenne inconnue permise, plusieurs relevés par année |
| V80 | Calculs persistés, index et clé étrangère ; reprise non destructive des identifiants existants |
| V81 | Unicité de la confirmation d'une analyse parent |

Les champs résultat ou relevé peuvent être null lorsqu'ils sont réellement inconnus ; aucun faux bulletin ou score zéro n'est nécessaire pour satisfaire une contrainte.

Environnement de validation : PostgreSQL 18.4 privé, 127.0.0.1:59745, base orientia_f1, schémas de test séparés f31_20261106, f31_final_20261106 et f31_release_20261106. Flyway crée le schéma et Hibernate utilise validate. Les profils historiques de tests H2 restent isolés eux aussi.

Les 78 migrations présentes avant F3.1 ont toutes conservé leur empreinte. Les identifiants de connexion privés restent hors du dépôt. Aucun lancement de l'application sur sa configuration de base principale n'a été effectué.

## J. Tests

Résultats définitifs :

| Vérification | Exécutés | Échecs | Erreurs | Ignorés |
|---|---:|---:|---:|---:|
| Dernière suite backend complète | 155 | 0 | 0 | 0 |
| Dernier contrôle bacSeries + parcours PostgreSQL réel | 36 | 0 | 0 | 0 |
| Cas backend distincts dans ces deux passages | 156 | 0 | 0 | 0 |
| Angular ChromeHeadless 156 | 32 | 0 | 0 | 0 |

Les 36 tests ciblés recouvrent des tests de la suite complète ; ils ne sont pas additionnés artificiellement aux 155. Le nouveau cas national porte l'ensemble distinct à 156. Le build Angular de production est réussi ; ses avertissements préexistants sont détaillés en M.

Le schéma PostgreSQL neuf a appliqué **83 migrations**, avec Hibernate validate et zéro migration en échec. La vérification SQL finale constate **8 calculs, 18 recommandations et 6 analyses** : 2 ELIGIBLE, 2 NOT_ELIGIBLE et 14 INSUFFICIENT_DATA. Elle constate zéro doublon calcul/programme, zéro probabilité inventée, zéro résultat privé d'un autre tenant et zéro année de guide incorrecte. Ces volumes proviennent des deux exécutions du scénario F3.1 dans ce schéma privé.

Commandes exécutées avec le JDK 21 privé et le wrapper Maven :

- Compilation : `mvnw.cmd -q -DskipTests compile`.
- Tests métier initiaux : `mvnw.cmd -q -Dtest=RealEligibilityEngineTest,GradeNormalizerTest test`.
- Suite complète : `mvnw.cmd -q -Df1.jdbc.url=<URL PostgreSQL privée> -Df1.jdbc.schema=f31_release_20261106 -Df2.tesseract=<exécutable local> -Df2.tessdata=<dossier local> test`.
- Contrôle final de série nationale : même environnement privé avec `-Dtest=RealEligibilityEngineTest,AcademicWorkflowIntegrationTest#f31RealCandidateTranscriptEligibilityHistoryAndTenantIsolation`.
- Angular : `npm.cmd run build:prod`.
- Angular : `node node_modules/@angular/cli/bin/ng.js test --watch=false --browsers=ChromeHeadless`, avec CHROME_BIN défini.

Le scénario f31RealCandidateTranscriptEligibilityHistoryAndTenantIsolation utilise deux candidats et tenants, deux années, trois programmes issus d'une véritable publication F2, un relevé PDF scolaire, des matières connues/inconnues, une correction, une confirmation répétée, plusieurs relevés, les trois issues d'éligibilité, le recalcul, l'historique et l'archivage du guide. Ses assertions portent sur les données, sources et résultats, pas seulement HTTP 200.

Les tests unitaires vérifient les seuils exacts et juste inférieurs, notes absentes/faibles, listes/exclusions, AND/OR, règles inconnues, sources retirées, années/pays/tenants incompatibles, barèmes /20 et /100, lettres connues/inconnues, valeurs hors limites, pondérations invalides et classement indépendant d'une probabilité fictive.

Les échecs intermédiaires ne sont pas présentés comme des réussites : l'ancien test de recommandation avec @WithMockUser("admin") attendait un accès sans identité persistée ; il vérifie désormais le refus, tandis que le scénario réel vérifie le succès avec JWT. Un passage ciblé a également testé le nouveau champ sourceCurrent contre un bytecode compilé avant son ajout ; la reconstruction complète finale est requise pour valider le contrat.

Preuves reproductibles et inventaire des tests : SPRINT_F3_1_PREUVES.json.

## K. Sécurité

Les contrôles F1 de compte actif, JWT, sessions, propriétaire, tenant et permissions sont réutilisés. Les entrées HTTP ne peuvent pas modifier le rôle, le tenant ou la certification officielle d'un candidat. Les écritures administratives requièrent les permissions canoniques existantes.

Le scénario F3.1 démontre qu'un candidat ne lit ni le profil, ni les analyses, ni les recommandations de l'autre ; un autre tenant ne reçoit pas les programmes privés du premier. La confirmation ne vérifie pas officiellement les documents.

Le contrôle final confirme les 78 empreintes de migrations historiques inchangées, aucun fichier supprimé et l'état Git de Flutter identique à celui capturé avant F3.1. Les changements antérieurs F1/F2 sont conservés.

## L. Préparation Flutter

Les contrats disponibles sont AcademicProfileRequest/View, TranscriptResponse, TranscriptReview/Mark, TranscriptAnalysisService.View, Generation, Item et RunView.

Parcours à intégrer ultérieurement : charger le profil/version ; enregistrer le contexte ; uploader le relevé avec son année ; analyser avec barème/période ; corriger ; confirmer ; sélectionner l'analyse confirmée dans le profil ; confirmer ce profil ; générer avec {} ; afficher les trois états et les explications ; consulter les calculs précédents.

Flutter devra utiliser l'année d'admission et l'année des résultats comme deux valeurs distinctes, les codes exacts des matières et les identifiants pays/barème du référentiel. Il doit accepter les valeurs nulles, gérer les conflits 409, distinguer confirmation et certification et afficher la date, stale et sourceCurrent. La couverture ne doit pas être renommée probabilité d'admission.

Aucun écran, client HTTP, fichier de configuration ou test Flutter n'a été modifié. Aucune intégration mobile F3.3 n'a commencé.

## M. Problèmes restants

1. Les règles non prises en charge restent visibles comme données insuffisantes. Ajouter de nouveaux opérateurs métier exige des contrats documentaires validés et leurs tests ; aucune acceptation par défaut.
2. L'extraction de notes n'est pas un interpréteur universel de relevés. Les tableaux complexes, périodes multiples ou libellés ambigus demandent une correction humaine explicite.
3. Le budget ne modifie pas le score tant que les périodes des frais et du budget ne sont pas comparables. Les statistiques de cohortes et probabilités restent indisponibles faute de données observées.
4. L'OCR local doit être configuré dans l'environnement d'exécution. Les chemins privés utilisés pour les tests ne sont pas une configuration de production.
5. Un volume important pourra nécessiter une exécution asynchrone des analyses/calculs et des politiques de conservation. Cette industrialisation n'est pas simulée ici.
6. Les avertissements Angular préexistants concernent les budgets CSS de login/layout et SockJS CommonJS. Les dépendances n'ont pas fait l'objet d'une nouvelle montée de version dans F3.1.
7. Les anciennes analyses/recommandations antérieures au contrat F3.1 ne sont pas automatiquement transformées en données académiques confirmées ou nouveaux calculs certifiés.

## N. Verdict

**Sprint terminé et vérifié**, dans les limites explicites de prise en charge des règles et formats décrites en E et M.

Les 15 conditions de réussite sont démontrées : profil persistant ; traitement réel des relevés ; correction et confirmation ; barèmes explicites ; critères publiés évalués ; matières et groupes logiques ; données insuffisantes ; scoring déterministe ; programmes vérifiés ; sources et années contrôlées ; historique persistant ; API protégées ; non-régression F1/F2 ; parcours candidat complet ; absence d'intervention sur la base principale.

Les preuves associent les 156 cas backend distincts, les 32 tests Angular, le build de production, les 83 migrations validées sur un schéma neuf et les contrôles SQL d'isolation. Les dépendances OCR nécessaires étaient disponibles et utilisées pendant les tests. Aucun test ignoré n'est compté comme réussi. F3.2, F3.3 et tout déploiement restent soumis à une demande distincte.
