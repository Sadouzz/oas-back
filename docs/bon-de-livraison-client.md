Auteur : Codex | Date : 2026-10-29 | Action : documenter le flux BL, facture et restitution ainsi que les lignes PDG

# Bon de livraison client

Le bon de livraison client est distinct du bon de réception fournisseur. `/api/bons-de-reception` reste réservé aux réceptions de commandes fournisseurs. `/api/bons-de-livraison` gère les livraisons liées à un ordre de réparation.

## Règles métier

- Un OR peut avoir au plus un BL. Le lien est également protégé par une contrainte unique en base.
- Un BL est créé à partir de tous les BS validés liés à l'OR. La sélection est automatique et le serveur vérifie que chaque BS appartient au même OR, au même client et au même véhicule. Sans BS validé, la création est refusée avec un message explicite.
- Les lignes et prix du BL sont des instantanés calculés à partir des lignes des BS inclus; les remises de ligne sont conservées. La TVA choisie est figée sur le BL. Par défaut, elle reprend le réglage explicite du proforma; si l'ancien proforma n'a pas ce réglage, sa TVA calculée sert d'indicateur. Sans proforma, la TVA est activée par défaut. L'écran permet de modifier ce choix avant création.
- Les ajouts répétés d'un PDG restent des lignes distinctes dans l'OR, le proforma et le BS. Au BL, seules les lignes du même PDG avec le même prix unitaire et la même remise sont réunies; les quantités sont additionnées et les dates/heures de saisie des lignes BS sont conservées sur la ligne BL. Les PDG à prix ou remise différents restent séparés. Les PDP et PDS restent des lignes distinctes.
- Les lignes consolidées et leurs dates source sont copiées lors de la conversion du BL en facture ou en note de prix.
- Un BL peut produire une note de prix et une facture, chacune au plus une fois. La facture peut être créée après la note de prix; une seule facture par OR est autorisée.
- Un OR ne peut être facturé que depuis son BL. La conversion de proforma et la création manuelle de facture depuis un OR ne contournent pas cette règle. L'action de l'étape Paiement crée le BL depuis tous les BS validés, le convertit en facture dans la même opération serveur, puis recharge l'écran avec la facture. L'action est idempotente : elle réutilise le BL et la facture déjà associés au même OR.
- Un véhicule peut passer à l'état « prêt à livrer » sans paiement. La restitution effective exige un BL et sa facture; la facture peut rester impayée. La conversion en note de prix ne satisfait pas la condition de restitution.

## API

- `GET /api/bons-de-livraison?page=0&size=10&keyword=...` : liste paginée, recherche facultative par référence BL, OR, nom du client ou immatriculation.
- `GET /api/bons-de-livraison/{id}` : détail du BL.
- `GET /api/bons-de-livraison/{id}/pdf` : impression du BL.
- `GET /api/bons-de-livraison/ordres-reparation/{ordreId}/bons-de-sortie` : BS validés disponibles pour l'OR.
- `GET /api/bons-de-livraison/ordres-reparation/{ordreId}/contexte` : contexte de création consolidé (BS validés, valeur TVA par défaut, BL et documents associés éventuels).
- `POST /api/bons-de-livraison` : création à partir de `ordreReparationId`, `appliquerTVA` facultatif et `remarque` facultative; tous les BS validés de l'OR sont inclus par le serveur.
- `POST /api/bons-de-livraison/ordres-reparation/{ordreId}/facture` : crée le BL si nécessaire puis le convertit en facture; retourne la facture existante si elle provient déjà du BL de cet OR. Accepte `appliquerTVA` et `remarque` facultatifs.
- `POST /api/bons-de-livraison/{id}/convertir-en-facture` et `.../convertir-en-note-de-prix` : conversions idempotentes; les deux documents peuvent être liés au même BL.

Le schéma est géré par les entités JPA et la stratégie Hibernate `ddl-auto: update` des profils dev et prod. Les colonnes/collections peuvent aussi être ajoutées avec `20261029_bon_sortie_date_heure_saisie_ligne.sql` et `20261029_ligne_facturation_piece_dates_source.sql`. La contrainte historique du statut proforma doit être mise à jour avec `20261029_proforma_statut_convertie.sql`, car `ddl-auto: update` ne modifie pas une contrainte CHECK déjà présente. Pour les anciennes lignes BS, la migration reprend la date du BS parent comme approximation historique; elle ne reconstitue pas l'heure réelle de saisie de chaque ligne.
