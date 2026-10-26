-- Auteur : Codex | Date : 2026-10-26 | Action : préserver les soldes et mouvements historiques dans le portefeuille prépayé
-- À exécuter après le démarrage applicatif ayant créé portefeuilles_clients et mouvements_portefeuille_client.
-- Les anciennes tables/colonnes restent intactes pour préserver les données et permettre un retour arrière.

-- Les anciens crédits n'ont pas de garage associé. Ils ne sont attribués que si un seul garage est actif.
-- En environnement multi-garage, les montants restent dans comptes_clients jusqu'à leur attribution métier.
INSERT INTO portefeuilles_clients (client_id, garage_id, solde, version, date_creation, date_modification)
SELECT cc.client_id,
       g.id,
       COALESCE(cc.solde_credit, 0),
       0,
       COALESCE(cc.date_creation, CURRENT_TIMESTAMP),
       COALESCE(cc.date_modification, CURRENT_TIMESTAMP)
FROM comptes_clients cc
CROSS JOIN (SELECT id FROM garages WHERE archived = false) g
WHERE NOT EXISTS (
    SELECT 1 FROM portefeuilles_clients pc WHERE pc.client_id = cc.client_id AND pc.garage_id = g.id
)
AND (SELECT COUNT(*) FROM garages WHERE archived = false) = 1;

INSERT INTO mouvements_portefeuille_client
    (portefeuille_id, type, montant, mode_paiement, commentaire, acteur_id,
     facture_id, recu_id, source_mouvement_legacy_id, date_creation)
SELECT pc.id,
       'ALIMENTATION',
       mc.montant,
       NULL,
       COALESCE(mc.commentaire, 'Reprise du crédit historique'),
       mc.acteur_id,
       NULL,
       NULL,
       mc.id,
       COALESCE(mc.date_creation, CURRENT_TIMESTAMP)
FROM mouvements_credit_client mc
JOIN comptes_clients cc ON cc.id = mc.compte_client_id
JOIN portefeuilles_clients pc ON pc.client_id = cc.client_id
    AND pc.garage_id = (SELECT id FROM garages WHERE archived = false)
WHERE NOT EXISTS (
    SELECT 1 FROM mouvements_portefeuille_client mp
    WHERE mp.source_mouvement_legacy_id = mc.id
)
AND (SELECT COUNT(*) FROM garages WHERE archived = false) = 1;
