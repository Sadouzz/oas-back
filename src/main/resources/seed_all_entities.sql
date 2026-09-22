-- ============================================================================
-- SCRIPT DE PEUPLEMENT COHERENT DE TOUTES LES ENTITES - OAS FACTURATION
-- ============================================================================
-- Ce script peuple de maniere idempotente et interconnectee les 39 entites
-- restantes de la base de donnees OAS (portant le total a 50 entites).
-- Les relations font reference aux Garages (1, 2), Utilisateurs/Agents (1-7),
-- Clients (8-13), Techniciens (14-18), Vehicules (1-9), Fiches Atelier (1-2),
-- et Articles de Blog (1-8).

INSERT INTO depots (id, nom, description, garage_id)
VALUES 
  (1, 'Magasin Central Dakar', 'Magasin principal de stockage des pièces neuves et consommables - OAS Dakar', 1),
  (2, 'Atelier Rapide Dakar', 'Stock tampon de l''atelier mécanique pour entretien courant et pièces de rotation', 1),
  (3, 'Magasin Thiès', 'Dépôt régional - OAS Thiès pour pièces détachées et pneumatiques', 2)
ON CONFLICT (id) DO NOTHING;
SELECT setval(pg_get_serial_sequence('depots', 'id'), GREATEST((SELECT MAX(id) FROM depots), 10));

-- 2. CATEGORIES DE PIECES
INSERT INTO categories (id, nom, depot_id, garage_id)
VALUES
  (1, 'Freinage', 1, 1),
  (2, 'Filtration & Huiles', 1, 1),
  (3, 'Embrayage & Transmission', 1, 1),
  (4, 'Suspension & Direction', 1, 1),
  (5, 'Électricité & Batterie', 1, 1),
  (6, 'Carrosserie & Éclairage', 2, 1),
  (7, 'Pneumatiques', 3, 2)
ON CONFLICT (id) DO NOTHING;
SELECT setval(pg_get_serial_sequence('categories', 'id'), GREATEST((SELECT MAX(id) FROM categories), 10));

-- 3. PIECES DETACHEES (PDP, PDG, PDS)
-- numero_serie = reference unique catalogue
-- reference = legacyReference
INSERT INTO pieces_detachees (
  id, type_piece, designation, reference, numero_serie, numero, 
  pourcentage, prix_gros, prix_unitaire, stock_magasin, stock_atelier, 
  qte_reelle, seuil_minimum, statut, deleted, categorie_id, garage_id, created_at, update_at
) VALUES
  (1, 'PDP', 'Jeu de 4 plaquettes de frein avant Toyota Corolla', 'NA-TOY-001', 'TOY-BRK-04465', 'PC-2026-0001',
   38.89, 18000.0, 25000.0, 14.0, 2.0, 16.0, 5.0, 'ACTIF', false, 1, 1, NOW() - INTERVAL '20 days', NOW() - INTERVAL '1 day'),
  (2, 'PDP', 'Disque de frein avant ventilé (la paire)', 'NA-BRK-002', 'BRK-VENT-43512', 'PC-2026-0002',
   42.86, 35000.0, 50000.0, 8.0, 1.0, 9.0, 4.0, 'ACTIF', false, 1, 1, NOW() - INTERVAL '20 days', NOW() - INTERVAL '2 days'),
  (3, 'PDP', 'Kit embrayage complet Kia Sportage (Disque + Mécanisme + Butée)', 'NA-KIA-003', 'VAL-CLU-41200', 'PC-2026-0003',
   50.0, 110000.0, 165000.0, 4.0, 1.0, 5.0, 2.0, 'ACTIF', false, 3, 1, NOW() - INTERVAL '25 days', NOW() - INTERVAL '8 days'),
  (4, 'PDP', 'Volant moteur bi-masse Kia Sportage 2.0 CRDi', 'NA-KIA-004', 'LUK-DMF-41502', 'PC-2026-0004',
   43.59, 195000.0, 280000.0, 3.0, 0.0, 3.0, 2.0, 'ACTIF', false, 3, 1, NOW() - INTERVAL '25 days', NOW() - INTERVAL '8 days'),
  (5, 'PDP', 'Filtre à huile synthétique Toyota Hilux / Corolla', 'NA-FLT-005', 'MANN-W683-01', 'PC-2026-0005',
   71.43, 3500.0, 6000.0, 25.0, 5.0, 30.0, 10.0, 'ACTIF', false, 2, 1, NOW() - INTERVAL '30 days', NOW() - INTERVAL '3 days'),
  (6, 'PDP', 'Bidon 5L Huile Moteur 5W30 Synthèse Total Quartz 9000', 'NA-OIL-006', 'TOT-5W30-Q9000', 'PC-2026-0006',
   55.56, 18000.0, 28000.0, 30.0, 6.0, 36.0, 12.0, 'ACTIF', false, 2, 1, NOW() - INTERVAL '30 days', NOW() - INTERVAL '3 days'),
  (7, 'PDP', 'Filtre à air habitacle anti-pollen Duster', 'NA-FLT-007', 'PUR-HAB-1102', 'PC-2026-0007',
   70.0, 5000.0, 8500.0, 12.0, 2.0, 14.0, 5.0, 'ACTIF', false, 2, 1, NOW() - INTERVAL '15 days', NOW() - INTERVAL '5 days'),
  (8, 'PDP', 'Batterie VARTA Blue Dynamic 12V 70Ah 640A', 'NA-BAT-008', 'VAR-E11-574012', 'PC-2026-0008',
   50.0, 48000.0, 72000.0, 7.0, 1.0, 8.0, 3.0, 'ACTIF', false, 5, 1, NOW() - INTERVAL '15 days', NOW() - INTERVAL '4 days'),
  (9, 'PDP', 'Amortisseur avant à gaz pour Nissan Qashqai', 'NA-SUS-009', 'MON-G8055-AV', 'PC-2026-0009',
   52.63, 38000.0, 58000.0, 6.0, 0.0, 6.0, 2.0, 'ACTIF', false, 4, 1, NOW() - INTERVAL '12 days', NOW() - INTERVAL '2 days'),
  (10, 'PDG', 'Lot de 20 Filtres à Carburant Diesel Bosch', 'NA-GROS-010', 'BOS-LOT-DIESEL20', 'PC-2026-0010',
   50.0, 120000.0, 180000.0, 5.0, 0.0, 5.0, 1.0, 'ACTIF', false, 2, 1, NOW() - INTERVAL '10 days', NOW() - INTERVAL '1 day'),
  (11, 'PDS', 'Injecteur Common Rail Bosch Reconditionné Hyundai Tucson', 'NA-SRV-011', 'BOS-INJ-0445110', 'PC-2026-0011',
   46.15, 65000.0, 95000.0, 2.0, 0.0, 2.0, 1.0, 'ACTIF', false, 2, 2, NOW() - INTERVAL '8 days', NOW() - INTERVAL '1 day'),
  (12, 'PDP', 'Pneumatique Michelin Primacy 4 215/60 R16 99V', 'NA-PNEU-012', 'MICH-215-60R16', 'PC-2026-0012',
   44.23, 52000.0, 75000.0, 12.0, 2.0, 14.0, 4.0, 'ACTIF', false, 7, 2, NOW() - INTERVAL '14 days', NOW() - INTERVAL '3 days')
