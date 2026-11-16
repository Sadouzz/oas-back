Auteur : Codex | Date : 2026-11-16 | Action : aligner le timbre fiscal sur le régime TVA des factures

# Configuration fiscale globale

La configuration fiscale est unique au niveau du système et n'est associée à aucun garage. La table `fiscal_configuration` utilise la ligne d'identifiant `1`; si elle n'existe pas encore, le backend crée les valeurs initiales suivantes à la première lecture : TVA 18 % et taux de timbre fiscal 1 %.

La consultation de la configuration requiert une session authentifiée. Seul le rôle `ROLE_SUPER_AGENT` peut la modifier. Les taux de TVA et de timbre sont validés côté backend entre 0 et 100 %.

Les factures mémorisent le taux de timbre en vigueur à leur émission; les factures issues d'un proforma reprennent le taux enregistré sur celui-ci. Une facture sans proforma mémorise le taux courant à sa création. Une note de prix n'est pas soumise au timbre, ni à la création ni au règlement. Les modifications ultérieures de configuration ne recalculent ni les documents ni les reçus déjà créés.

Le régime TVA de la facture détermine le timbre, indépendamment du mode de règlement. Une facture HT reçoit un timbre calculé sur son montant HT, inclus une seule fois dans son total dès l'émission. Une facture avec TVA ne reçoit aucun timbre, y compris lors d'un paiement en espèces. Les reçus ne recalculent ni n'ajoutent de timbre. Les notes de prix restent sans timbre à l'émission et au règlement.

Un proforma est une estimation et n'ajoute pas de timbre à son total. Il indique que le timbre sera ajouté à l'émission si une facture HT en est issue, et qu'aucun timbre ne sera appliqué si la facture comporte la TVA. Les montants historiques des factures, proformas et reçus déjà enregistrés sont conservés; aucune migration de données n'est nécessaire pour cette règle. La migration manuelle `src/main/resources/db/manual/20261029_timbre_fiscal_especes.sql` reste la migration historique des colonnes de taux et de suivi.
