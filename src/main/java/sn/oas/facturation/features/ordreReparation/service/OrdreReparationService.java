package sn.oas.facturation.features.ordreReparation.service;

import sn.oas.facturation.features.ordreReparation.data.entity.OrdreReparation;
import sn.oas.facturation.features.ordreReparation.data.enums.StatutOrdreReparation;
import sn.oas.facturation.features.ordreReparation.dto.OrdreReparationRequest;

import org.springframework.data.domain.Page;
import java.util.List;
import java.util.Optional;

import sn.oas.facturation.features.diagnostic.data.enums.TypePieceJointe;
import sn.oas.facturation.features.diagnostic.dto.PieceJointeDiagnosticRequest;
import sn.oas.facturation.features.diagnostic.dto.PieceJointeDiagnosticResponse;
import sn.oas.facturation.features.diagnostic.dto.RemarqueDiagnosticResponse;
import sn.oas.facturation.features.ordreReparation.dto.OrdreReparationLightDTO;
import sn.oas.facturation.features.ordreReparation.dto.OrdreReparationResponseDTO;
import sn.oas.facturation.features.technicien.data.entity.Technicien;

public interface OrdreReparationService {
    OrdreReparation createOrdreReparation(OrdreReparationRequest request);
    Page<OrdreReparation> getAllOrdresReparation(int page, int size);
    List<OrdreReparationLightDTO> getAllOrdresReparation();
    Optional<OrdreReparation> getOrdreReparationById(Long id);
    OrdreReparationResponseDTO getOrdreReparationResponseById(Long id);
    sn.oas.facturation.features.ordreReparation.dto.responses.OrdreReparationSummaryDto getOrdreReparationSummary(Long id);
    OrdreReparation updateOrdreReparation(Long id, OrdreReparationRequest request);
    void deleteOrdreReparation(Long id);
    void assignTechnicien(Long ficheId, Long technicienId);
    void removeTechnicien(Long ficheId, Long technicienId);

    sn.oas.facturation.features.ordreReparation.dto.responses.StepReceptionResponseDto getStepReception(Long id);
    sn.oas.facturation.features.ordreReparation.dto.responses.StepDiagnosticResponseDto getStepDiagnostic(Long id);
    sn.oas.facturation.features.ordreReparation.dto.responses.StepPiecesMoResponseDto getStepPiecesMo(Long id);
    sn.oas.facturation.features.ordreReparation.dto.responses.StepProformaResponseDto getStepProforma(Long id);
    sn.oas.facturation.features.ordreReparation.dto.responses.StepApprovisionnementResponseDto getStepApprovisionnement(Long id);
    sn.oas.facturation.features.ordreReparation.dto.responses.StepBonSortieResponseDto getStepBonSortie(Long id);
    sn.oas.facturation.features.ordreReparation.dto.responses.StepAssignationResponseDto getStepAssignation(Long id);
    sn.oas.facturation.features.ordreReparation.dto.responses.StepReparationResponseDto getStepReparation(Long id);
    sn.oas.facturation.features.ordreReparation.dto.responses.StepPaiementResponseDto getStepPaiement(Long id);
    sn.oas.facturation.features.ordreReparation.dto.responses.StepPretALivrerResponseDto getStepPretALivrer(Long id);
    sn.oas.facturation.features.ordreReparation.dto.responses.StepLivraisonResponseDto getStepLivraison(Long id);

    sn.oas.facturation.features.ordreReparation.dto.steps.StepReceptionDto updateStepReception(Long id, sn.oas.facturation.features.ordreReparation.dto.steps.StepReceptionDto dto);
    sn.oas.facturation.features.ordreReparation.dto.steps.StepDiagnosticDto updateStepDiagnostic(Long id, sn.oas.facturation.features.ordreReparation.dto.steps.StepDiagnosticDto dto);
    sn.oas.facturation.features.ordreReparation.dto.steps.StepPiecesMoDto updateStepPiecesMo(Long id, sn.oas.facturation.features.ordreReparation.dto.steps.StepPiecesMoDto dto);
    sn.oas.facturation.features.ordreReparation.dto.steps.StepProformaDto updateStepProforma(Long id, sn.oas.facturation.features.ordreReparation.dto.steps.StepProformaDto dto);
    sn.oas.facturation.features.ordreReparation.dto.steps.StepApprovisionnementDto updateStepApprovisionnement(Long id, sn.oas.facturation.features.ordreReparation.dto.steps.StepApprovisionnementDto dto);
    sn.oas.facturation.features.ordreReparation.dto.steps.StepBonSortieDto updateStepBonSortie(Long id, sn.oas.facturation.features.ordreReparation.dto.steps.StepBonSortieDto dto);
    sn.oas.facturation.features.ordreReparation.dto.steps.StepAssignationDto updateStepAssignation(Long id, sn.oas.facturation.features.ordreReparation.dto.steps.StepAssignationDto dto);
    sn.oas.facturation.features.ordreReparation.dto.steps.StepReparationDto updateStepReparation(Long id, sn.oas.facturation.features.ordreReparation.dto.steps.StepReparationDto dto);
    sn.oas.facturation.features.ordreReparation.dto.steps.StepPaiementDto updateStepPaiement(Long id, sn.oas.facturation.features.ordreReparation.dto.steps.StepPaiementDto dto);
    sn.oas.facturation.features.ordreReparation.dto.steps.StepPretALivrerDto updateStepPretALivrer(Long id, sn.oas.facturation.features.ordreReparation.dto.steps.StepPretALivrerDto dto);
    sn.oas.facturation.features.ordreReparation.dto.steps.StepLivraisonDto updateStepLivraison(Long id, sn.oas.facturation.features.ordreReparation.dto.steps.StepLivraisonDto dto);

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
