Auteur : Codex | Date : 2026-10-16 | Action : retirer le plafond d'encours et documenter les contrôles financiers conservés

# Compte financier client

## Modèle métier

Le compte financier est un objet métier rattaché à un client; il ne crée aucun utilisateur ni identifiant de connexion. Son ouverture transforme le client en entreprise. Cette forme juridique est conservée après désactivation du compte. Le NINEA légal est stocké dans `clients.numero_entreprise`; le champ historique `clients.ninea` reste réservé à son ancienne valeur de justificatif.

Les remises, le solde de crédits et les conditions de paiement sont séparés. Les crédits ajoutés sont tracés dans un journal append-only et augmentent le solde disponible. À ce stade ils ne sont pas imputés automatiquement aux factures: aucune règle d'affectation ou de remboursement automatique n'est spécifiée.

## Conditions financières

Le contrôle de facturation conserve le délai de paiement en jours et un plafond optionnel sur le montant total des factures émises pendant cette période glissante. Les factures payées comptent dans ce cumul. Le total restant dû peut être consulté à titre informatif, mais aucun plafond d'encours n'est configuré ni appliqué.

La création de proforma par un agent reste autorisée. Le serveur calcule et renvoie les alertes de facture échue ou de plafond de période; l'interface agent les affiche après l'enregistrement. Les clients ne disposent pas de route de création de proforma.

La réservation créée depuis l'espace client est refusée côté serveur lorsqu'une facture est échue et impayée ou que le plafond de facturation de la période est atteint. Le total restant dû, à lui seul, ne bloque pas une réservation. Les réservations créées par les agents restent hors de ce contrôle.

## Routes administrateur

- `GET /api/clients/{id}/compte-financier` : profil financier et agrégats calculés côté serveur.
- `PUT /api/clients/{id}/compte-financier` : ouvre ou modifie le compte et les conditions; réservé à MASTER et SUPER_AGENT.
- `PUT /api/clients/{id}/conditions-financieres` : modifie les conditions communes sans convertir un particulier en entreprise; réservé à MASTER et SUPER_AGENT.
- `POST /api/clients/{id}/compte-financier/credits` : ajoute un crédit et une ligne d'audit; réservé à MASTER et SUPER_AGENT.
- `GET /api/clients/{id}/compte-financier/mouvements` : renvoie au plus les 100 derniers mouvements sans sérialiser l'entité Client.
- `DELETE /api/clients/{id}/compte-financier` : désactive le compte sans effacer l'historique ni les conditions d'échéance.

## Retrait du plafond d'encours

Le champ `clients.montant_plafond` n'est plus exposé ni utilisé par le code applicatif. Les bases existantes peuvent encore conserver cette colonne, car Hibernate `ddl-auto: update` ne supprime pas les colonnes retirées des entités. Après sauvegarde et revue des données, l'opération SQL de retrait est fournie dans `src/main/resources/db/manual/20261016_drop_montant_plafond.sql`; elle supprime les anciennes valeurs de plafond d'encours.

Les nouvelles colonnes et tables sont ajoutées par le mécanisme Hibernate `ddl-auto: update` déjà utilisé par les profils de l'application. Aucune donnée métier existante n'est réinitialisée.
