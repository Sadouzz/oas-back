Auteur : Codex | Date : 2026-11-02 | Action : préciser le verrouillage de l'OR et la révalidation du proforma

# Intégrité de facturation

## Émission d'une facture

Une facture créée depuis un ordre de réparation doit reprendre le client et le véhicule rattachés à cet ordre. Le serveur verrouille l'ordre pendant l'émission, vérifie qu'aucune facture n'y est déjà liée et refuse une seconde émission. Les chemins de création manuelle, automatique et de conversion d'un proforma partagent ce verrou afin que deux requêtes concurrentes ne puissent pas émettre deux factures pour le même ordre.

Lorsqu'un proforma existe pour l'ordre, il doit être accepté avant l'émission. La facture conserve son lien avec le proforma source et celui-ci passe au statut `CONVERTIE`. Un proforma converti ne peut plus être modifié, supprimé, accepté ou refusé. L'index unique partiel sur `factures.proforma_id` protège également le lien source au niveau de la base. Les factures historiques dont ce lien est inconnu restent sans `proforma_id`.

Le paiement de la facture n'est pas une condition pour passer à `PRET_A_LIVRER` ni pour restituer le véhicule : le garage peut livrer un véhicule avant règlement. L'interface permet donc de poursuivre depuis l'étape de paiement sans facture soldée. Elle utilise la facture intégrée à la réponse de l'étape OR; elle ne déduit pas son absence d'une page de factures paginée. Les reçus verrouillent la facture pendant le calcul du reste à payer afin que des encaissements simultanés ne dépassent pas le solde.

## Ordre de réparation et proforma

Le passage de l'OR au statut `ORDRE_DE_TRAVAIL` marque sa validation. À partir de ce point, les étapes Réception, Diagnostic et Pièces/MO ne sont plus accessibles ni modifiables; le serveur refuse également les changements de statut vers une étape antérieure et les mises à jour directes des lignes de l'OR. Le proforma reste accessible depuis le parcours de l'OR.

Un proforma non converti et non réglé peut être modifié depuis cette étape, même après son acceptation. La sauvegarde invalide l'acceptation précédente en remettant le statut à `EN_ATTENTE` et en le masquant de l'espace client. L'agent doit donc republier ou valider la version modifiée avant de poursuivre. Un proforma converti en facture ou déjà réglé reste immuable.

## Autorisations financières

La création et la consultation générale des reçus ainsi que la gestion des avoirs HT/TTC sont réservées aux rôles d'exploitation autorisés. Un client peut consulter uniquement l'historique de ses reçus via `/api/recus/me`. L'ajout de crédits sur un compte client reste réservé au Master et au Super Agent.

## Migration

Avant le déploiement, exécuter `src/main/resources/db/manual/20261021_facture_proforma_unique.sql` dans la base PostgreSQL concernée. Cette migration ajoute uniquement le lien nullable vers le proforma, sa clé étrangère et son index unique partiel; elle ne modifie ni les montants ni les factures existantes.
