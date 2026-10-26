Auteur : Codex | Date : 2026-10-26 | Action : documenter la séparation du compte entreprise, de la remise et du solde prépayé

# Compte financier client

## Modèle métier

Le compte financier est un objet métier rattaché à un client; il ne crée aucun utilisateur ni identifiant de connexion. Son ouverture transforme le client en entreprise. Cette forme juridique est conservée après désactivation du compte. Le NINEA légal est stocké dans `clients.numero_entreprise`; le champ historique `clients.ninea` reste réservé à son ancienne valeur de justificatif.

Le compte entreprise conserve l'identité légale, le statut et les conditions de facturation. Tous ses champs sont obligatoires à l'enregistrement. Son activation reste indépendante du solde prépayé et de la remise client.

La remise est un réglage propre au client, quel que soit son type. Elle conserve le calcul existant sur le montant net des pièces après remises de ligne, et sa valeur appliquée est figée sur le proforma/facture selon le flux habituel. Elle n'est plus désactivée par le retrait du statut fidèle ni par la désactivation du compte entreprise.

Le solde prépayé représente l'argent déjà confié au garage. Il est propre au couple client/garage et journalise les versements et paiements. Il n'est jamais appliqué automatiquement. Un agent peut choisir explicitement ce mode lors du paiement d'une facture, en totalité ou en partie; le solde est débité dans la même transaction que le reçu, avec contrôle de concurrence et de montant disponible. Il n'est pas proposé pour les notes de prix.

Les anciens crédits étaient rattachés à un compte client, sans garage associé et sans être dépensables. La reprise SQL copie leur solde et journal uniquement si un seul garage est actif. En contexte multi-garage, les anciennes valeurs restent conservées dans leurs tables et ne sont pas attribuées arbitrairement à un garage.

## Conditions financières

Le contrôle de facturation conserve le délai de paiement en jours et le plafond du montant total des factures émises pendant cette période glissante. Les deux champs sont obligatoires sur le compte entreprise. Les factures payées comptent dans ce cumul. Le total restant dû peut être consulté à titre informatif, mais aucun plafond d'encours n'est configuré ni appliqué.

La création de proforma par un agent reste autorisée. Le serveur calcule et renvoie les alertes de facture échue ou de plafond de période; l'interface agent les affiche après l'enregistrement. Les clients ne disposent pas de route de création de proforma.

La réservation créée depuis l'espace client est refusée côté serveur lorsqu'une facture est échue et impayée ou que le plafond de facturation de la période est atteint. Le total restant dû, à lui seul, ne bloque pas une réservation. Les réservations créées par les agents restent hors de ce contrôle.

## Routes administrateur

- `GET /api/clients/{id}/compte-financier` : profil financier et agrégats calculés côté serveur.
- `PUT /api/clients/{id}/compte-financier` : ouvre ou modifie le compte et les conditions; réservé à MASTER et SUPER_AGENT.
- `PUT /api/clients/{id}/conditions-financieres` : modifie les conditions communes sans convertir un particulier en entreprise; réservé à MASTER et SUPER_AGENT.
- `PUT /api/clients/{id}/remise` : configure la remise individuelle de 0 à 100 %; réservé à MASTER et SUPER_AGENT.
- `GET /api/clients/{id}/portefeuille-prepaye` : renvoie le solde et les 100 derniers mouvements dans le garage courant.
- `POST /api/clients/{id}/portefeuille-prepaye/alimentations` : enregistre un versement dans le garage courant et ajoute une ligne d'audit.
- `POST /api/recus/payer` avec `methodePaiement=SOLDE_PREPAYE` : paie explicitement tout ou partie d'une facture et débite le portefeuille du garage de cette facture.
- `DELETE /api/clients/{id}/compte-financier` : désactive le compte sans effacer l'historique ni les conditions d'échéance.

## Retrait du plafond d'encours

Le champ `clients.montant_plafond` n'est plus exposé ni utilisé par le code applicatif. Les bases existantes peuvent encore conserver cette colonne, car Hibernate `ddl-auto: update` ne supprime pas les colonnes retirées des entités. Après sauvegarde et revue des données, l'opération SQL de retrait est fournie dans `src/main/resources/db/manual/20261016_drop_montant_plafond.sql`; elle supprime les anciennes valeurs de plafond d'encours.

Les nouvelles colonnes et tables sont ajoutées par le mécanisme Hibernate `ddl-auto: update` déjà utilisé par les profils de l'application. Aucune donnée métier existante n'est réinitialisée.
