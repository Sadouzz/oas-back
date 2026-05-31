package sn.oas.facturation.bonDeCommande.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.oas.facturation.bonDeCommande.data.entity.BonDeCommande;
import sn.oas.facturation.bonDeCommande.data.entity.LigneBonDeCommande;
import sn.oas.facturation.bonDeCommande.data.enums.StatutBonCommande;
import sn.oas.facturation.bonDeCommande.dto.BonDeCommandeCreateRequest;
import sn.oas.facturation.bonDeCommande.dto.BonDeCommandeResponse;
import sn.oas.facturation.bonDeCommande.dto.BonDeCommandeUpdateRequest;
import sn.oas.facturation.bonDeCommande.dto.LigneBonDeCommandeRequest;
import sn.oas.facturation.bonDeCommande.dto.LigneBonDeCommandeResponse;
import sn.oas.facturation.bonDeCommande.repository.BonDeCommandeRepository;
import sn.oas.facturation.fournisseur.data.entity.Fournisseur;
import sn.oas.facturation.fournisseur.repository.FournisseurRepository;
import sn.oas.facturation.garage.data.entity.Garage;
import sn.oas.facturation.garage.repository.GarageRepository;
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

@Service
@RequiredArgsConstructor
@Slf4j
public class BonDeCommandeServiceImpl implements BonDeCommandeService {
    private final BonDeCommandeRepository bonDeCommandeRepository;
    private final FournisseurRepository fournisseurRepository;
    private final VehiculeRepository vehiculeRepository;
    private final PieceDetacheRepository pieceDetacheRepository;
    private final GarageRepository garageRepository;
    private final PdfGeneratorService pdfGeneratorService;

    @Override
    @Transactional
    public BonDeCommandeResponse create(BonDeCommandeCreateRequest request) {
        log.info("Création d'un nouveau bon de commande");

        Fournisseur fournisseur = fournisseurRepository.findById(request.getFournisseurId())
                .orElseThrow(() -> new RuntimeException("Fournisseur non trouvé avec l'id " + request.getFournisseurId()));

        Vehicule vehicule = null;
        if (request.getVehiculeId() != null) {
            vehicule = vehiculeRepository.findById(request.getVehiculeId())
                    .orElseThrow(() -> new RuntimeException("Véhicule non trouvé avec l'id " + request.getVehiculeId()));
        }

        BonDeCommande bonDeCommande = BonDeCommande.builder()
                .numero("BC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .dateCommande(LocalDate.now())
                .dateModification(LocalDateTime.now())
                .statut(StatutBonCommande.EN_ATTENTE)
                .paye(false)
                .observation(request.getObservation())
                .fournisseur(fournisseur)
                .vehicule(vehicule)
                .lignes(new ArrayList<>())
                .build();

        BigDecimal montantHT = BigDecimal.ZERO;

        if (request.getLignes() != null) {
            for (LigneBonDeCommandeRequest ligneReq : request.getLignes()) {
                LigneBonDeCommande ligne = buildLigneFromRequest(ligneReq, bonDeCommande);
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

    private LigneBonDeCommande buildLigneFromRequest(LigneBonDeCommandeRequest ligneReq, BonDeCommande bonDeCommande) {
        LigneBonDeCommande ligne = LigneBonDeCommande.builder()
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
                Garage garage = garageRepository.findAll().stream().findFirst()
                        .orElseThrow(() -> new RuntimeException("Aucun garage disponible pour associer à la pièce."));

                PieceDetache nouvellePiece;
                if (ligneReq.getTypePiece() == TypePiece.PDP) {
                    nouvellePiece = PDP.builder()
                            .numeroDeSerie(ligneReq.getNumeroDeSerie())
                            .reference(ligneReq.getReference())
                            .categorie(ligneReq.getCategorie())
                            .pourcentage(ligneReq.getPourcentage() != null ? ligneReq.getPourcentage() : 0.0)
                            .statut(StatutPiece.ACTIF)
                            .garage(garage)
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
                            .garage(garage)
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
                LigneBonDeCommande ligne = buildLigneFromRequest(ligneReq, bonDeCommande);
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
        
        for (LigneBonDeCommande ligne : bonDeCommande.getLignes()) {
            PieceDetache piece = ligne.getPieceDetachee();
            if (piece != null) {
                if (piece instanceof PDP pdp) {
                    pdp.setQteReelle((pdp.getQteReelle() != null ? pdp.getQteReelle() : 0) + ligne.getQuantite());
                    pdp.setStockMagasin((pdp.getStockMagasin() != null ? pdp.getStockMagasin() : 0) + ligne.getQuantite());
                } else if (piece instanceof PDG pdg) {
                    // Les pièces de type PDG (Pièce Détachée Générique) ne gèrent pas le stock ici
                }
                pieceDetacheRepository.save(piece);
            }
        }
        
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
                        .prixUnitaire(ligne.getPrixUnitaire().doubleValue())
                        .montant(ligne.getMontant().doubleValue())
                        .build()
                ).collect(Collectors.toList()))
                .build();
    }
}
