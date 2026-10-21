Auteur : Codex | Date : 2026-10-21 | Action : préciser le calcul et la propagation des remises OR

# Workflow des bons de sortie

La création d’un bon de sortie se fait sur un seul écran. Depuis un OR, un OT ou un proforma lié à un OR, le formulaire reçoit le client, le véhicule et les identifiants de l’ordre ; aucune pièce n’est ajoutée automatiquement. La saisie affiche les quantités prévues sur l’OR et le reliquat déjà présent sur ses BS, comme repère modifiable. Une quantité supérieure au reliquat est permise, sous réserve du stock PDP disponible.

Un BS peut contenir des PDP, PDG, PDS et mains-d’œuvre. Seules les PDP déplacent le stock : à la création, du magasin vers l’atelier ; à la validation, la quantité sort de l’atelier. Les autres types n’entraînent aucun mouvement de stock.

Le BS est rattaché à un OR et peut aussi être rattaché à un OT. L’écran de l’étape BS de l’OT présente tous les bons liés à l’OT ou à son OR, avec le détail des lignes et le cumul des quantités de pièces et des montants.

Les taux de remise sont saisis sur les lignes de l’OR, jamais sur le BS ou le proforma. Chaque ligne de pièce ou de main-d’œuvre conserve son propre taux ; deux lignes identiques peuvent donc différer. Les taux sont enregistrés avec l’OR. Les anciens OR ont un taux historique de 0 %. Lorsqu’un proforma est lié à un OR, le serveur reprend les taux des lignes correspondantes de l’OR ; il ignore tout taux envoyé par le formulaire. Un proforma autonome ne peut pas introduire de remise.

La remise générale est appliquée séparément à chaque ligne de pièce et de main-d’œuvre. Ensuite, si le client a un compte actif avec une remise, celle-ci est calculée sur le sous-total des pièces déjà remisées ; elle ne s’applique pas à la main-d’œuvre. Le proforma et la facture reprennent ce calcul et affichent les remises par ligne ainsi que la remise de compte client séparément. Le BS ne porte ni taux ni montant de remise.

Le magasinier peut créer un BS. La validation est réservée au chef d’atelier, au master et au super-agent, et n’est proposée qu’après la réparation (statut OR `PAIEMENT` ou ultérieur). Le master reste dans son garage ; le super-agent conserve sa portée système selon les règles de sécurité existantes.

## Déploiement des colonnes

Appliquer les migrations `src/main/resources/db/manual/20261021_bon_sortie_multi_lignes_liens.sql`, `src/main/resources/db/manual/20261021_ordre_reparation_remises_par_ligne.sql` et `src/main/resources/db/manual/20261021_supprimer_remises_bon_sortie.sql` avant de déployer le backend. La première rend la pièce nullable pour les lignes de main-d’œuvre/PDS personnalisées, ajoute les liaisons OT et les montants du BS. La deuxième ajoute les taux par ligne sur les PDP, PDG, PDS et mains-d’œuvre de l’OR ainsi que leur copie de calcul dans les lignes de proforma et de facture. La troisième supprime les anciennes colonnes de remise du BS ; le BS conserve ses montants bruts et les remises restent portées par l’OR et les documents de facturation concernés.
