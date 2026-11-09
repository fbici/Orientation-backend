# Orientia — Auth Experience 2.0 et priorité Bénin

9 novembre 2026. Rapport avant publication du nouveau lot. Les pushes des lots
précédents ont été explicitement autorisés par le propriétaire le 9 novembre.
Aucune ressource Render ni donnée de production modifiée par cette mission.

## Audit préalable

Architecture Angular 19 standalone conservée : routes lazy, AuthService,
intercepteur JWT, session/localStorage selon « rester connecté », refresh rotatif
et guards existants. Aucun projet reconstruit, aucune dépendance ajoutée.

Avant modification, seuls Login, ForgotPassword et ResetPassword existaient côté
Angular. Les endpoints Spring existants comprennent POST /auth/login, /register,
/forgot-password, /reset-password, /refresh ; GET /auth/me et /auth/verify?token=...
L'inscription publique est liée au tenant configuré et au rôle CANDIDAT.
Elle ne crée pas d'administrateur et ne connecte pas automatiquement l'utilisateur.

La vérification réelle emploie un jeton aléatoire base64url de **43 caractères**,
à usage unique, expirant après 24 h. Il n'existe pas d'OTP numérique ni d'endpoint
de renvoi dédié ; une nouvelle demande d'inscription éligible peut renouveler
l'envoi selon le service existant. La récupération expire après une heure.

Les trois vidéos nommées dans la mission ne sont pas accessibles dans les pièces
jointes. Les animations suivent leurs descriptions ; aucune reproduction exacte
des mouvements n'est revendiquée. Aucun husky, personnage ou mascotte ajouté.
Le signe « école » déjà utilisé par Orientia est repris en SVG local ; aucun
nouveau logo officiel n'est prétendu.

## Palette officielle trouvée et réutilisée

- Principal : --brand #2563eb, variantes #3b82f6 et #1d4ed8.
- Accent : --sky-500 #0ea5e9 ; reflets --brand-200 #bfdbfe.
- Fond/surface/texte : neutres existants --n-950 #030712, --n-900 #111827,
  --n-25 #fcfcfd, --n-200/300/400.
- Erreur : --red-500 #ef4444 et --red-600 #dc2626.
- Succès : --green-500 #22c55e et --green-700 #15803d.

Les alias --auth-* restent limités à .auth-experience. Les transparences sont
dérivées de ces variables. Les styles métier existants sont conservés.

## Résultat par parcours

| Parcours | Réalisation | Validation | Limite |
|---|---|---|---|
| Connexion | Champs alignés, labels, œil dans le champ, état requête réel, succès avant redirection, refus/timeout et double clic protégés | Tests Angular + login HTTP réel sur JAR/base privée | Aucun compte de production utilisé |
| Login ↔ inscription | Shell partagé, transitions natives de route 3D, entrée des champs, dimensions adaptées | Navigation Chrome locale et captures | Navigateurs sans View Transitions : entrée CSS ; pas de copie vidéo exacte |
| Inscription | Champs réellement requis, confirmation, limites UTF-8, payload candidat sans rôle/tenant imposé | Tests payload, erreurs et succès simulés | Aucun e-mail réel envoyé |
| Vérification | Écran de jeton 43 caractères, retrait du jeton de l'URL, confirmation explicite, animation d'anneau après réponse, retour login après succès | Jeton invalide/valide simulés, navigateur et tests | Pas de cases OTP fictives ni de nouveau protocole numérique |
| Mot de passe oublié | Même thème, message générique protégeant l'existence du compte, gestion d'échec | Tests auth existants et écran Chrome | Livraison SendGrid réelle non testée |
| Réinitialisation | Lien absent/expiré, mots de passe identiques, limites 12 caractères/72 octets, œil et confirmation | Tests Angular + contrôles serveur existants | Pas de réinitialisation d'un compte de production |
| Google | Bouton explicitement désactivé et procédure de conception/configuration séparée | Désactivation et message testés | **Non implémenté et non opérationnel** ; client OAuth absent confirmé par le propriétaire |
| Priorité Bénin | Message produit Bénin d'abord, exigence et audit SQL en lecture seule documentés | Audit réellement exécuté sur schéma isolé | Catalogue national incomplet ; aucun faux établissement ajouté |

## Animations et accessibilité

Panneau translucide avec bordure lumineuse, halo et fond de profondeur. Le bouton
possède un indicateur à droite, reflet au survol, pression, attente, succès et
feedback d'erreur. Les états suivent les réponses HTTP, sans réussite anticipée.
La connexion dispose d'un délai court de feedback avant navigation ; aucun compte
n'est rendu administrateur pour permettre l'accès au dashboard. Un candidat rejoint
son profil par défaut.

