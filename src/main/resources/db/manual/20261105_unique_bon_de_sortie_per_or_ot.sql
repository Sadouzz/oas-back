-- Migration manuelle : contraintes d'unicité sur ordre_reparation_id et ordre_travail_id dans bons_de_sortie
-- Un Ordre de Réparation (OR) et un Ordre de Travail (OT) ne peuvent avoir qu'un seul Bon de Sortie (BS).

-- 1. Index unique partiel pour ordre_reparation_id (non null)
CREATE UNIQUE INDEX IF NOT EXISTS uq_bons_de_sortie_ordre_reparation 
ON bons_de_sortie (ordre_reparation_id) 
WHERE ordre_reparation_id IS NOT NULL;

-- 2. Index unique partiel pour ordre_travail_id (non null)
CREATE UNIQUE INDEX IF NOT EXISTS uq_bons_de_sortie_ordre_travail 
ON bons_de_sortie (ordre_travail_id) 
WHERE ordre_travail_id IS NOT NULL;

