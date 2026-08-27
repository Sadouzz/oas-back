package sn.oas.facturation.ordreReparation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sn.oas.facturation.ordreReparation.data.entity.OrdreReparation;
import sn.oas.facturation.ordreReparation.data.enums.StatutOrdreReparation;
import sn.oas.facturation.ordreReparation.dto.OrdreReparationRequest;
import sn.oas.facturation.ordreReparation.repository.OrdreReparationRepository;
import sn.oas.facturation.vehicule.data.entity.Vehicule;
import sn.oas.facturation.vehicule.repository.VehiculeRepository;
import sn.oas.facturation.auth.data.entity.Technicien;
import sn.oas.facturation.technicien.repository.TechnicienRepository;
import sn.oas.facturation.ordreReparation.data.entity.LigneOrdreReparationPiece;
import sn.oas.facturation.ordreReparation.data.entity.LigneReceptionOrdre;
import sn.oas.facturation.ordreReparation.data.entity.LigneTravailOrdre;
import sn.oas.facturation.ordreReparation.data.entity.LigneOrdreReparationMainDoeuvre;
import sn.oas.facturation.ordreReparation.dto.LigneOrdreReparationPieceRequest;
import sn.oas.facturation.ordreReparation.dto.LigneOrdreReparationMainDoeuvreRequest;
import sn.oas.facturation.piecedetache.data.entity.PDP;
import sn.oas.facturation.piecedetache.data.entity.PieceDetache;
import sn.oas.facturation.main_doeuvre.data.entity.MainDoeuvre;
import sn.oas.facturation.main_doeuvre.repository.MainDoeuvreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.annotation.Transactional;
import sn.oas.facturation.auth.data.enums.Role;
import sn.oas.facturation.notification.service.AgentNotificationService;

