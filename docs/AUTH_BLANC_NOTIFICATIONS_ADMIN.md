# Orientia — formulaires blancs et notifications administratives

9 novembre 2026. Cette version remplace la présentation sombre de Auth Experience 2.0,
selon les nouvelles consignes du propriétaire. Validation locale avant publication ; aucune modification
manuelle de Render, aucune donnée de production modifiée, aucun e-mail réel envoyé
pendant les tests.

## Présentation livrée

Pages connexion, inscription, vérification, récupération et réinitialisation :
fond blanc, largeur contenue et centrage, titres courts, champs de 50 px, bordures
discrètes, focus visible, labels associés, mots de passe avec bouton œil intégré,
espacements cohérents et adaptation aux petits écrans. Le texte d'introduction a
été réduit. Les halos, le fond sombre et le panneau futuriste ont été retirés.

Le logo existant du mobile est copié à l'identique dans les assets Angular, sans
retouche, et remplace le chapeau sur les pages auth ainsi que dans la marque de la barre latérale. Le fichier contient son propre
fond clair : celui-ci est conservé avec le logo original. Le bouton Google affiche
l'asset officiel téléchargé auprès de Google, pas une lettre dessinée en CSS.

Les animations conservées : entrée des formulaires/champs, transition Login/Register,
pression et reflet du bouton, indicateur droit de chargement, feedback d'erreur,
succès et anneau de vérification. Les animations de fond ont été retirées avec le
style néon. Le mode prefers-reduced-motion est conservé.

Le contact d'aide pointe vers **lestudioviral@gmail.com**. Aucun expéditeur SendGrid
vérifié existant n'a été remplacé par cette adresse de contact.

Références de composition consultées, sans installation de React ni de dépendance :
- https://ui.shadcn.com/blocks/authentication (formulaire simple et centré).
- https://developers.google.com/identity/branding-guidelines (logo/bouton Google).

## Google : type du client confirmé

Identifiant fourni :
1048361062873-9p9jrn2umec435bddqen2epn6fur93d8.apps.googleusercontent.com

Le propriétaire confirme le type **Android**. Cet identifiant public est documenté
pour le mobile ; il n'est pas injecté artificiellement dans Angular comme client
Web. Le flux Google reste non opérationnel et le bouton Web désactivé avec message
explicite. Un client Application Web distinct et le raccordement serveur OIDC
restent nécessaires, conformément à AUTH_GOOGLE_CONFIGURATION.md.
Aucun secret demandé, enregistré ou ajouté au frontend.

## Notifications administratives réellement raccordées

Destinataire par défaut : **adminorientia@gmail.com**.

1. Notification enregistrée par NotificationService.create pour un compte actif
   ADMIN ou SUPER_ADMIN : événement publié sans titre/message privé ; mail après
   validation de la transaction.
2. Pipeline documentaire existant : événements READY_REVIEW et FAILED déclenchés
   pour un administrateur actif ; les étapes intermédiaires ne génèrent pas d'e-mail.
3. Un candidat, un compte verrouillé ou un tenant suspendu ne déclenche pas ce
   transfert administratif.

Le destinataire central reçoit un texte générique et un lien public vers /auth/login avec un returnUrl interne vers /notifications
ou /documents. Cela évite les erreurs de navigation directe sans en-tête Bearer.
L’authentification et les contrôles d’accès aux pages cibles restent requis. Aucun contenu documentaire,
nom de fichier, relevé, adresse d'utilisateur, lien de vérification ou jeton de
récupération n'est copié dans le message. Les e-mails personnels restent adressés
à leurs titulaires.

La même intégration EmailService/SendGrid existante est réutilisée. Le traitement
est asynchrone avec 1 à 2 threads et une file bornée de 100 tâches. Les erreurs
fournisseur et la saturation sont journalisées avec un message générique et ne
font pas échouer le traitement métier. Les événements transactionnels sont traités
après commit ; les événements documentaires hors transaction utilisent le chemin
fallback après leur publication existante.

Cette file est en mémoire : elle ne garantit pas une livraison durable après
arrêt brutal, ni un renvoi automatique ou une livraison exactement une fois.
Une outbox persistante serait une évolution distincte si ces garanties sont requises.
L'acceptation HTTP par SendGrid ne prouve pas la réception dans la boîte finale.

## Configuration de production

| Variable | Usage |
|---|---|
| ADMIN_NOTIFICATION_EMAIL | Destinataire central ; défaut adminorientia@gmail.com |
| ADMIN_NOTIFICATIONS_EMAIL_ENABLED | Activation des alertes administratives ; défaut true |
| SENDGRID_ENABLED | Activation du fournisseur existant |
| SENDGRID_API_KEY | Clé existante côté serveur uniquement ; valeur jamais publiée |
| SENDGRID_FROM_EMAIL | Expéditeur vérifié existant, conservé |
| APP_BASE_URL | Origine HTTPS du backoffice, utilisée pour les liens authentifiés |

Avec SendGrid déjà correctement configuré, aucun nouveau secret n'est nécessaire
pour ce raccordement. Les deux variables ADMIN_* sont facultatives grâce à leurs
défauts. Pour désactiver les alertes, définir ADMIN_NOTIFICATIONS_EMAIL_ENABLED=false.
L'adresse de contact lestudioviral@gmail.com peut servir d'expéditeur uniquement
après vérification adéquate dans SendGrid ; elle n'a pas été imposée au fournisseur.

