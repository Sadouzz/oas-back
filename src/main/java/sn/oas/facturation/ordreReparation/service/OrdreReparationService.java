package sn.oas.facturation.ordreReparation.service;

import sn.oas.facturation.ordreReparation.data.entity.OrdreReparation;
import sn.oas.facturation.ordreReparation.dto.OrdreReparationRequest;

import java.util.List;
import java.util.Optional;

import sn.oas.facturation.ordreReparation.dto.OrdreReparationLightDTO;
import sn.oas.facturation.ordreReparation.dto.PieceJointeDiagnosticRequest;
import sn.oas.facturation.ordreReparation.dto.PieceJointeDiagnosticResponse;
import sn.oas.facturation.ordreReparation.data.enums.TypePieceJointe;

public interface OrdreReparationService {
    OrdreReparation createOrdreReparation(OrdreReparationRequest request);
    List<OrdreReparationLightDTO> getAllOrdresReparation();
    Optional<OrdreReparation> getOrdreReparationById(Long id);
    OrdreReparation updateOrdreReparation(Long id, OrdreReparationRequest request);
    void deleteOrdreReparation(Long id);
    void assignMecanicien(Long ficheId, Long mecanicienId);
    void removeMecanicien(Long ficheId, Long mecanicienId);

    void assignMecanicienReparation(Long ficheId, Long mecanicienId);
    void removeMecanicienReparation(Long ficheId, Long mecanicienId);
    OrdreReparation updateStatut(Long id, String statut);
    boolean existsByVehiculeIdAndStatutNotIn(Long vehiculeId, List<sn.oas.facturation.ordreReparation.data.enums.StatutOrdreReparation> statuts);

    // Pièces jointes de diagnostic
    List<PieceJointeDiagnosticResponse> getPiecesJointesDiagnostic(Long ordreReparationId, TypePieceJointe type);
    PieceJointeDiagnosticResponse addPieceJointeDiagnostic(Long ordreReparationId, PieceJointeDiagnosticRequest request);
    void deletePieceJointeDiagnostic(Long ordreReparationId, Long pieceJointeId);

    // Lien Fiche Atelier → Ordre de réparation
    OrdreReparation createFromFicheAtelier(Long ficheAtelierId);
    boolean existsByFicheAtelierId(Long ficheAtelierId);
}
