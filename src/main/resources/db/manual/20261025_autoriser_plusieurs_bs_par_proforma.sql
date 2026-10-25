-- Migration pour autoriser plusieurs Bons de Sortie par Proforma
BEGIN;

ALTER TABLE bons_de_sortie DROP CONSTRAINT IF EXISTS uk2x3pylvagcd9d99tu8brc420n;

DO $$
DECLARE constraint_name text;
BEGIN
    FOR constraint_name IN
        SELECT c.conname
        FROM pg_constraint c
        JOIN pg_attribute a ON a.attrelid = c.conrelid AND a.attnum = ANY(c.conkey)
        WHERE c.conrelid = 'bons_de_sortie'::regclass
          AND c.contype = 'u'
          AND a.attname = 'proforma_id'
    LOOP
        EXECUTE format('ALTER TABLE bons_de_sortie DROP CONSTRAINT %I', constraint_name);
    END LOOP;
END $$;

CREATE INDEX IF NOT EXISTS idx_bon_sortie_proforma
    ON bons_de_sortie (proforma_id);

COMMIT;
