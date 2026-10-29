-- Migration manuelle : Suppression de la colonne redondante 'reference' de pieces_detachees
-- (La référence active de la pièce est stockée dans la colonne 'numero_serie')
ALTER TABLE pieces_detachees DROP COLUMN IF EXISTS reference;

ALTER TABLE pieces_detachees DROP COLUMN IF EXISTS deleted;