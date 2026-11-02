-- Signature personnelle chiffrée des agents et preuve d'acceptation des conditions.
ALTER TABLE agents
    ADD COLUMN IF NOT EXISTS signature_fiche_atelier_chiffree TEXT;

ALTER TABLE fiches_atelier
    ADD COLUMN IF NOT EXISTS conditions_acceptees BOOLEAN;

ALTER TABLE fiches_atelier
    ADD COLUMN IF NOT EXISTS conditions_acceptees_at TIMESTAMP;
