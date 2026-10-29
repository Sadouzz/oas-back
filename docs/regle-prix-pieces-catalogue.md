Auteur : Codex | Date : 2026-10-29 | Action : documenter la règle de prix des pièces au catalogue

# Prix des pièces du catalogue dans les documents

`prixUnitaire` est le prix catalogue de référence utilisé pour les pièces PDP et PDG. Le prix peut être renseigné ou modifié dans le catalogue; les documents commerciaux ne changent jamais cette valeur.

- **PDP** : le prix est toujours lu dans `prixUnitaire`. Il n'est pas modifiable sur une ligne de document. Si aucun prix catalogue n'est renseigné, la création de la ligne est refusée jusqu'à la mise à jour du catalogue.
- **PDG** : si `prixUnitaire` est renseigné, il est imposé et la ligne ne permet pas de le modifier. Si le catalogue n'a pas de prix, un prix peut être saisi pour la ligne du document seulement; cette saisie ne met pas à jour le catalogue.
- **Documents déjà acceptés ou payés** : leurs prix de ligne restent des instantanés. Une mise à jour du catalogue ne réécrit pas leurs lignes. Un bon de sortie issu d'un proforma accepté reprend le prix de ce proforma pour les pièces correspondantes.
- **Documents dérivés** : les conversions en note de prix, facture et bon de livraison copient les montants des lignes du document source.
- **Hors périmètre de cette règle** : prix d'achat des bons de commande, PDS et main-d'œuvre.

Le backend applique la règle même si un client envoie un autre prix pour une PDP ou une PDG dont le prix catalogue existe. Le front affiche le prix appliqué et ne propose la saisie que pour une PDG sans prix catalogue.
