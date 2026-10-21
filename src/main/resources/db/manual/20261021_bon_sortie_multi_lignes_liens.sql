-- Auteur : Codex | Date : 2026-10-21 | Action : permettre plusieurs BS par OR/OT et les lignes PDG/PDS/MO
-- Migration additive : conserve les bons et lignes existants.

BEGIN;

ALTER TABLE lignes_bon_de_sortie_piece
    ALTER COLUMN piece_id DROP NOT NULL,
    ADD COLUMN IF NOT EXISTS type_ligne varchar(32) NOT NULL DEFAULT 'PIECE',
    ADD COLUMN IF NOT EXISTS main_doeuvre_id bigint,
    ADD COLUMN IF NOT EXISTS designation_snapshot varchar(255);

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fk_ligne_bs_main_doeuvre'
          AND conrelid = 'lignes_bon_de_sortie_piece'::regclass
    ) THEN
        ALTER TABLE lignes_bon_de_sortie_piece
            ADD CONSTRAINT fk_ligne_bs_main_doeuvre
            FOREIGN KEY (main_doeuvre_id) REFERENCES main_doeuvre(id);
    END IF;
END $$;

ALTER TABLE bons_de_sortie
    ADD COLUMN IF NOT EXISTS ordre_travail_id bigint,
    ADD COLUMN IF NOT EXISTS montant_total_pieces numeric(15,2) NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS montant_total_main_doeuvre numeric(15,2) NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS montant_total numeric(15,2) NOT NULL DEFAULT 0;

-- Reconstitue les montants des BS à partir des lignes enregistrées.
UPDATE bons_de_sortie bs
SET montant_total_pieces = totals.total_pieces,
    montant_total_main_doeuvre = totals.total_main_doeuvre,
    montant_total = totals.total_pieces + totals.total_main_doeuvre
FROM (
    SELECT ligne.bon_de_sortie_id,
           COALESCE(SUM(CASE WHEN ligne.type_ligne = 'PIECE' THEN ligne.prix * ligne.quantite ELSE 0 END), 0)::numeric(15,2) AS total_pieces,
           COALESCE(SUM(CASE WHEN ligne.type_ligne = 'MAIN_DOEUVRE' THEN ligne.prix * ligne.quantite ELSE 0 END), 0)::numeric(15,2) AS total_main_doeuvre
    FROM lignes_bon_de_sortie_piece ligne
    GROUP BY ligne.bon_de_sortie_id
) totals
WHERE totals.bon_de_sortie_id = bs.id;

UPDATE bons_de_sortie bs
SET ordre_travail_id = ot.id
FROM ordres_travail ot
WHERE ot.bon_de_sortie_id = bs.id
  AND bs.ordre_travail_id IS NULL;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fk_bon_sortie_ordre_travail'
          AND conrelid = 'bons_de_sortie'::regclass
    ) THEN
        ALTER TABLE bons_de_sortie
            ADD CONSTRAINT fk_bon_sortie_ordre_travail
            FOREIGN KEY (ordre_travail_id) REFERENCES ordres_travail(id);
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_bon_sortie_ordre_reparation
    ON bons_de_sortie (ordre_reparation_id);
CREATE INDEX IF NOT EXISTS idx_bon_sortie_ordre_travail
    ON bons_de_sortie (ordre_travail_id);
CREATE INDEX IF NOT EXISTS idx_ligne_bs_main_doeuvre
    ON lignes_bon_de_sortie_piece (main_doeuvre_id);

DO $$
DECLARE constraint_name text;
BEGIN
    FOR constraint_name IN
        SELECT c.conname
        FROM pg_constraint c
        JOIN pg_attribute a ON a.attrelid = c.conrelid AND a.attnum = ANY(c.conkey)
        WHERE c.conrelid = 'bons_de_sortie'::regclass
          AND c.contype = 'u'
          AND a.attname = 'ordre_reparation_id'
    LOOP
        EXECUTE format('ALTER TABLE bons_de_sortie DROP CONSTRAINT %I', constraint_name);
    END LOOP;
END $$;

COMMIT;
