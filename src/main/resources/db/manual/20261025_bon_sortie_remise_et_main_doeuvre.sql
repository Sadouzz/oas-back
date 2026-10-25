-- Migration pour ajouter taux_remise et montant_total aux lignes de bon de sortie existantes
ALTER TABLE IF EXISTS lignes_bon_de_sortie_piece ADD COLUMN IF NOT EXISTS taux_remise NUMERIC(5,2) DEFAULT 0.00;
ALTER TABLE IF EXISTS lignes_bon_de_sortie_piece ADD COLUMN IF NOT EXISTS montant_total NUMERIC(15,2);

UPDATE lignes_bon_de_sortie_piece SET taux_remise = 0.00 WHERE taux_remise IS NULL;
UPDATE lignes_bon_de_sortie_piece 
SET montant_total = (quantite * prix) * (1.0 - (COALESCE(taux_remise, 0.0) / 100.0))
WHERE montant_total IS NULL AND quantite IS NOT NULL AND prix IS NOT NULL;
