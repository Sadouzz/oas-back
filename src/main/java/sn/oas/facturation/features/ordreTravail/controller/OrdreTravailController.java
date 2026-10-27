package sn.oas.facturation.features.ordreTravail.controller;

import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import sn.oas.facturation.features.ordreTravail.data.enums.TypeDocumentOt;
import sn.oas.facturation.features.ordreTravail.dto.DocumentOrdreTravailRequestDto;
import sn.oas.facturation.features.ordreTravail.dto.responses.DocumentOrdreTravailResponseDto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import sn.oas.facturation.features.ordreTravail.data.enums.StatutOrdreTravail;
import sn.oas.facturation.features.ordreTravail.dto.OrdreTravailListResponse;
import sn.oas.facturation.features.ordreTravail.dto.OrdreTravailRequest;
import sn.oas.facturation.features.ordreTravail.dto.OrdreTravailResponseDto;
import sn.oas.facturation.features.ordreTravail.dto.responses.*;
import sn.oas.facturation.features.ordreTravail.dto.steps.*;
import sn.oas.facturation.features.ordreTravail.service.OrdreTravailService;

import java.util.List;

import sn.oas.facturation.shared.exception.ResourceNotFoundException;

@Tag(name = "Ordres de Travail", description = "Gestion des ordres de travail (OT)")
@RestController
@RequestMapping("/api/ordres-travail")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('AGENT', 'SUPER_AGENT', 'MASTER', 'CHEF_ATELIER', 'AGENT_MAGASIN')")
public class OrdreTravailController {

    private final OrdreTravailService ordreTravailService;

