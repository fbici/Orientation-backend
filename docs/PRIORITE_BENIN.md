# Orientia : le Bénin d'abord

9 novembre 2026 — exigence produit explicite du propriétaire.

Le catalogue doit couvrir les établissements béninois publics **et privés**, leurs
campus, formations, critères d'admission et sources. La couverture internationale
vient ensuite. Un filtre « privé » ne doit jamais être exclu par défaut.

## Situation vérifiée dans les sources locales

- `University` possède déjà `institutionType`, `sourceUrl` et `verified`, ainsi qu'un pays et une ville.
- V58 et V64 contiennent des données béninoises. Le titre « complete » de V64 ne constitue pas une preuve d'exhaustivité : certaines données sont conditionnelles à la présence préalable d'établissements.
- Les données historiques n'ont pas toutes une source et un statut de vérification renseignés. Aucun agrément ne doit être déduit d'un simple nom présent dans une migration.
- Aucune donnée de production n'a été consultée ni modifiée pendant cette mission.

## Mesure sur la base isolée reconstruite

Audit SQL réellement exécuté dans le schéma de test `auth_20261109` : **5 fiches béninoises**, toutes sans type renseigné, sans source et non vérifiées : UAC, UP, ESGIS, ENA et INMA. Ce sont les données historiques de test, pas un inventaire officiel ni un relevé de production. UNA et UNSTIM, présentes sur la page officielle du ministère, ne figurent pas dans ce résultat. La couverture du privé n'est pas mesurable sans la liste officielle complète attendue.

Aucune insertion ni migration n'a été faite pour masquer ces lacunes.

## Sources de référence consultées

- Universités publiques : https://enseignementsuperieur.gouv.bj/enseignement/superieur/universite — page mentionnant UAC, UP, UNSTIM et UNA.
- Établissements privés : https://enseignementsuperieur.gouv.bj/enseignement/superieur/etablissements_prives — annonce une liste officielle actualisée, mais le contenu récupérable ne fournit pas la liste complète.
- https://guide.apresmonbac.bj — non accessible avec l'outil de consultation lors de la vérification.

Ces pages ont été consultées le 9 novembre 2026. La liste complète et datée des
établissements privés autorisés doit encore être obtenue et rapprochée du catalogue.
**L'exhaustivité nationale n'est pas acquise ni annoncée.** Aucun établissement,
agrément, frais ou critère d'admission n'a été inventé pour combler les absences.

## Critères de réception du référentiel

1. Obtenir l'édition officielle datée du MESRS ; conserver source, date et périmètre d'autorisation.
2. Rapprocher chaque établissement public et privé par identifiant/noms usuels, ville et campus ; traiter les doublons sans fusion automatique destructive.
3. Distinguer établissement, campus, faculté et formation ; l'autorisation d'un établissement ne prouve pas celle de toutes ses formations.
4. Renseigner le type public/privé selon les conventions de l'API, la source et la vérification après contrôle humain.
5. Produire un tableau attendus/présents/manquants/à confirmer, avec couverture par type et ville ; compléter uniquement via un import relu et testé sur une base isolée.
6. Valider recherche, filtres et recommandations avec des cas publics et privés au Bénin avant les extensions internationales.

`AUDIT_CATALOGUE_BENIN.sql` permet un diagnostic **en lecture seule** des fiches,
types, sources et vérifications. Il ne mesure pas à lui seul l'exhaustivité : il
faut la liste officielle attendue. Ne pas lancer contre la production sans accès
et autorisation appropriés. Les migrations existantes restent intactes.
