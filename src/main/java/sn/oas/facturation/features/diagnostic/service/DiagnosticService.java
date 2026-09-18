package sn.oas.facturation.features.diagnostic.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sn.oas.facturation.features.diagnostic.data.enums.StatutDiagnostic;
import sn.oas.facturation.features.diagnostic.dto.DiagnosticListResponse;
import sn.oas.facturation.features.diagnostic.dto.DiagnosticRequest;
import sn.oas.facturation.features.diagnostic.dto.DiagnosticResponse;
import sn.oas.facturation.features.diagnostic.dto.PieceJointeDiagnosticRequest;
import sn.oas.facturation.features.diagnostic.dto.PieceJointeDiagnosticResponse;
import sn.oas.facturation.features.diagnostic.dto.RemarqueDiagnosticResponse;
import sn.oas.facturation.features.ordreReparation.dto.steps.DiagnosticStepDto;

public interface DiagnosticService {

    DiagnosticResponse create(DiagnosticRequest request);

    DiagnosticResponse update(Long id, DiagnosticRequest request);

    DiagnosticResponse getById(Long id);

    DiagnosticResponse getByOrdreReparationId(Long ordreReparationId);

    Page<DiagnosticListResponse> getAll(Pageable pageable, String search, StatutDiagnostic statut);

    DiagnosticResponse updateStatut(Long id, StatutDiagnostic statut);

    void delete(Long id);

    DiagnosticResponse saveFromStep(DiagnosticStepDto stepDto);

    PieceJointeDiagnosticResponse addPieceJointe(Long diagnosticId, PieceJointeDiagnosticRequest request);

    void deletePieceJointe(Long pieceJointeId);

    RemarqueDiagnosticResponse addRemarque(Long diagnosticId, String contenu, Long technicienId);

    void deleteRemarque(Long remarqueId);
}