    @Operation(summary = "Créer un ordre de travail")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Ordre de travail créé avec succès"),
            @ApiResponse(responseCode = "400", description = "Données invalides")
    })
    @PostMapping
    public ResponseEntity<OrdreTravailResponseDto> creer(@RequestBody OrdreTravailRequest request) {
        return new ResponseEntity<>(ordreTravailService.creer(request), HttpStatus.CREATED);
    }

    @Operation(summary = "Créer un ordre de travail depuis un ordre de réparation (Step Proforma)")
    @PostMapping("/depuis-ordre-reparation/{ordreReparationId}")
    public ResponseEntity<OrdreTravailResponseDto> creerDepuisOrdreReparation(@PathVariable Long ordreReparationId) {
        return new ResponseEntity<>(ordreTravailService.creerDepuisOrdreReparation(ordreReparationId), HttpStatus.CREATED);
    }

    @Operation(summary = "Créer un ordre de travail depuis un proforma (ayant un ordre de réparation)")
    @PostMapping("/depuis-proforma/{proformaId}")
    public ResponseEntity<OrdreTravailResponseDto> creerDepuisProforma(@PathVariable Long proformaId) {
        return new ResponseEntity<>(ordreTravailService.creerDepuisProforma(proformaId), HttpStatus.CREATED);
    }

    @Operation(summary = "Obtenir l'ordre de travail associé à un ordre de réparation")
    @GetMapping("/ordre-reparation/{ordreReparationId}")
    public ResponseEntity<OrdreTravailResponseDto> getByOrdreReparationId(@PathVariable Long ordreReparationId) {
        return ordreTravailService.getByOrdreReparationId(ordreReparationId)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Aucun ordre de travail associé à l'ordre de réparation : " + ordreReparationId));
    }

    @Operation(summary = "Obtenir l'ordre de travail associé à un proforma")
    @GetMapping("/proforma/{proformaId}")
    public ResponseEntity<OrdreTravailResponseDto> getByProformaId(@PathVariable Long proformaId) {
        return ordreTravailService.getByProformaId(proformaId)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Aucun ordre de travail associé au proforma : " + proformaId));
    }

    @Operation(summary = "Obtenir un ordre de travail par son id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ordre de travail trouvé"),
            @ApiResponse(responseCode = "404", description = "Ordre de travail introuvable")
    })
    @GetMapping("/{id}")
    public ResponseEntity<OrdreTravailResponseDto> getById(@PathVariable Long id) {
        return ordreTravailService.getById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Ordre de travail introuvable avec l'id : " + id));
    }

    @Operation(summary = "Récupérer le résumé minimal d'un ordre de travail (pour le header)")
    @GetMapping("/{id}/summary")
    public ResponseEntity<OrdreTravailSummaryDto> getSummary(@PathVariable Long id) {
        return ResponseEntity.ok(ordreTravailService.getSummary(id));
    }

    @Operation(summary = "Récupérer le résumé minimal d'un ordre de travail associé à un ordre de réparation")
    @GetMapping("/ordre-reparation/{ordreReparationId}/summary")
    public ResponseEntity<OrdreTravailSummaryDto> getSummaryByOrdreReparationId(@PathVariable Long ordreReparationId) {
        return ResponseEntity.ok(ordreTravailService.getSummaryByOrdreReparationId(ordreReparationId));
    }

    @Operation(summary = "Lister les ordres de travail avec pagination et recherche")
    @GetMapping
    public ResponseEntity<Page<OrdreTravailListResponse>> lister(
            @RequestParam(required = false) String search,
            @PageableDefault(sort = "updatedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(ordreTravailService.lister(search, pageable));
    }

    @Operation(summary = "Modifier un ordre de travail")
    @PutMapping("/{id}")
    public ResponseEntity<OrdreTravailResponseDto> modifier(
            @PathVariable Long id,
            @RequestBody OrdreTravailRequest request) {
        return ResponseEntity.ok(ordreTravailService.modifier(id, request));
    }

    @Operation(summary = "Supprimer un ordre de travail")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        ordreTravailService.supprimer(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Changer le statut d'un ordre de travail (avancement des étapes)")
    @PatchMapping("/{id}/statut")
    public ResponseEntity<OrdreTravailResponseDto> changerStatut(
            @PathVariable Long id,
            @RequestParam StatutOrdreTravail statut) {
        return ResponseEntity.ok(ordreTravailService.changerStatut(id, statut));
    }

    @Operation(summary = "Assigner des techniciens à un ordre de travail")
    @PostMapping("/{id}/techniciens")
    public ResponseEntity<OrdreTravailResponseDto> assignerTechniciens(
            @PathVariable Long id,
            @RequestBody List<Long> technicienIds) {
        return ResponseEntity.ok(ordreTravailService.assignerTechniciens(id, technicienIds));
    }

    @Operation(summary = "Retirer un technicien d'un ordre de travail")
    @DeleteMapping("/{id}/techniciens/{technicienId}")
    public ResponseEntity<OrdreTravailResponseDto> retirerTechnicien(
            @PathVariable Long id,
            @PathVariable Long technicienId) {
        return ResponseEntity.ok(ordreTravailService.retirerTechnicien(id, technicienId));
    }

    // ==================== STEPS ENDPOINTS ====================

    @Operation(summary = "Obtenir l'étape Récapitulatif (Step 1)")
    @GetMapping("/{id}/step-recapitulatif")
    public ResponseEntity<StepRecapitulatifResponseDto> getStepRecapitulatif(@PathVariable Long id) {
        return ResponseEntity.ok(ordreTravailService.getStepRecapitulatif(id));
    }

    @Operation(summary = "Obtenir l'étape Bon de Sortie (Step 2)")
    @GetMapping("/{id}/step-bon-sortie")
    public ResponseEntity<StepBonSortieResponseDto> getStepBonSortie(@PathVariable Long id) {
        return ResponseEntity.ok(ordreTravailService.getStepBonSortie(id));
    }

    @Operation(summary = "Obtenir l'étape Assignation Techniciens (Step 3)")
    @GetMapping("/{id}/step-assignation")
    public ResponseEntity<StepAssignationResponseDto> getStepAssignation(@PathVariable Long id) {
        return ResponseEntity.ok(ordreTravailService.getStepAssignation(id));
    }

    @Operation(summary = "Mettre à jour l'étape Assignation Techniciens (Step 3)")
    @PutMapping("/{id}/step-assignation")
    public ResponseEntity<StepAssignationDto> updateStepAssignation(
            @PathVariable Long id,
            @RequestBody StepAssignationDto dto) {
        return ResponseEntity.ok(ordreTravailService.updateStepAssignation(id, dto));
    }

    @Operation(summary = "Obtenir l'étape Réparation (Step 4)")
    @GetMapping("/{id}/step-reparation")
    public ResponseEntity<StepReparationResponseDto> getStepReparation(@PathVariable Long id) {
        return ResponseEntity.ok(ordreTravailService.getStepReparation(id));
    }

    @Operation(summary = "Mettre à jour l'étape Réparation (Step 4)")
    @PutMapping("/{id}/step-reparation")
    public ResponseEntity<StepReparationDto> updateStepReparation(
            @PathVariable Long id,
            @RequestBody StepReparationDto dto) {
        return ResponseEntity.ok(ordreTravailService.updateStepReparation(id, dto));
    }

    // ==================== DOCUMENTS & PHOTOS (STEP 4 & OT) ====================

    @Operation(summary = "Lister les documents / photos d'un ordre de travail")
    @GetMapping("/{id}/documents")
    public ResponseEntity<List<DocumentOrdreTravailResponseDto>> getDocuments(
            @PathVariable Long id,
            @RequestParam(required = false) TypeDocumentOt type,
            @RequestParam(required = false) Integer etape) {
        return ResponseEntity.ok(ordreTravailService.getDocuments(id, type, etape));
    }

    @Operation(summary = "Lister les documents / photos de l'étape Réparation (Step 4)")
    @GetMapping("/{id}/step-reparation/documents")
    public ResponseEntity<List<DocumentOrdreTravailResponseDto>> getDocumentsStepReparation(
            @PathVariable Long id,
            @RequestParam(required = false) TypeDocumentOt type) {
        return ResponseEntity.ok(ordreTravailService.getDocuments(id, type, 4));
    }

    @Operation(summary = "Ajouter un document / photo (métadonnées + URL Cloudinary ou externe)")
    @PostMapping("/{id}/documents")
    public ResponseEntity<DocumentOrdreTravailResponseDto> addDocument(
            @PathVariable Long id,
            @RequestBody DocumentOrdreTravailRequestDto request) {
        return new ResponseEntity<>(ordreTravailService.addDocument(id, request), HttpStatus.CREATED);
    }

    @Operation(summary = "Ajouter un document / photo pour l'étape Réparation (Step 4)")
    @PostMapping("/{id}/step-reparation/documents")
    public ResponseEntity<DocumentOrdreTravailResponseDto> addDocumentStepReparation(
            @PathVariable Long id,
            @RequestBody DocumentOrdreTravailRequestDto request) {
        if (request != null && request.getEtape() == null) {
            request.setEtape(4);
        }
        return new ResponseEntity<>(ordreTravailService.addDocument(id, request), HttpStatus.CREATED);
    }

    @Operation(summary = "Uploader directement un fichier (photo, PDF, doc) pour un ordre de travail")
    @PostMapping(value = "/{id}/documents/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentOrdreTravailResponseDto> uploadDocument(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) TypeDocumentOt type,
            @RequestParam(required = false) String remarque) {
        return new ResponseEntity<>(ordreTravailService.uploadDocument(id, file, type, remarque), HttpStatus.CREATED);
    }

    @Operation(summary = "Uploader directement un fichier (photo, PDF, doc) pour l'étape Réparation (Step 4)")
    @PostMapping(value = "/{id}/step-reparation/documents/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentOrdreTravailResponseDto> uploadDocumentStepReparation(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) TypeDocumentOt type,
            @RequestParam(required = false) String remarque) {
        return new ResponseEntity<>(ordreTravailService.uploadDocument(id, file, type, remarque), HttpStatus.CREATED);
    }

    @Operation(summary = "Supprimer un document par son identifiant")
    @DeleteMapping("/documents/{documentId}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long documentId) {
        ordreTravailService.deleteDocument(documentId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Supprimer un document d'un ordre de travail")
    @DeleteMapping("/{id}/documents/{documentId}")
    public ResponseEntity<Void> deleteDocumentFromOt(
            @PathVariable Long id,
            @PathVariable Long documentId) {
        ordreTravailService.deleteDocument(documentId);
        return ResponseEntity.noContent().build();
    }
}
