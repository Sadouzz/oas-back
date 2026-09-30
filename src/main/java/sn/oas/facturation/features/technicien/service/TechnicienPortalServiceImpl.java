package sn.oas.facturation.features.technicien.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sn.oas.facturation.features.diagnostic.data.entity.Diagnostic;
import sn.oas.facturation.features.diagnostic.data.entity.PieceJointeDiagnostic;
import sn.oas.facturation.features.diagnostic.data.enums.TypePieceJointe;
import sn.oas.facturation.features.diagnostic.dto.PieceJointeDiagnosticRequest;
import sn.oas.facturation.features.diagnostic.dto.PieceJointeDiagnosticResponse;
import sn.oas.facturation.features.diagnostic.repository.DiagnosticRepository;
import sn.oas.facturation.features.diagnostic.repository.PieceJointeDiagnosticRepository;
import sn.oas.facturation.features.main_doeuvre.data.entity.MainDoeuvre;
import sn.oas.facturation.features.main_doeuvre.repository.MainDoeuvreRepository;
import sn.oas.facturation.features.ordreReparation.data.entity.LigneOrdreReparationMainDoeuvre;
import sn.oas.facturation.features.ordreReparation.data.entity.LigneOrdreReparationPiece;
import sn.oas.facturation.features.ordreReparation.data.entity.OrdreReparation;
import sn.oas.facturation.features.ordreReparation.repository.OrdreReparationRepository;
import sn.oas.facturation.features.piecedetache.data.entity.PieceDetache;
import sn.oas.facturation.features.piecedetache.repository.PieceDetacheRepository;
import sn.oas.facturation.features.technicien.data.entity.Technicien;
import sn.oas.facturation.features.technicien.dto.PannesRequest;
import sn.oas.facturation.features.technicien.dto.TechnicienLigneMainDoeuvreRequest;
import sn.oas.facturation.features.technicien.dto.TechnicienLignePieceRequest;