## Vérifications

- Build Angular production réussi ; 85 tests Angular réussis.
- Chrome local : 9 vues/états, formats 1440×1000, 1280×800, 820×1180, 390×844,
  labels présents, absence de débordement et zéro exception JavaScript.
- États loading/error/success, navigation inscription et reduced-motion contrôlés.
  Les réponses des captures sont synthétiques et confinées au harnais temporaire.
- Tests serveur ciblés : envoi après commit, absence d'envoi après rollback,
  destinataire central, contenu générique, contrôle de rôle et état du compte,
  saturation, fournisseur en échec, désactivation et payload SendGrid simulé.
- Backend : **218 tests réussis, 37 suites, aucun test ignoré**. Verify complet réussi ; les tests ciblés de messagerie ont été rejoués après correction du lien de connexion.
- Packaging final réussi ; index du JAR identique au dernier build Angular. Le vrai JAR local valide login HTTP 200, six liens HTML (dont celui des e-mails), logos identiques et API privée toujours protégée.
- Voir AUTH_BLANC_PREUVES.json pour les preuves. Aucune validation d’e-mail réel revendiquée.
- Migrations existantes conservées ; aucune nouvelle table ni migration requise.

Captures :
[Connexion ordinateur](auth-white/login-desktop.png),
[Connexion téléphone](auth-white/login-mobile.png),
[Inscription téléphone](auth-white/register-mobile.png),
[Chargement](auth-white/login-loading-mobile.png),
[Erreur](auth-white/login-error-mobile.png),
[Vérification](auth-white/verification-success-mobile.png).

Les captures ont été prises après l'animation d'entrée, sauf celles qui montrent
explicitement un état animé. Aucun test de clavier virtuel sur téléphone physique
ni validation avec un lecteur d'écran n'est revendiqué.

## Livraison

Les sources sont dans le dépôt backend existant. Le Dockerfile racine continue de
compiler Angular puis le JAR Java 21. Les e-mails ne seront actifs dans le service
hébergé qu'après publication/déploiement de cette version ; la configuration locale
ne modifie pas automatiquement le backend déjà en ligne.

## Fichiers du lot

- docs/AUTH_GOOGLE_CONFIGURATION.md
- docs/IDENTITE_CONTACTS_OAUTH.md
- docs/auth-white/browser-results.json
- docs/auth-white/login-desktop.png
- docs/auth-white/login-error-mobile.png
- docs/auth-white/login-laptop.png
- docs/auth-white/login-loading-mobile.png
- docs/auth-white/login-mobile.png
- docs/auth-white/login-tablet.png
- docs/auth-white/register-mobile.png
- docs/auth-white/verification-success-mobile.png
- frontend/src/app/auth/auth-experience.spec.ts
- frontend/src/app/auth/forgot-password/forgot-password.component.ts
- frontend/src/app/auth/login/login.component.ts
- frontend/src/app/auth/register/register.component.ts
- frontend/src/app/auth/reset-password/reset-password.component.ts
- frontend/src/app/auth/shared/auth-shell.component.ts
- frontend/src/app/auth/verify/verify.component.ts
- frontend/src/assets/brand/README.md
- frontend/src/assets/brand/google-g.png
- frontend/src/assets/brand/orientia.png
- frontend/src/styles.scss
- src/main/java/com/orientation/orientationapp/backoffice/config/AdministrativeEmailConfiguration.java
- src/main/java/com/orientation/orientationapp/backoffice/notification_center/service/AdministrativeEmailListener.java
- src/main/java/com/orientation/orientationapp/backoffice/notification_center/service/AdministrativeNotificationCreated.java
- src/main/java/com/orientation/orientationapp/backoffice/notification_center/service/AdministrativeRecipientPolicy.java
- src/main/java/com/orientation/orientationapp/backoffice/notification_center/service/impl/NotificationServiceImpl.java
- src/main/java/com/orientation/orientationapp/modules/auth/service/EmailService.java
- src/main/resources/application-prod.yml
- src/main/resources/application.yml
- src/test/java/com/orientation/orientationapp/modules/auth/service/EmailServiceTest.java
- src/test/java/com/orientation/orientationapp/security/AdministrativeEmailTest.java
- src/test/java/com/orientation/orientationapp/security/AdministrativeRecipientPolicyTest.java

Commande Angular vérifiée sous PowerShell : node node_modules/@angular/cli/bin/ng.js test --watch=false --browsers=ChromeHeadless --progress=false, depuis frontend. Le wrapper npm.ps1 absorbait le séparateur -- et lançait auparavant le mode watch ; les anciens runners de validation ont été arrêtés et une exécution unique a terminé avec le code 0.

Google Web reste à raccorder avec un client Web distinct ; les notifications administratives sont implémentées dans ce lot, avec une validation fournisseur simulée. La publication Git ne certifie pas la fin du déploiement Render.

Contrôles finaux : zéro détection Gitleaks sur le lot modifié ; 83 migrations SQL inchangées ; base privée de test arrêtée et conservée. Le scan ciblé ne constitue pas un nouvel audit de tout l’historique Git.
