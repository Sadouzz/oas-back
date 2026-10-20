Auteur : Codex | Date : 2026-10-19 | Action : documenter la configuration fiscale globale

# Configuration fiscale globale

La configuration fiscale est unique au niveau du système et n'est associée à aucun garage. La table `fiscal_configuration` utilise la ligne d'identifiant `1`; si elle n'existe pas encore, le backend crée les valeurs initiales suivantes à la première lecture : TVA 18 % et timbre fiscal 100 FCFA.

La consultation de la configuration requiert une session authentifiée. Seul le rôle `ROLE_SUPER_AGENT` peut la modifier. Les valeurs sont validées côté backend : taux compris entre 0 et 100 %, montants positifs dans une limite de 100 000 000 FCFA.

Les nouvelles factures, proformas, bons de commande et avoirs TTC lisent cette configuration au moment de leur création. La réception d'un bon de commande conserve le taux calculé sur ce bon de commande. Les montants des documents existants ne sont pas recalculés lors de la modification de la configuration; pour les bons de commande modifiés, le taux existant est déduit des montants enregistrés avant recalcul des lignes.

La configuration comprend uniquement le taux de TVA et le montant du timbre fiscal. Les règles propres à chaque document déterminent si la TVA ou le timbre s'appliquent à ce type de document.