La vérification anime quatre éléments décoratifs en anneau et un symbole de
validation seulement après confirmation du serveur. Ce sont des éléments
décoratifs, pas quatre cases simulant un code que le serveur n'envoie pas.

Labels associés, autocomplete, régions live/alert, boutons nommés, contrôles natifs,
focus visible et préférence de mouvement réduit. Les icônes auth sont des SVG locaux,
ce qui évite les mots « school » ou « visibility » en cas de police distante absente.
Les instantanés View Transitions ne sont pas des formulaires cachés interactifs.
Quand le mouvement réduit est demandé au chargement, les transitions natives ne
sont pas activées ; le CSS couvre également les changements de préférence en cours
de session. Les contrôles clavier complets et lecteurs d'écran ne sont pas certifiés.

## Changements serveur strictement nécessaires

- Nouveau AuthPageController : HTML pour les cinq chemins auth explicites, avec
  Cache-Control: no-store et Referrer-Policy: no-referrer. Aucun fallback général
  n'expose les API privées.
- GET /auth/verify-email autorisé comme page publique ; les règles JWT/tenant et
  les permissions des API métier ne sont pas assouplies.
- Nouveaux e-mails de vérification : /auth/verify-email?token=... vers l'écran.
  L'ancien endpoint GET /auth/verify reste inchangé pour les anciens liens et le mobile.
- Le test SendGrid vérifie ce lien avec HttpClient simulé : aucun envoi réel.
- Aucune migration Flyway modifiée ou ajoutée. Aucun nouveau secret.

## Tests et preuves

- Angular : **85 tests réussis**, dont 18 nouveaux, ChromeHeadless.
- Build Angular production : réussi. Avertissements restants : budget CSS du layout
  existant et CommonJS sockjs ; pas de seuil augmenté pour masquer les avertissements.
- Maven verify : réussi, 204 cas recensés dont 201 exécutés et 3 OCR initialement
  ignorés par condition opt-in. Les trois tests OCR sont relancés séparément avec
  le moteur local ; ils réussissent tous. Bilan cumulé : **204 tests exécutés, zéro échec** (35 suites). Les preuves figurent dans AUTH_EXPERIENCE_PREUVES.json.
- Packaging JAR : réussi après copie du dernier build ; index embarqué identique
  au build Angular. Cette étape skipTests suit le verify, elle ne le remplace pas.
- Chrome local : 9 vues/états, 1440×1000, 1280×800, 820×1180 et 390×844.
  Pas de débordement de page ; labels, navigation inscription, attente, refus,
  vérification réussie et préférence reduced-motion contrôlés. Zéro exception
  JavaScript sur le dernier passage.
- Réponses HTTP synthétiques pour les captures d'états. Elles servent exclusivement
  aux tests locaux ; aucun mock ni serveur de test n'est livré dans les sources
  de production.
- Vrai JAR Spring avec profil prod et base PostgreSQL privée neuve : login HTTP 200,
  paire de jetons émise, cinq pages auth directes HTTP 200 et no-store. Les jetons
  et le mot de passe synthétique ne figurent pas dans les preuves publiées.
- Aucun test authentifié en production, aucun envoi réel d'e-mail, aucun OAuth Google réel.
- Tests mobile avant son push : flutter analyze sans anomalie, **35 tests réussis**.

Preuves : docs/auth-experience/browser-results.json et real-server-results.json.
Captures : [ordinateur](auth-experience/login-desktop.png),
[téléphone](auth-experience/login-mobile.png),
[inscription](auth-experience/register-mobile.png),
[chargement](auth-experience/login-loading-mobile.png),
[erreur](auth-experience/login-error-mobile.png),
[vérification](auth-experience/verification-success-mobile.png).

Les captures ne certifient pas le clavier virtuel d'un téléphone physique ni les
performances sur un réseau mobile réel. Docker n'est pas disponible dans cet
environnement. PostgreSQL privé 18.4 produit l'avertissement Flyway connu ; aucune
conclusion de compatibilité PostgreSQL 16 n'est tirée de ce seul avertissement.

## Bénin : constat à traiter en priorité

La base isolée reconstruite contient seulement **5 fiches béninoises**, toutes
sans type, source ni vérification : UAC, UP, ESGIS, ENA, INMA. Ce constat ne décrit
pas le contenu de la base Render. Le nom d'une ancienne migration « complete » ne
prouve aucune exhaustivité. Les établissements publics et privés doivent tous
être couverts ; UNA et UNSTIM sont notamment absents de cette reconstruction.

Voir [PRIORITE_BENIN.md](PRIORITE_BENIN.md) pour les sources MESRS consultées, les
limites de récupération de la liste privée et les critères d'import/contrôle.
Une nouvelle liste complète datée reste nécessaire avant une réconciliation
fiable. Aucun chiffre, agrément ou établissement fictif n'a été ajouté.

