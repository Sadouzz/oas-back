package sn.oas.facturation.bonDeCommande.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.oas.facturation.auth.data.entity.Agent;
import sn.oas.facturation.auth.data.entity.User;
import sn.oas.facturation.auth.repository.UserRepository;
import sn.oas.facturation.bonDeCommande.data.entity.BonDeCommande;
import sn.oas.facturation.bonDeCommande.data.entity.LigneBonDeCommandePiece;
import sn.oas.facturation.bonDeCommande.data.enums.StatutBonCommande;
import sn.oas.facturation.bonDeCommande.dto.BonDeCommandeCreateRequest;
import sn.oas.facturation.bonDeCommande.dto.BonDeLivraisonRequest;
import sn.oas.facturation.bonDeCommande.dto.BonDeCommandeResponse;
import sn.oas.facturation.bonDeCommande.dto.BonDeCommandeUpdateRequest;
import sn.oas.facturation.bonDeCommande.dto.LigneBonDeCommandeRequest;
import sn.oas.facturation.bonDeCommande.dto.LigneBonDeCommandeResponse;
import sn.oas.facturation.bonDeCommande.repository.BonDeCommandeRepository;
import sn.oas.facturation.fournisseur.data.entity.Fournisseur;
import sn.oas.facturation.fournisseur.repository.FournisseurRepository;
import sn.oas.facturation.bonDeLivraison.repository.BonDeLivraisonRepository;
import sn.oas.facturation.bonDeLivraison.data.entity.BonDeLivraison;
import sn.oas.facturation.facturation.data.entity.LigneFacturationPiece;

import sn.oas.facturation.piecedetache.data.entity.PDG;
import sn.oas.facturation.piecedetache.data.entity.PDP;
import sn.oas.facturation.piecedetache.data.entity.PieceDetache;
import sn.oas.facturation.piecedetache.data.enums.StatutPiece;
import sn.oas.facturation.piecedetache.data.enums.TypePiece;
import sn.oas.facturation.piecedetache.repository.PieceDetacheRepository;
import sn.oas.facturation.vehicule.data.entity.Vehicule;
import sn.oas.facturation.vehicule.repository.VehiculeRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import sn.oas.facturation.piecedetache.service.StockService;
import sn.oas.facturation.piecedetache.dto.EntreeStockRequest;

@Service
@RequiredArgsConstructor
@Slf4j
public class BonDeCommandeServiceImpl implements BonDeCommandeService {
    private final BonDeCommandeRepository bonDeCommandeRepository;
    private final FournisseurRepository fournisseurRepository;
    private final VehiculeRepository vehiculeRepository;
    private final PieceDetacheRepository pieceDetacheRepository;
    private final sn.oas.facturation.ficheAtelier.repository.FicheAtelierRepository ficheAtelierRepository;
    private final sn.oas.facturation.bonDeSortie.service.BonDeSortieService bonDeSortieService;
    private final sn.oas.facturation.proforma.repository.ProformaRepository proformaRepository;
    private final PdfGeneratorService pdfGeneratorService;
    private final UserRepository userRepository;
    private final BonDeLivraisonRepository bonDeLivraisonRepository;
    private final StockService stockService;
    private final sn.oas.facturation.bonDeSortie.repository.BonDeSortieRepository bonDeSortieRepository;


