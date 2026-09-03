package sn.oas.facturation.features.ordreReparation.service;

import sn.oas.facturation.features.ordreReparation.data.entity.OrdreReparation;
import sn.oas.facturation.features.ordreReparation.data.enums.StatutOrdreReparation;
import sn.oas.facturation.features.ordreReparation.dto.OrdreReparationRequest;

import org.springframework.data.domain.Page;
import java.util.List;
import java.util.Optional;

import sn.oas.facturation.features.ordreReparation.dto.OrdreReparationLightDTO;
import sn.oas.facturation.features.ordreReparation.dto.PieceJointeDiagnosticRequest;
import sn.oas.facturation.features.ordreReparation.dto.PieceJointeDiagnosticResponse;
import sn.oas.facturation.features.ordreReparation.dto.RemarqueDiagnosticResponse;
import sn.oas.facturation.features.technicien.data.entity.Technicien;
import sn.oas.facturation.features.ordreReparation.data.enums.TypePieceJointe;

public interface OrdreReparationService {
    OrdreReparation createOrdreReparation(OrdreReparationRequest request);
    Page<OrdreReparationLightDTO> getAllOrdresReparation(int page, int size);
    List<OrdreReparationLightDTO> getAllOrdresReparation();
    Optional<OrdreReparation> getOrdreReparationById(Long id);
    OrdreReparation updateOrdreReparation(Long id, OrdreReparationRequest request);
    void deleteOrdreReparation(Long id);
    void assignTechnicien(Long ficheId, Long technicienId);
    void removeTechnicien(Long ficheId, Long technicienId);

    void assignTechnicienReparation(Long ficheId, Long technicienId);
    void removeTechnicienReparation(Long ficheId, Long technicienId);
    OrdreReparation updateStatut(Long id, String statut);
    boolean existsByVehiculeIdAndStatutNotIn(Long vehiculeId, List<StatutOrdreReparation> statuts);

    // Pièces jointes de diagnostic
    List<PieceJointeDiagnosticResponse> getPiecesJointesDiagnostic(Long ordreReparationId, TypePieceJointe type);
    PieceJointeDiagnosticResponse addPieceJointeDiagnostic(Long ordreReparationId, PieceJointeDiagnosticRequest request);
    void deletePieceJointeDiagnostic(Long ordreReparationId, Long pieceJointeId);

    // Remarques de diagnostic
    List<RemarqueDiagnosticResponse> getRemarquesDiagnostic(Long ordreReparationId);
    RemarqueDiagnosticResponse addRemarqueDiagnostic(Long ordreReparationId, Technicien technicien, String contenu);
    void deleteRemarqueDiagnostic(Long ordreReparationId, Long remarqueId);

    // Lien Fiche Atelier → Ordre de réparation
    OrdreReparation createFromFicheAtelier(Long ficheAtelierId);
    boolean existsByFicheAtelierId(Long ficheAtelierId);
}
