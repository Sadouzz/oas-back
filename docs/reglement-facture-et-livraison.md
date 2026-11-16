Auteur : Codex | Date : 2026-11-16 | Action : documenter le paiement intégral et la livraison d'un OR

# Règlement d'une facture dans le workflow OR

## Facture émise

La conversion d'un bon de livraison en facture crée une facture avec son solde restant à payer. Le fait d'atteindre l'étape Facturation de l'ordre de réparation ne vaut pas paiement.

## Valider le paiement intégral depuis l'OR

Sur l'étape Facturation, l'agent peut confirmer l'encaissement du solde complet. Le backend verrouille la facture, calcule le solde comme le montant total moins les paiements déjà enregistrés, crée un reçu pour ce montant et met à jour les montants et statuts de paiement dans une même transaction. Le mode d'encaissement est conservé sur le reçu. Un solde prépayé est débité via le service de portefeuille existant.

Une facture déjà soldée ne reçoit pas de reçu supplémentaire. Le paiement intégral prépare le passage à la livraison ; la restitution du véhicule reste une action distincte avec signature et durée de garantie.

## Poursuivre sans paiement

L'agent peut aussi choisir explicitement de passer à la livraison sans encaisser. Cette action ne crée aucun reçu et ne change pas le montant payé ni le reste à payer. La facture reste impayée ou partiellement payée. Le backend autorise la restitution si l'ordre est à l'étape Facturation ou Prêt à livrer, que le BL et la facture existent et que les justificatifs de restitution requis sont fournis ; le règlement n'est pas une condition de livraison.

## Encaissements partiels

L'action d'encaissement depuis la liste des factures conserve la possibilité d'enregistrer un paiement partiel. La validation intégrale de l'étape OR, elle, règle toujours tout le solde restant en une seule opération.