import java.util.List;
import java.util.Optional;
import sn.oas.facturation.proforma.service.ProformaService;
import sn.oas.facturation.proforma.dto.ProformaCreateRequest;
import sn.oas.facturation.facturation.dto.LigneFacturationPieceRequest;
import sn.oas.facturation.facturation.dto.LigneFacturationMainDoeuvreRequest;
import sn.oas.facturation.ordreReparation.dto.OrdreReparationLightDTO;
import sn.oas.facturation.ordreReparation.dto.VehiculeLightDTO;
import sn.oas.facturation.ordreReparation.dto.ClientLightDTO;
import sn.oas.facturation.ordreReparation.data.entity.PieceJointeDiagnostic;
import sn.oas.facturation.ordreReparation.data.entity.RemarqueDiagnostic;
import sn.oas.facturation.ordreReparation.data.enums.TypePieceJointe;
import sn.oas.facturation.ordreReparation.dto.PieceJointeDiagnosticRequest;
import sn.oas.facturation.ordreReparation.dto.PieceJointeDiagnosticResponse;
import sn.oas.facturation.ordreReparation.dto.RemarqueDiagnosticResponse;
import sn.oas.facturation.ordreReparation.repository.PieceJointeDiagnosticRepository;
import sn.oas.facturation.ordreReparation.repository.RemarqueDiagnosticRepository;
import sn.oas.facturation.ficheAtelier.data.entity.FicheAtelier;
import sn.oas.facturation.ficheAtelier.repository.FicheAtelierRepository;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrdreReparationServiceImpl implements OrdreReparationService {

    private final OrdreReparationRepository ordreReparationRepository;
    private final VehiculeRepository vehiculeRepository;
    private final TechnicienRepository technicienRepository;
    private final sn.oas.facturation.proforma.repository.ProformaRepository proformaRepository;
    private final sn.oas.facturation.piecedetache.repository.PieceDetacheRepository pieceDetacheRepository;
    private final MainDoeuvreRepository mainDoeuvreRepository;
    private final AgentNotificationService agentNotificationService;
    private final sn.oas.facturation.shared.documentNumber.DocumentNumberGeneratorService documentNumberGeneratorService;
    private final PieceJointeDiagnosticRepository pieceJointeDiagnosticRepository;
    private final RemarqueDiagnosticRepository remarqueDiagnosticRepository;
    private final FicheAtelierRepository ficheAtelierRepository;
    private final sn.oas.facturation.devisPrevisionnel.repository.DevisPrevisionnelRepository devisPrevisionnelRepository;

    @Autowired
    @Lazy
    private ProformaService proformaService;

    @Override
    public OrdreReparation createOrdreReparation(OrdreReparationRequest request) {
        Vehicule vehicule = null;
        if (request.getVehiculeId() != null) {
            vehicule = vehiculeRepository.findById(request.getVehiculeId())
                    .orElseThrow(() -> new RuntimeException("Véhicule non trouvé"));
        } else {
            throw new RuntimeException("L'ID du véhicule est obligatoire");
        }

        String numero = request.getNumero();
        if (numero == null || numero.trim().isEmpty()) {
            numero = documentNumberGeneratorService.generateNextNumber(sn.oas.facturation.shared.documentNumber.DocumentType.OR);
        }

        OrdreReparation ordreReparation = OrdreReparation.builder()
                .numero(numero)
                .descriptionTravaux(request.getDescriptionTravaux())
                .lignesTravaux(request.getLignesTravaux())
                .lignesReception(request.getLignesReception())
                .listeDefauts(request.getListeDefauts())
                .dateSortie(request.getDateSortie())
                .vehicule(vehicule)
                .statut(request.getStatut() != null ? request.getStatut() : StatutOrdreReparation.A_FAIRE)
                .build();

        if (request.getLignesPieces() != null) {
            for (LigneOrdreReparationPieceRequest ligneReq : request.getLignesPieces()) {
                PDP pdp = null;
                Integer prix = ligneReq.prix();
                
                if (Boolean.TRUE.equals(ligneReq.isCustom())) {
                    if (prix == null) prix = 0;
                } else {
                    PieceDetache piece = pieceDetacheRepository.findById(ligneReq.pieceId())
                            .orElseThrow(() -> new RuntimeException("Pièce non trouvée"));
                    pdp = (PDP) org.hibernate.Hibernate.unproxy(piece);
                    if (prix == null) prix = (pdp.getPrixUnitaire() != null ? pdp.getPrixUnitaire().intValue() : 0);
                }

                ordreReparation.getLignesOrdreReparationPieces().add(LigneOrdreReparationPiece.builder()
                        .ordreReparation(ordreReparation)
                        .piece(pdp)
                        .isCustom(Boolean.TRUE.equals(ligneReq.isCustom()))
                        .designationPds(ligneReq.designationPds())
                        .quantite(ligneReq.quantite())
                        .prix(prix)
                        .build());
            }
        }

        if (request.getLignesMainDoeuvres() != null) {
            for (LigneOrdreReparationMainDoeuvreRequest ligneReq : request.getLignesMainDoeuvres()) {
                MainDoeuvre md = mainDoeuvreRepository.findById(ligneReq.mainDoeuvreId())
                        .orElseThrow(() -> new RuntimeException("Main d'œuvre non trouvée"));
                ordreReparation.getLignesOrdreReparationMainDoeuvres().add(LigneOrdreReparationMainDoeuvre.builder()
                        .ordreReparation(ordreReparation)
                        .mainDoeuvre(md)
                        .nbreHeure(ligneReq.nbreHeure())
                        .prix(ligneReq.prix() != null ? ligneReq.prix()
                                : (md.getPrix() != null ? md.getPrix().intValue() : 0))
                        .build());
            }
        }

        return ordreReparationRepository.save(ordreReparation);
    }

    @Override
    public List<OrdreReparationLightDTO> getAllOrdresReparation() {
        return ordreReparationRepository.findAllWithVehiculeAndClient().stream().map(f -> {
            ClientLightDTO clientDTO = null;
            if (f.getVehicule() != null && f.getVehicule().getClient() != null) {
                clientDTO = ClientLightDTO.builder()
                        .id(f.getVehicule().getClient().getId())
                        .firstName(f.getVehicule().getClient().getFirstName())
                        .lastName(f.getVehicule().getClient().getLastName())
                        .phone(f.getVehicule().getClient().getPhone())
                        .build();
            }

            VehiculeLightDTO vehiculeDTO = null;
            if (f.getVehicule() != null) {
                vehiculeDTO = VehiculeLightDTO.builder()
                        .id(f.getVehicule().getId())
                        .immatriculation(f.getVehicule().getImmatriculation())
                        .marque(f.getVehicule().getMarque())
                        .modele(f.getVehicule().getModele())
                        .client(clientDTO)
                        .build();
            }

            return OrdreReparationLightDTO.builder()
                    .id(f.getId())
                    .numero(f.getNumero())
                    .descriptionTravaux(f.getDescriptionTravaux())
                    .dateCreation(f.getDateCreation())
                    .dateSortie(f.getDateSortie())
                    .statut(f.getStatut())
                    .vehicule(vehiculeDTO)
                    .hasPiecesOrMo(f.getHasPiecesOrMo())
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    public Optional<OrdreReparation> getOrdreReparationById(Long id) {
        return ordreReparationRepository.findById(id);
    }

    @Override
    public OrdreReparation updateOrdreReparation(Long id, OrdreReparationRequest request) {
        OrdreReparation ordreReparation = ordreReparationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Fiche Atelier non trouvée"));

        if (request.getNumero() != null)
            ordreReparation.setNumero(request.getNumero());
        if (request.getDescriptionTravaux() != null)
            ordreReparation.setDescriptionTravaux(request.getDescriptionTravaux());
        if (request.getLignesTravaux() != null)
            ordreReparation.setLignesTravaux(request.getLignesTravaux());
        if (request.getLignesReception() != null)
            ordreReparation.setLignesReception(request.getLignesReception());
        if (request.getListeDefauts() != null)
            ordreReparation.setListeDefauts(request.getListeDefauts());
        if (request.getDateSortie() != null)
            ordreReparation.setDateSortie(request.getDateSortie());
        if (request.getStatut() != null)
            ordreReparation.setStatut(request.getStatut());

        if (request.getVehiculeId() != null) {
            Vehicule vehicule = vehiculeRepository.findById(request.getVehiculeId())
                    .orElseThrow(() -> new RuntimeException("Véhicule non trouvé"));
            ordreReparation.setVehicule(vehicule);
        }

        if (request.getLignesPieces() != null) {
            ordreReparation.getLignesOrdreReparationPieces().clear();
            for (LigneOrdreReparationPieceRequest ligneReq : request.getLignesPieces()) {
                PDP pdp = null;
                Integer prix = ligneReq.prix();
                
                if (Boolean.TRUE.equals(ligneReq.isCustom())) {
                    // Custom piece, no PDP needed
                    if (prix == null) prix = 0;
                } else {
                    PieceDetache piece = pieceDetacheRepository.findById(ligneReq.pieceId())
                            .orElseThrow(() -> new RuntimeException("Pièce non trouvée"));
                    pdp = (PDP) org.hibernate.Hibernate.unproxy(piece);
                    if (prix == null) prix = (pdp.getPrixUnitaire() != null ? pdp.getPrixUnitaire().intValue() : 0);
                }

                ordreReparation.getLignesOrdreReparationPieces().add(LigneOrdreReparationPiece.builder()
                        .ordreReparation(ordreReparation)
                        .piece(pdp)
                        .isCustom(Boolean.TRUE.equals(ligneReq.isCustom()))
                        .designationPds(ligneReq.designationPds())
                        .quantite(ligneReq.quantite())
                        .prix(prix)
                        .build());
            }
        }

        if (request.getLignesMainDoeuvres() != null) {
            ordreReparation.getLignesOrdreReparationMainDoeuvres().clear();
            for (LigneOrdreReparationMainDoeuvreRequest ligneReq : request.getLignesMainDoeuvres()) {
                MainDoeuvre md = mainDoeuvreRepository.findById(ligneReq.mainDoeuvreId())
                        .orElseThrow(() -> new RuntimeException("Main d'œuvre non trouvée"));
                ordreReparation.getLignesOrdreReparationMainDoeuvres().add(LigneOrdreReparationMainDoeuvre.builder()
                        .ordreReparation(ordreReparation)
                        .mainDoeuvre(md)
                        .nbreHeure(ligneReq.nbreHeure())
                        .prix(ligneReq.prix() != null ? ligneReq.prix()
                                : (md.getPrix() != null ? md.getPrix().intValue() : 0))
                        .build());
            }
        }

        ordreReparation = ordreReparationRepository.save(ordreReparation);

        // Auto-create proforma if pieces or MO are added and it doesn't exist yet
        if ((request.getLignesPieces() != null && !request.getLignesPieces().isEmpty()) ||
                (request.getLignesMainDoeuvres() != null && !request.getLignesMainDoeuvres().isEmpty())) {

            if (proformaRepository.findByOrdreReparationId(ordreReparation.getId()).isEmpty()) {
                ProformaCreateRequest pcr = new ProformaCreateRequest();
                pcr.setOrdreReparationId(ordreReparation.getId());
                pcr.setClientId(
                        ordreReparation.getVehicule().getClient() != null ? ordreReparation.getVehicule().getClient().getId()
                                : null);
                pcr.setVehiculeId(ordreReparation.getVehicule().getId());
                pcr.setKilometrage(ordreReparation.getVehicule().getKilometrage() != null
                        ? ordreReparation.getVehicule().getKilometrage()
                        : 0.0);

                if (ordreReparation.getLignesOrdreReparationPieces() != null) {
                    pcr.setLignesPieces(ordreReparation.getLignesOrdreReparationPieces().stream().map(lp -> {
                        LigneFacturationPieceRequest lr = new LigneFacturationPieceRequest();
                        lr.setPieceId(lp.getPiece() != null ? lp.getPiece().getId() : null);
                        lr.setQuantite(lp.getQuantite());
                        lr.setPrix(lp.getPrix());
                        lr.setIsCustom(lp.getIsCustom());
                        lr.setDesignationPds(lp.getDesignationPds());
                        return lr;
                    }).collect(Collectors.toList()));
                }

                if (ordreReparation.getLignesOrdreReparationMainDoeuvres() != null) {
                    pcr.setLignesMainDoeuvres(ordreReparation.getLignesOrdreReparationMainDoeuvres().stream().map(lm -> {
                        LigneFacturationMainDoeuvreRequest lmr = new LigneFacturationMainDoeuvreRequest();
                        lmr.setMainDoeuvreId(lm.getMainDoeuvre() != null ? lm.getMainDoeuvre().getId() : null);
                        lmr.setNbreHeure(lm.getNbreHeure());
                        lmr.setTarifHoraire(lm.getPrix());
                        return lmr;
                    }).collect(Collectors.toList()));
                }

                proformaService.create(pcr);
                // proformaService.create already sets OrdreReparation status to
                // EN_ATTENTE_PROFORMA
            }
        }

        return ordreReparation;
    }

    @Override
    public void deleteOrdreReparation(Long id) {
        OrdreReparation ordreReparation = ordreReparationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Fiche Atelier non trouvée"));
        ordreReparationRepository.delete(ordreReparation);
    }

    @Transactional
    @Override
    public void assignTechnicien(Long ficheId, Long technicienId) {
        OrdreReparation fiche = ordreReparationRepository.findById(ficheId)
                .orElseThrow(() -> new RuntimeException("Fiche Atelier non trouvée"));
        Technicien technicien = technicienRepository.findById(technicienId)
                .orElseThrow(() -> new RuntimeException("Technicien non trouvé"));

        if (!fiche.getTechniciens().contains(technicien)) {
            fiche.getTechniciens().add(technicien);
            ordreReparationRepository.save(fiche);
        }
    }

    @Transactional
    @Override
    public void removeTechnicien(Long ficheId, Long technicienId) {
        OrdreReparation fiche = ordreReparationRepository.findById(ficheId)
                .orElseThrow(() -> new RuntimeException("Fiche Atelier non trouvée"));
        Technicien technicien = technicienRepository.findById(technicienId)
                .orElseThrow(() -> new RuntimeException("Technicien non trouvé"));

        fiche.getTechniciens().remove(technicien);
        ordreReparationRepository.save(fiche);
    }

    @Transactional
    @Override
    public void assignTechnicienReparation(Long ficheId, Long technicienId) {
        OrdreReparation fiche = ordreReparationRepository.findById(ficheId)
                .orElseThrow(() -> new RuntimeException("Fiche Atelier non trouvée"));
        Technicien technicien = technicienRepository.findById(technicienId)
                .orElseThrow(() -> new RuntimeException("Technicien non trouvé"));

        if (!fiche.getTechniciensReparation().contains(technicien)) {
            fiche.getTechniciensReparation().add(technicien);
            ordreReparationRepository.save(fiche);
        }
    }

    @Transactional
    @Override
    public void removeTechnicienReparation(Long ficheId, Long technicienId) {
        OrdreReparation fiche = ordreReparationRepository.findById(ficheId)
                .orElseThrow(() -> new RuntimeException("Fiche Atelier non trouvée"));
        Technicien technicien = technicienRepository.findById(technicienId)
                .orElseThrow(() -> new RuntimeException("Technicien non trouvé"));

        fiche.getTechniciensReparation().remove(technicien);
        ordreReparationRepository.save(fiche);
    }

    @Transactional
    @Override
    public OrdreReparation updateStatut(Long id, String statut) {
        OrdreReparation fiche = ordreReparationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Fiche Atelier non trouvée"));
        StatutOrdreReparation newStatut;
        try {
            newStatut = StatutOrdreReparation.valueOf(statut);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Statut invalide : " + statut);
        }

        // Au moins un technicien doit être affecté au pool "diagnostic" avant de
        // pouvoir démarrer le diagnostic (voir spec point 4).
        if (newStatut == StatutOrdreReparation.EN_DIAGNOSTIC
                && (fiche.getTechniciens() == null || fiche.getTechniciens().isEmpty())) {
            throw new RuntimeException("Veuillez affecter au moins un technicien avant de démarrer le diagnostic.");
        }

        // Si la réparation commence (EN_COURS), on déduit les pièces
        // du proforma du stock de l'atelier
        if (newStatut == StatutOrdreReparation.EN_COURS && fiche.getStatut() != StatutOrdreReparation.EN_COURS) {
            proformaRepository.findByOrdreReparationId(id).ifPresent(proforma -> {
                for (sn.oas.facturation.facturation.data.entity.LigneFacturationPiece lp : proforma
                        .getLignesFacturationPieces()) {
                    sn.oas.facturation.piecedetache.data.entity.PieceDetache piece = pieceDetacheRepository
                            .findById(lp.getPiece().getId()).orElse(null);
                    if (piece != null && piece instanceof sn.oas.facturation.piecedetache.data.entity.PDP pdp) {
                        double prixUnitaire = (pdp.getPrixUnitaire() != null) ? pdp.getPrixUnitaire() : 0.0;
                        double montantPiece = prixUnitaire * lp.getQuantite();
                        Double currentAtelier = pdp.getStockAtelier() != null ? pdp.getStockAtelier() : 0.0;
                        double quantiteUtilisee = lp.getQuantite();
                        pdp.setStockAtelier(Math.max(0.0, currentAtelier - quantiteUtilisee));
                        pdp.setQteReelle(
                                (pdp.getStockMagasin() != null ? pdp.getStockMagasin() : 0) + pdp.getStockAtelier());
                        pieceDetacheRepository.save(pdp);
                    }
                }
            });
        }

        fiche.setStatut(newStatut);
        OrdreReparation savedFiche = ordreReparationRepository.save(fiche);

        if (newStatut == StatutOrdreReparation.EN_ATTENTE_COMMANDE || newStatut == StatutOrdreReparation.EN_ATTENTE_SORTIE) {
            agentNotificationService.notifyRole(Role.AGENT_MAGASIN,
                    "Pièces en attente pour " + savedFiche.getNumero(),
                    "La fiche " + savedFiche.getNumero() + " est passée en " + newStatut + ".");
        }

        return savedFiche;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByVehiculeIdAndStatutNotIn(Long vehiculeId, List<sn.oas.facturation.ordreReparation.data.enums.StatutOrdreReparation> statuts) {
        return ordreReparationRepository.existsByVehiculeIdAndStatutNotIn(vehiculeId, statuts);
    }

    // ─── Pièces jointes de diagnostic ──────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<PieceJointeDiagnosticResponse> getPiecesJointesDiagnostic(Long ordreReparationId, TypePieceJointe type) {
        // PieceJointeDiagnostic n'a pas son propre filtre garage (cf. justification sur l'entité) :
        // on passe par le repository filtré d'OrdreReparation pour garder l'isolation multi-tenant.
        ordreReparationRepository.findById(ordreReparationId)
                .orElseThrow(() -> new RuntimeException("Ordre de réparation non trouvé"));
        List<PieceJointeDiagnostic> pieces = type != null
                ? pieceJointeDiagnosticRepository.findByOrdreReparationIdAndTypeOrderByCreatedAtDesc(ordreReparationId, type)
                : pieceJointeDiagnosticRepository.findByOrdreReparationIdOrderByCreatedAtDesc(ordreReparationId);
        return pieces.stream().map(this::toPieceJointeResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PieceJointeDiagnosticResponse addPieceJointeDiagnostic(Long ordreReparationId, PieceJointeDiagnosticRequest request) {
        OrdreReparation ordreReparation = ordreReparationRepository.findById(ordreReparationId)
                .orElseThrow(() -> new RuntimeException("Ordre de réparation non trouvé"));
        if (request.getUrl() == null || request.getUrl().trim().isEmpty()) {
            throw new RuntimeException("L'URL de la pièce jointe est obligatoire");
        }
        if (request.getType() == null) {
            throw new RuntimeException("Le type de la pièce jointe est obligatoire");
        }

        PieceJointeDiagnostic pieceJointe = PieceJointeDiagnostic.builder()
                .ordreReparation(ordreReparation)
                .url(request.getUrl())
                .type(request.getType())
                .remarque(request.getRemarque())
                .build();

        return toPieceJointeResponse(pieceJointeDiagnosticRepository.save(pieceJointe));
    }

    @Override
    @Transactional
    public void deletePieceJointeDiagnostic(Long ordreReparationId, Long pieceJointeId) {
        // Idem : passage par le repository filtré d'OrdreReparation pour l'isolation multi-tenant.
        ordreReparationRepository.findById(ordreReparationId)
                .orElseThrow(() -> new RuntimeException("Ordre de réparation non trouvé"));
        PieceJointeDiagnostic pieceJointe = pieceJointeDiagnosticRepository.findById(pieceJointeId)
                .orElseThrow(() -> new RuntimeException("Pièce jointe non trouvée"));
        if (pieceJointe.getOrdreReparation() == null || !pieceJointe.getOrdreReparation().getId().equals(ordreReparationId)) {
            throw new RuntimeException("Cette pièce jointe n'appartient pas à cet ordre de réparation");
        }
        pieceJointeDiagnosticRepository.delete(pieceJointe);
    }

    private PieceJointeDiagnosticResponse toPieceJointeResponse(PieceJointeDiagnostic p) {
        String techNom = null;
        if (p.getTechnicien() != null) {
            String prenom = p.getTechnicien().getFirstName() != null ? p.getTechnicien().getFirstName() : "";
            String nom = p.getTechnicien().getLastName() != null ? p.getTechnicien().getLastName() : "";
            techNom = (prenom + " " + nom).trim();
            if (techNom.isEmpty()) techNom = p.getTechnicien().getUsername();
        }
        return PieceJointeDiagnosticResponse.builder()
                .id(p.getId())
                .ordreReparationId(p.getOrdreReparation() != null ? p.getOrdreReparation().getId() : null)
                .url(p.getUrl())
                .type(p.getType())
                .remarque(p.getRemarque())
                .technicienNom(techNom)
                .createdAt(p.getCreatedAt())
                .build();
    }

    // ─── Remarques de diagnostic ────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<RemarqueDiagnosticResponse> getRemarquesDiagnostic(Long ordreReparationId) {
        ordreReparationRepository.findById(ordreReparationId)
                .orElseThrow(() -> new RuntimeException("Ordre de réparation non trouvé"));
        return remarqueDiagnosticRepository.findByOrdreReparationIdOrderByCreatedAtDesc(ordreReparationId)
                .stream().map(this::toRemarqueResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public RemarqueDiagnosticResponse addRemarqueDiagnostic(Long ordreReparationId, sn.oas.facturation.auth.data.entity.Technicien technicien, String contenu) {
        OrdreReparation ordreReparation = ordreReparationRepository.findById(ordreReparationId)
                .orElseThrow(() -> new RuntimeException("Ordre de réparation non trouvé"));
        if (contenu == null || contenu.trim().isEmpty()) {
            throw new RuntimeException("Le contenu de la remarque ne peut pas être vide");
        }
        RemarqueDiagnostic remarque = RemarqueDiagnostic.builder()
                .ordreReparation(ordreReparation)
                .technicien(technicien)
                .contenu(contenu.trim())
                .build();
        return toRemarqueResponse(remarqueDiagnosticRepository.save(remarque));
    }

    @Override
    @Transactional
    public void deleteRemarqueDiagnostic(Long ordreReparationId, Long remarqueId) {
        ordreReparationRepository.findById(ordreReparationId)
                .orElseThrow(() -> new RuntimeException("Ordre de réparation non trouvé"));
        RemarqueDiagnostic r = remarqueDiagnosticRepository.findById(remarqueId)
                .orElseThrow(() -> new RuntimeException("Remarque non trouvée"));
        if (r.getOrdreReparation() == null || !r.getOrdreReparation().getId().equals(ordreReparationId)) {
            throw new RuntimeException("Cette remarque n'appartient pas à cet ordre de réparation");
        }
        remarqueDiagnosticRepository.delete(r);
    }

    private RemarqueDiagnosticResponse toRemarqueResponse(RemarqueDiagnostic r) {
        String techNom = null;
        if (r.getTechnicien() != null) {
            String prenom = r.getTechnicien().getFirstName() != null ? r.getTechnicien().getFirstName() : "";
            String nom = r.getTechnicien().getLastName() != null ? r.getTechnicien().getLastName() : "";
            techNom = (prenom + " " + nom).trim();
            if (techNom.isEmpty()) techNom = r.getTechnicien().getUsername();
        }
        return RemarqueDiagnosticResponse.builder()
                .id(r.getId())
                .ordreReparationId(r.getOrdreReparation() != null ? r.getOrdreReparation().getId() : null)
                .technicienNom(techNom)
                .contenu(r.getContenu())
                .createdAt(r.getCreatedAt())
                .build();
    }

    // ─── Lien Fiche Atelier → Ordre de réparation ──────────────────────

    @Override
    @Transactional
    public OrdreReparation createFromFicheAtelier(Long ficheAtelierId) {
        FicheAtelier ficheAtelier = ficheAtelierRepository.findById(ficheAtelierId)
                .orElseThrow(() -> new RuntimeException("Fiche Atelier non trouvée"));

        java.util.Optional<OrdreReparation> existingExact = ordreReparationRepository.findFirstByFicheAtelierId(ficheAtelierId);
        if (existingExact.isPresent()) {
            return existingExact.get();
        }
        
        java.util.Optional<sn.oas.facturation.devisPrevisionnel.data.entity.DevisPrevisionnel> devisOpt = devisPrevisionnelRepository.findByFicheAtelierId(ficheAtelierId);
        if (devisOpt.isEmpty()) {
            throw new RuntimeException("Un devis prévisionnel doit être créé et accepté avant de créer l'ordre de réparation.");
        }
        sn.oas.facturation.devisPrevisionnel.data.entity.DevisPrevisionnel devis = devisOpt.get();
        if (devis.getStatut() != sn.oas.facturation.facturation.data.enums.StatutFacturation.ACCEPTE &&
            devis.getStatut() != sn.oas.facturation.facturation.data.enums.StatutFacturation.PAYEE) {
            throw new RuntimeException("Le devis prévisionnel doit être accepté avant de créer l'ordre de réparation.");
        }
        if (ficheAtelier.getVehicule() == null) {
            throw new RuntimeException("La fiche atelier n'a pas de véhicule associé");
        }
        
        java.util.Optional<OrdreReparation> activeOr = ordreReparationRepository.findFirstByVehiculeIdAndStatutNotIn(
                ficheAtelier.getVehicule().getId(), 
                java.util.List.of(StatutOrdreReparation.LIVRE, StatutOrdreReparation.TERMINE)
        );
        if (activeOr.isPresent()) {
            return activeOr.get();
        }

        String numero = documentNumberGeneratorService.generateNextNumber(sn.oas.facturation.shared.documentNumber.DocumentType.OR);
        String travauxDemandes = ficheAtelier.getDesignationTravaux();

        OrdreReparation ordreReparation = OrdreReparation.builder()
                .numero(numero)
                .descriptionTravaux(travauxDemandes != null ? travauxDemandes : "")
                .lignesTravaux(syntheseTravaux(travauxDemandes))
                .lignesReception(syntheseReception(ficheAtelier))
                .vehicule(ficheAtelier.getVehicule())
                .ficheAtelier(ficheAtelier)
                .statut(StatutOrdreReparation.A_FAIRE)
                .build();

        return ordreReparationRepository.save(ordreReparation);
    }

    /**
     * Reprend la désignation des travaux de la fiche atelier d'origine comme unique ligne
     * verrouillée de la section "Travaux demandés" (le chef d'atelier peut ensuite en ajouter
     * d'autres, non verrouillées, mais pas modifier/supprimer celle-ci).
     */
    private List<LigneTravailOrdre> syntheseTravaux(String designationTravaux) {
        java.util.ArrayList<LigneTravailOrdre> lignes = new java.util.ArrayList<>();
        if (designationTravaux != null && !designationTravaux.isBlank()) {
            lignes.add(LigneTravailOrdre.builder().nom(designationTravaux).verrouille(true).build());
        }
        return lignes;
    }

    /**
     * Reprend les lignes de réception de la fiche atelier d'origine, verrouillées (le chef
     * d'atelier ne peut pas modifier leur désignation ni les supprimer côté UI — il peut
     * seulement ajouter de nouvelles lignes, non verrouillées, via le formulaire de l'ordre
     * de réparation).
     */
    private List<LigneReceptionOrdre> syntheseReception(FicheAtelier ficheAtelier) {
        List<sn.oas.facturation.ficheAtelier.data.entity.LigneReception> lignesReception = ficheAtelier.getLignesReception();
        if (lignesReception == null || lignesReception.isEmpty()) {
            return new java.util.ArrayList<>();
        }
        return lignesReception.stream()
                .map(l -> LigneReceptionOrdre.builder()
                        .nom(l.getNom())
                        .etat(l.getEtat())
                        .verrouille(true)
                        .build())
                .collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByFicheAtelierId(Long ficheAtelierId) {
        if (ordreReparationRepository.existsByFicheAtelierId(ficheAtelierId)) {
            return true;
        }
        FicheAtelier fiche = ficheAtelierRepository.findById(ficheAtelierId).orElse(null);
        if (fiche != null && fiche.getVehicule() != null) {
            return ordreReparationRepository.existsByVehiculeIdAndStatutNotIn(
                fiche.getVehicule().getId(), 
                java.util.List.of(StatutOrdreReparation.LIVRE, StatutOrdreReparation.TERMINE)
            );
        }
        return false;
    }
}
