# Connexion Google — conception et configuration restantes

9 novembre 2026. Le propriétaire a fourni un client Android et confirmé son type. Aucun client Application Web n'est encore fourni ; voir IDENTITE_CONTACTS_OAUTH.md pour l'identifiant Android.
**Aucun flux Google opérationnel n'est livré ou annoncé.** Le bouton Angular est désactivé,
avec un message explicite. Aucune bibliothèque externe ni faux endpoint Google ajouté.

## Architecture retenue pour l'implémentation suivante

- Client OAuth Web confidentiel côté Spring Boot avec Spring Security OAuth2 Client.
- Authorization Code + PKCE S256, scopes openid/email/profile, state et nonce aléatoires, stockage temporaire serveur, cookie de corrélation HttpOnly/Secure/SameSite=Lax.
- Validation serveur via les métadonnées OIDC Google : signature, issuer, audience, expiration et nonce. Ne jamais accepter une identité transmise uniquement par Angular.
- Identité stable par couple issuer/sub, avec contrainte d'unicité dans une nouvelle migration revue. Pas de liaison automatique par simple correspondance d'e-mail ; liaison d'un compte existant uniquement après réauthentification Orientia et consentement explicite.
- Nouvelle inscription uniquement avec le tenant public configuré et le rôle CANDIDAT ; aucune élévation de droits. Refus d'un compte désactivé/verrouillé selon les règles existantes.
- Réutiliser l'émission JWT/refresh et les contrôles tenant existants après validation de l'identité liée. Aucun jeton Google ou Orientia dans l'URL ; code d'échange court, à usage unique, lié au navigateur, puis échange POST.
- Redirections limitées aux URI exactes configurées ; aucune returnUrl externe. Annulation/erreur vers un écran fixe, sans token ni diagnostic fournisseur dans l'URL.
- Éviter de changer la chaîne JWT stateless globale : isoler le stockage temporaire du handshake OIDC ; vérifier CSRF et nettoyage/expiration du flux dans les tests.

## Google Cloud Console

1. Créer/configurer le projet et l'écran de consentement, support, domaines et politique de confidentialité.
2. Créer un client de type Application Web ; définir les comptes de test tant que le consentement reste en mode test.
3. Fixer l'URL publique finale, puis enregistrer l'URI de retour exacte. Convention proposée, **pas encore un endpoint implémenté** : `https://orientation-backend-d0ys.onrender.com/login/oauth2/code/google`.
4. Stocker exclusivement côté Render le client ID et le secret. Variables prévues : `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`, `GOOGLE_REDIRECT_URI`, `GOOGLE_ENABLED=false` jusqu'à validation. Ces variables ne sont pas consommées par le code actuel.
5. Exécuter les tests de compte nouveau/existant, liaison explicite, refus de rôles/tenant, compte bloqué, mauvais state/nonce/audience/signature, annulation et rejeu avant activation.

Aucun secret n'est requis dans Angular. L'absence de configuration n'empêche pas
les formulaires e-mail/mot de passe existants. Ne pas activer le bouton avec un
simple changement visuel : l'implémentation serveur et sa validation restent à faire.

Références officielles :
- https://developers.google.com/identity/openid-connect/openid-connect
- https://docs.spring.io/spring-security/reference/servlet/oauth2/login/core.html
- https://docs.spring.io/spring-security/reference/servlet/oauth2/client/authorization-grants.html
