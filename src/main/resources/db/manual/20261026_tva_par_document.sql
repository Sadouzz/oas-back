-- Ajoute un choix TVA figé par document et préserve l'état des documents déjà émis.
-- À exécuter après déploiement des entités qui créent ces colonnes (ddl-auto=update).

ALTER TABLE IF EXISTS factures ADD COLUMN IF NOT EXISTS tva_applicable boolean;
ALTER TABLE IF EXISTS proformas ADD COLUMN IF NOT EXISTS tva_applicable boolean;
ALTER TABLE IF EXISTS avoirs_ttc ADD COLUMN IF NOT EXISTS tva_applicable boolean;
ALTER TABLE IF EXISTS bons_de_reception ADD COLUMN IF NOT EXISTS tva_applicable boolean;
ALTER TABLE IF EXISTS bons_de_sortie ADD COLUMN IF NOT EXISTS tva_applicable boolean;
ALTER TABLE IF EXISTS bons_de_sortie ADD COLUMN IF NOT EXISTS montant_tva numeric(15, 2);
ALTER TABLE IF EXISTS bons_de_sortie ADD COLUMN IF NOT EXISTS montant_ttc numeric(15, 2);
ALTER TABLE IF EXISTS notes_de_prix ADD COLUMN IF NOT EXISTS tva_applicable boolean;
ALTER TABLE IF EXISTS notes_de_prix ADD COLUMN IF NOT EXISTS montant_tva numeric(15, 2);

UPDATE factures SET tva_applicable = COALESCE(montant_tva, 0) > 0 WHERE tva_applicable IS NULL;
UPDATE proformas SET tva_applicable = COALESCE(montant_tva, 0) > 0 WHERE tva_applicable IS NULL;
UPDATE avoirs_ttc SET tva_applicable = COALESCE(montant_tva, 0) > 0 WHERE tva_applicable IS NULL;
UPDATE bons_de_reception SET tva_applicable = COALESCE(montant_tva, 0) > 0 WHERE tva_applicable IS NULL;
UPDATE bons_de_sortie
SET tva_applicable = false,
    montant_tva = 0,
    montant_ttc = COALESCE(montant_total, 0)
WHERE tva_applicable IS NULL;
UPDATE notes_de_prix
SET tva_applicable = COALESCE(montant_tva, 0) > 0,
    montant_tva = COALESCE(montant_tva, 0)
WHERE tva_applicable IS NULL;
