-- Horodatage serveur de création de chaque ligne de pièce d'un bon de sortie.
ALTER TABLE IF EXISTS lignes_bon_de_sortie_piece
    ADD COLUMN IF NOT EXISTS date_heure_saisie TIMESTAMP;
ALTER TABLE IF EXISTS lignes_bon_de_sortie_piece
    ADD COLUMN IF NOT EXISTS proforma_ligne_id BIGINT;

-- Pour les lignes historiques, la date du BS est la meilleure date source disponible.
UPDATE lignes_bon_de_sortie_piece ligne
SET date_heure_saisie = bs.date
FROM bons_de_sortie bs
WHERE ligne.bon_de_sortie_id = bs.id
  AND ligne.date_heure_saisie IS NULL;