    @Override
    @Transactional
    public BonDeCommandeResponse create(BonDeCommandeCreateRequest request) {
        log.info("Création d'un nouveau bon de commande");

        // Récupération de l'agent connecté depuis le contexte de sécurité
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Agent authentifié non trouvé : " + username));
        Agent agent = (user instanceof Agent a) ? a : null;

        Fournisseur fournisseur = null;
        if (request.getFournisseurId() != null) {
            fournisseur = fournisseurRepository.findById(request.getFournisseurId())
                    .orElseThrow(() -> new RuntimeException("Fournisseur non trouvé avec l'id " + request.getFournisseurId()));
        }

        Vehicule vehicule = null;
        if (request.getVehiculeId() != null) {
            vehicule = vehiculeRepository.findById(request.getVehiculeId())
                    .orElseThrow(() -> new RuntimeException("Véhicule non trouvé avec l'id " + request.getVehiculeId()));
        }

        BonDeCommande bonDeCommande = BonDeCommande.builder()
                .numero("BC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .dateCommande(LocalDateTime.now())
                .dateModification(LocalDateTime.now())
                .statut(StatutBonCommande.EN_ATTENTE)
                .paye(false)
                .observation(request.getObservation())
                .fournisseur(fournisseur)
                .vehicule(vehicule)
                .agent(agent)
                .lignes(new ArrayList<>())
                .build();

        BigDecimal montantHT = BigDecimal.ZERO;

        if (request.getLignes() != null) {
            for (LigneBonDeCommandeRequest ligneReq : request.getLignes()) {
                LigneBonDeCommandePiece ligne = buildLigneFromRequest(ligneReq, bonDeCommande);
                bonDeCommande.getLignes().add(ligne);
                montantHT = montantHT.add(ligne.getMontant());
            }
        }

        bonDeCommande.setMontantHT(montantHT);
        bonDeCommande.setTvaApplicable(request.getTvaApplicable() != null ? request.getTvaApplicable() : false);
        BigDecimal tva = bonDeCommande.getTvaApplicable() ? montantHT.multiply(new BigDecimal("0.18")) : BigDecimal.ZERO;
        bonDeCommande.setMontantTVA(tva);
        bonDeCommande.setMontantTTC(montantHT.add(tva));

        BonDeCommande saved = bonDeCommandeRepository.save(bonDeCommande);
        return mapToResponse(saved);
    }

    private LigneBonDeCommandePiece buildLigneFromRequest(LigneBonDeCommandeRequest ligneReq, BonDeCommande bonDeCommande) {
        LigneBonDeCommandePiece ligne = LigneBonDeCommandePiece.builder()
                .bonDeCommande(bonDeCommande)
                .quantite(ligneReq.getQuantite())
                .prixUnitaire(BigDecimal.valueOf(ligneReq.getPrixUnitaire()))
                .montant(BigDecimal.valueOf(ligneReq.getQuantite() * ligneReq.getPrixUnitaire()))
                .build();

        if (ligneReq.getPieceDetacheeId() != null) {
            PieceDetache piece = pieceDetacheRepository.findById(ligneReq.getPieceDetacheeId())
                    .orElseThrow(() -> new RuntimeException("Pièce détachée non trouvée avec l'id " + ligneReq.getPieceDetacheeId()));
            ligne.setPieceDetachee(piece);
        } else if (ligneReq.getTypePiece() != null) {
            if (ligneReq.getTypePiece() == TypePiece.PDS) {
                ligne.setDesignationPds(ligneReq.getDesignationPds());
                ligne.setReferencePds(ligneReq.getReference());
                ligne.setCategoriePds(ligneReq.getCategorie());
            } else {
                PieceDetache nouvellePiece;
                if (ligneReq.getTypePiece() == TypePiece.PDP) {
                    nouvellePiece = PDP.builder()
                            .numeroDeSerie(ligneReq.getNumeroDeSerie())
                            .reference(ligneReq.getReference())
                            .categorie(ligneReq.getCategorie())
                            .pourcentage(ligneReq.getPourcentage() != null ? ligneReq.getPourcentage() : 0.0)
                            .statut(StatutPiece.ACTIF)

                            .prix(ligneReq.getPrixUnitaire())
                            .qteReelle(0)
                            .stockAtelier(0)
                            .stockMagasin(0)
                            .seuilMinimum(1)
                            .build();
                } else {
                    nouvellePiece = PDG.builder()
                            .numeroDeSerie(ligneReq.getNumeroDeSerie())
                            .reference(ligneReq.getReference())
                            .categorie(ligneReq.getCategorie())
                            .pourcentage(ligneReq.getPourcentage() != null ? ligneReq.getPourcentage() : 0.0)
                            .statut(StatutPiece.ACTIF)

                            .build();
                }
                nouvellePiece = pieceDetacheRepository.save(nouvellePiece);
                ligne.setPieceDetachee(nouvellePiece);
            }
        } else {
            throw new RuntimeException("Informations de pièce détachée incomplètes pour la ligne de commande.");
        }

        return ligne;
    }

    @Override
    @Transactional
    public BonDeCommandeResponse update(Long id, BonDeCommandeUpdateRequest request) {
        log.info("Mise à jour du bon de commande id: {}", id);
        BonDeCommande bonDeCommande = bonDeCommandeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bon de commande non trouvé"));

        if (bonDeCommande.getStatut() != StatutBonCommande.EN_ATTENTE) {
            throw new RuntimeException("Seuls les bons de commande en attente peuvent être modifiés");
        }

        Fournisseur fournisseur = fournisseurRepository.findById(request.getFournisseurId())
                .orElseThrow(() -> new RuntimeException("Fournisseur non trouvé"));
        bonDeCommande.setFournisseur(fournisseur);

        if (request.getVehiculeId() != null) {
            Vehicule vehicule = vehiculeRepository.findById(request.getVehiculeId())
                    .orElseThrow(() -> new RuntimeException("Véhicule non trouvé"));
            bonDeCommande.setVehicule(vehicule);
        } else {
            bonDeCommande.setVehicule(null);
        }

        bonDeCommande.setObservation(request.getObservation());
        bonDeCommande.setDateModification(LocalDateTime.now());

        bonDeCommande.getLignes().clear();
        BigDecimal montantHT = BigDecimal.ZERO;

        if (request.getLignes() != null) {
            for (LigneBonDeCommandeRequest ligneReq : request.getLignes()) {
                LigneBonDeCommandePiece ligne = buildLigneFromRequest(ligneReq, bonDeCommande);
                bonDeCommande.getLignes().add(ligne);
                montantHT = montantHT.add(ligne.getMontant());
            }
        }

        bonDeCommande.setMontantHT(montantHT);
        bonDeCommande.setTvaApplicable(request.getTvaApplicable() != null ? request.getTvaApplicable() : false);
        BigDecimal tva = bonDeCommande.getTvaApplicable() ? montantHT.multiply(new BigDecimal("0.18")) : BigDecimal.ZERO;
        bonDeCommande.setMontantTVA(tva);
        bonDeCommande.setMontantTTC(montantHT.add(tva));

        return mapToResponse(bonDeCommandeRepository.save(bonDeCommande));
    }

    @Override
    public BonDeCommandeResponse getById(Long id) {
        BonDeCommande bonDeCommande = bonDeCommandeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bon de commande non trouvé"));
        return mapToResponse(bonDeCommande);
    }

    @Override
    public List<BonDeCommandeResponse> getAll() {
        return bonDeCommandeRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<BonDeCommandeResponse> search(String keyword) {
        return bonDeCommandeRepository.searchBonsDeCommande(keyword).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<BonDeCommandeResponse> getRecentBonDeCommandes() {
        return bonDeCommandeRepository.findTop5ByOrderByDateCommandeDesc().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public BonDeCommandeResponse envoyer(Long id) {
        BonDeCommande bonDeCommande = bonDeCommandeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bon de commande non trouvé"));
        
        if (bonDeCommande.getStatut() != StatutBonCommande.EN_ATTENTE) {
            throw new RuntimeException("Le bon de commande doit être en attente pour être envoyé.");
        }
        
        if (bonDeCommande.getFournisseur() == null) {
            throw new RuntimeException("Veuillez d'abord assigner un fournisseur au bon de commande.");
        }
        
        bonDeCommande.setStatut(StatutBonCommande.ENVOYE);
        bonDeCommande.setDateModification(LocalDateTime.now());
        return mapToResponse(bonDeCommandeRepository.save(bonDeCommande));
    }

    @Override
    @Transactional
    public BonDeCommandeResponse receptionner(Long id) {
        BonDeCommande bonDeCommande = bonDeCommandeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bon de commande non trouvé"));
        
        if (bonDeCommande.getStatut() != StatutBonCommande.ENVOYE) {
            throw new RuntimeException("Le bon de commande doit être envoyé pour être réceptionné.");
        }
        
        bonDeCommande.setStatut(StatutBonCommande.RECU);
        bonDeCommande.setDateModification(LocalDateTime.now());
        
        for (LigneBonDeCommandePiece ligne : bonDeCommande.getLignes()) {
            PieceDetache piece = ligne.getPieceDetachee();
            if (piece != null) {
                piece = (PieceDetache) org.hibernate.Hibernate.unproxy(piece);
                if (piece instanceof PDP pdp) {
                    pdp.setQteReelle((pdp.getQteReelle() != null ? pdp.getQteReelle() : 0) + ligne.getQuantite());
                    pdp.setStockMagasin((pdp.getStockMagasin() != null ? pdp.getStockMagasin() : 0) + ligne.getQuantite());
                } else if (piece instanceof PDG pdg) {
                    // Les pièces de type PDG (Pièce Détachée Générique) ne gèrent pas le stock ici
                }
                pieceDetacheRepository.save(piece);
            }
        }
        
        // Auto-générer le Bon de Sortie pour la fiche atelier en attente
        if (bonDeCommande.getVehicule() != null) {
            List<sn.oas.facturation.ficheAtelier.data.entity.FicheAtelier> fiches = ficheAtelierRepository.findByVehiculeIdAndStatut(
                    bonDeCommande.getVehicule().getId(), 
                    sn.oas.facturation.ficheAtelier.data.enums.StatutReparation.EN_ATTENTE_COMMANDE);
            
            for (sn.oas.facturation.ficheAtelier.data.entity.FicheAtelier fiche : fiches) {
                sn.oas.facturation.proforma.data.entity.Proforma proforma = proformaRepository.findByFicheAtelierId(fiche.getId()).orElse(null);
                if (proforma != null && fiche.getVehicule().getClient() != null) {
                    List<sn.oas.facturation.bonDeSortie.dto.LignePieceRequest> lignesPieces = new ArrayList<>();
                    for (sn.oas.facturation.facturation.data.entity.LigneFacturationPiece lp : proforma.getLignesFacturationPieces()) {
                        PieceDetache piece = pieceDetacheRepository.findById(lp.getPiece().getId()).orElse(null);
                        if (piece != null) {
                            piece = (PieceDetache) org.hibernate.Hibernate.unproxy(piece);
                        }
                        if (piece instanceof PDP pdp) {
                            int stockAtelier = pdp.getStockAtelier() != null ? pdp.getStockAtelier() : 0;
                            int stockMagasin = pdp.getStockMagasin() != null ? pdp.getStockMagasin() : 0;
                            int manquantAtelier = Math.max(0, lp.getQuantite() - stockAtelier);
                            int aSortirMagasin = Math.min(manquantAtelier, stockMagasin);
                            
                            if (aSortirMagasin > 0) {
                                lignesPieces.add(new sn.oas.facturation.bonDeSortie.dto.LignePieceRequest(pdp.getId(), aSortirMagasin));
                            }
                        }
                    }
                    
                    sn.oas.facturation.bonDeSortie.dto.BonDeSortieRequest bdsRequest = new sn.oas.facturation.bonDeSortie.dto.BonDeSortieRequest(
                        fiche.getVehicule().getClient().getId(),
                        fiche.getVehicule().getId(),
                        lignesPieces,
                        "Bon de sortie automatique suite à la réception de commande"
                    );
                    
                    try {
                        sn.oas.facturation.bonDeSortie.data.entity.BonDeSortie bds = bonDeSortieService.creer(bdsRequest);
                        bds.setFicheAtelier(fiche);
                        bds = bonDeSortieRepository.save(bds); // update le BDS avec la fiche
                        
                        fiche.setBonDeSortie(bds);
                        fiche.setStatut(sn.oas.facturation.ficheAtelier.data.enums.StatutReparation.EN_ATTENTE_SORTIE);
                        ficheAtelierRepository.save(fiche);
                    } catch (Exception e) {
                        log.error("Erreur auto bon de sortie FA-" + fiche.getId(), e);
                    }
                }
            }
        }
        
        return mapToResponse(bonDeCommandeRepository.save(bonDeCommande));
    }

    @Override
    @Transactional
    public BonDeCommandeResponse receptionnerAvecQuantites(Long id, BonDeLivraisonRequest request) {
        BonDeCommande bonDeCommande = bonDeCommandeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bon de commande non trouvé"));
        
        if (bonDeCommande.getStatut() != StatutBonCommande.ENVOYE && bonDeCommande.getStatut() != StatutBonCommande.INCOMPLET) {
            throw new RuntimeException("Le bon de commande doit être envoyé ou incomplet pour être réceptionné.");
        }
        
        bonDeCommande.setDateModification(LocalDateTime.now());
        
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username).orElse(null);
        Agent agent = (user instanceof Agent a) ? a : null;

        BonDeLivraison bonDeLivraison = BonDeLivraison.builder()
            .numero("BL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
            .dateCreation(LocalDateTime.now())
            .dateModification(LocalDateTime.now())
            .agent(agent)
            .bonDeCommande(bonDeCommande)
            .kilometrage(bonDeCommande.getVehicule() != null && bonDeCommande.getVehicule().getKilometrage() != null ? bonDeCommande.getVehicule().getKilometrage() : 0.0)
            .lignesFacturationPieces(new ArrayList<>())
            .lignesFacturationMainDoeuvres(new ArrayList<>())
            .build();
        
        BigDecimal montantHT = BigDecimal.ZERO;

        if (request.getLignes() != null) {
            for (BonDeLivraisonRequest.LigneLivraison ll : request.getLignes()) {
                LigneBonDeCommandePiece ligne = bonDeCommande.getLignes().stream()
                        .filter(l -> l.getId().equals(ll.getLigneId()))
                        .findFirst()
                        .orElse(null);
                if (ligne != null && ll.getQuantiteRecue() != null && ll.getQuantiteRecue() > 0) {
                    
                    int currentRecue = ligne.getQuantiteRecue() != null ? ligne.getQuantiteRecue() : 0;
                    if (currentRecue + ll.getQuantiteRecue() > ligne.getQuantite()) {
                        throw new RuntimeException("La quantité reçue dépasse la quantité commandée pour la ligne " + ligne.getId());
                    }
                    ligne.setQuantiteRecue(currentRecue + ll.getQuantiteRecue());

                    montantHT = montantHT.add(ligne.getPrixUnitaire().multiply(BigDecimal.valueOf(ll.getQuantiteRecue())));

                    if (ligne.getPieceDetachee() != null) {
                        PieceDetache piece = ligne.getPieceDetachee();
                        piece = (PieceDetache) org.hibernate.Hibernate.unproxy(piece);
                        if (piece instanceof PDP pdp) {
                            EntreeStockRequest entreeReq = new EntreeStockRequest(
                                    pdp.getId(),
                                    ll.getQuantiteRecue(),
                                    "Réception BC " + bonDeCommande.getNumero()
                            );
                            stockService.entree(entreeReq);
                            
                            LigneFacturationPiece lfp = LigneFacturationPiece.builder()
                                    .facturation(bonDeLivraison)
                                    .piece(pdp)
                                    .quantite(ll.getQuantiteRecue())
                                    .prix(ligne.getPrixUnitaire().intValue())
                                    .build();
                            bonDeLivraison.getLignesFacturationPieces().add(lfp);
                        }
                    }
                }
            }
        }
        
        boolean toutRecu = true;
        for (LigneBonDeCommandePiece ligne : bonDeCommande.getLignes()) {
            int recu = ligne.getQuantiteRecue() != null ? ligne.getQuantiteRecue() : 0;
            if (recu < ligne.getQuantite()) {
                toutRecu = false;
                break;
            }
        }
        bonDeCommande.setStatut(toutRecu ? StatutBonCommande.RECU : StatutBonCommande.INCOMPLET);
        
        bonDeLivraison.setMontantHT(montantHT);
        BigDecimal tva = bonDeCommande.getTvaApplicable() ? montantHT.multiply(new BigDecimal("0.18")) : BigDecimal.ZERO;
        bonDeLivraison.setMontantTVA(tva);
        bonDeLivraison.setMontantTTC(montantHT.add(tva));
        bonDeLivraison.setMontantTimbre(BigDecimal.ZERO);
        bonDeLivraison.setMontantTotal(bonDeLivraison.getMontantTTC().add(bonDeLivraison.getMontantTimbre()));
        bonDeLivraisonRepository.save(bonDeLivraison);
        
        if (toutRecu && bonDeCommande.getVehicule() != null) {
            List<sn.oas.facturation.ficheAtelier.data.entity.FicheAtelier> fiches = ficheAtelierRepository.findByVehiculeIdAndStatut(
                    bonDeCommande.getVehicule().getId(), 
                    sn.oas.facturation.ficheAtelier.data.enums.StatutReparation.EN_ATTENTE_COMMANDE);
            
            for (sn.oas.facturation.ficheAtelier.data.entity.FicheAtelier fiche : fiches) {
                sn.oas.facturation.proforma.data.entity.Proforma proforma = proformaRepository.findByFicheAtelierId(fiche.getId()).orElse(null);
                if (proforma != null && fiche.getVehicule().getClient() != null) {
                    List<sn.oas.facturation.bonDeSortie.dto.LignePieceRequest> lignesPieces = new ArrayList<>();
                    for (sn.oas.facturation.facturation.data.entity.LigneFacturationPiece lp : proforma.getLignesFacturationPieces()) {
                        PieceDetache piece = pieceDetacheRepository.findById(lp.getPiece().getId()).orElse(null);
                        if (piece != null) {
                            piece = (PieceDetache) org.hibernate.Hibernate.unproxy(piece);
                        }
                        if (piece instanceof PDP pdp) {
                            int stockAtelier = pdp.getStockAtelier() != null ? pdp.getStockAtelier() : 0;
                            int stockMagasin = pdp.getStockMagasin() != null ? pdp.getStockMagasin() : 0;
                            int manquantAtelier = Math.max(0, lp.getQuantite() - stockAtelier);
                            int aSortirMagasin = Math.min(manquantAtelier, stockMagasin);
                            if (aSortirMagasin > 0) {
                                lignesPieces.add(new sn.oas.facturation.bonDeSortie.dto.LignePieceRequest(pdp.getId(), aSortirMagasin));
                            }
                        }
                    }
                    if (!lignesPieces.isEmpty()) {
                        sn.oas.facturation.bonDeSortie.dto.BonDeSortieRequest bdsRequest = new sn.oas.facturation.bonDeSortie.dto.BonDeSortieRequest(
                            fiche.getVehicule().getClient().getId(),
                            fiche.getVehicule().getId(),
                            lignesPieces,
                            "Bon de sortie auto suite réception commande"
                        );
                        try {
                            sn.oas.facturation.bonDeSortie.data.entity.BonDeSortie bds = bonDeSortieService.creer(bdsRequest);
                            bds.setFicheAtelier(fiche);
                            bds = bonDeSortieRepository.save(bds);
                            
                            fiche.setBonDeSortie(bds);
                            fiche.setStatut(sn.oas.facturation.ficheAtelier.data.enums.StatutReparation.EN_ATTENTE_SORTIE);
                            ficheAtelierRepository.save(fiche);
                        } catch (Exception e) {
                            log.error("Erreur auto bon de sortie FA-" + fiche.getId(), e);
                            throw new RuntimeException("Erreur lors de la création automatique du bon de sortie", e);
                        }
                    }
                }
            }
        }
        
        return mapToResponse(bonDeCommandeRepository.save(bonDeCommande));
    }

    @Override
    @Transactional
    public BonDeCommandeResponse assignerFournisseur(Long id, Long fournisseurId) {
        BonDeCommande bonDeCommande = bonDeCommandeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bon de commande non trouvé"));
        if (bonDeCommande.getStatut() != StatutBonCommande.EN_ATTENTE) {
            throw new RuntimeException("Le fournisseur ne peut être modifié que sur un bon en attente.");
        }
        Fournisseur fournisseur = fournisseurRepository.findById(fournisseurId)
                .orElseThrow(() -> new RuntimeException("Fournisseur non trouvé"));
        bonDeCommande.setFournisseur(fournisseur);
        bonDeCommande.setDateModification(LocalDateTime.now());
        return mapToResponse(bonDeCommandeRepository.save(bonDeCommande));
    }

    @Override
    @Transactional
    public BonDeCommandeResponse annuler(Long id) {
        BonDeCommande bonDeCommande = bonDeCommandeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bon de commande non trouvé"));
        
        if (bonDeCommande.getStatut() == StatutBonCommande.RECU) {
            throw new RuntimeException("Un bon de commande déjà reçu ne peut pas être annulé.");
        }
        
        bonDeCommande.setStatut(StatutBonCommande.ANNULE);
        bonDeCommande.setDateModification(LocalDateTime.now());
        return mapToResponse(bonDeCommandeRepository.save(bonDeCommande));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!bonDeCommandeRepository.existsById(id)) {
            throw new RuntimeException("Bon de commande non trouvé");
        }
        bonDeCommandeRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generatePdf(Long id) {
        BonDeCommande bonDeCommande = bonDeCommandeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bon de commande non trouvé"));
        // Accéder à la collection pour forcer le chargement paresseux (lazy loading) si nécessaire
        bonDeCommande.getLignes().size();
        return pdfGeneratorService.genererBonDeCommandePdf(bonDeCommande);
    }

    private BonDeCommandeResponse mapToResponse(BonDeCommande bc) {
        return BonDeCommandeResponse.builder()
                .id(bc.getId())
                .numero(bc.getNumero())
                .dateCommande(bc.getDateCommande())
                .statut(bc.getStatut().name())
                .fournisseurId(bc.getFournisseur() != null ? bc.getFournisseur().getId() : null)
                .fournisseurNom(bc.getFournisseur() != null ? bc.getFournisseur().getNomEntreprise() : null)
                .vehiculeId(bc.getVehicule() != null ? bc.getVehicule().getId() : null)
                .immatriculationVehicule(bc.getVehicule() != null ? bc.getVehicule().getImmatriculation() : null)
                .montantHT(bc.getMontantHT())
                .montantTVA(bc.getMontantTVA())
                .montantTTC(bc.getMontantTTC())
                .tvaApplicable(bc.getTvaApplicable())
                .paye(bc.getPaye())
                .observation(bc.getObservation())
                .lignes(bc.getLignes().stream().map(ligne -> LigneBonDeCommandeResponse.builder()
                        .id(ligne.getId())
                        .pieceDetacheeId(ligne.getPieceDetachee() != null ? ligne.getPieceDetachee().getId() : null)
                        .designationPiece(ligne.getPieceDetachee() != null ? ligne.getPieceDetachee().getCategorie() : ligne.getDesignationPds())
                        .reference(ligne.getPieceDetachee() != null ? ligne.getPieceDetachee().getReference() : ligne.getReferencePds())
                        .categorie(ligne.getPieceDetachee() != null ? ligne.getPieceDetachee().getCategorie() : ligne.getCategoriePds())
                        .quantite(ligne.getQuantite())
                        .quantiteRecue(ligne.getQuantiteRecue())
                        .prixUnitaire(ligne.getPrixUnitaire().doubleValue())
                        .montant(ligne.getMontant().doubleValue())
                        .build()
                ).collect(Collectors.toList()))
                .build();
    }
}
