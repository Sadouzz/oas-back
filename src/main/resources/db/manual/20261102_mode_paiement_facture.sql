-- Enregistre le choix de facturation (moyen et variante fiscale) à l'émission.
ALTER TABLE factures
    ADD COLUMN IF NOT EXISTS mode_paiement VARCHAR(40);