ON CONFLICT (id) DO NOTHING;
SELECT setval(pg_get_serial_sequence('pieces_detachees', 'id'), GREATEST((SELECT MAX(id) FROM pieces_detachees), 20));

-- 4. STOCK GARAGE
INSERT INTO stock_garage (id, garage_id, piece_id, quantite, emplacement, last_updated)
VALUES
  (1, 1, 1, 16, 'Rayon A-01 (Freinage)', NOW() - INTERVAL '1 day'),
  (2, 1, 2, 9, 'Rayon A-02 (Disques)', NOW() - INTERVAL '2 days'),
  (3, 1, 3, 5, 'Rayon B-04 (Embrayages)', NOW() - INTERVAL '8 days'),
  (4, 1, 4, 3, 'Rayon B-05 (Volants)', NOW() - INTERVAL '8 days'),
  (5, 1, 5, 30, 'Rayon C-01 (Filtres)', NOW() - INTERVAL '3 days'),
  (6, 1, 6, 36, 'Palette H-01 (Lubrifiants)', NOW() - INTERVAL '3 days'),
  (7, 1, 7, 14, 'Rayon C-02 (Habitacle)', NOW() - INTERVAL '5 days'),
  (8, 1, 8, 8, 'Rayon D-01 (Batteries)', NOW() - INTERVAL '4 days'),
  (9, 1, 9, 6, 'Rayon D-03 (Suspension)', NOW() - INTERVAL '2 days'),
  (10, 1, 10, 5, 'Zone Gros-01 (Palettes)', NOW() - INTERVAL '1 day'),
  (11, 2, 11, 2, 'Rayon T-02 (Injection Thiès)', NOW() - INTERVAL '1 day'),
  (12, 2, 12, 14, 'Rack Pneus-01 (Thiès)', NOW() - INTERVAL '3 days')
ON CONFLICT (id) DO NOTHING;
SELECT setval(pg_get_serial_sequence('stock_garage', 'id'), GREATEST((SELECT MAX(id) FROM stock_garage), 20));

-- 5. STOCK MOUVEMENTS & PIECE MOUVEMENTS
INSERT INTO piece_mouvements (
  id, type, quantite, stock_magasin_avant, stock_atelier_avant, stock_magasin_apres, stock_atelier_apres, 
  stock_reel_apres, prenom, nom, num_document, type_document, numero_serie, immatriculation, motif, 
  date_operation, piece_id, agent_id, garage_id
) VALUES
  (1, 'ENTREE', 20.0, 0.0, 0.0, 20.0, 0.0, 20.0, 'Ibrahima', 'Kane', 'BR-2026-0001', 'BON_RECEPTION', 'TOY-BRK-04465', NULL, 'Réception commande fournisseur CFAO', NOW() - INTERVAL '15 days', 1, 5, 1),
  (2, 'SORTIE_MAGASIN_VERS_ATELIER', 4.0, 20.0, 0.0, 16.0, 4.0, 20.0, 'Ibrahima', 'Kane', 'BS-2026-0001', 'BON_SORTIE', 'TOY-BRK-04465', 'DK-4471-CD', 'Transfert atelier pour fiche atelier F-01', NOW() - INTERVAL '5 days', 1, 5, 1),
  (3, 'SORTIE_ATELIER_VALIDEE', 2.0, 16.0, 4.0, 14.0, 2.0, 16.0, 'Moussa', 'Sow', 'BS-2026-0001', 'BON_SORTIE', 'TOY-BRK-04465', 'DK-4471-CD', 'Pose plaquettes sur Toyota Corolla DK-4471-CD', NOW() - INTERVAL '1 day', 1, 3, 1),
  (4, 'ENTREE', 6.0, 0.0, 0.0, 6.0, 0.0, 6.0, 'Ibrahima', 'Kane', 'BR-2026-0002', 'BON_RECEPTION', 'VAL-CLU-41200', NULL, 'Réception commande SenPièces', NOW() - INTERVAL '20 days', 3, 5, 1),
  (5, 'SORTIE_REELLE', 1.0, 6.0, 0.0, 5.0, 0.0, 5.0, 'Moussa', 'Sow', 'BS-2026-0002', 'BON_SORTIE', 'VAL-CLU-41200', 'DK-7745-KL', 'Remplacement kit embrayage Sportage', NOW() - INTERVAL '8 days', 3, 3, 1),
  (6, 'SORTIE_REELLE', 1.0, 4.0, 0.0, 3.0, 0.0, 3.0, 'Moussa', 'Sow', 'BS-2026-0002', 'BON_SORTIE', 'LUK-DMF-41502', 'DK-7745-KL', 'Remplacement volant moteur Sportage', NOW() - INTERVAL '8 days', 4, 3, 1)
ON CONFLICT (id) DO NOTHING;
SELECT setval(pg_get_serial_sequence('piece_mouvements', 'id'), GREATEST((SELECT MAX(id) FROM piece_mouvements), 20));

-- Synchronisation de stock_mouvements pour compatibilité totale
INSERT INTO stock_mouvements (
  id, type, quantite, stock_magasin_avant, stock_atelier_avant, stock_magasin_apres, stock_atelier_apres, 
  stock_reel_apres, prenom, nom, num_document, type_document, numero_serie, immatriculation, motif, 
  date_operation, piece_id, agent_id, garage_id
)
SELECT id, type, quantite, stock_magasin_avant, stock_atelier_avant, stock_magasin_apres, stock_atelier_apres, 
       stock_reel_apres, prenom, nom, num_document, type_document, numero_serie, immatriculation, motif, 
       date_operation, piece_id, agent_id, garage_id
FROM piece_mouvements
ON CONFLICT (id) DO NOTHING;
SELECT setval(pg_get_serial_sequence('stock_mouvements', 'id'), GREATEST((SELECT MAX(id) FROM stock_mouvements), 20));

