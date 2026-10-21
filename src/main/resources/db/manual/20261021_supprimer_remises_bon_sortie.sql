-- Auteur : Codex | Date : 2026-10-21 | Action : supprimer les remises du modèle BS
-- Les remises sont gérées par l'OR. Cette migration retire les anciens champs BS.

ALTER TABLE bons_de_sortie
    DROP COLUMN IF EXISTS remise_generale,
    DROP COLUMN IF EXISTS taux_remise_client,
    DROP COLUMN IF EXISTS montant_remise_generale,
    DROP COLUMN IF EXISTS montant_remise_client;
