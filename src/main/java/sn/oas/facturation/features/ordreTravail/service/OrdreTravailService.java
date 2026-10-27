package sn.oas.facturation.features.ordreTravail.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import sn.oas.facturation.features.ordreTravail.data.entity.OrdreTravail;
import sn.oas.facturation.features.ordreTravail.data.enums.StatutOrdreTravail;
import sn.oas.facturation.features.ordreTravail.data.enums.TypeDocumentOt;
import sn.oas.facturation.features.ordreTravail.dto.DocumentOrdreTravailRequestDto;
import sn.oas.facturation.features.ordreTravail.dto.OrdreTravailListResponse;
import sn.oas.facturation.features.ordreTravail.dto.OrdreTravailRequest;
import sn.oas.facturation.features.ordreTravail.dto.OrdreTravailResponseDto;
import sn.oas.facturation.features.ordreTravail.dto.responses.*;
import sn.oas.facturation.features.ordreTravail.dto.steps.*;

import java.util.List;
import java.util.Optional;

public interface OrdreTravailService {

    OrdreTravailResponseDto creer(OrdreTravailRequest request);

    OrdreTravailResponseDto creerDepuisOrdreReparation(Long ordreReparationId);

    OrdreTravailResponseDto creerDepuisProforma(Long proformaId);

    OrdreTravailResponseDto modifier(Long id, OrdreTravailRequest request);

    Optional<OrdreTravailResponseDto> getById(Long id);

    Optional<OrdreTravail> getEntityById(Long id);

    Optional<OrdreTravailResponseDto> getByOrdreReparationId(Long ordreReparationId);

    Optional<OrdreTravailResponseDto> getByProformaId(Long proformaId);

    OrdreTravailSummaryDto getSummary(Long id);

    OrdreTravailSummaryDto getSummaryByOrdreReparationId(Long ordreReparationId);

    Page<OrdreTravailListResponse> lister(String search, Pageable pageable);

    void supprimer(Long id);

    OrdreTravailResponseDto changerStatut(Long id, StatutOrdreTravail statut);

    OrdreTravailResponseDto assignerTechniciens(Long id, List<Long> technicienIds);

    OrdreTravailResponseDto retirerTechnicien(Long id, Long technicienId);

    // --- Steps Getters ---
    StepRecapitulatifResponseDto getStepRecapitulatif(Long id);

    StepBonSortieResponseDto getStepBonSortie(Long id);

    StepAssignationResponseDto getStepAssignation(Long id);

    StepReparationResponseDto getStepReparation(Long id);

    // --- Steps Updaters ---
    StepAssignationDto updateStepAssignation(Long id, StepAssignationDto dto);

    StepReparationDto updateStepReparation(Long id, StepReparationDto dto);

    // --- Documents / Pièces jointes (Step 4 Réparation & OT) ---
    DocumentOrdreTravailResponseDto addDocument(Long ordreTravailId, DocumentOrdreTravailRequestDto request);

    DocumentOrdreTravailResponseDto uploadDocument(Long ordreTravailId, MultipartFile file, TypeDocumentOt type, String remarque);

    List<DocumentOrdreTravailResponseDto> getDocuments(Long ordreTravailId, TypeDocumentOt type, Integer etape);

    void deleteDocument(Long documentId);
}
