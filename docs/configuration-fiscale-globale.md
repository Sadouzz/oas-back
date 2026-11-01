Auteur : Codex | Date : 2026-10-29 | Action : documenter le timbre appliqué aux règlements en espèces

# Configuration fiscale globale

La configuration fiscale est unique au niveau du système et n'est associée à aucun garage. La table `fiscal_configuration` utilise la ligne d'identifiant `1`; si elle n'existe pas encore, le backend crée les valeurs initiales suivantes à la première lecture : TVA 18 % et taux de timbre fiscal espèces 1 %.

La consultation de la configuration requiert une session authentifiée. Seul le rôle `ROLE_SUPER_AGENT` peut la modifier. Les taux de TVA et de timbre espèces sont validés côté backend entre 0 et 100 %.

Les proformas mémorisent le taux de timbre en vigueur à leur émission; les factures issues d'un proforma en reprennent le taux. Une facture sans proforma mémorise le taux courant à sa création. Une note de prix calcule le timbre au moment de chaque règlement. Les modifications ultérieures de configuration ne recalculent ni les documents ni les reçus déjà créés.

Le timbre ne majore plus le total des documents. Lors d'un règlement, le backend ajoute un timbre égal au taux configuré appliqué à la seule part payée en espèces. Pour une facture, ce montant est enregistré sur le reçu; pour une note de prix, le cumul est conservé sur la note. Les paiements par chèque, virement, carte, mobile money ou solde prépayé ne sont pas timbrés. Le montant imputé au document et son reste à payer excluent ce timbre.

Pour les nouveaux proformas et factures, le TTC du document est `montantHT + montantTVA`; le timbre espèces est présenté séparément au règlement et n'est pas inclus dans ce TTC. Les montants historiques de documents déjà émis sont conservés. La migration manuelle `src/main/resources/db/manual/20261029_timbre_fiscal_especes.sql` ajoute les colonnes de taux et de suivi nécessaires sans recalculer les anciens totaux.