-- 6. CATEGORIE MAIN-D'ŒUVRE
INSERT INTO categorie_main_doeuvre (id, nom)
VALUES
  (1, 'Entretien Courant & Vidange'),
  (2, 'Système de Freinage'),
  (3, 'Embrayage & Transmission'),
  (4, 'Diagnostic & Électronique'),
  (5, 'Carrosserie & Peinture'),
  (6, 'Climatisation & Chauffage')
ON CONFLICT (id) DO NOTHING;
SELECT setval(pg_get_serial_sequence('categorie_main_doeuvre', 'id'), GREATEST((SELECT MAX(id) FROM categorie_main_doeuvre), 10));

-- 7. MAIN-D'ŒUVRE
INSERT INTO main_doeuvre (id, description, nbre_heure, prix, is_archived, categorie_id, created_at, updated_at)
VALUES
  (1, 'Remplacement plaquettes de frein avant', 1, 15000.0, false, 2, NOW() - INTERVAL '30 days', NOW()),
  (2, 'Purge et remplacement liquide de frein complet', 1, 12000.0, false, 2, NOW() - INTERVAL '30 days', NOW()),
  (3, 'Dépose/repose boîte de vitesses et kit embrayage', 5, 75000.0, false, 3, NOW() - INTERVAL '30 days', NOW()),
  (4, 'Remplacement volant moteur bi-masse', 2, 30000.0, false, 3, NOW() - INTERVAL '30 days', NOW()),
  (5, 'Vidange moteur + remplacement tous filtres', 1, 10000.0, false, 1, NOW() - INTERVAL '30 days', NOW()),
  (6, 'Diagnostic valise électronique complet', 1, 20000.0, false, 4, NOW() - INTERVAL '30 days', NOW()),
  (7, 'Recharge climatisation au gaz R134a', 1, 25000.0, false, 6, NOW() - INTERVAL '30 days', NOW()),
  (8, 'Redressage aile et peinture complète', 4, 50000.0, false, 5, NOW() - INTERVAL '30 days', NOW())
ON CONFLICT (id) DO NOTHING;
SELECT setval(pg_get_serial_sequence('main_doeuvre', 'id'), GREATEST((SELECT MAX(id) FROM main_doeuvre), 20));

-- 8. FOURNISSEURS
INSERT INTO fournisseur (id, matricule, nom_entreprise, nom, prenom, garage_id, archived, created_at, updated_at)
VALUES
  (1, 'FO-2026-0001', 'CFAO Motors Sénégal', 'Diouf', 'Ousmane', 1, false, NOW() - INTERVAL '60 days', NOW()),
  (2, 'FO-2026-0002', 'SenPièces Auto Dakar', 'Ndiaye', 'Babacar', 1, false, NOW() - INTERVAL '60 days', NOW()),
  (3, 'FO-2026-0003', 'TotalEnergies Sénégal', 'Fall', 'Seydou', 1, false, NOW() - INTERVAL '60 days', NOW()),
  (4, 'FO-2026-0004', 'Comptoir Général de Thiès', 'Sow', 'Moustapha', 2, false, NOW() - INTERVAL '60 days', NOW())
ON CONFLICT (id) DO NOTHING;
SELECT setval(pg_get_serial_sequence('fournisseur', 'id'), GREATEST((SELECT MAX(id) FROM fournisseur), 10));

-- 9. PARTENAIRES
INSERT INTO partenaire (id, nom, description, logo, type, archived, created_at, updated_at)
VALUES
  (1, 'TotalEnergies Sénégal', 'Fourniture de lubrifiants homologués constructeurs et carburants premium', 'total.png', 'LOCAL', false, NOW() - INTERVAL '90 days', NOW()),
  (2, 'AXA Assurances Sénégal', 'Partenaire agréé pour les expertises sinistres, bris de glace et carrosserie', 'axa.png', 'LOCAL', false, NOW() - INTERVAL '90 days', NOW()),
  (3, 'Bosch Car Service International', 'Fournisseur officiel d''équipements de diagnostic de pointe et pièces électroniques', 'bosch.png', 'EXTERIEUR', false, NOW() - INTERVAL '90 days', NOW()),
  (4, 'Wafa Assurance Sénégal', 'Partenaire conventionné pour le tiers payant et la prise en charge des flottes', 'wafa.png', 'LOCAL', false, NOW() - INTERVAL '90 days', NOW())
ON CONFLICT (id) DO NOTHING;
SELECT setval(pg_get_serial_sequence('partenaire', 'id'), GREATEST((SELECT MAX(id) FROM partenaire), 10));

-- 10. FICHE ATELIER CONFIGS
INSERT INTO fiche_atelier_configs (id, garage_id, config_json)
VALUES
  (1, 1, '{"lignesReception": ["Carrosserie", "Intérieur / Habitacle", "Vitrage / Pare-brise", "Eclairage", "Accessoires (Cric, roue de secours...)"], "defautsConstates": ["Mécanique", "Électrique", "Climatisation", "Peinture", "Tôlerie"], "rubriquesDefauts": ["Mécanique", "Électrique", "Climatisation", "Peinture", "Tôlerie"], "lignesDefauts": ["Mécanique", "Électrique", "Climatisation", "Peinture", "Tôlerie"], "defautsCarrosserie": ["Mécanique", "Électrique", "Climatisation", "Peinture", "Tôlerie"]}'),
  (2, 2, '{"lignesReception": ["Carrosserie", "Intérieur / Habitacle", "Vitrage / Pare-brise", "Eclairage", "Accessoires (Cric, roue de secours...)"], "defautsConstates": ["Mécanique", "Électrique", "Climatisation", "Peinture", "Tôlerie"], "rubriquesDefauts": ["Mécanique", "Électrique", "Climatisation", "Peinture", "Tôlerie"], "lignesDefauts": ["Mécanique", "Électrique", "Climatisation", "Peinture", "Tôlerie"], "defautsCarrosserie": ["Mécanique", "Électrique", "Climatisation", "Peinture", "Tôlerie"]}')
ON CONFLICT (id) DO UPDATE SET config_json = EXCLUDED.config_json;
SELECT setval(pg_get_serial_sequence('fiche_atelier_configs', 'id'), GREATEST((SELECT MAX(id) FROM fiche_atelier_configs), 10));

