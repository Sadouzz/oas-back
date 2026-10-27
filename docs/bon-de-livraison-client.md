Auteur : Codex | Date : 2026-10-27 | Action : documenter le flux BL client depuis les bons de sortie

# Bon de livraison client

Le bon de livraison client est distinct du bon de réception fournisseur. `/api/bons-de-reception` reste réservé aux réceptions de commandes fournisseurs. `/api/bons-de-livraison` gère les livraisons liées à un ordre de réparation.

## Règles métier

- Un OR peut avoir au plus un BL. Le lien est également protégé par une contrainte unique en base.
- Un BL est créé à partir d'au moins un BS validé qui appartient au même OR, au même client et au même véhicule.
- Les lignes et prix du BL sont des instantanés calculés à partir des lignes des BS sélectionnés; les remises de ligne sont conservées. La TVA est choisie et figée sur le BL.
- Un BL peut être converti en note de prix et en facture. Chaque conversion ne crée qu'un document cible; l'unicité de facture par OR reste imposée.
- Un OR ne peut être facturé que depuis son BL. La conversion de proforma et la création manuelle de facture depuis un OR ne contournent pas cette règle.
- La restitution du véhicule exige un BL et sa facture. La conversion en note de prix ne satisfait pas cette condition.

## API

- `GET /api/bons-de-livraison?page=0&size=10&keyword=...` : liste paginée, recherche facultative par référence BL, OR, nom du client ou immatriculation.
- `GET /api/bons-de-livraison/{id}` : détail du BL.
- `GET /api/bons-de-livraison/{id}/pdf` : impression du BL.
- `GET /api/bons-de-livraison/ordres-reparation/{ordreId}/bons-de-sortie` : BS validés disponibles pour l'OR.
- `POST /api/bons-de-livraison` : création à partir de `ordreReparationId`, `bonsDeSortieIds`, `appliquerTVA` et `remarque` facultative.
- `POST /api/bons-de-livraison/{id}/convertir-en-facture` et `.../convertir-en-note-de-prix` : conversions mutuellement exclusives.

Le schéma est géré par les entités JPA et la stratégie Hibernate `ddl-auto: update` des profils dev et prod. Les documents existants ne sont pas réécrits par cette évolution.
