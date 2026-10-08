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
import sn.oas.facturation.features.ordreReparation.dto.responses.OrdreReparationSummaryDto;
import sn.oas.facturation.features.ordreReparation.dto.responses.StepApprovisionnementResponseDto;
import sn.oas.facturation.features.ordreReparation.dto.responses.StepAssignationResponseDto;
import sn.oas.facturation.features.ordreReparation.dto.responses.StepBonSortieResponseDto;
import sn.oas.facturation.features.ordreReparation.dto.responses.StepDiagnosticResponseDto;
import sn.oas.facturation.features.ordreReparation.dto.responses.StepLivraisonResponseDto;
import sn.oas.facturation.features.ordreReparation.dto.responses.StepPaiementResponseDto;
import sn.oas.facturation.features.ordreReparation.dto.responses.StepPiecesMoResponseDto;
import sn.oas.facturation.features.ordreReparation.dto.responses.StepPretALivrerResponseDto;
import sn.oas.facturation.features.ordreReparation.dto.responses.StepProformaResponseDto;
import sn.oas.facturation.features.ordreReparation.dto.responses.StepReceptionResponseDto;
import sn.oas.facturation.features.ordreReparation.dto.responses.StepReparationResponseDto;
import sn.oas.facturation.features.ordreReparation.dto.steps.StepApprovisionnementDto;
import sn.oas.facturation.features.ordreReparation.dto.steps.StepAssignationDto;
import sn.oas.facturation.features.ordreReparation.dto.steps.StepBonSortieDto;
import sn.oas.facturation.features.ordreReparation.dto.steps.StepDiagnosticDto;
import sn.oas.facturation.features.ordreReparation.dto.steps.StepLivraisonDto;
import sn.oas.facturation.features.ordreReparation.dto.steps.StepPaiementDto;
import sn.oas.facturation.features.ordreReparation.dto.steps.StepPiecesMoDto;
import sn.oas.facturation.features.ordreReparation.dto.steps.StepPretALivrerDto;
import sn.oas.facturation.features.ordreReparation.dto.steps.StepProformaDto;
import sn.oas.facturation.features.ordreReparation.dto.steps.StepReceptionDto;
import sn.oas.facturation.features.ordreReparation.dto.steps.StepReparationDto;
import sn.oas.facturation.features.technicien.data.entity.Technicien;

public interface OrdreReparationService {
    OrdreReparation createOrdreReparation(OrdreReparationRequest request);
    Page<OrdreReparation> getAllOrdresReparation(int page, int size);
    List<OrdreReparationLightDTO> getAllOrdresReparation();
    Optional<OrdreReparation> getOrdreReparationById(Long id);
    OrdreReparationResponseDTO getOrdreReparationResponseById(Long id);
    OrdreReparationSummaryDto getOrdreReparationSummary(Long id);
    OrdreReparation updateOrdreReparation(Long id, OrdreReparationRequest request);
    void deleteOrdreReparation(Long id);
    void assignTechnicien(Long ficheId, Long technicienId);
    void removeTechnicien(Long ficheId, Long technicienId);

    StepReceptionResponseDto getStepReception(Long id);
    StepDiagnosticResponseDto getStepDiagnostic(Long id);
    StepPiecesMoResponseDto getStepPiecesMo(Long id);
    StepProformaResponseDto getStepProforma(Long id);
    StepApprovisionnementResponseDto getStepApprovisionnement(Long id);
    StepBonSortieResponseDto getStepBonSortie(Long id);
    StepAssignationResponseDto getStepAssignation(Long id);
    StepReparationResponseDto getStepReparation(Long id);
    StepPaiementResponseDto getStepPaiement(Long id);
    StepPretALivrerResponseDto getStepPretALivrer(Long id);
    StepLivraisonResponseDto getStepLivraison(Long id);

    StepReceptionDto updateStepReception(Long id, StepReceptionDto dto);
    StepDiagnosticDto updateStepDiagnostic(Long id, StepDiagnosticDto dto);
    StepPiecesMoDto updateStepPiecesMo(Long id, StepPiecesMoDto dto);
    StepProformaDto updateStepProforma(Long id, StepProformaDto dto);
    StepApprovisionnementDto updateStepApprovisionnement(Long id, StepApprovisionnementDto dto);
    StepBonSortieDto updateStepBonSortie(Long id, StepBonSortieDto dto);
    StepAssignationDto updateStepAssignation(Long id, StepAssignationDto dto);
    StepReparationDto updateStepReparation(Long id, StepReparationDto dto);
    StepPaiementDto updateStepPaiement(Long id, StepPaiementDto dto);
    StepPretALivrerDto updateStepPretALivrer(Long id, StepPretALivrerDto dto);
    StepLivraisonDto updateStepLivraison(Long id, StepLivraisonDto dto);

    void assignTechnicienReparation(Long ficheId, Long technicienId);
    void removeTechnicienReparation(Long ficheId, Long technicienId);
    OrdreReparation updateStatut(Long id, String statut);
    OrdreReparation restituerVehicule(Long id, String signature, Integer garantieMois);
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
