-- Auteur : Codex | Date : 2026-10-21 | Action : migrer les anciens statuts OR vers le parcours OT unifié
-- Les anciens statuts représentaient des étapes de l'atelier désormais portées par l'OT.
-- Ils sont regroupés sous ORDRE_DE_TRAVAIL, sans supprimer ni recréer d'ordres.

BEGIN;

UPDATE ordres_reparation
SET statut = 'ORDRE_DE_TRAVAIL'
WHERE statut IN (
    'BON_DE_COMMANDE',
    'BON_DE_SORTIE',
    'ASSIGN_TECHNICIEN',
    'REPARATION'
);

-- L'OT ne porte plus le paiement ni la livraison : ces étapes sont sur l'OR.
-- L'approvisionnement a été supprimé du parcours, donc l'ancien statut mène au BS.
UPDATE ordres_travail
SET statut = CASE
    WHEN statut = 'BON_DE_COMMANDE' THEN 'BON_DE_SORTIE'
    WHEN statut IN ('PAIEMENT', 'PRET_A_LIVRER', 'LIVRE') THEN 'TERMINE'
    ELSE statut
END
WHERE statut IN ('BON_DE_COMMANDE', 'PAIEMENT', 'PRET_A_LIVRER', 'LIVRE');

ALTER TABLE ordres_travail
    DROP CONSTRAINT IF EXISTS ordres_travail_statut_check;

ALTER TABLE ordres_travail
    ADD CONSTRAINT ordres_travail_statut_check
    CHECK (statut IN (
        'RECAPITULATIF',
        'BON_DE_SORTIE',
        'ASSIGN_TECHNICIEN',
        'REPARATION',
        'TERMINE'
    ));

COMMIT;
