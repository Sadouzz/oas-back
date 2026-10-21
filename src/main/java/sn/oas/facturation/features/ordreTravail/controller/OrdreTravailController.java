package sn.oas.facturation.features.ordreTravail.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
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
}
