-- Conserve les dates/heures BS sources lorsque plusieurs lignes PDG sont consolidées au BL.
CREATE TABLE IF NOT EXISTS ligne_facturation_piece_dates_source (
    ligne_facturation_piece_id BIGINT NOT NULL,
    position INTEGER NOT NULL,
    date_heure_saisie TIMESTAMP NOT NULL,
    PRIMARY KEY (ligne_facturation_piece_id, position),
    CONSTRAINT fk_ligne_facturation_piece_dates_source
        FOREIGN KEY (ligne_facturation_piece_id)
        REFERENCES lignes_facturation_piece (id)
        ON DELETE CASCADE
);
