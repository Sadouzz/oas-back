-- Auteur : Codex | Date : 2026-11-09 | Action : Ajouter la date de saisie aux lignes de pièces et de main-d'œuvre
-- Migration additive. Les anciennes lignes restent NULL : leur date d'ajout historique est inconnue.

ALTER TABLE lignes_ordre_reparation_piece
    ADD COLUMN IF NOT EXISTS date_heure_saisie TIMESTAMP;

ALTER TABLE lignes_ordre_reparation_main_doeuvre
    ADD COLUMN IF NOT EXISTS date_heure_saisie TIMESTAMP;

ALTER TABLE lignes_facturation_piece
    ADD COLUMN IF NOT EXISTS date_heure_saisie TIMESTAMP;

ALTER TABLE lignes_facturation_main_doeuvre
    ADD COLUMN IF NOT EXISTS date_heure_saisie TIMESTAMP;

ALTER TABLE lignes_bon_de_sortie_main_doeuvre
    ADD COLUMN IF NOT EXISTS date_heure_saisie TIMESTAMP;

ALTER TABLE ligne_bon_de_commande
    ADD COLUMN IF NOT EXISTS date_heure_saisie TIMESTAMP;
