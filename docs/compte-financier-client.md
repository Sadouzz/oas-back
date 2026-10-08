Auteur : Codex | Date : 2026-10-07 | Action : décrire le compte financier entreprise et les contrôles de crédit

# Compte financier client

## Modèle métier

Le compte financier est un objet métier rattaché à un client; il ne crée aucun utilisateur ni identifiant de connexion. Son ouverture transforme le client en entreprise. Cette forme juridique est conservée après désactivation du compte. Le NINEA légal est stocké dans `clients.numero_entreprise`; le champ historique `clients.ninea` reste réservé à son ancienne valeur de justificatif.

Les remises, le solde de crédits et les conditions de paiement sont séparés. Les crédits ajoutés sont tracés dans un journal append-only et augmentent le solde disponible. À ce stade ils ne sont pas imputés automatiquement aux factures: aucune règle d'affectation ou de remboursement automatique n'est spécifiée.

## Conditions financières

Le plafond d'encours compare la somme des soldes restant dus sur les factures avec le plafond configuré. L'échéance est le délai de paiement en jours; chaque nouvelle facture garde une date d'échéance calculée à l'émission. Le plafond de période compare les montants des factures émises sur les `échéance` derniers jours.

La création de proforma par un agent reste autorisée. Le serveur calcule et renvoie les alertes d'encours, de facture échue ou de plafond de période; l'interface agent les affiche après l'enregistrement. Les clients ne disposent pas de route de création de proforma.

La réservation créée depuis l'espace client est refusée côté serveur lorsqu'une facture est échue et impayée, que l'encours dépasse le plafond, ou que le plafond de facturation de la période est atteint. Les réservations créées par les agents restent hors de ce contrôle.

## Routes administrateur

- `GET /api/clients/{id}/compte-financier` : profil financier et agrégats calculés côté serveur.
- `PUT /api/clients/{id}/compte-financier` : ouvre ou modifie le compte et les conditions; réservé à MASTER et SUPER_AGENT.
- `PUT /api/clients/{id}/conditions-financieres` : modifie les conditions communes sans convertir un particulier en entreprise; réservé à MASTER et SUPER_AGENT.
- `POST /api/clients/{id}/compte-financier/credits` : ajoute un crédit et une ligne d'audit; réservé à MASTER et SUPER_AGENT.
- `GET /api/clients/{id}/compte-financier/mouvements` : renvoie au plus les 100 derniers mouvements sans sérialiser l'entité Client.
- `DELETE /api/clients/{id}/compte-financier` : désactive le compte sans effacer l'historique ni les conditions d'échéance.

Les nouvelles colonnes et tables sont ajoutées par le mécanisme Hibernate `ddl-auto: update` déjà utilisé par les profils de l'application. Aucune donnée métier existante n'est réinitialisée.
