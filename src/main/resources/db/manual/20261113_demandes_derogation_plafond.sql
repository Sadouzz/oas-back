-- Requests for one-time quota overrides at appointment booking.
CREATE TABLE IF NOT EXISTS demandes_derogation_plafond (
    id BIGSERIAL PRIMARY KEY,
    client_id BIGINT NOT NULL REFERENCES clients(id),
    vehicule_id BIGINT NOT NULL REFERENCES vehicules(id),
    garage_id BIGINT NULL REFERENCES garages(id),
    rendez_vous_id BIGINT NULL REFERENCES rendez_vous(id),
    date_rendez_vous TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    motif TEXT,
    commentaire TEXT,
    statut VARCHAR(20) NOT NULL DEFAULT 'EN_ATTENTE',
    statut_rendez_vous_demande VARCHAR(20) NOT NULL DEFAULT 'EN_ATTENTE',
    origine VARCHAR(20) NOT NULL,
    demandeur VARCHAR(255) NOT NULL,
    plafond_snapshot NUMERIC(15, 2),
    montant_consomme_snapshot NUMERIC(15, 2),
    echeance_jours_snapshot INTEGER,
    date_demande TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    decisionnaire VARCHAR(255),
    date_decision TIMESTAMP WITHOUT TIME ZONE,
    motif_decision TEXT
);

CREATE INDEX IF NOT EXISTS idx_derogation_plafond_statut_date
    ON demandes_derogation_plafond (statut, date_demande);

CREATE INDEX IF NOT EXISTS idx_derogation_plafond_client_statut
    ON demandes_derogation_plafond (client_id, statut);
