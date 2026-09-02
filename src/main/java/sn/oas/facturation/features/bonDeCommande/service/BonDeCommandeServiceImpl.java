package sn.oas.facturation.features.bonDeCommande.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.oas.facturation.features.auth.data.entity.Agent;
import sn.oas.facturation.features.auth.data.entity.User;
import sn.oas.facturation.features.auth.repository.UserRepository;
import sn.oas.facturation.features.bonDeCommande.data.entity.BonDeCommande;
import sn.oas.facturation.features.bonDeCommande.data.entity.LigneBonDeCommandePiece;
import sn.oas.facturation.features.bonDeCommande.data.enums.StatutBonCommande;
import sn.oas.facturation.features.bonDeCommande.dto.BonDeCommandeCreateRequest;
import sn.oas.facturation.features.bonDeCommande.dto.ReceptionBonDeCommandeRequest;
import sn.oas.facturation.features.bonDeCommande.dto.BonDeCommandeResponse;
import sn.oas.facturation.features.bonDeCommande.dto.BonDeCommandeUpdateRequest;
import sn.oas.facturation.features.bonDeCommande.dto.LigneBonDeCommandeRequest;
import sn.oas.facturation.features.bonDeCommande.dto.LigneBonDeCommandeResponse;
import sn.oas.facturation.features.bonDeCommande.repository.BonDeCommandeRepository;
import sn.oas.facturation.features.bonDeSortie.data.entity.BonDeSortie;
import sn.oas.facturation.features.bonDeSortie.dto.BonDeSortieRequest;
import sn.oas.facturation.features.bonDeSortie.dto.LignePieceRequest;
import sn.oas.facturation.features.bonDeSortie.repository.BonDeSortieRepository;
import sn.oas.facturation.features.bonDeSortie.service.BonDeSortieService;
import sn.oas.facturation.features.ordreReparation.data.entity.OrdreReparation;
import sn.oas.facturation.features.ordreReparation.data.enums.StatutOrdreReparation;
import sn.oas.facturation.features.ordreReparation.repository.OrdreReparationRepository;
import sn.oas.facturation.features.fournisseur.data.entity.Fournisseur;
import sn.oas.facturation.features.piecedetache.repository.CategorieRepository;
import sn.oas.facturation.features.fournisseur.repository.FournisseurRepository;
import sn.oas.facturation.features.bonDeReception.repository.BonDeReceptionRepository;
import sn.oas.facturation.features.bonDeReception.data.entity.BonDeReception;
import sn.oas.facturation.features.facturation.data.entity.LigneFacturationPiece;

import sn.oas.facturation.features.piecedetache.data.entity.PDG;
import sn.oas.facturation.features.piecedetache.data.entity.PDP;
import sn.oas.facturation.features.piecedetache.data.entity.PieceDetache;
import sn.oas.facturation.features.piecedetache.data.enums.TypePiece;
import sn.oas.facturation.features.piecedetache.repository.PieceDetacheRepository;
import sn.oas.facturation.features.proforma.data.entity.Proforma;
import sn.oas.facturation.features.proforma.repository.ProformaRepository;
import sn.oas.facturation.features.vehicule.data.entity.Vehicule;
import sn.oas.facturation.features.vehicule.repository.VehiculeRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import sn.oas.facturation.features.piecedetache.service.StockService;
import sn.oas.facturation.features.piecedetache.dto.EntreeStockRequest;
import sn.oas.facturation.features.auth.data.enums.Role;
import sn.oas.facturation.features.notification.service.AgentNotificationService;

@Service
@RequiredArgsConstructor
@Slf4j
public class BonDeCommandeServiceImpl implements BonDeCommandeService {
    private final BonDeCommandeRepository bonDeCommandeRepository;
    private final FournisseurRepository fournisseurRepository;
    private final VehiculeRepository vehiculeRepository;
    private final PieceDetacheRepository pieceDetacheRepository;
    private final OrdreReparationRepository ordreReparationRepository;
    private final BonDeSortieService bonDeSortieService;
    private final ProformaRepository proformaRepository;
    private final PdfGeneratorService pdfGeneratorService;

