# Orientia — identité, contacts et clients OAuth

9 novembre 2026. Directives du propriétaire à appliquer à la prochaine refonte.

## Direction visuelle

- Fond blanc, formulaires professionnels centrés, très peu de texte introductif.
- Couleurs de l'application mises en avant ; supprimer les effets néon/futuristes.
- Conserver les animations, leurs états HTTP réels et prefers-reduced-motion.
- Pages connexion, inscription et vérification, avec cohérence pour la récupération.
- Le propriétaire a ensuite demandé de poursuivre sans images de référence, en suivant ses consignes et des ressources publiques.
- Utiliser le logo existant Orientation-mobile/assets/images/orientia.png, inspecté visuellement : identité Orientia bleu/vert/violet. Aucun chapeau d'étudiant comme logo.
- Remplacer la lettre G simple par l'asset officiel Google, sans dessiner une imitation.
- La correction du bouton n'active pas le flux Google, encore à implémenter/configurer.

## Adresses officielles fournies

- Contact public, informations du site et support : **lestudioviral@gmail.com**.
- Notifications concernant l'administration : **adminorientia@gmail.com**.

Aucun e-mail envoyé, aucun réglage Render ou SendGrid changé. Les e-mails de
vérification et de récupération doivent rester adressés au titulaire du compte :
ne jamais rediriger ces liens personnels vers la boîte administrative. Le flux e-mail administratif a désormais été raccordé aux notifications destinées à un administrateur actif et aux étapes documentaires READY_REVIEW/FAILED d'un administrateur. Les tests utilisent des mocks ; aucun envoi réel de validation effectué.

Pour utiliser lestudioviral@gmail.com comme expéditeur transactionnel, vérifier
cet expéditeur dans SendGrid puis renseigner SENDGRID_FROM_EMAIL côté hébergement.
Le choix d'une adresse de contact n'effectue pas cette vérification.

## OAuth Android — informations réellement lues

- Package/applicationId et namespace : **com.orientia.orientia**.
- Source : android/app/build.gradle.kts, et non le nom Dart de pubspec.yaml.
- Certificat local : debug.keystore, alias androiddebugkey.
- SHA-1 : **0A:DB:07:6A:F5:CE:ED:C7:37:77:AC:77:66:61:4C:2A:EC:86:D5:69**.
- SHA-256 : B5:EC:F0:D4:05:F2:C4:0E:73:00:BB:20:8E:60:3B:DB:E5:B4:CC:5F:69:30:5D:EF:33:A5:62:CB:89:0B:4B:96.

Empreinte de développement de cette machine. Le build release utilise aussi la
signature debug actuellement : ce n'est pas une signature de publication définitive.
Pour Google Play, enregistrer le SHA-1 du certificat de signature de l'application
fourni par Play Console, éventuellement différent de la clé d'envoi.
Aucune clé ni configuration de signature modifiée.

## Client OAuth Web distinct

Le package et le SHA-1 concernent Android. Pour Angular, créer un autre client
de type Application Web dans le même projet Google Cloud.
Origine actuelle : https://orientation-backend-d0ys.onrender.com (sans chemin).

Le callback proposé dans la conception est /login/oauth2/code/google, mais il
n'est pas implémenté actuellement. Sa configuration finale devra correspondre
exactement au flux serveur livré. Google ne devient pas fonctionnel après la seule
création du client. Aucun secret Web dans Angular, Flutter ou Git.

Références officielles :
- https://codelabs.developers.google.com/sign-in-with-google-android
- https://developers.google.com/identity/branding-guidelines
- https://developers.google.com/identity/gsi/web/guides/get-google-api-clientid


## Client Android fourni et confirmé par le propriétaire

1048361062873-9p9jrn2umec435bddqen2epn6fur93d8.apps.googleusercontent.com

Type confirmé : Android. Identifiant public, conservé pour la future intégration
mobile. Il n'est pas branché comme client Web Angular et ne constitue pas un secret.
Un client Web distinct reste nécessaire pour le flux d'authentification du navigateur.