-- 11. BONS DE COMMANDE
INSERT INTO bons_de_commande (
  id, date_commande, date_modification, montantht, montanttva, montantttc, 
  numero, observation, paye, statut, tva_applicable, agent_id, fournisseur_id, garage_id, vehicule_id
) VALUES
  (1, NOW() - INTERVAL '16 days', NOW() - INTERVAL '15 days', 360000.0, 64800.0, 424800.0, 
   'BC-2026-0001', 'Commande réapprovisionnement plaquettes et filtres CFAO', true, 'RECU', true, 5, 1, 1, NULL),
  (2, NOW() - INTERVAL '22 days', NOW() - INTERVAL '20 days', 305000.0, 54900.0, 359900.0, 
   'BC-2026-0002', 'Pièces spécifiques embrayage Sportage et volant moteur', true, 'RECU', true, 5, 2, 1, 6),
  (3, NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days', 130000.0, 23400.0, 153400.0, 
   'BC-2026-0003', 'Commande injecteurs reconditionnés Thiès', false, 'ENVOYE', true, 6, 4, 2, 5)
ON CONFLICT (id) DO NOTHING;
SELECT setval(pg_get_serial_sequence('bons_de_commande', 'id'), GREATEST((SELECT MAX(id) FROM bons_de_commande), 10));

-- 12. LIGNES BON DE COMMANDE
INSERT INTO ligne_bon_de_commande (
  id, bon_commande_id, piece_detachee_id, quantite, quantite_recue, 
  prix_unitaire, montant, designation_pds, reference_pds, categorie_pds
) VALUES
  (1, 1, 1, 20, 20, 18000.0, 360000.0, 'Jeu de 4 plaquettes de frein avant Toyota Corolla', 'TOY-BRK-04465', 'Freinage'),
  (2, 2, 3, 1, 1, 110000.0, 110000.0, 'Kit embrayage complet Kia Sportage', 'VAL-CLU-41200', 'Embrayage & Transmission'),
  (3, 2, 4, 1, 1, 195000.0, 195000.0, 'Volant moteur bi-masse Kia Sportage 2.0 CRDi', 'LUK-DMF-41502', 'Embrayage & Transmission'),
  (4, 3, 11, 2, 0, 65000.0, 130000.0, 'Injecteur Common Rail Bosch Reconditionné Hyundai Tucson', 'BOS-INJ-0445110', 'Filtration & Huiles')
ON CONFLICT (id) DO NOTHING;
SELECT setval(pg_get_serial_sequence('ligne_bon_de_commande', 'id'), GREATEST((SELECT MAX(id) FROM ligne_bon_de_commande), 10));

-- 13. ORDRES DE REPARATION
INSERT INTO ordres_reparation (
  id, numero, date_creation, update_at, date_sortie, statut, description_travaux, 
  liste_defauts, lignes_travaux, lignes_reception, fiche_atelier_id, garage_id, vehicule_id
) VALUES
  (1, 'OR-2026-0001', NOW() - INTERVAL '2 days', NOW() - INTERVAL '1 day', NULL, 'EN_COURS',
   'Remplacement plaquettes de frein avant et purge circuit hydraulique',
   'Léger sifflement au freinage, plaquettes usées à 85%',
   '[{"nom": "Remplacement plaquettes de frein avant", "verrouille": true}, {"nom": "Purge circuit de frein", "verrouille": false}]'::jsonb,
   '[{"nom": "Autoradio", "etat": true, "verrouille": true}, {"nom": "Roue de secours", "etat": true, "verrouille": true}, {"nom": "Cric et clé", "etat": true, "verrouille": true}]'::jsonb,
   1, 1, 2),
  (2, 'OR-2026-0002', NOW() - INTERVAL '10 days', NOW() - INTERVAL '8 days', NOW() - INTERVAL '8 days', 'TERMINE',
   'Remplacement kit embrayage complet + volant moteur bi-masse',
   'Patinage excessif en 3ème et 4ème vitesse, bruits de claquement au point mort',
   '[{"nom": "Remplacement kit d''embrayage complet + volant moteur", "verrouille": true}]'::jsonb,
   '[{"nom": "Autoradio", "etat": true, "verrouille": true}, {"nom": "Tapis de sol", "etat": true, "verrouille": true}]'::jsonb,
   2, 1, 6),
  (3, 'OR-2026-0003', NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day', NULL, 'EN_DIAGNOSTIC',
   'Diagnostic voyant moteur allumé et perte de puissance ponctuelle',
   'Voyant injection allumé sur tableau de bord, fumée noire à l''accélération',
   '[{"nom": "Diagnostic électronique et contrôle injecteurs", "verrouille": false}]'::jsonb,
   '[{"nom": "Roue de secours", "etat": true, "verrouille": false}]'::jsonb,
   NULL, 2, 5)
ON CONFLICT (id) DO NOTHING;
SELECT setval(pg_get_serial_sequence('ordres_reparation', 'id'), GREATEST((SELECT MAX(id) FROM ordres_reparation), 10));

-- 14. FICHE MECANICIENS & REPARATION
INSERT INTO fiche_mecaniciens (fiche_id, mecanicien_id)
SELECT v.fiche_id, v.mecanicien_id FROM (VALUES (1, 14), (2, 14), (2, 15), (3, 18)) AS v(fiche_id, mecanicien_id)
WHERE NOT EXISTS (
  SELECT 1 FROM fiche_mecaniciens fm WHERE fm.fiche_id = v.fiche_id AND fm.mecanicien_id = v.mecanicien_id
);

INSERT INTO fiche_mecaniciens_reparation (fiche_id, mecanicien_id)
SELECT v.fiche_id, v.mecanicien_id FROM (VALUES (1, 14), (2, 14), (2, 15), (3, 18)) AS v(fiche_id, mecanicien_id)
WHERE NOT EXISTS (
  SELECT 1 FROM fiche_mecaniciens_reparation fmr WHERE fmr.fiche_id = v.fiche_id AND fmr.mecanicien_id = v.mecanicien_id
);

-- 15. LIGNES ORDRE REPARATION PIECE
INSERT INTO lignes_ordre_reparation_piece (
  id, ordre_reparation_id, piece_id, quantite, prix, is_custom, designation_pds
) VALUES
  (1, 1, 1, 1, 25000, false, 'Jeu de 4 plaquettes de frein avant Toyota Corolla'),
  (2, 2, 3, 1, 165000, false, 'Kit embrayage complet Kia Sportage'),
  (3, 2, 4, 1, 280000, false, 'Volant moteur bi-masse Kia Sportage 2.0 CRDi'),
  (4, 3, 11, 2, 95000, false, 'Injecteur Common Rail Bosch Reconditionné Hyundai Tucson')
ON CONFLICT (id) DO NOTHING;
SELECT setval(pg_get_serial_sequence('lignes_ordre_reparation_piece', 'id'), GREATEST((SELECT MAX(id) FROM lignes_ordre_reparation_piece), 10));