    private final CategorieRepository categorieRepository;
    private final UserRepository userRepository;
    private final BonDeReceptionRepository bonDeReceptionRepository;
    private final StockService stockService;
    private final BonDeSortieRepository bonDeSortieRepository;
    private final AgentNotificationService agentNotificationService;
    private final sn.oas.facturation.shared.documentNumber.DocumentNumberGeneratorService documentNumberGeneratorService;

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
                    .orElseThrow(() -> new RuntimeException(
                            "Fournisseur non trouvé avec l'id " + request.getFournisseurId()));
        }

        Vehicule vehicule = null;
        if (request.getVehiculeId() != null) {
            vehicule = vehiculeRepository.findById(request.getVehiculeId())
                    .orElseThrow(
                            () -> new RuntimeException("Véhicule non trouvé avec l'id " + request.getVehiculeId()));
        }

        BonDeCommande bonDeCommande = BonDeCommande.builder()
                .numero(documentNumberGeneratorService
                        .generateNextNumber(sn.oas.facturation.shared.documentNumber.DocumentType.BC))
                .dateCommande(LocalDateTime.now())
                .dateModification(LocalDateTime.now())
                .statut(StatutBonCommande.EN_ATTENTE)
                .paye(false)
                .observation(request.getObservation())
                .fournisseur(fournisseur)
                .vehicule(vehicule)
                .agent(agent)
                .garage(agent != null ? agent.getGarage() : null)
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
        BigDecimal tva = bonDeCommande.getTvaApplicable() ? montantHT.multiply(new BigDecimal("0.18"))
                : BigDecimal.ZERO;
        bonDeCommande.setMontantTVA(tva);
        bonDeCommande.setMontantTTC(montantHT.add(tva));

        BonDeCommande saved = bonDeCommandeRepository.save(bonDeCommande);

        agentNotificationService.notifyRole(Role.AGENT_MAGASIN,
                "Nouveau Bon de Commande",
                "Le bon de commande " + saved.getNumero() + " a été créé et est en attente.");

        return mapToResponse(saved);
    }

    private LigneBonDeCommandePiece buildLigneFromRequest(LigneBonDeCommandeRequest ligneReq,
            BonDeCommande bonDeCommande) {
        LigneBonDeCommandePiece ligne = LigneBonDeCommandePiece.builder()
                .bonDeCommande(bonDeCommande)
                .quantite(ligneReq.getQuantite())
                .prixUnitaire(BigDecimal.valueOf(ligneReq.getPrixUnitaire()))
                .montant(BigDecimal.valueOf(ligneReq.getQuantite() * ligneReq.getPrixUnitaire()))
                .build();

        if (ligneReq.getPieceDetacheeId() != null) {
            PieceDetache piece = pieceDetacheRepository.findById(ligneReq.getPieceDetacheeId())
                    .orElseThrow(() -> new RuntimeException(
                            "Pièce détachée non trouvée avec l'id " + ligneReq.getPieceDetacheeId()));
            ligne.setPieceDetachee(piece);
        } else if (ligneReq.getTypePiece() != null) {
            if (ligneReq.getTypePiece() == TypePiece.PDS) {
                ligne.setDesignationPds(ligneReq.getDesignationPds());
                ligne.setReferencePds(ligneReq.getDesignation());
                ligne.setCategoriePds(ligneReq.getCategorie());
            } else {
                PieceDetache nouvellePiece;
                if (ligneReq.getTypePiece() == TypePiece.PDP) {
                    nouvellePiece = PDP.builder()
                            .reference(ligneReq.getReference())
                            .designation(ligneReq.getDesignation())
                            .categorie(ligneReq.getCategorie() != null
                                    ? categorieRepository.findByNom(ligneReq.getCategorie()).orElse(null)
                                    : null)

                            .prixUnitaire(ligneReq.getPrixUnitaire())
                            .qteReelle(0.0)
                            .stockAtelier(0.0)
                            .stockMagasin(0.0)
                            .seuilMinimum(1.0)
                            .build();
                } else {
                    nouvellePiece = PDG.builder()
                            .reference(ligneReq.getReference())
                            .designation(ligneReq.getDesignation())
                            .categorie(ligneReq.getCategorie() != null
                                    ? categorieRepository.findByNom(ligneReq.getCategorie()).orElse(null)
                                    : null)

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

        if (bonDeCommande.getStatut() == StatutBonCommande.ANNULE) {
            throw new RuntimeException("Un bon de commande annulé ne peut pas être modifié.");
        }

        if (request.getFournisseurId() != null) {
            Fournisseur fournisseur = fournisseurRepository.findById(request.getFournisseurId())
                    .orElseThrow(() -> new RuntimeException("Fournisseur non trouvé"));
            bonDeCommande.setFournisseur(fournisseur);
        } else {
            bonDeCommande.setFournisseur(null);
        }

        if (request.getVehiculeId() != null) {
            Vehicule vehicule = vehiculeRepository.findById(request.getVehiculeId())
                    .orElseThrow(() -> new RuntimeException("Véhicule non trouvé"));
            bonDeCommande.setVehicule(vehicule);
        } else {
            bonDeCommande.setVehicule(null);
        }

        bonDeCommande.setObservation(request.getObservation());
        bonDeCommande.setDateModification(LocalDateTime.now());

        // Map des lignes existantes
        java.util.Map<Long, LigneBonDeCommandePiece> existingLinesById = new java.util.HashMap<>();
        if (bonDeCommande.getLignes() != null) {
            for (LigneBonDeCommandePiece oldLigne : bonDeCommande.getLignes()) {
                if (oldLigne.getId() != null) {
                    existingLinesById.put(oldLigne.getId(), oldLigne);
                }
                int recu = oldLigne.getQuantiteRecue() != null ? oldLigne.getQuantiteRecue() : 0;
                if (recu > 0) {
                    LigneBonDeCommandeRequest matchingReq = request.getLignes() != null
                            ? request.getLignes().stream()
                                    .filter(r -> r.getId() != null && r.getId().equals(oldLigne.getId()))
                                    .findFirst()
                                    .orElse(null)
                            : null;

                    String desc = oldLigne.getPieceDetachee() != null ? oldLigne.getPieceDetachee().getDesignation()
                            : (oldLigne.getDesignationPds() != null ? oldLigne.getDesignationPds() : "Ligne " + oldLigne.getId());

                    if (matchingReq == null) {
                        throw new RuntimeException("Impossible de supprimer la ligne '" + desc + "' car " + recu + " pièce(s) ont déjà été reçue(s).");
                    }
                    if (matchingReq.getQuantite() == null || matchingReq.getQuantite() < recu) {
                        throw new RuntimeException("La quantité pour '" + desc + "' (" + matchingReq.getQuantite() + ") ne peut pas être inférieure à la quantité déjà reçue (" + recu + ").");
                    }
                }
            }
        }

        List<LigneBonDeCommandePiece> newLignes = new ArrayList<>();
        BigDecimal montantHT = BigDecimal.ZERO;

        if (request.getLignes() != null) {
            for (LigneBonDeCommandeRequest ligneReq : request.getLignes()) {
                LigneBonDeCommandePiece ligne;
                if (ligneReq.getId() != null && existingLinesById.containsKey(ligneReq.getId())) {
                    ligne = existingLinesById.get(ligneReq.getId());
                    ligne.setQuantite(ligneReq.getQuantite());
                    ligne.setPrixUnitaire(BigDecimal.valueOf(ligneReq.getPrixUnitaire()));
                    ligne.setMontant(BigDecimal.valueOf(ligneReq.getQuantite() * ligneReq.getPrixUnitaire()));
                    if (ligneReq.getPieceDetacheeId() != null) {
                        PieceDetache piece = pieceDetacheRepository.findById(ligneReq.getPieceDetacheeId())
                                .orElseThrow(() -> new RuntimeException("Pièce détachée non trouvée avec l'id : " + ligneReq.getPieceDetacheeId()));
                        ligne.setPieceDetachee(piece);
                        ligne.setDesignationPds(null);
                        ligne.setReferencePds(null);
                        ligne.setCategoriePds(null);
                    } else if (ligneReq.getDesignationPds() != null) {
                        ligne.setDesignationPds(ligneReq.getDesignationPds());
                        ligne.setPieceDetachee(null);
                    }
                } else {
                    ligne = buildLigneFromRequest(ligneReq, bonDeCommande);
                }
                newLignes.add(ligne);
                montantHT = montantHT.add(ligne.getMontant());
            }
        }

        bonDeCommande.getLignes().clear();
        bonDeCommande.getLignes().addAll(newLignes);

        bonDeCommande.setMontantHT(montantHT);
        bonDeCommande.setTvaApplicable(request.getTvaApplicable() != null ? request.getTvaApplicable() : false);
        BigDecimal tva = bonDeCommande.getTvaApplicable() ? montantHT.multiply(new BigDecimal("0.18"))
                : BigDecimal.ZERO;
        bonDeCommande.setMontantTVA(tva);
        bonDeCommande.setMontantTTC(montantHT.add(tva));

        // Re-calculer le statut si la réception a commencé
        boolean hasReception = newLignes.stream().anyMatch(l -> l.getQuantiteRecue() != null && l.getQuantiteRecue() > 0);
        if (hasReception) {
            boolean toutRecu = newLignes.stream().allMatch(l -> l.getQuantiteRecue() != null && l.getQuantiteRecue() >= l.getQuantite());
            bonDeCommande.setStatut(toutRecu ? StatutBonCommande.RECU : StatutBonCommande.INCOMPLET);
        }

        return mapToResponse(bonDeCommandeRepository.save(bonDeCommande));
    }

    @Override
    public BonDeCommandeResponse getById(Long id) {
        BonDeCommande bonDeCommande = bonDeCommandeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bon de commande non trouvé"));
        return mapToResponse(bonDeCommande);
    }

    @Override
    public org.springframework.data.domain.Page<BonDeCommandeResponse> getAll(int page, int size) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        return bonDeCommandeRepository.findAll(pageable).map(this::mapToResponse);
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
                    EntreeStockRequest entreeReq = new EntreeStockRequest(
                            pdp.getId(),
                            ligne.getQuantite(),
                            "Réception totale BC " + bonDeCommande.getNumero());
                    stockService.entree(entreeReq);
                } else if (piece instanceof PDG pdg) {
                    // Les pièces de type PDG ne gèrent pas le stock ici
                }
            }
        }

        // Auto-générer le Bon de Sortie pour la fiche atelier en attente
        if (bonDeCommande.getVehicule() != null) {
            List<OrdreReparation> fiches = ordreReparationRepository
                    .findByVehiculeIdAndStatut(
                            bonDeCommande.getVehicule().getId(),
                            StatutOrdreReparation.EN_ATTENTE_COMMANDE);

            for (OrdreReparation fiche : fiches) {
                Proforma proforma = proformaRepository
                        .findByOrdreReparationId(fiche.getId()).orElse(null);
                if (proforma != null && fiche.getVehicule().getClient() != null) {
                    List<LignePieceRequest> lignesPieces = new ArrayList<>();
                    for (LigneFacturationPiece lp : proforma
                            .getLignesFacturationPieces()) {
                        PieceDetache piece = pieceDetacheRepository.findById(lp.getPiece().getId()).orElse(null);
                        if (piece != null) {
                            piece = (PieceDetache) org.hibernate.Hibernate.unproxy(piece);
                        }
                        if (piece instanceof PDP pdp) {
                            Double stockAtelier = pdp.getStockAtelier() != null ? pdp.getStockAtelier() : 0.0;
                            Double stockMagasin = pdp.getStockMagasin() != null ? pdp.getStockMagasin() : 0.0;
                            Double manquantAtelier = Math.max(0.0, lp.getQuantite() - stockAtelier);
                            Double aSortirMagasin = Math.min(manquantAtelier, stockMagasin);

                            if (aSortirMagasin > 0) {
                                lignesPieces.add(new LignePieceRequest(pdp.getId(),
                                        aSortirMagasin.intValue()));
                            }
                        }
                    }

                    BonDeSortieRequest bdsRequest = new BonDeSortieRequest(
                            fiche.getVehicule().getClient().getId(),
                            fiche.getVehicule().getId(),
                            lignesPieces,
                            fiche.getId(),
                            "Bon de sortie automatique suite à la réception de commande");

                    try {
                        BonDeSortie bds = bonDeSortieService
                                .creer(bdsRequest);
                        bds.setOrdreReparation(fiche);
                        bds = bonDeSortieRepository.save(bds); // update le BDS avec la fiche

                        fiche.setBonDeSortie(bds);
                        fiche.setStatut(
                                StatutOrdreReparation.EN_ATTENTE_SORTIE);
                        ordreReparationRepository.save(fiche);
                    } catch (Exception e) {
                        log.error("Erreur auto bon de sortie FA-" + fiche.getId(), e);
                    }
                }
            }
        }

        agentNotificationService.notifyRole(Role.AGENT_MAGASIN,
                "Bon de Commande Réceptionné",
                "Le bon de commande " + bonDeCommande.getNumero() + " a été réceptionné totalement.");

        return mapToResponse(bonDeCommandeRepository.save(bonDeCommande));
    }

    @Override
    @Transactional
    public BonDeCommandeResponse receptionnerAvecQuantites(Long id, ReceptionBonDeCommandeRequest request) {
        BonDeCommande bonDeCommande = bonDeCommandeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bon de commande non trouvé"));

        if (bonDeCommande.getStatut() != StatutBonCommande.ENVOYE
                && bonDeCommande.getStatut() != StatutBonCommande.INCOMPLET) {
            throw new RuntimeException("Le bon de commande doit être envoyé ou incomplet pour être réceptionné.");
        }

        bonDeCommande.setDateModification(LocalDateTime.now());

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username).orElse(null);
        Agent agent = (user instanceof Agent a) ? a : null;

        BonDeReception bonDeReception = BonDeReception.builder()
                .numero(documentNumberGeneratorService
                        .generateNextNumber(agent != null ? agent.getGarage() : null, sn.oas.facturation.shared.documentNumber.DocumentType.BR))
                .dateCreation(LocalDateTime.now())
                .dateModification(LocalDateTime.now())
                .agent(agent)
                .garage(agent != null ? agent.getGarage() : null)
                .bonDeCommande(bonDeCommande)
                .kilometrage(bonDeCommande.getVehicule() != null && bonDeCommande.getVehicule().getKilometrage() != null
                        ? bonDeCommande.getVehicule().getKilometrage()
                        : 0.0)
                .lignesFacturationPieces(new ArrayList<>())
                .lignesFacturationMainDoeuvres(new ArrayList<>())
                .build();

        BigDecimal montantHT = BigDecimal.ZERO;

        if (request.getLignes() != null) {
            for (ReceptionBonDeCommandeRequest.LigneReception ll : request.getLignes()) {
                LigneBonDeCommandePiece ligne = bonDeCommande.getLignes().stream()
                        .filter(l -> l.getId().equals(ll.getLigneId()))
                        .findFirst()
                        .orElse(null);
                if (ligne != null && ll.getQuantiteRecue() != null && ll.getQuantiteRecue() > 0) {

                    int currentRecue = ligne.getQuantiteRecue() != null ? ligne.getQuantiteRecue() : 0;
                    if (currentRecue + ll.getQuantiteRecue() > ligne.getQuantite()) {
                        throw new RuntimeException(
                                "La quantité reçue dépasse la quantité commandée pour la ligne " + ligne.getId());
                    }
                    ligne.setQuantiteRecue(currentRecue + ll.getQuantiteRecue());

                    montantHT = montantHT
                            .add(ligne.getPrixUnitaire().multiply(BigDecimal.valueOf(ll.getQuantiteRecue())));

                    if (ligne.getPieceDetachee() != null) {
                        PieceDetache piece = ligne.getPieceDetachee();
                        piece = (PieceDetache) org.hibernate.Hibernate.unproxy(piece);
                        if (piece instanceof PDP pdp) {
                            EntreeStockRequest entreeReq = new EntreeStockRequest(
                                    pdp.getId(),
                                    ll.getQuantiteRecue(),
                                    "Réception BC " + bonDeCommande.getNumero());
                            stockService.entree(entreeReq);

                            LigneFacturationPiece lfp = LigneFacturationPiece.builder()
                                    .facturation(bonDeReception)
                                    .piece(pdp)
                                    .quantite(ll.getQuantiteRecue())
                                    .prix(ligne.getPrixUnitaire().intValue())
                                    .build();
                            bonDeReception.getLignesFacturationPieces().add(lfp);
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

        bonDeReception.setMontantHT(montantHT);
        BigDecimal tva = bonDeCommande.getTvaApplicable() ? montantHT.multiply(new BigDecimal("0.18"))
                : BigDecimal.ZERO;
        bonDeReception.setMontantTVA(tva);
        bonDeReception.setMontantTTC(montantHT.add(tva));
        bonDeReception.setMontantTimbre(BigDecimal.ZERO);
        bonDeReception.setMontantTotal(bonDeReception.getMontantTTC().add(bonDeReception.getMontantTimbre()));
        bonDeReceptionRepository.save(bonDeReception);

        if (toutRecu && bonDeCommande.getVehicule() != null) {
            List<OrdreReparation> fiches = ordreReparationRepository
                    .findByVehiculeIdAndStatut(
                            bonDeCommande.getVehicule().getId(),
                            StatutOrdreReparation.EN_ATTENTE_COMMANDE);

            for (OrdreReparation fiche : fiches) {
                Proforma proforma = proformaRepository
                        .findByOrdreReparationId(fiche.getId()).orElse(null);
                if (proforma != null && fiche.getVehicule().getClient() != null) {
                    List<LignePieceRequest> lignesPieces = new ArrayList<>();
                    for (LigneFacturationPiece lp : proforma
                            .getLignesFacturationPieces()) {
                        PieceDetache piece = pieceDetacheRepository.findById(lp.getPiece().getId()).orElse(null);
                        if (piece != null) {
                            piece = (PieceDetache) org.hibernate.Hibernate.unproxy(piece);
                        }
                        if (piece instanceof PDP pdp) {
                            Double stockAtelier = pdp.getStockAtelier() != null ? pdp.getStockAtelier() : 0.0;
                            Double stockMagasin = pdp.getStockMagasin() != null ? pdp.getStockMagasin() : 0.0;
                            Double manquantAtelier = Math.max(0.0, lp.getQuantite() - stockAtelier);
                            Double aSortirMagasin = Math.min(manquantAtelier, stockMagasin);
                            if (aSortirMagasin > 0) {
                                lignesPieces.add(new LignePieceRequest(pdp.getId(),
                                        aSortirMagasin.intValue()));
                            }
                        }
                    }
                    if (!lignesPieces.isEmpty()) {
                        BonDeSortieRequest bdsRequest = new BonDeSortieRequest(
                                fiche.getVehicule().getClient().getId(),
                                fiche.getVehicule().getId(),
                                lignesPieces,
                                fiche.getId(),
                                "Bon de sortie auto suite réception commande");
                        try {
                            BonDeSortie bds = bonDeSortieService
                                    .creer(bdsRequest);
                            bds.setOrdreReparation(fiche);
                            bds = bonDeSortieRepository.save(bds);

                            fiche.setBonDeSortie(bds);
                            fiche.setStatut(
                                    StatutOrdreReparation.EN_ATTENTE_SORTIE);
                            ordreReparationRepository.save(fiche);
                        } catch (Exception e) {
                            log.error("Erreur auto bon de sortie FA-" + fiche.getId(), e);
                            throw new RuntimeException("Erreur lors de la création automatique du bon de sortie", e);
                        }
                    }
                }
            }
        }

        agentNotificationService.notifyRole(Role.AGENT_MAGASIN,
                "Bon de Commande " + (toutRecu ? "Réceptionné" : "Partiellement Réceptionné"),
                "Le bon de commande " + bonDeCommande.getNumero() + " a été "
                        + (toutRecu ? "réceptionné" : "partiellement réceptionné") + ".");

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
        BonDeCommande bonDeCommande = bonDeCommandeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bon de commande non trouvé"));

        boolean receptionCommencee = bonDeCommande.getStatut() == StatutBonCommande.RECU
                || bonDeCommande.getStatut() == StatutBonCommande.INCOMPLET
                || (bonDeCommande.getLignes() != null && bonDeCommande.getLignes().stream()
                        .anyMatch(l -> l.getQuantiteRecue() != null && l.getQuantiteRecue() > 0));

        if (receptionCommencee) {
            throw new RuntimeException("Impossible de supprimer ce bon de commande car la réception des pièces a déjà commencé.");
        }

        bonDeCommandeRepository.delete(bonDeCommande);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generatePdf(Long id) {
        BonDeCommande bonDeCommande = bonDeCommandeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bon de commande non trouvé"));
        // Accéder à la collection pour forcer le chargement paresseux (lazy loading) si
        // nécessaire
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
                        .designationPiece(ligne.getPieceDetachee() != null ? ligne.getPieceDetachee().getDesignation()
                                : ligne.getDesignationPds())
                        .reference(ligne.getPieceDetachee() != null ? ligne.getPieceDetachee().getReference()
                                : ligne.getReferencePds())
                        .categorie(ligne.getPieceDetachee() != null && ligne.getPieceDetachee().getCategorie() != null
                                ? ligne.getPieceDetachee().getCategorie().getNom()
                                : ligne.getCategoriePds())
                        .quantite(ligne.getQuantite())
                        .quantiteRecue(ligne.getQuantiteRecue())
                        .prixUnitaire(ligne.getPrixUnitaire().doubleValue())
                        .montant(ligne.getMontant().doubleValue())
                        .build()).collect(Collectors.toList()))
                .build();
    }
}
