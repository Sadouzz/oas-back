package sn.oas.facturation.features.technicien.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.oas.facturation.features.auth.data.entity.Technicien;
import sn.oas.facturation.features.main_doeuvre.data.entity.MainDoeuvre;
import sn.oas.facturation.features.main_doeuvre.repository.MainDoeuvreRepository;
import sn.oas.facturation.features.ordreReparation.data.entity.LigneOrdreReparationMainDoeuvre;
import sn.oas.facturation.features.ordreReparation.data.entity.LigneOrdreReparationPiece;
import sn.oas.facturation.features.ordreReparation.data.entity.OrdreReparation;
import sn.oas.facturation.features.ordreReparation.data.entity.PieceJointeDiagnostic;
import sn.oas.facturation.features.ordreReparation.data.enums.TypePieceJointe;
import sn.oas.facturation.features.ordreReparation.dto.PieceJointeDiagnosticRequest;
import sn.oas.facturation.features.ordreReparation.dto.PieceJointeDiagnosticResponse;
import sn.oas.facturation.features.ordreReparation.repository.OrdreReparationRepository;
import sn.oas.facturation.features.ordreReparation.repository.PieceJointeDiagnosticRepository;
import sn.oas.facturation.features.piecedetache.data.entity.PDP;
import sn.oas.facturation.features.piecedetache.data.entity.PieceDetache;
import sn.oas.facturation.features.piecedetache.repository.PieceDetacheRepository;
import sn.oas.facturation.features.technicien.dto.PannesRequest;
import sn.oas.facturation.features.technicien.dto.TechnicienLigneMainDoeuvreRequest;
import sn.oas.facturation.features.technicien.dto.TechnicienLignePieceRequest;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TechnicienPortalServiceImpl implements TechnicienPortalService {

    private final OrdreReparationRepository ordreReparationRepository;
    private final PieceJointeDiagnosticRepository pieceJointeDiagnosticRepository;
    private final PieceDetacheRepository pieceDetacheRepository;
    private final MainDoeuvreRepository mainDoeuvreRepository;

    @Override
    @Transactional(readOnly = true)
    public List<OrdreReparation> getMesOrdresReparation(Technicien technicien) {
        return ordreReparationRepository.findByTechnicienAssigne(technicien.getId());
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
        verifierTechnicienAssigne(ordreReparation, technicien);
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
                .technicien(technicien)
                .build();

        return toPieceJointeResponse(pieceJointeDiagnosticRepository.save(pieceJointe));
    }

    @Override
    @Transactional
    public void deletePieceJointeDiagnostic(Technicien technicien, Long ordreReparationId, Long pieceJointeId) {
        OrdreReparation ordreReparation = ordreReparationRepository.findById(ordreReparationId)
                .orElseThrow(() -> new RuntimeException("Ordre de réparation non trouvé"));
        verifierTechnicienAssigne(ordreReparation, technicien);
        PieceJointeDiagnostic pieceJointe = pieceJointeDiagnosticRepository.findById(pieceJointeId)
                .orElseThrow(() -> new RuntimeException("Pièce jointe non trouvée"));
        if (pieceJointe.getOrdreReparation() == null || !pieceJointe.getOrdreReparation().getId().equals(ordreReparationId)) {
            throw new RuntimeException("Cette pièce jointe n'appartient pas à cet ordre de réparation");
        }
        pieceJointeDiagnosticRepository.delete(pieceJointe);
    }

    @Override
    @Transactional
    public OrdreReparation updatePannesDetectees(Technicien technicien, Long ordreReparationId, PannesRequest request) {
        OrdreReparation ordreReparation = ordreReparationRepository.findById(ordreReparationId)
                .orElseThrow(() -> new RuntimeException("Ordre de réparation non trouvé"));
        verifierTechnicienAssigne(ordreReparation, technicien);
        ordreReparation.setListeDefauts(request.listeDefauts());
        return ordreReparationRepository.save(ordreReparation);
    }

    @Override
    @Transactional
    public void proposerPiece(Technicien technicien, Long ordreReparationId, TechnicienLignePieceRequest request) {
        OrdreReparation ordreReparation = ordreReparationRepository.findById(ordreReparationId)
                .orElseThrow(() -> new RuntimeException("Ordre de réparation non trouvé"));
        verifierTechnicienAssigne(ordreReparation, technicien);
        if (request.pieceId() == null) {
            throw new RuntimeException("L'ID de la pièce est obligatoire");
        }
        PieceDetache piece = pieceDetacheRepository.findById(request.pieceId())
                .orElseThrow(() -> new RuntimeException("Pièce non trouvée"));
        PDP pdp = (PDP) org.hibernate.Hibernate.unproxy(piece);

        // Le technicien ne fixe jamais le prix : forcé à 0, ajusté ensuite par le chef
        // d'atelier via l'écran gestion existant (ordres-reparation.component.ts).
        ordreReparation.getLignesOrdreReparationPieces().add(LigneOrdreReparationPiece.builder()
                .ordreReparation(ordreReparation)
                .piece(pdp)
                .quantite(request.quantite() != null ? request.quantite() : 1)
                .prix(0)
                .build());
        ordreReparationRepository.save(ordreReparation);
    }

    @Override
    @Transactional
    public void proposerMainDoeuvre(Technicien technicien, Long ordreReparationId, TechnicienLigneMainDoeuvreRequest request) {
        OrdreReparation ordreReparation = ordreReparationRepository.findById(ordreReparationId)
                .orElseThrow(() -> new RuntimeException("Ordre de réparation non trouvé"));
        verifierTechnicienAssigne(ordreReparation, technicien);
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

    /**
     * Vérifie explicitement que le technicien connecté fait partie des techniciens assignés à
     * l'ordre (pool diagnostic OU pool réparation) — contrôle nouveau, en plus du filtre
     * garage, requis avant toute lecture/écriture sur un endpoint du portail technicien.
     */
    private void verifierTechnicienAssigne(OrdreReparation ordreReparation, Technicien technicien) {
        boolean assigne = (ordreReparation.getTechniciens() != null && ordreReparation.getTechniciens().stream()
                .anyMatch(t -> t.getId().equals(technicien.getId())))
                || (ordreReparation.getTechniciensReparation() != null && ordreReparation.getTechniciensReparation().stream()
                .anyMatch(t -> t.getId().equals(technicien.getId())));
        if (!assigne) {
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
}