-- 16. LIGNES ORDRE REPARATION MAIN D'ŒUVRE
INSERT INTO lignes_ordre_reparation_main_doeuvre (
  id, ordre_reparation_id, main_doeuvre_id, nbre_heure, prix
) VALUES
  (1, 1, 1, 1, 15000),
  (2, 1, 2, 1, 12000),
  (3, 2, 3, 5, 75000),
  (4, 2, 4, 2, 30000),
  (5, 3, 6, 1, 20000)
ON CONFLICT (id) DO NOTHING;
SELECT setval(pg_get_serial_sequence('lignes_ordre_reparation_main_doeuvre', 'id'), GREATEST((SELECT MAX(id) FROM lignes_ordre_reparation_main_doeuvre), 10));

-- 17. REMARQUES DIAGNOSTIC
INSERT INTO remarques_diagnostic (id, ordre_reparation_id, technicien_id, contenu, created_at)
VALUES
  (1, 1, 14, 'Plaquettes arrivées au témoin d''usure. Disques avant encore en bon état (épaisseur 24.2mm, minimum 22mm). Purge recommandée.', NOW() - INTERVAL '1 day'),
  (2, 2, 14, 'Butée d''embrayage complètement grippée ayant endommagé le diaphragme. Jeu axial important constaté sur le volant bi-masse.', NOW() - INTERVAL '9 days'),
  (3, 3, 18, 'Codes défauts DTC P0201 et P0203 relevés : anomalies débit injecteurs cylindres 1 et 3.', NOW() - INTERVAL '1 day')
ON CONFLICT (id) DO NOTHING;
SELECT setval(pg_get_serial_sequence('remarques_diagnostic', 'id'), GREATEST((SELECT MAX(id) FROM remarques_diagnostic), 10));

-- 18. PIECES JOINTES DIAGNOSTIC
INSERT INTO pieces_jointes_diagnostic (id, ordre_reparation_id, technicien_id, type, url, remarque, created_at)
VALUES
  (1, 1, 14, 'PHOTO', 'https://res.cloudinary.com/oas/diag/plaquettes_usees_corolla.jpg', 'Garniture usée plaquette intérieure gauche', NOW() - INTERVAL '1 day'),
  (2, 2, 14, 'PHOTO', 'https://res.cloudinary.com/oas/diag/butee_embrayage_grippee.jpg', 'Disque d''embrayage usé jusqu''aux rivets', NOW() - INTERVAL '9 days'),
  (3, 3, 18, 'PDF', 'https://res.cloudinary.com/oas/diag/rapport_valise_tucson.pdf', 'Rapport de scan complet calculateur Delphi', NOW() - INTERVAL '1 day')
ON CONFLICT (id) DO NOTHING;
SELECT setval(pg_get_serial_sequence('pieces_jointes_diagnostic', 'id'), GREATEST((SELECT MAX(id) FROM pieces_jointes_diagnostic), 10));

-- 19. BONS DE SORTIE
INSERT INTO bons_de_sortie (
  id, reference, date, date_validation, statut, remarque, ordre_reparation_id, 
  vehicule_id, client_id, agent_emetteur_id, agent_validateur_id, garage_id
) VALUES
  (1, 'BS-2026-0001', NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day', 'VALIDE', 
   'Sortie plaquettes avant pour Corolla Khadija Sarr', 1, 2, 9, 3, 1, 1),
  (2, 'BS-2026-0002', NOW() - INTERVAL '8 days', NOW() - INTERVAL '8 days', 'VALIDE', 
   'Sortie kit embrayage + volant moteur pour Sportage Abdoulaye Thiam', 2, 6, 12, 3, 1, 1),
  (3, 'BS-2026-0003', NOW() - INTERVAL '1 day', NULL, 'EN_ATTENTE', 
   'Demande de sortie injecteurs pour Tucson Bineta Cissé', 3, 5, 11, 7, NULL, 2)
ON CONFLICT (id) DO NOTHING;
SELECT setval(pg_get_serial_sequence('bons_de_sortie', 'id'), GREATEST((SELECT MAX(id) FROM bons_de_sortie), 10));

-- 20. LIGNES BON DE SORTIE PIECE
INSERT INTO lignes_bon_de_sortie_piece (id, bon_de_sortie_id, piece_id, quantite, prix)
VALUES
  (1, 1, 1, 1, 25000),
  (2, 2, 3, 1, 165000),
  (3, 2, 4, 1, 280000),
  (4, 3, 11, 2, 95000)
ON CONFLICT (id) DO NOTHING;
SELECT setval(pg_get_serial_sequence('lignes_bon_de_sortie_piece', 'id'), GREATEST((SELECT MAX(id) FROM lignes_bon_de_sortie_piece), 10));

-- 21. BONS DE SORTIE HISTORIQUE
INSERT INTO bons_de_sortie_historique (
  id, bon_de_sortie_id, piece_id, agent_id, garage_id, statut, quantite, qte_reelle, 
  stock_magasin, stock_atelier, num_bs, designation, numero_serie, immatriculation, nom, prenom, motif, date_action
) VALUES
  (1, 1, 1, 1, 1, 'VALIDE', 1.0, 16.0, 14.0, 2.0, 'BS-2026-0001', 'Jeu de 4 plaquettes de frein avant Toyota Corolla', 'TOY-BRK-04465', 'DK-4471-CD', 'Sarr', 'Khadija', 'Validation sortie par Super Agent', NOW() - INTERVAL '1 day'),
  (2, 2, 3, 1, 1, 'VALIDE', 1.0, 5.0, 4.0, 1.0, 'BS-2026-0002', 'Kit embrayage complet Kia Sportage', 'VAL-CLU-41200', 'DK-7745-KL', 'Thiam', 'Abdoulaye', 'Validation sortie kit embrayage', NOW() - INTERVAL '8 days'),
  (3, 2, 4, 1, 1, 'VALIDE', 1.0, 3.0, 3.0, 0.0, 'BS-2026-0002', 'Volant moteur bi-masse Kia Sportage', 'LUK-DMF-41502', 'DK-7745-KL', 'Thiam', 'Abdoulaye', 'Validation sortie volant moteur', NOW() - INTERVAL '8 days')
ON CONFLICT (id) DO NOTHING;
SELECT setval(pg_get_serial_sequence('bons_de_sortie_historique', 'id'), GREATEST((SELECT MAX(id) FROM bons_de_sortie_historique), 10));

-- 22. FACTURATIONS (TABLE_PER_CLASS - PROFORMAS, BONS_DE_RECEPTION, FACTURES, NOTES_DE_PRIX, AVOIRS_HT, AVOIRS_TTC)
-- Utilisation d'identifiants dedies de la sequence facturation_seq (101 à 120)

