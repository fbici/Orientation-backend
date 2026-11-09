# Inventaire initial - Backoffice Orientia

8 novembre 2026. Depot propre, main 019c9b1f. Inventaire avant corrections.

| Zone | Anomalie constatee dans le code |
|---|---|
| Tableau partage | Filtre vide teste sur data au lieu de filteredData, tri lexical des nombres, pagination pendant chargement, tri non accessible au clavier |
| Modales | Confirmation active pendant confirmLoading, fermeture pendant sauvegarde, absence de role dialog |
| Utilisateurs | Recherche non envoyee, aucune navigation de page, erreurs silencieuses, roleCode au lieu de roleId, update oblige un password ignore par le service |
| Creation utilisateur | Service create leve toujours une erreur ; ne pas promettre une creation fonctionnelle |
| Roles/equipes/departements/parametres | Aucun controleur correspondant aux endpoints annonces ; boutons trompeurs, indicateurs fictifs dans les parametres |
| Administration | Boutons Ajouter et menu de ligne sans action ; recherches et comptes limites a la premiere page |
| Organisations/tenants | Pas de pagination visible, erreurs silencieuses, compteurs non fournis presentes comme zero |
| Dashboard | Graphiques attendent une ancienne forme Analytics, periodes sans effet, valeurs sante fabriquees, connexions DB confondues avec threads |
| Monitoring | Attend services/systemHealth au lieu de healthChecks/systemMetrics ; valeurs par defaut inventees, backend mesure aussi des constantes |
| Notifications | Chargement simule 500ms, erreurs silencieuses, actions non protegees pendant traitement |
| Profil | Bouton MFA non raccorde, edition telephone sans explication de son absence dans UserInfo |
| Guards | Refus de role renvoie un utilisateur connecte vers login plutot qu une page autorisee |

Modules presentes : dashboard, analytics, monitoring, universites et details,
catalogues campus/facultes/programmes/bourses, guides, recommandations, documents
et revue, imports, regles, intelligence/decisions, knowledge/recherche,
notifications, administration utilisateurs/roles/organisations/tenants/equipes/
departements, parametres, rapports (decisions), profil et authentification.
Sources : composants, services et routes Angular compares aux controleurs et DTO
Spring locaux. Aucun compte admin de production utilise ni mutation distante.
Cette liste n est pas une preuve d execution de chaque action en production.
