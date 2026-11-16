-- Preserve the financial eligibility context submitted to the Super Agent.
ALTER TABLE demandes_derogation_plafond
    ADD COLUMN IF NOT EXISTS details_blocage_snapshot TEXT;