-- A. PROFORMAS
INSERT INTO proformas (
  id, numero, statut, date_creation, date_modification, kilometrage, remarque, 
  montant_ht, montant_tva, montant_timbre, montant_ttc, montant_total, visible_client, 
  agent_id, garage_id, ordre_reparation_id, bon_de_commande_id
) VALUES
  (101, 'PF-2026-0001', 'ACCEPTE', NOW() - INTERVAL '2 days', NOW() - INTERVAL '1 day', 21000.0, 
   'Devis proforma validé par Mme Sarr par téléphone', 52000.0, 9360.0, 0.0, 61360.0, 61360.0, true, 4, 1, 1, NULL),
  (102, 'PF-2026-0002', 'ACCEPTE', NOW() - INTERVAL '10 days', NOW() - INTERVAL '9 days', 95000.0, 
   'Proforma remplacement complet embrayage + volant moteur', 550000.0, 99000.0, 0.0, 649000.0, 649000.0, true, 4, 1, 2, NULL),
  (103, 'PF-2026-0003', 'EN_ATTENTE', NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day', 9800.0, 
   'Proforma révision injecteurs Tucson', 210000.0, 37800.0, 0.0, 247800.0, 247800.0, true, 6, 2, 3, NULL)
ON CONFLICT (id) DO NOTHING;

-- B. BONS DE RECEPTION
INSERT INTO bons_de_reception (
  id, numero, statut, date_creation, date_modification, kilometrage, remarque, 
  montant_ht, montant_tva, montant_timbre, montant_ttc, montant_total, 
  agent_id, garage_id, ordre_reparation_id, bon_de_commande_id
) VALUES
  (104, 'BR-2026-0001', 'ACCEPTE', NOW() - INTERVAL '15 days', NOW() - INTERVAL '15 days', 0.0, 
   'Réception conforme commande CFAO', 360000.0, 64800.0, 0.0, 424800.0, 424800.0, 5, 1, NULL, 1),
  (105, 'BR-2026-0002', 'ACCEPTE', NOW() - INTERVAL '20 days', NOW() - INTERVAL '20 days', 95000.0, 
   'Réception kit embrayage et volant moteur SenPièces', 305000.0, 54900.0, 0.0, 359900.0, 359900.0, 5, 1, 2, 2)
ON CONFLICT (id) DO NOTHING;

-- C. FACTURES
INSERT INTO factures (
  id, numero, statut, statut_paiement, date_creation, date_modification, kilometrage, remarque, 
  montant_ht, montant_tva, montant_timbre, montant_ttc, montant_total, montant_autre, 
  montant_paye, reste_a_payer, numero_bon_de_commande, agent_id, garage_id, 
  ordre_reparation_id, bon_de_commande_id, client_id, vehicule_id
) VALUES
  (106, 'FC-2026-0001', 'PAYEE', 'PAYE', NOW() - INTERVAL '8 days', NOW() - INTERVAL '8 days', 95000.0, 
   'Facture soldée par chèque de banque', 550000.0, 99000.0, 0.0, 649000.0, 649000.0, 0.0, 
   649000.0, 0.0, 'BC-2026-0002', 4, 1, 2, NULL, 12, 6),
  (107, 'FC-2026-0002', 'PARTIELLEMENT_PAYEE', 'PARTIEL', NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day', 21000.0, 
   'Acompte versé en espèces, solde à la livraison', 52000.0, 9360.0, 0.0, 61360.0, 61360.0, 0.0, 
   30000.0, 31360.0, NULL, 4, 1, 1, NULL, 9, 2)
ON CONFLICT (id) DO NOTHING;

-- D. NOTES DE PRIX
INSERT INTO notes_de_prix (
  id, numero, statut, statut_paiement, mode_paiement, date_creation, date_modification, 
  kilometrage, remarque, montant_ht, montant_total, montant_autre, montant_paye, 
  reste_a_payer, numero_bon_de_commande, agent_id, garage_id, ordre_reparation_id, 
  bon_de_commande_id, client_id, vehicule_id
) VALUES
  (108, 'NP-2026-0001', 'PAYEE', 'PAYE', 'ESPECE', NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days', 
   52000.0, 'Vidange rapide + filtre à huile au comptoir', 34000.0, 34000.0, 0.0, 34000.0, 
   0.0, NULL, 4, 1, NULL, NULL, 8, 1)
ON CONFLICT (id) DO NOTHING;

-- E. AVOIRS HT
INSERT INTO avoirs_ht (
  id, numero, statut, date_creation, date_modification, kilometrage, remarque, 
  montant_ht, montant_total, agent_id, garage_id, ordre_reparation_id, 
  bon_de_commande_id, client_id, vehicule_id
) VALUES
  (109, 'AH-2026-0001', 'ACCEPTE', NOW() - INTERVAL '6 days', NOW() - INTERVAL '6 days', 95000.0, 
   'Remise commerciale exceptionnelle accordée sur la main-d''œuvre', 15000.0, 15000.0, 4, 1, 2, NULL, 12, 6)
ON CONFLICT (id) DO NOTHING;

-- F. AVOIRS TTC
INSERT INTO avoirs_ttc (
  id, numero, statut, date_creation, date_modification, kilometrage, remarque, 
  montant_ht, montant_tva, montant_timbre, montant_ttc, montant_total, 
  agent_id, garage_id, ordre_reparation_id, bon_de_commande_id, client_id, vehicule_id
) VALUES
  (110, 'AT-2026-0001', 'ACCEPTE', NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days', 95000.0, 
   'Avoir TTC régularisation client fidèle', 20000.0, 3600.0, 0.0, 23600.0, 23600.0, 4, 1, 2, NULL, 12, 6)
ON CONFLICT (id) DO NOTHING;

-- Avancer la sequence facturation_seq pour ne pas entrer en conflit avec les futures creations
SELECT setval('facturation_seq', 200);

-- 23. LIGNES FACTURATION PIECE
INSERT INTO lignes_facturation_piece (
  id, facturation_id, piece_id, quantite, prix, is_custom, designation_pds
) VALUES
  (1, 101, 1, 1, 25000, false, 'Jeu de 4 plaquettes de frein avant Toyota Corolla'),
  (2, 102, 3, 1, 165000, false, 'Kit embrayage complet Kia Sportage'),
  (3, 102, 4, 1, 280000, false, 'Volant moteur bi-masse Kia Sportage 2.0 CRDi'),
  (4, 106, 3, 1, 165000, false, 'Kit embrayage complet Kia Sportage'),
  (5, 106, 4, 1, 280000, false, 'Volant moteur bi-masse Kia Sportage 2.0 CRDi'),
  (6, 107, 1, 1, 25000, false, 'Jeu de 4 plaquettes de frein avant Toyota Corolla'),
  (7, 108, 5, 1, 6000, false, 'Filtre à huile synthétique Toyota Hilux'),
  (8, 108, 6, 1, 28000, false, 'Bidon 5L Huile Moteur 5W30 Synthèse Total Quartz 9000')
