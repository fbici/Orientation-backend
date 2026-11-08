# Orientia - Nouveau deploiement Render (8 novembre 2026)

Ce guide remplace, pour le nouveau deploiement, les reserves concernant l'ancien
service dans RENDER_VALIDATION_F1_F33.md. L'ancien backend est supprime et
l'ancienne base inaccessible selon le proprietaire. Aucune ressource Render ni
base de production n'a ete creee, consultee ou modifiee pendant cette mission.

## Service et commandes exactes

- Type : Web Service Docker, avec disque persistant pour les documents.
- Depot : https://github.com/fbici/Orientation-backend.git
- Branche : main. Root directory : racine du depot (champ vide).
- Language/Runtime : Docker. Dockerfile Path : ./Dockerfile. Contexte : .
- Build Command Render : aucun champ de commande native a renseigner ; Render
  construit le Dockerfile. Equivalent local : `docker build -t orientia .`.
- Docker Command : laisser vide pour conserver l'ENTRYPOINT du Dockerfile.
- Commande effective, WORKDIR /app :
  `java -Djava.awt.headless=true -jar app.jar --spring.profiles.active=prod`
- Java : 21 (Temurin builder et JRE runtime). Spring Boot : profil prod.
- Build interne frontend : `npm ci`, puis
  `npx ng build --configuration=production` dans /app/frontend, Node 22.
- Build interne backend : `mvn clean package -DskipTests -B`. Les tests sont
  executes separement par Maven verify et la CI avec PostgreSQL isole ; le
  Dockerfile ne pretend pas les executer.
- Health Check Path : /actuator/health. PORT est fourni par Render, bind 0.0.0.0.
- Le contexte prod est / : API /auth/login, /transcripts, etc., SANS /api/v1.
- Angular est construit et embarque dans le JAR ; pas de second service requis.
- WebSocket Angular prod utilise /ws sur la meme origine, sans ancien hostname.
- Preferer un service disposant de memoire suffisante pour JVM et OCR ; mesurer
  la consommation en recette. Le choix de plan et toute creation restent manuels.
- Conserver l'auto-deploiement desactive pendant le premier parametrage ; deployer
  manuellement le SHA valide apres saisie des nouvelles variables.

Render construit les images Docker lui-meme ; ne pas mettre une commande Maven
native dans un service Docker : https://render.com/docs/docker
Le disque persistant necessite un service payant et seul son chemin monte est
conserve : https://render.com/docs/disks

## Variables (aucune valeur secrete dans Git)

