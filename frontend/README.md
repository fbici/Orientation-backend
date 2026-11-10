# Orientia — backoffice Angular

Angular 19 standalone, signals, routes lazy, composants partagés. Ce README documente le projet Angular ; [le README backend](../README.md) couvre déploiement, migrations, sécurité, SendGrid et données sources. Dernière consolidation : 10 novembre 2026. Mettre à jour ces README à chaque évolution, sans créer de nouveau rapport de sprint ou fichier de preuves.

## Commandes

```powershell
npm ci
npm start
npm run build:prod
npm test -- --watch=false --browsers=ChromeHeadless
```

Node 22 pour le build Docker. Production : Angular embarqué dans le JAR Spring, même origine API et WebSocket `/ws`. Sortie : `dist/orientation-backoffice/browser`. Ne pas commiter dist, node_modules ou des secrets. Le build Docker construit Angular avant Java ; une simple modification locale n'actualise pas le service Render déjà déployé.

## Parcours et corrections conservées

| Module | Comportement |
|---|---|
| Authentification | Connexion, inscription, récupération/reset et vérification avec états loading/error/success ; retour interne contrôlé |
| Utilisateurs/organisations | Création/mise à jour distinctes, paramètres de pagination conformes, validations et droits côté serveur ; pas de mot de passe artificiel lors d'une édition |
| Notifications | Pagination et unread-count globaux réels, erreurs affichées, actions protégées ; WebSocket authentifié et scopes conservés |
| Dashboard | Séries datées et périodes réelles, absence de métriques non mesurées explicitée |
| Monitoring/audit | JSON réel, CPU JVM/connexion DB lorsqu'exposés, audit paginé ; pas de latence/charge fictive |
| Universités | Filtres pays/type/statut, identifiants plats pays/ville, erreur distincte du vide ; ville facultative pour une source incomplète, envoyée comme null |
| Guides/documents/imports | Contrats pipeline, revue, sources et publication conservés ; aucune publication documentaire fictive |
| Recommandations/décisions | Historique, simulations, comparaison, explications, exports selon API et permissions |
| Connaissance/recherche | Parcours documentaires et liens conservés ; résultat de recherche distinct d'une éligibilité |
| Rôles/équipes/départements/paramètres/MFA | Actions sans API complète désactivées avec motif ; ne pas prétendre qu'elles sont sauvegardées |
| Analytics/rapports | Inconnus/non instrumentés signalés ; pas d'agrégat inventé pour masquer une API indisponible |

## Présentation Web actuelle

La refonte Web blanche a remplacé Auth Experience 2.0 sombre : formulaire centré, largeur contenue, titres courts, vrais labels, champs arrondis, œil du mot de passe, vrai logo Orientia et G Google. Entrée des champs, transition connexion/inscription, pression/reflet du bouton, chargement, erreurs/succès et anneau de vérification restent animés ; `prefers-reduced-motion` est pris en compte.

Le 10 novembre, un navigateur neuf sur `https://orientation-backend-d0ys.onrender.com/auth/login` a affiché cette version en 1440×1000 et 390×844, avec logos chargés et sans débordement. Ce contrôle public ne valide pas les actions administrateur privées. La nouvelle référence violette fournie pour les **trois écrans mobiles** est implémentée dans Flutter ; elle ne prétend pas avoir refondu l'ensemble des écrans Angular authentifiés.

Les assets de marque sont dans `src/assets/brand/`. Le logo appartient au projet ; le G provient des ressources Google ([règles officielles](https://developers.google.com/identity/branding-guidelines)). Aucune clé Google ou SendGrid n'est dans Angular. Le flux Google du backoffice reste désactivé avec explication ; le parcours navigateur Authorization Code/PKCE et son callback restent à implémenter. Le nouveau flux natif Android POST `/auth/google` ne constitue pas une connexion Google navigateur ; son client Application Web public est désormais fourni et le profil prod est préconfiguré. Le parcours natif dépend toujours du déploiement effectif et des éventuelles surcharges Render, comme décrit dans le README backend.

## Bénin, contacts et validation

Le registre MESRS 2025–2026 est importé par la migration backend V83 : fiches à revoir, aucune ville ou formation inventée. Les 117 identités ne sont pas automatiquement publiées aux candidats ; vérifier source, validité actuelle et statut avant publication. Le détail, les comptes exacts et la procédure figurent dans le README backend.

Contact public : `lestudioviral@gmail.com`. Notifications administratives : `adminorientia@gmail.com`, traitées côté Spring/SendGrid selon les règles du README backend.

Dernière validation exécutée : **86 tests ChromeHeadless réussis et build production réussi**, dont la transmission d'une ville inconnue comme null. Avertissements connus : budget CSS du layout et sockjs-client CommonJS. Les 42 puis 67 et 85 tests des campagnes antérieures restent des résultats historiques. Des captures précédentes étaient réalisées avec des réponses synthétiques locales ; elles ne prouvent pas une recette fonctionnelle de production. Aucun import ni modification de données de production n'a été effectué pour ces contrôles.

## Téléphone du profil

Le compte accepte les numéros internationaux précédés de `+` et de l’indicatif. Spring revalide et normalise avec libphonenumber ; le champ facultatif vide reste accepté. Le format béninois actuel est `+229 01 XX XX XX XX` (dix chiffres nationaux), sans limiter les autres pays. Aucun SMS de vérification n’est envoyé.

Vérification courante : `npm run build:prod` réussi après correction de l’aide téléphone du profil. Les avertissements existants restent le budget CSS du layout et le module CommonJS `sockjs-client`. Les 86 tests de la campagne précédente ne sont pas comptés comme une nouvelle exécution.
