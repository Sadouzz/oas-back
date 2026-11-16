Auteur : Codex | Date : 2026-11-16 | Action : appliquer le timbre selon le régime TVA de la facture

# Factures HT, TVA et timbre

## Choix de régime

Au moment de créer la facture depuis un BL ou depuis l’OR, l’agent choisit le mode de paiement dans une liste unique. Elle comprend Espèces TTC, Chèque TTC, Espèces HT, Chèque HT, ainsi que les autres moyens disponibles (virement, solde prépayé, Wave, Orange Money, carte et autre).

Le choix sélectionné détermine le régime fiscal appliqué à la facture :

- **Espèces TTC** et **Chèque TTC** : TVA calculée selon la configuration fiscale.
- **Espèces HT** et **Chèque HT** : aucune TVA; le timbre est calculé sur le montant HT et inclus dans le total dû.
- **Virement, solde prépayé, Wave, Orange Money, carte et autre** : TVA appliquée; aucun timbre n'est ajouté.

Le mode choisi est conservé sur la facture. Les reçus enregistrent le moyen réellement utilisé pour chaque encaissement.

## Timbre

- Pour une facture **HT**, le timbre configuré est calculé une seule fois sur le montant HT à l'émission et inclus dans le total dû. Les paiements partiels règlent ce solde sans recalculer ni ajouter de timbre.
- Pour une facture **avec TVA**, aucun timbre n'est appliqué, y compris lors d'un règlement en espèces.
- Le mode réel de chaque encaissement est conservé sur son reçu, indépendamment du régime fiscal de la facture. Le reçu n'ajoute jamais de timbre.
- Une **note de prix** n'est jamais soumise au timbre, quel que soit son régime TVA ou son mode de règlement.
- Un proforma n'ajoute pas de timbre à son estimation; le timbre est inclus uniquement si la facture issue est émise en HT.

## Historique et affichage

Les factures, proformas et reçus déjà enregistrés conservent leurs montants. Les nouveaux totaux HT incluent le timbre une fois; les interfaces et PDFs distinguent ce montant du solde à régler. Les bons de livraison affichent leur régime sans présenter un montant HT comme un total TTC.
