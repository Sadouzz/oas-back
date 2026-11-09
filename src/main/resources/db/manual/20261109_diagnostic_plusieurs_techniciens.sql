-- Auteur : Codex | Date : 2026-11-09 | Action : persister plusieurs techniciens par diagnostic
-- À exécuter une fois après déploiement de l'entité Diagnostic mise à jour.
-- Préserve l'affectation historique enregistrée dans diagnostics.technicien_id.

CREATE TABLE IF NOT EXISTS diagnostic_techniciens (
    diagnostic_id BIGINT NOT NULL REFERENCES diagnostics(id) ON DELETE CASCADE,
    technicien_id BIGINT NOT NULL REFERENCES techniciens(id),
    PRIMARY KEY (diagnostic_id, technicien_id)
);

INSERT INTO diagnostic_techniciens (diagnostic_id, technicien_id)
SELECT id, technicien_id
FROM diagnostics
WHERE technicien_id IS NOT NULL
ON CONFLICT (diagnostic_id, technicien_id) DO NOTHING;
