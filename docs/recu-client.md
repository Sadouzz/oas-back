Auteur : Claude | Date : 2026-11-26 | Action : reprendre les reçus clients de l'ancienne application

# Reçus clients

## Origine

L'ancienne application gérait un reçu par client (`recu`) sur trois écrans : `add_recu_facture.php` (le reçu et ses versements), `recu-encours.php` et `recu-payees.php`. Le reçu regroupait les factures et notes de prix non payées du client. Les versements (`detail_recu_client`) étaient enregistrés en vrac, sans être rattachés à un document, et ne changeaient pas le statut des factures.

## Modèle

- `Recu` (`recus`) : le reçu client, numéroté `PREFIXE_GARAGE-RE-ANNEE-0001` (type `RE` du générateur de numéros, compteur par garage et par année). Les routes utilisent son `id`. Il porte le client, la date de début, la date de fin, le montant, le montant payé et la validation.
- `VersementClient` (`versements_client`) : un versement sur un reçu.
- `LigneVersementClient` (`lignes_versement_client`) : la part d'un versement imputée à une facture (avec son reçu d'encaissement `RC`) ou à une note de prix.
- `RecuFacture` (`recus_facture`) : l'encaissement d'une facture, numéroté `RC`. Il reste utilisé par `Facture.recus`, le solde prépayé et l'annulation des versements.

Les documents ne sont pas rattachés au reçu par une clé. Un reçu en cours couvre toutes les factures et notes de prix impayées du client. Le client d'un document est retrouvé par le client du document, puis celui de l'ordre de réparation, puis celui du véhicule. Les documents d'un reçu payé sont ceux réglés par ses versements. Une facture impayée reste donc supprimable.

## Cycle de vie

1. **Ouverture** : un client a au plus un reçu en cours. Il est créé à la première ouverture s'il reste quelque chose à payer. Son montant vaut montant payé + reste à payer actuel, et suit donc les nouvelles factures du client.
2. **Versement** : le montant est réparti sur les documents impayés, du plus ancien au plus récent. La part d'une facture crée un reçu `RC` et met à jour le montant payé, le statut, le solde prépayé et l'ordre de réparation. La part d'une note de prix met à jour son montant payé, son reste et son statut. Tout se fait dans une seule transaction. Un versement supérieur au reste est refusé. Le mode `SOLDE_PREPAYE` ne règle que des factures, les notes de prix n'acceptant pas le solde prépayé.
3. **Clôture** : quand plus rien n'est dû, la date de fin est posée et le reçu passe dans les reçus payés. Un nouveau reçu sera ouvert pour les factures suivantes.
4. **Validation** : un reçu payé peut être validé une fois.
5. **Annulation d'un versement** : ses parts sont retirées des factures et notes de prix (statut `NON_PAYE` ou `PARTIEL`). Ses reçus `RC` restent en base, marqués `annule`, car le solde prépayé et la numérotation `RC` y renvoient. Un versement par solde prépayé est rendu au portefeuille (mouvement `ANNULATION_PAIEMENT`). Un reçu payé repasse en cours, sauf s'il est validé ou si le client a déjà un autre reçu en cours. Le statut de l'ordre de réparation n'est pas modifié.
6. **Suppression** : seul un reçu en cours sans versement peut être supprimé. Un reçu en cours vide est supprimé à sa réouverture.

## Routes

Réservées aux rôles AGENT, SUPER_AGENT, MASTER, CHEF_ATELIER et AGENT_MAGASIN. Les anciennes routes d'encaissement par facture (`/payer`, `/payer-integral`, `/me`, `/facture/{id}`) ont été supprimées.

- `GET /api/recus/en-cours?numeroRecu=&numeroClient=&page=&size=` : reçus en cours (recherche partielle sur le numéro de reçu et le numéro client), du plus récent au plus ancien. Le total est recalculé à l'affichage.
- `GET /api/recus/payes?numeroRecu=&numeroClient=&page=&size=` : reçus payés.
- `POST /api/recus/client/{clientId}` : ouvre le reçu en cours du client, ou le met à jour.
- `GET /api/recus/{id}` : documents, totaux et versements du reçu.
- `POST /api/recus/{id}/versements` avec `{ "montant", "modePaiement", "remarque" }` : enregistre un versement.
- `DELETE /api/recus/{id}/versements/{versementId}` : annule un versement et renvoie le reçu à jour.
- `GET /api/recus/{id}/pdf?layoutKey=AVEC_ENTETE|SANS_ENTETE` : PDF du reçu (documents, versements et totaux).
- `POST /api/recus/{id}/valider` : valide un reçu payé.
- `DELETE /api/recus/{id}` : supprime un reçu en cours sans versement.

## Base de données

Le schéma est préparé au démarrage, avant la mise à jour par Hibernate (`shared/schema/PreparationSchemaRecus`), sur PostgreSQL et de façon idempotente :

- l'ancienne table `recus` (encaissements `RC`, avec `facture_id`) est renommée `recus_facture`, puis `recus_client` est renommée `recus` ; les clés étrangères suivent les tables ;
- les contraintes CHECK de `document_sequences.document_type` et de `mouvements_portefeuille_client.type` sont recréées à partir des enums Java (`DocumentType`, `TypeMouvementPortefeuille`) : ajouter une valeur à ces enums ne demande plus de script ;
- les reçus sans numéro sont numérotés et le compteur `RE` est recalé.

Les colonnes `annule` et `date_annulation` de `recus_facture` sont ajoutées par Hibernate.

## Limites

- L'impression PDF des listes de reçus n'est pas reprise.
- Le PDF du reçu n'utilise pas encore les modèles configurables (`PdfTemplate`).
- Les reçus de l'ancienne base ne sont pas importés.
