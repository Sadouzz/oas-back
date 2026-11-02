Auteur : Codex | Date : 2026-11-01 | Action : préciser le regroupement des PDS identiques

# Lignes répétées de pièces : OR, proforma, BS et BL

Les PDG répétés sont des lignes distinctes dans l'OR, le proforma et le BS, même quand l'article et le prix sont les mêmes. Le prix et la remise restent propres à chaque ligne. L'import du proforma dans le BS utilise l'identifiant de ligne source pour éviter une seconde importation sans dédupliquer deux lignes distinctes du même PDG.

Les PDS de même désignation, prix unitaire et remise sont regroupées en une ligne avec quantité cumulée dans l'OR, le proforma et le BS. Une différence de désignation, de prix ou de remise conserve des lignes séparées. Les liens de lignes source du proforma restent distincts au BS pour préserver le suivi des quantités. Les horodatages de chaque ligne BS sont conservés lors du regroupement au BL.

À la création du BL, les lignes PDG du même article, au même prix unitaire et avec la même remise sont regroupées. Les PDS de même désignation, prix unitaire et remise le sont également. Le BL additionne les quantités et conserve la date/heure de chaque saisie source. Une différence de prix ou de remise produit une ligne séparée. Les factures et notes de prix créées depuis le BL conservent les lignes consolidées et leurs dates sources.

La colonne `date_heure_saisie` est ajoutée à `lignes_bon_de_sortie_piece`. Les dates des lignes BL consolidées sont stockées dans `ligne_facturation_piece_dates_source`. Les scripts manuels sont `20261029_bon_sortie_date_heure_saisie_ligne.sql` et `20261029_ligne_facturation_piece_dates_source.sql`. Les lignes historiques sans date restent sans date; aucune date n'est inventée.
