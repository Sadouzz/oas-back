-- Auteur : Codex | Date : 2026-10-16 | Action : supprimer la colonne obsolète du plafond d'encours
-- Migration manuelle : sauvegarder et vérifier les anciennes valeurs avant exécution.
-- L'application n'utilise plus ces valeurs; cette opération les supprime définitivement.
ALTER TABLE clients DROP COLUMN IF EXISTS montant_plafond;
