-- Timbre fiscal applicable aux paiements en espèces uniquement.
-- La colonne historique timbre_fiscal (montant fixe) est conservée pour préserver les données des anciens documents.
ALTER TABLE fiscal_configuration
    ADD COLUMN IF NOT EXISTS timbre_fiscal_rate NUMERIC(7, 4) NOT NULL DEFAULT 1.0000;

ALTER TABLE proformas
    ADD COLUMN IF NOT EXISTS timbre_fiscal_rate NUMERIC(7, 4);

ALTER TABLE factures
    ADD COLUMN IF NOT EXISTS timbre_fiscal_rate NUMERIC(7, 4);

UPDATE proformas
SET timbre_fiscal_rate = COALESCE(
    (SELECT fc.timbre_fiscal_rate FROM fiscal_configuration fc WHERE fc.id = 1),
    1.0000
)
WHERE timbre_fiscal_rate IS NULL;

UPDATE factures f
SET timbre_fiscal_rate = p.timbre_fiscal_rate
FROM proformas p
WHERE f.proforma_id = p.id AND f.timbre_fiscal_rate IS NULL;

ALTER TABLE recus
    ADD COLUMN IF NOT EXISTS montant_timbre NUMERIC(15, 2) NOT NULL DEFAULT 0.00;

ALTER TABLE notes_de_prix
    ADD COLUMN IF NOT EXISTS montant_timbre_paye NUMERIC(15, 2) NOT NULL DEFAULT 0.00;