ON CONFLICT (id) DO NOTHING;
SELECT setval(pg_get_serial_sequence('lignes_facturation_piece', 'id'), GREATEST((SELECT MAX(id) FROM lignes_facturation_piece), 20));

-- 24. LIGNES FACTURATION MAIN D'ŒUVRE
INSERT INTO lignes_facturation_main_doeuvre (
  id, facturation_id, main_doeuvre_id, nbre_heure, tarif_horaire
) VALUES
  (1, 101, 1, 1, 15000),
  (2, 101, 2, 1, 12000),
  (3, 102, 3, 5, 15000),
  (4, 102, 4, 2, 15000),
  (5, 106, 3, 5, 15000),
  (6, 106, 4, 2, 15000),
  (7, 107, 1, 1, 15000),
  (8, 107, 2, 1, 12000)
ON CONFLICT (id) DO NOTHING;
SELECT setval(pg_get_serial_sequence('lignes_facturation_main_doeuvre', 'id'), GREATEST((SELECT MAX(id) FROM lignes_facturation_main_doeuvre), 20));

-- 25. RECUS
INSERT INTO recus (id, numero, facture_id, montant, mode_paiement, date_paiement, remarque, garage_id)
VALUES
  (1, 'RC-2026-0001', 106, 649000.0, 'CHEQUE', NOW() - INTERVAL '8 days', 'Chèque CBAO n°849302 réglant la facture FC-2026-0001', 1),
  (2, 'RC-2026-0002', 107, 30000.0, 'ESPECE', NOW() - INTERVAL '1 day', 'Acompte en espèces reçu au comptoir par Aissatou Ba', 1)
ON CONFLICT (id) DO NOTHING;
SELECT setval(pg_get_serial_sequence('recus', 'id'), GREATEST((SELECT MAX(id) FROM recus), 10));

-- 26. DEVIS PREVISIONNELS
-- StatutFacturation ordinal : 0=EN_ATTENTE, 1=ACCEPTE, 2=REJETE, 3=PAYEE, 4=PARTIELLEMENT_PAYEE, 5=ANNULEE
INSERT INTO devis_previsionnels (
  id, numero, date_creation, kilometrage_vehicule, montant_total, notes_reparation, 
  statut, agent_id, client_id, fiche_atelier_id, garage_id, vehicule_id
) VALUES
  (1, 'DP-2026-0001', NOW() - INTERVAL '2 days', 21000.0, 61360.0, 
   'Devis préliminaire pour remplacement plaquettes et purge', 1, 4, 9, 1, 1, 2),
  (2, 'DP-2026-0002', NOW() - INTERVAL '3 days', 35500.0, 95000.0, 
   'Estimation remplacement 2 amortisseurs avant + géométrie', 0, 4, 10, NULL, 1, 4),
  (3, 'DP-2026-0003', NOW() - INTERVAL '1 day', 9800.0, 210000.0, 
   'Estimation diagnostic et contrôle injecteurs diesel', 0, 6, 11, NULL, 2, 5)
ON CONFLICT (id) DO NOTHING;
SELECT setval(pg_get_serial_sequence('devis_previsionnels', 'id'), GREATEST((SELECT MAX(id) FROM devis_previsionnels), 10));

-- 27. MARKETPLACE PRODUITS
INSERT INTO marketplace_produits (id, nom, description, prix, disponible, archive, media_url)
VALUES
  (1, 'Pack Entretien Moteur Total Quartz 5W30 + Filtre Bosch', 
   'Pack complet comprenant 5L d''huile synthèse 5W30 et filtre à huile adapté', 32000.0, true, false, 
   'https://images.unsplash.com/photo-1486006920555-c77dce18193b'),
  (2, 'Batterie Auto VARTA 12V 70Ah Haute Performance', 
   'Batterie sans entretien garantie 2 ans avec technologie PowerFrame', 75000.0, true, false, 
   'https://images.unsplash.com/photo-1619642751034-765dfdf7c58e'),
  (3, 'Compresseur d''air portable 12V avec manomètre digital', 
   'Gonfleur autonome haute précision idéal pour pneumatiques et contrôle pression', 25000.0, true, false, 
   'https://images.unsplash.com/photo-1578844251758-2f71da64c96f'),
  (4, 'Kit de Sécurité Routière Conforme CEDEAO', 
   'Comprend 2 triangles de présignalisation, gilet jaune haute visibilité et extincteur 1kg', 18000.0, true, false, 
   'https://images.unsplash.com/photo-1542282088-72c9c27ed0cd')
ON CONFLICT (id) DO NOTHING;
SELECT setval(pg_get_serial_sequence('marketplace_produits', 'id'), GREATEST((SELECT MAX(id) FROM marketplace_produits), 10));

-- 28. MARKETPLACE DEMANDES
-- StatutDemandeProduit ordinal : 0=EN_ATTENTE, 1=ACCEPTEE, 2=REFUSEE, 3=COMMANDEE, 4=ANNULEE, 5=LIVREE
INSERT INTO marketplace_demandes (
  id, numero, client_id, produit_id, garage_id, quantite, statut, message, date_creation
) VALUES
  (1, 'DMD-2026-0001', 8, 1, 1, 2, 1, 'Bonjour, je souhaite commander 2 packs pour mon Hilux et le Land Cruiser', NOW() - INTERVAL '4 days'),
  (2, 'DMD-2026-0002', 13, 2, 1, 1, 0, 'Besoin urgent de remplacement batterie pour mon Nissan Qashqai', NOW() - INTERVAL '1 day'),
  (3, 'DMD-2026-0003', 10, 4, 1, 1, 5, 'Je passe récupérer le kit de sécurité cet après-midi au garage Dakar', NOW() - INTERVAL '7 days')
ON CONFLICT (id) DO NOTHING;
SELECT setval(pg_get_serial_sequence('marketplace_demandes', 'id'), GREATEST((SELECT MAX(id) FROM marketplace_demandes), 10));

