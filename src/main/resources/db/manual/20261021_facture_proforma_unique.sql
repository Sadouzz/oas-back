-- Auteur : Codex | Date : 2026-10-21 | Action : lier chaque facture à son proforma source
-- Migration additive : les anciennes factures restent sans proforma_id.

BEGIN;

ALTER TABLE factures
    ADD COLUMN IF NOT EXISTS proforma_id BIGINT;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_factures_proforma'
          AND conrelid = 'factures'::regclass
    ) THEN
        ALTER TABLE factures
            ADD CONSTRAINT fk_factures_proforma
            FOREIGN KEY (proforma_id) REFERENCES proformas(id);
    END IF;
END $$;

CREATE UNIQUE INDEX IF NOT EXISTS uq_factures_proforma_id
    ON factures (proforma_id)
    WHERE proforma_id IS NOT NULL;

COMMIT;
