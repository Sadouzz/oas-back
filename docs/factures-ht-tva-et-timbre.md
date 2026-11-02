Auteur : Codex | Date : 2026-11-02 | Action : Décrire le régime HT/TTC des factures et le traitement du timbre

# Factures HT, TVA et timbre

## Choix de régime

Au moment de créer la facture depuis un BL ou depuis l’OR, l’agent choisit le mode de paiement dans une liste unique. Elle comprend Espèces TTC, Chèque TTC, Espèces HT, Chèque HT, ainsi que les autres moyens disponibles (virement, solde prépayé, Wave, Orange Money, carte et autre).

Le moyen sélectionné détermine le régime et les taxes appliqués à la facture :

- **Espèces TTC** et **Chèque TTC** : TVA calculée selon la configuration fiscale du garage.
- **Espèces HT** et **Chèque HT** : aucune TVA; le timbre est calculé une fois sur le montant HT et inclus dans le total dû.
- **Virement, solde prépayé, Wave, Orange Money, carte et autre** : TVA appliquée; pas de timbre espèces ajouté à l’émission.

Le mode choisi est conservé sur la facture. Les reçus enregistrent le moyen réellement utilisé pour chaque encaissement.

## Timbre

- Pour une facture **HT**, le timbre configuré est calculé une seule fois sur le montant HT, enregistré sur la facture et inclus dans son total dû. Les paiements partiels consomment ce solde sans recalculer ni ajouter le timbre.
- Pour une facture **TTC**, le timbre reste calculé à l’encaissement uniquement sur la somme réglée en espèces. Ce timbre s’ajoute au montant remis et ne réduit pas le solde de la facture.
- Les autres moyens de règlement restent disponibles. Le mode réel de chaque encaissement est conservé sur son reçu, indépendamment du régime fiscal de la facture.

## Historique et affichage

Les factures déjà émises conservent leurs montants. Les nouveaux totaux HT incluent le timbre une fois; les interfaces de détail et le PDF distinguent le montant du timbre du solde à régler. Les bons de livraison affichent leur régime sans présenter un montant HT comme un total TTC.