import sn.oas.facturation.features.ordreReparation.data.enums.StatutOrdreReparation;
import sn.oas.facturation.features.technicien.dto.TechnicienDashboardDto;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TechnicienPortalServiceImpl implements TechnicienPortalService {

    private final OrdreReparationRepository ordreReparationRepository;
    private final DiagnosticRepository diagnosticRepository;
    private final PieceJointeDiagnosticRepository pieceJointeDiagnosticRepository;
    private final PieceDetacheRepository pieceDetacheRepository;
    private final MainDoeuvreRepository mainDoeuvreRepository;

    @Override
    @Transactional(readOnly = true)
    public TechnicienDashboardDto getDashboardMetrics(Technicien technicien) {
        long diagnostics = ordreReparationRepository.countByTechnicienAssigneAndStatutIn(
                technicien.getId(), Arrays.asList(StatutOrdreReparation.DIAGNOSTIC));
        
        long reparations = ordreReparationRepository.countByTechnicienAssigneAndStatutIn(
                technicien.getId(), Arrays.asList(StatutOrdreReparation.REPARATION));
        
        long termines = ordreReparationRepository.countByTechnicienAssigneAndStatutIn(
                technicien.getId(), Arrays.asList(StatutOrdreReparation.PAIEMENT, StatutOrdreReparation.PRET_A_LIVRER, StatutOrdreReparation.LIVRE));

        return TechnicienDashboardDto.builder()
                .totalDiagnostics(diagnostics)
                .totalReparations(reparations)
                .totalTermines(termines)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrdreReparation> getMesOrdresReparation(Technicien technicien) {
        return ordreReparationRepository.findByTechnicienAssigne(technicien.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrdreReparation> getMesOrdresReparation(Technicien technicien, Pageable pageable) {
        return getMesOrdresReparation(technicien, null, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrdreReparation> getMesOrdresReparation(Technicien technicien, String keyword, Pageable pageable) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            return ordreReparationRepository.searchByTechnicienAssigne(technicien.getId(), keyword.trim(), pageable);
        }
        return ordreReparationRepository.findByTechnicienAssigne(technicien.getId(), pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public OrdreReparation getMonOrdreReparation(Technicien technicien, Long ordreReparationId) {
        OrdreReparation ordreReparation = ordreReparationRepository.findById(ordreReparationId)
                .orElseThrow(() -> new RuntimeException("Ordre de réparation non trouvé"));
        verifierTechnicienAssigne(ordreReparation, technicien);
        return ordreReparation;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PieceJointeDiagnosticResponse> getPiecesJointesDiagnostic(Technicien technicien, Long ordreReparationId, TypePieceJointe type) {
        OrdreReparation ordreReparation = ordreReparationRepository.findById(ordreReparationId)
                .orElseThrow(() -> new RuntimeException("Ordre de réparation non trouvé"));
        verifierTechnicienAssigne(ordreReparation, technicien);
        List<PieceJointeDiagnostic> pieces = type != null
                ? pieceJointeDiagnosticRepository.findByOrdreReparationIdAndTypeOrderByCreatedAtDesc(ordreReparationId, type)
                : pieceJointeDiagnosticRepository.findByOrdreReparationIdOrderByCreatedAtDesc(ordreReparationId);
        return pieces.stream().map(this::toPieceJointeResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PieceJointeDiagnosticResponse addPieceJointeDiagnostic(Technicien technicien, Long ordreReparationId, PieceJointeDiagnosticRequest request) {
        OrdreReparation ordreReparation = ordreReparationRepository.findById(ordreReparationId)
                .orElseThrow(() -> new RuntimeException("Ordre de réparation non trouvé"));
        verifierAccesIntervention(ordreReparation, technicien);
        if (request.getUrl() == null || request.getUrl().trim().isEmpty()) {
            throw new RuntimeException("L'URL de la pièce jointe est obligatoire");
        }
        if (request.getType() == null) {
            throw new RuntimeException("Le type de la pièce jointe est obligatoire");
        }

        Diagnostic diag = diagnosticRepository.findByOrdreReparationId(ordreReparationId).orElse(null);
        if (diag == null) {
            diag = ordreReparation.getDiagnostic();
        }
        if (diag == null) {
            diag = Diagnostic.builder()
                    .ordreReparation(ordreReparation)
                    .garage(ordreReparation.getGarage())
                    .technicien(technicien)
                    .statut(sn.oas.facturation.features.diagnostic.data.enums.StatutDiagnostic.EN_COURS)
                    .build();
            diag = diagnosticRepository.save(diag);
            ordreReparation.setDiagnostic(diag);
            ordreReparationRepository.save(ordreReparation);
        }

        OrdreReparation orRef = diag.getOrdreReparation() != null ? diag.getOrdreReparation() : ordreReparation;
        PieceJointeDiagnostic pieceJointe = PieceJointeDiagnostic.builder()
                .diagnostic(diag)
                .ordreReparation(orRef)
                .url(request.getUrl())
                .type(request.getType())
                .remarque(request.getRemarque())
                .technicien(technicien)
                .build();

        return toPieceJointeResponse(pieceJointeDiagnosticRepository.save(pieceJointe));
    }

    @Override
    @Transactional
    public void deletePieceJointeDiagnostic(Technicien technicien, Long ordreReparationId, Long pieceJointeId) {
        OrdreReparation ordreReparation = ordreReparationRepository.findById(ordreReparationId)
                .orElseThrow(() -> new RuntimeException("Ordre de réparation non trouvé"));
        verifierAccesIntervention(ordreReparation, technicien);
        PieceJointeDiagnostic pieceJointe = pieceJointeDiagnosticRepository.findById(pieceJointeId)
                .orElseThrow(() -> new RuntimeException("Pièce jointe non trouvée"));
        boolean matchOr = (pieceJointe.getOrdreReparation() != null && pieceJointe.getOrdreReparation().getId().equals(ordreReparationId))
                || (pieceJointe.getDiagnostic() != null && pieceJointe.getDiagnostic().getOrdreReparation() != null
                && pieceJointe.getDiagnostic().getOrdreReparation().getId().equals(ordreReparationId));
        if (!matchOr) {
            throw new RuntimeException("Cette pièce jointe n'appartient pas à cet ordre de réparation");
        }
        pieceJointeDiagnosticRepository.delete(pieceJointe);
    }

    @Override
    @Transactional
    public OrdreReparation updatePannesDetectees(Technicien technicien, Long ordreReparationId, PannesRequest request) {
        OrdreReparation ordreReparation = ordreReparationRepository.findById(ordreReparationId)
                .orElseThrow(() -> new RuntimeException("Ordre de réparation non trouvé"));
        verifierAccesIntervention(ordreReparation, technicien);
        ordreReparation.setListeDefauts(request.listeDefauts());
        Diagnostic diag = diagnosticRepository.findByOrdreReparationId(ordreReparationId).orElse(null);
        if (diag != null) {
            diag.setPannesDetectees(request.listeDefauts());
            diag.setObservations(request.listeDefauts());
            diagnosticRepository.save(diag);
        }
        return ordreReparationRepository.save(ordreReparation);
    }

    @Override
    @Transactional
    public void proposerPiece(Technicien technicien, Long ordreReparationId, TechnicienLignePieceRequest request) {
        OrdreReparation ordreReparation = ordreReparationRepository.findById(ordreReparationId)
                .orElseThrow(() -> new RuntimeException("Ordre de réparation non trouvé"));
        verifierAccesIntervention(ordreReparation, technicien);

        boolean isCustom = Boolean.TRUE.equals(request.isCustom()) || request.pieceId() == null;
        PieceDetache piece = null;

        if (isCustom) {
            if (request.designationPds() == null || request.designationPds().trim().isEmpty()) {
                throw new RuntimeException("La désignation de la pièce spéciale (PDS) est obligatoire");
            }
        } else {
            piece = pieceDetacheRepository.findById(request.pieceId())
                    .orElseThrow(() -> new RuntimeException("Pièce non trouvée"));
        }

        // Le technicien ne fixe jamais le prix : forcé à 0, ajusté ensuite par le chef
        // d'atelier via l'écran gestion existant (ordres-reparation.component.ts).
        ordreReparation.getLignesOrdreReparationPieces().add(LigneOrdreReparationPiece.builder()
                .ordreReparation(ordreReparation)
                .piece(piece)
                .isCustom(isCustom)
                .designationPds(isCustom ? request.designationPds().trim() : null)
                .quantite(request.quantite() != null ? request.quantite() : 1)
                .prix(0)
                .build());
        ordreReparationRepository.save(ordreReparation);
    }

    @Override
    @Transactional
    public void supprimerPiece(Technicien technicien, Long ordreReparationId, Long pieceLigneId) {
        OrdreReparation ordreReparation = ordreReparationRepository.findById(ordreReparationId)
                .orElseThrow(() -> new RuntimeException("Ordre de réparation non trouvé"));
        verifierAccesIntervention(ordreReparation, technicien);
        ordreReparation.getLignesOrdreReparationPieces().removeIf(l -> l.getId() != null && l.getId().equals(pieceLigneId));
        ordreReparationRepository.save(ordreReparation);
    }

    @Override
    @Transactional
    public void proposerMainDoeuvre(Technicien technicien, Long ordreReparationId, TechnicienLigneMainDoeuvreRequest request) {
        OrdreReparation ordreReparation = ordreReparationRepository.findById(ordreReparationId)
                .orElseThrow(() -> new RuntimeException("Ordre de réparation non trouvé"));
        verifierAccesIntervention(ordreReparation, technicien);
        if (request.mainDoeuvreId() == null) {
            throw new RuntimeException("L'ID de la main d'œuvre est obligatoire");
        }
        MainDoeuvre md = mainDoeuvreRepository.findById(request.mainDoeuvreId())
                .orElseThrow(() -> new RuntimeException("Main d'œuvre non trouvée"));

        // Idem : pas de prix fixé par le technicien.
        ordreReparation.getLignesOrdreReparationMainDoeuvres().add(LigneOrdreReparationMainDoeuvre.builder()
                .ordreReparation(ordreReparation)
                .mainDoeuvre(md)
                .nbreHeure(request.nbreHeure() != null ? request.nbreHeure() : 0)
                .prix(0)
                .build());
        ordreReparationRepository.save(ordreReparation);
    }

    @Override
    @Transactional
    public void supprimerMainDoeuvre(Technicien technicien, Long ordreReparationId, Long moLigneId) {
        OrdreReparation ordreReparation = ordreReparationRepository.findById(ordreReparationId)
                .orElseThrow(() -> new RuntimeException("Ordre de réparation non trouvé"));
        verifierAccesIntervention(ordreReparation, technicien);
        ordreReparation.getLignesOrdreReparationMainDoeuvres().removeIf(l -> l.getId() != null && l.getId().equals(moLigneId));
        ordreReparationRepository.save(ordreReparation);
    }

    @Override
    @Transactional(readOnly = true)
    public void verifierAccesIntervention(Technicien technicien, Long ordreReparationId) {
        OrdreReparation ordreReparation = ordreReparationRepository.findById(ordreReparationId)
                .orElseThrow(() -> new RuntimeException("Ordre de réparation non trouvé"));
        verifierAccesIntervention(ordreReparation, technicien);
    }

    private void verifierAccesIntervention(OrdreReparation ordreReparation, Technicien technicien) {
        verifierTechnicienAssigne(ordreReparation, technicien);
        
        StatutOrdreReparation statut = ordreReparation.getStatut();
        if (statut != StatutOrdreReparation.DIAGNOSTIC) {
            throw new AccessDeniedException("Vous ne pouvez intervenir sur cet ordre de réparation que lorsqu'il est en phase de diagnostic.");
        }
    }

    /**
     * Vérifie explicitement que le technicien connecté fait partie des techniciens assignés à
     * l'ordre (pool diagnostic OU pool réparation) — contrôle nouveau, en plus du filtre
     * garage, requis avant toute lecture/écriture sur un endpoint du portail technicien.
     */
    private void verifierTechnicienAssigne(OrdreReparation ordreReparation, Technicien technicien) {
        boolean assigne = (ordreReparation.getDiagnostic() != null && ordreReparation.getDiagnostic().getTechnicien() != null
                && ordreReparation.getDiagnostic().getTechnicien().getId().equals(technicien.getId()))
                || (ordreReparation.getTechniciensReparation() != null && ordreReparation.getTechniciensReparation().stream()
                .anyMatch(t -> t.getId().equals(technicien.getId())));
        if (!assigne) {
            Diagnostic diag = diagnosticRepository.findByOrdreReparationId(ordreReparation.getId()).orElse(null);
            if (diag != null && diag.getTechnicien() != null && diag.getTechnicien().getId().equals(technicien.getId())) {
                return;
            }
            throw new AccessDeniedException("Vous n'êtes pas assigné à cet ordre de réparation");
        }
    }

    private PieceJointeDiagnosticResponse toPieceJointeResponse(PieceJointeDiagnostic p) {
        String techNom = null;
        if (p.getTechnicien() != null) {
            String prenom = p.getTechnicien().getFirstName() != null ? p.getTechnicien().getFirstName() : "";
            String nom = p.getTechnicien().getLastName() != null ? p.getTechnicien().getLastName() : "";
            techNom = (prenom + " " + nom).trim();
            if (techNom.isEmpty()) techNom = p.getTechnicien().getUsername();
        }
        Long orId = (p.getDiagnostic() != null && p.getDiagnostic().getOrdreReparation() != null)
                ? p.getDiagnostic().getOrdreReparation().getId()
                : null;
        return PieceJointeDiagnosticResponse.builder()
                .id(p.getId())
                .ordreReparationId(orId)
                .url(p.getUrl())
                .type(p.getType())
                .remarque(p.getRemarque())
                .technicienNom(techNom)
                .createdAt(p.getCreatedAt())
                .build();
    }
}