-- 29. NOTIFICATIONS CLIENTS
INSERT INTO notifications (id, client_id, titre, message, lu, date_creation)
VALUES
  (1, 9, 'Diagnostic terminé', 'Le diagnostic de votre Toyota Corolla DK-4471-CD est terminé. Veuillez consulter le devis proforma PF-2026-0001.', true, NOW() - INTERVAL '1 day'),
  (2, 12, 'Véhicule prêt pour livraison', 'Les travaux sur votre Kia Sportage DK-7745-KL sont terminés avec succès. Votre facture FC-2026-0001 est disponible.', true, NOW() - INTERVAL '8 days'),
  (3, 8, 'Demande produit validée', 'Votre commande pour 2 Pack Entretien Moteur a été validée par OAS Dakar.', false, NOW() - INTERVAL '3 days'),
  (4, 11, 'Rendez-vous confirmé', 'Votre rendez-vous du vendredi pour le contrôle voyant moteur à Thiès a été confirmé.', false, NOW() - INTERVAL '1 day')
ON CONFLICT (id) DO NOTHING;
SELECT setval(pg_get_serial_sequence('notifications', 'id'), GREATEST((SELECT MAX(id) FROM notifications), 10));

-- 30. NOTIFICATIONS AGENTS
INSERT INTO agent_notifications (id, agent_id, titre, message, lu, date_creation)
VALUES
  (1, 3, 'Nouvelle Fiche Atelier', 'Fiche Atelier créée pour la Toyota Corolla DK-4471-CD (Khadija Sarr). Diagnostic requis.', true, NOW() - INTERVAL '2 days'),
  (2, 1, 'Bon de Sortie en attente', 'Le bon de sortie BS-2026-0001 attend votre validation pour sortie de stock.', true, NOW() - INTERVAL '1 day'),
  (3, 5, 'Alerte Seuil Minimum Stock', 'Le stock de Volant moteur bi-masse (LUK-DMF-41502) a atteint 3 unités (seuil : 2).', false, NOW() - INTERVAL '2 days'),
  (4, 7, 'Nouveau diagnostic programmé', 'Véhicule Hyundai Tucson TH-1123-IJ assigné au technicien Lamine Gueye.', false, NOW() - INTERVAL '1 day')
ON CONFLICT (id) DO NOTHING;
SELECT setval(pg_get_serial_sequence('agent_notifications', 'id'), GREATEST((SELECT MAX(id) FROM agent_notifications), 10));

-- 31. COMMENTAIRES DE BLOG
INSERT INTO blog_comments (id, post_id, author_name, author_email, content, date_creation, likes, dislikes, admin, parent_id)
VALUES
  (1, 1, 'Mamadou Diagne', 'mamadou.diagne@gmail.com', 'Excellent article ! J''ai fait la vérification avant mon voyage à Saint-Louis le week-end dernier.', NOW() - INTERVAL '1 day', 4, 0, false, NULL),
  (2, 1, 'Amadou Diallo', 'amadou.diallo@orientautoservice.sn', 'Merci pour votre retour M. Diagne ! Toute l''équipe OAS reste à votre entière disposition.', NOW() - INTERVAL '1 day', 2, 0, true, 1),
  (3, 2, 'Khadija Sarr', 'khadija.sarr@gmail.com', 'Très instructif, c''est exactement le petit grincement que j''entendais sur ma Corolla.', NOW() - INTERVAL '7 days', 3, 0, false, NULL),
  (4, 3, 'Ousmane Fall', 'ousmane.fall@gmail.com', 'Je ne savais pas qu''il fallait allumer la clim au moins 10 minutes en période fraîche. Bon à savoir à Dakar !', NOW() - INTERVAL '12 days', 5, 1, false, NULL),
  (5, 5, 'Bineta Cissé', 'bineta.cisse@gmail.com', 'Mon voyant moteur s''est allumé hier, je viens justement de prendre rendez-vous chez vous à Thiès.', NOW() - INTERVAL '2 days', 1, 0, false, NULL)
ON CONFLICT (id) DO NOTHING;
SELECT setval(pg_get_serial_sequence('blog_comments', 'id'), GREATEST((SELECT MAX(id) FROM blog_comments), 10));

-- 32. HISTORIQUE DE CONNEXIONS
INSERT INTO connection_history (id, username, ip_address, status, timestamp)
VALUES
  (1, 'admin', '192.168.1.50', 'SUCCESS', NOW() - INTERVAL '2 hours'),
  (2, 'moussa.sow', '192.168.1.52', 'SUCCESS', NOW() - INTERVAL '3 hours'),
  (3, 'khadija.sarr', '10.0.4.12', 'SUCCESS', NOW() - INTERVAL '4 hours'),
  (4, 'alioune.diouf', '192.168.1.60', 'SUCCESS', NOW() - INTERVAL '5 hours'),
  (5, 'abdoulaye.thiam', '10.0.8.21', 'SUCCESS', NOW() - INTERVAL '1 day'),
  (6, 'hacker_test', '45.33.32.156', 'FAILED', NOW() - INTERVAL '2 days'),
  (7, 'ibrahima.kane', '192.168.1.55', 'SUCCESS', NOW() - INTERVAL '6 hours'),
  (8, 'awa.diop', '192.168.2.10', 'SUCCESS', NOW() - INTERVAL '7 hours')
ON CONFLICT (id) DO NOTHING;
SELECT setval(pg_get_serial_sequence('connection_history', 'id'), GREATEST((SELECT MAX(id) FROM connection_history), 20));

-- 33. SEQUENCES DE DOCUMENTS (INITIALISATION DES TYPES MANQUANTS)
INSERT INTO document_sequences (document_type, next_value, year, garage_id)
VALUES
  ('FA', 3, 2026, 1),
  ('FA', 1, 2026, 2),
  ('FC', 3, 2026, 1),
  ('FC', 1, 2026, 2),
  ('PF', 3, 2026, 1),
  ('PF', 2, 2026, 2),
  ('BS', 3, 2026, 1),
  ('BS', 2, 2026, 2),
  ('BC', 3, 2026, 1),
  ('BC', 2, 2026, 2),
  ('BR', 3, 2026, 1),
  ('BR', 1, 2026, 2),
  ('RC', 3, 2026, 1),
  ('RC', 1, 2026, 2),
  ('AH', 2, 2026, 1),
  ('AH', 1, 2026, 2),
  ('AT', 2, 2026, 1),
  ('AT', 1, 2026, 2),
  ('NP', 2, 2026, 1),
  ('NP', 1, 2026, 2),
  ('DP', 3, 2026, 1),
  ('DP', 2, 2026, 2),
  ('FO', 4, 2026, 1),
  ('FO', 2, 2026, 2),
  ('PC', 11, 2026, 1),
  ('PC', 3, 2026, 2),
  ('OR', 3, 2026, 1),
  ('OR', 2, 2026, 2),
  ('DMD', 4, 2026, 1),
  ('DMD', 1, 2026, 2)
ON CONFLICT (garage_id, document_type, year) DO NOTHING;