| Variable | Configuration attendue |
|---|---|
| SPRING_PROFILES_ACTIVE | prod ; egalement fixe dans l'ENTRYPOINT |
| SPRING_DATASOURCE_URL | URL JDBC de la NOUVELLE base ; gabarit ci-dessous |
| SPRING_DATASOURCE_USERNAME | utilisateur de la nouvelle base, saisi dans Render |
| SPRING_DATASOURCE_PASSWORD | NOUVEAU secret, saisi dans Render |
| JWT_SECRET | NOUVELLE valeur aleatoire, au moins 32 octets, jamais un ancien defaut |
| APP_BASE_URL | veritable URL HTTPS du NOUVEAU service, sans / final ni /api/v1 |
| CORS_ALLOWED_ORIGINS | origines HTTPS reelles, separees par virgule ; pas de joker avec credentials |
| SENDGRID_ENABLED | true pour inscription et recuperation ; false desactive ces parcours avec 503 |
| SENDGRID_API_KEY | NOUVELLE cle privee, permission Mail Send, saisie dans Render |
| SENDGRID_FROM_EMAIL | expediteur verifie SendGrid, obligatoirement renseigne |
| PASSWORD_RESET_URL | facultatif : URL HTTPS de la page de reset ; par defaut APP_BASE_URL/auth/reset-password |
| REGISTRATION_TENANT_CODE | DEFAULT, tenant actif cree par les migrations |
| STORAGE_DIR | /app/uploads, chemin du disque monte et inscriptible par UID/GID 10001 |
| TESSERACT_COMMAND | tesseract (binaire installe dans l'image) |
| OCR_LANGUAGES | fra+eng (paquets installes) |
| TESSDATA_DIR | facultatif pour les paquets Debian standards |
| OCR_TIMEOUT_SECONDS | 45 par defaut |
| BOOTSTRAP_ENABLED | false normalement ; procedure ponctuelle ci-dessous |
| BOOTSTRAP_EMAIL | seulement pour la creation initiale explicite de l'administrateur |
| BOOTSTRAP_PASSWORD | NOUVEAU secret robuste 16-72 caracteres et <=72 octets UTF-8, hors Git |
| BOOTSTRAP_TENANT_CODE | DEFAULT pour l'amorcage initial |
| PORT | gere par Render, ne pas imposer une adresse locale |

Ne pas utiliser DB_PASSWORD, H2, un .env local, une URL DATABASE_URL postgresql://
non convertie ou les secrets de l'ancien service pour le profil prod. Le gabarit
suivant n'est pas une adresse existante : remplacer HOST_INTERNE et NOUVELLE_BASE
par les champs de connexion de la nouvelle base ; utilisateur/mot de passe restent
separes de l'URL :

`jdbc:postgresql://HOST_INTERNE:5432/NOUVELLE_BASE?sslmode=require`

Choisir PostgreSQL 16 pour rester dans la plage prise en charge par la version
Flyway embarquee et correspondre au service de CI. Le SQL exige au minimum 15 a
cause de V74. Les tests locaux passent sur 18.4, avec un avertissement Flyway
indiquant une prise en charge testee jusqu a 17 : ce succes ne vaut pas
certification de support de PostgreSQL 18. L execution distante sur 16 doit etre
verifiee apres push, sans assimiler sa configuration a un resultat de test. Utiliser la meme region que le service
Web et les champs reseau interne proposes par Render ; conserver TLS require.
Versions proposees et TLS : https://render.com/docs/postgresql-creating-connecting

## Flyway : uniquement une NOUVELLE base vide

1. Provisionner manuellement la nouvelle base apres choix du plan/region/version.
2. Confirmer qu'elle ne contient aucun schema metier ni historique a reprendre.
3. L'utilisateur de migration doit pouvoir creer tables, indexes, fonctions et
   extensions uuid-ossp et pgcrypto (V1). Aucun acces superutilisateur global n'est
   presuppose ; verifier les droits offerts par Render.
4. Configurer les nouvelles variables JDBC, JWT, URL et stockage.
5. Demarrer le JAR avec prod : Flyway execute les 83 migrations dans l'ordre de
   V1 a V82, dont V63.1 avant V64 ; Hibernate valide ensuite le schema.
6. Conserver flyway.enabled=true, baseline-on-migrate=false, clean-disabled=true,
   ddl-auto=validate et sql.init.mode=never. Aucun baseline, repair, clean ou
   outOfOrder n'est necessaire pour une base vide.
7. Verifier les journaux Flyway, la sante /actuator/health et le backoffice.
8. Verifier l'absence de compte administrateur connu avant amorcage.

V38 a ete revisee pour RETIRER le compte administrateur et son ancienne valeur
publique. Son checksum differe de l'ancienne version : cette livraison vise une
base vierge. Ne jamais la lancer sur une base ayant deja applique l'ancienne V38 ;
une reprise eventuelle exige une strategie distincte, pas un repair automatique.
Les autres migrations ne sont pas reecrites. L'historique Git ancien est conserve.

## Premier administrateur

Aucun compte administrateur ni mot de passe public n'est cree par Flyway.
Configurer ponctuellement BOOTSTRAP_ENABLED=true, BOOTSTRAP_EMAIL,
BOOTSTRAP_PASSWORD et BOOTSTRAP_TENANT_CODE=DEFAULT. Le mot de passe doit satisfaire
les controles de robustesse du bootstrap (majuscule, minuscule, chiffre, symbole).
Cette operation est manuelle et cree un compte deja verifie. Apres le premier
demarrage reussi, mettre BOOTSTRAP_ENABLED=false et supprimer BOOTSTRAP_PASSWORD
de Render. Ne pas laisser true lors du redeploiement suivant : le bootstrap refuse
de modifier un administrateur existant, y compris un compte supprime logiquement.
La base du service nouveau est definitive : toute migration future devra respecter
ses checksums et etre additive.

## SendGrid : fonctions et limites reelles

L'integration conserve l'API HTTPS /v3/mail/send et Authorization Bearer. Les trois
variables SENDGRID_API_KEY, SENDGRID_ENABLED et SENDGRID_FROM_EMAIL sont branchees
sur les proprietes Spring prod. APP_BASE_URL est aussi obligatoire pour les liens.
PASSWORD_RESET_URL etait consomme par le service ; il est maintenant raccorde a une
variable explicite. Aucune cle de production n'a ete creee ou teste en reseau.

Parcours existants : inscription -> demande de verification ; renvoi pour un compte
eligible non verifie ; recuperation de mot de passe -> lien vers la page de reset.
La verification consomme ensuite le jeton, sans nouvel email. La methode d'email de
bienvenue existe mais aucun appel actif n'a ete trouve. Les notifications du
backoffice sont internes/WebSocket ; aucun envoi SendGrid de notification n'a ete
trouve. Aucun parcours fictif n'est annonce.

Les refus journalisent uniquement HTTP status ; les erreurs IO/interruption ont
un message generique. Ni cle, Authorization, destinataire, contenu, jeton de lien
ni corps d'erreur fournisseur ne sont journalises. Les tests utilisent HttpClient
simule : aucune connexion SendGrid ni livraison reelle. Une reponse acceptee ne
prouve pas la livraison dans la boite destinataire. La recette reelle attend une
autorisation distincte et un expediteur/domaine verifie :
https://www.twilio.com/docs/sendgrid/for-developers/sending-email/sender-identity
https://www.twilio.com/docs/sendgrid/api-reference/mail-send/mail-send

## Flutter apres publication de la nouvelle URL

L'ancienne URL n'est plus proposee par l'aide Android. Aucune nouvelle URL n'est
inventee. A la racine du depot mobile, une fois le service reel disponible :

```powershell
$env:ORIENTIA_API_URL = Read-Host 'URL HTTPS reelle du nouveau backend Render'
dart run tool/android_phone_command.dart --api-url $env:ORIENTIA_API_URL
```

L'aide affiche la commande exacte pour le telephone detecte : flutter run avec
APP_ENV=development et API_BASE_URL egal a l'URL saisie, sans /api/v1. Le package
com.orientia.orientia et la signature ne sont pas modifies pour forcer une
installation. Pour une livraison signee, utiliser APP_ENV=production et une
signature configuree hors Git. Aucun APK historique ne remplace ce parametrage.

## Limites et conditions avant la mise en ligne

- Nouvelle URL, secrets, base, disque, droits et compte SendGrid a configurer par
  le proprietaire ; aucune de ces ressources n'a ete creee par cet audit.
- L'ancien historique contient des valeurs publiques JWT/seed ; les nouvelles
  variables ne doivent en reutiliser aucune. Une purge historique demanderait une
  operation distincte ; aucun force-push ou historique reecrit ici.
- Docker indisponible sur la machine d'audit : build d'image non execute. Le JAR,
  Angular et le demarrage prod isole sont verifies et leurs resultats figurent
  dans le rapport final ; la construction de l'image sur Render reste a observer.
- Conservation des fichiers : verifier permissions UID 10001 et ecriture effective
  sur le disque, sauvegardes de la nouvelle base et des documents.
- La validation locale est une preuve sur une base privee neuve, jamais une preuve
  de l'etat d'une base ou d'un service Render non crees.

## Validation locale finale

Le 8 novembre 2026 : Maven verify reussi, 195 tests dans 32 suites, aucun echec,
erreur ou test ignore ; 42 tests Angular et build production reussis ; Flutter
analyze sans anomalie et 32 tests reussis dans le depot mobile.

Le JAR embarquant Angular a ete execute avec prod depuis un nouveau dossier sans
.env et avec environnement minimal. Une nouvelle base vide a ete creee uniquement
sur le cluster prive 127.0.0.1:59745, PostgreSQL 18.4. Resultat : 83 migrations
reussies, aucune echouee, aucun utilisateur seed, health UP, GET /,
/auth/login et /auth/reset-password en 200 HTML. Les routes POST restent couvertes
par la suite metier. Le processus de test a ete arrete ; aucune adresse Render ni
cle SendGrid reelle n'a ete utilisee. L'avertissement Flyway relatif a PostgreSQL 18
est conserve dans les limites ci-dessus.

Le scan Gitleaks courant/historique est complete par une comparaison sans affichage
des anciennes valeurs JWT et d'amorcage : aucune occurrence ne reste dans les
fichiers courants publiables. Deux alertes connues de SPRINT_F3_2_PREUVES.json sont
les empreintes des migrations V68 et V37, verifiees contre les fichiers. L'historique
ancien conserve des valeurs retirees ; sa purge n'est pas realisee avec un push
normal et aucun force-push n'a ete execute.