## Google et variables

Le propriétaire confirme « Pas encore configuré ». Le flux OIDC n'est pas livré.
[AUTH_GOOGLE_CONFIGURATION.md](AUTH_GOOGLE_CONFIGURATION.md) décrit la conception
Code + PKCE, state/nonce, identité issuer/sub, liaison explicite, tenant candidat
et configuration Cloud Console. L'implémentation serveur, les migrations de liaison
et les tests de sécurité restent à réaliser avant activation.

Les noms GOOGLE_CLIENT_ID/SECRET/REDIRECT_URI/ENABLED dans ce document sont des
variables **prévues, non consommées actuellement**. Ne pas simplement les ajouter
et déclarer Google opérationnel. Aucun secret Google ne doit entrer dans Angular.

Pour les parcours actuels, conserver APP_BASE_URL (origine Web publique),
CORS_ALLOWED_ORIGINS, REGISTRATION_TENANT_CODE si autre que DEFAULT,
SENDGRID_API_KEY, SENDGRID_FROM_EMAIL et SENDGRID_ENABLED, ainsi que les variables
JWT/PostgreSQL existantes. Ne pas transmettre leurs valeurs dans Git ou les rapports.

## Publication et Render

Pushes déjà confirmés : backend main b3d08b60 ; mobile master 5041c0b.
Le nouveau lot auth est séparé pour faciliter sa revue. Le push est autorisé par
le message explicite du propriétaire ; aucun push forcé et aucun écrasement distant.
Le présent rapport ne prouve pas l'achèvement d'un déploiement Render automatique.

Pour Render : dépôt backend, branche main, Dockerfile racine existant (Node 22 puis
Java 21). Le Dockerfile compile Angular et l'embarque dans le JAR. Ne pas déployer
uniquement le frontend. Aucune nouvelle migration à appliquer. Après publication,
contrôler health, les cinq pages auth, connexion/déconnexion, liens SendGrid avec
un compte de test autorisé, expiration et restrictions candidat/admin. Les liens
nouveaux de vérification doivent ouvrir l'écran, les anciens restent utilisables.

Aucune reconstruction/rotation de base ni réactivation du bootstrap administrateur
n'est nécessaire. Aucun réglage Render n'a été modifié manuellement.

## Fichiers de ce lot

- docs/AUDIT_CATALOGUE_BENIN.sql
- docs/AUTH_GOOGLE_CONFIGURATION.md
- docs/PRIORITE_BENIN.md
- docs/auth-experience/browser-results.json
- docs/auth-experience/login-desktop.png
- docs/auth-experience/login-error-mobile.png
- docs/auth-experience/login-laptop.png
- docs/auth-experience/login-loading-mobile.png
- docs/auth-experience/login-mobile.png
- docs/auth-experience/login-tablet.png
- docs/auth-experience/real-server-results.json
- docs/auth-experience/register-mobile.png
- docs/auth-experience/verification-success-mobile.png
- frontend/src/app/app.config.ts
- frontend/src/app/auth/auth-experience.spec.ts
- frontend/src/app/auth/auth.routes.ts
- frontend/src/app/auth/forgot-password/forgot-password.component.ts
- frontend/src/app/auth/login/login.component.ts
- frontend/src/app/auth/register/register.component.ts
- frontend/src/app/auth/reset-password/reset-password.component.ts
- frontend/src/app/auth/shared/auth-errors.ts
- frontend/src/app/auth/shared/auth-icon.component.ts
- frontend/src/app/auth/shared/auth-shell.component.ts
- frontend/src/app/auth/shared/auth-submit.component.ts
- frontend/src/app/auth/verify/verify.component.ts
- frontend/src/app/core/services/auth.service.spec.ts
- frontend/src/app/core/services/auth.service.ts
- frontend/src/styles.scss
- src/main/java/com/orientation/orientationapp/modules/auth/controller/AuthPageController.java
- src/main/java/com/orientation/orientationapp/modules/auth/service/EmailService.java
- src/main/java/com/orientation/orientationapp/security/config/SecurityConfig.java
- src/test/java/com/orientation/orientationapp/modules/auth/service/EmailServiceTest.java
- src/test/java/com/orientation/orientationapp/security/AuthPageContractTest.java

- docs/AUTH_EXPERIENCE_2_RAPPORT.md
- docs/AUTH_EXPERIENCE_PREUVES.json

Contrôles finaux : Gitleaks zéro détection sur les fichiers modifiés/ajoutés ; 83 migrations SQL identiques aux empreintes initiales ; base privée arrêtée et conservée. Ce scan ciblé ne constitue pas un nouvel audit complet de l’historique.
