-- Auteur : Codex | Date : 2026-10-21 | Action : enregistrer la remise saisie par ligne de l’OR
-- Les OR existants sont conservés avec un taux historique de 0 %.

ALTER TABLE lignes_ordre_reparation_piece
    ADD COLUMN IF NOT EXISTS taux_remise numeric(5,2) NOT NULL DEFAULT 0;

ALTER TABLE lignes_ordre_reparation_main_doeuvre
    ADD COLUMN IF NOT EXISTS taux_remise numeric(5,2) NOT NULL DEFAULT 0;

-- Le proforma conserve les taux issus de l’OR pour garantir la même base de calcul.
ALTER TABLE lignes_facturation_piece
    ADD COLUMN IF NOT EXISTS taux_remise numeric(5,2) NOT NULL DEFAULT 0;

ALTER TABLE lignes_facturation_main_doeuvre
    ADD COLUMN IF NOT EXISTS taux_remise numeric(5,2) NOT NULL DEFAULT 0;
