-- Création de la table pour les documents et photos de l'ordre de travail (Step 4 Réparation & OT)
CREATE TABLE IF NOT EXISTS documents_ordre_travail (
    id BIGSERIAL PRIMARY KEY,
    ordre_travail_id BIGINT NOT NULL,
    nom_fichier VARCHAR(255),
    url VARCHAR(1000) NOT NULL,
    type VARCHAR(50) NOT NULL DEFAULT 'DOCUMENT',
    taille BIGINT,
    remarque TEXT,
    public_id VARCHAR(255),
    etape INTEGER DEFAULT 4,
    technicien_id BIGINT,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_document_ot_ordre_travail FOREIGN KEY (ordre_travail_id) REFERENCES ordres_travail(id) ON DELETE CASCADE,
    CONSTRAINT fk_document_ot_technicien FOREIGN KEY (technicien_id) REFERENCES techniciens(id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_documents_ot_ordre_travail_id ON documents_ordre_travail(ordre_travail_id);
CREATE INDEX IF NOT EXISTS idx_documents_ot_etape ON documents_ordre_travail(etape);
CREATE INDEX IF NOT EXISTS idx_documents_ot_type ON documents_ordre_travail(type);

