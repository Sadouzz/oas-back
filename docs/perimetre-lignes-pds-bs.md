Auteur : Codex | Date : 2026-10-29 | Action : Décrire la conservation et la consolidation des lignes de pièces

# Lignes répétées de pièces : OR, proforma, BS et BL

Les PDG répétés sont des lignes distinctes dans l'OR, le proforma et le BS, même quand l'article et le prix sont les mêmes. Le prix et la remise restent propres à chaque ligne. L'import du proforma dans le BS utilise l'identifiant de ligne source pour éviter une seconde importation sans dédupliquer deux lignes distinctes du même PDG.

Au BS, chaque ligne de pièce reçoit un horodatage serveur à l'enregistrement. Cela concerne les PDG et les PDS. Les PDP ne sont pas consolidées au BL par cette règle. Les PDS gardent leur comportement existant : les lignes distinctes au BS sont conservées, et l'import du proforma les rapproche par désignation et prix unitaire.

À la création du BL, seules les lignes PDG du même article, au même prix unitaire et avec la même remise sont regroupées. Le BL additionne leurs quantités et conserve la date/heure de chaque saisie source. Un prix ou une remise différente produit une ligne BL séparée. Les autres lignes du BS ne sont pas regroupées. Les factures et notes de prix créées depuis le BL conservent les lignes consolidées et leurs dates sources.

La colonne `date_heure_saisie` est ajoutée à `lignes_bon_de_sortie_piece`. Les dates des lignes BL consolidées sont stockées dans `ligne_facturation_piece_dates_source`. Les scripts manuels sont `20261029_bon_sortie_date_heure_saisie_ligne.sql` et `20261029_ligne_facturation_piece_dates_source.sql`. Les lignes historiques sans date restent sans date; aucune date n'est inventée.
