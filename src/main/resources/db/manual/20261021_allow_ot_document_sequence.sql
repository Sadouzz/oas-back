-- Auteur : Codex | Date : 2026-10-21 | Action : autoriser la numérotation des ordres de travail
-- Migration additive : conserve les types autorisés et les compteurs déjà enregistrés.

BEGIN;

ALTER TABLE document_sequences
    DROP CONSTRAINT IF EXISTS document_sequences_document_type_check;

ALTER TABLE document_sequences
    ADD CONSTRAINT document_sequences_document_type_check
    CHECK (document_type IN (
        'FA', 'FC', 'PF', 'BS', 'BC', 'BR', 'BL', 'RC', 'AH', 'AT',
        'NP', 'DP', 'FO', 'PC', 'MEC', 'RDV', 'MSG', 'DMD', 'AG', 'OR', 'OT'
    ));

COMMIT;
