-- Auteur : Codex | Date : 2026-10-29 | Action : autoriser le statut CONVERTIE des proformas
-- La contrainte historique générée par PostgreSQL/Hibernate ne contenait pas CONVERTIE.

BEGIN;

DO $$
DECLARE
    contrainte RECORD;
BEGIN
    FOR contrainte IN
        SELECT conname
        FROM pg_constraint
        WHERE conrelid = 'proformas'::regclass
          AND contype = 'c'
          AND pg_get_constraintdef(oid) ILIKE '%statut%'
    LOOP
        EXECUTE format('ALTER TABLE proformas DROP CONSTRAINT %I', contrainte.conname);
    END LOOP;

    ALTER TABLE proformas
        ADD CONSTRAINT proformas_statut_check
        CHECK (statut IN (
            'EN_ATTENTE',
            'ACCEPTE',
            'REJETE',
            'PAYEE',
            'PARTIELLEMENT_PAYEE',
            'ANNULEE',
            'CONVERTIE'
        ));
END $$;

COMMIT;
