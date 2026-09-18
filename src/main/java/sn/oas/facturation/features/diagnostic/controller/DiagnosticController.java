package sn.oas.facturation.features.diagnostic.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.oas.facturation.features.diagnostic.data.enums.StatutDiagnostic;
import sn.oas.facturation.features.diagnostic.dto.DiagnosticListResponse;
import sn.oas.facturation.features.diagnostic.dto.DiagnosticRequest;
import sn.oas.facturation.features.diagnostic.dto.DiagnosticResponse;
import sn.oas.facturation.features.diagnostic.dto.PieceJointeDiagnosticRequest;
import sn.oas.facturation.features.diagnostic.dto.PieceJointeDiagnosticResponse;
import sn.oas.facturation.features.diagnostic.dto.RemarqueDiagnosticResponse;
import sn.oas.facturation.features.diagnostic.service.DiagnosticService;
import sn.oas.facturation.features.ordreReparation.dto.steps.DiagnosticStepDto;

import java.util.Map;

@Tag(name = "Diagnostics", description = "API pour la gestion des diagnostics techniques des ordres de réparation")
@RestController
@RequestMapping("/api/diagnostics")
@RequiredArgsConstructor
public class DiagnosticController {

    private final DiagnosticService diagnosticService;

    @GetMapping
    @Operation(summary = "Lister les diagnostics avec pagination, recherche et filtre par statut")
    public ResponseEntity<Page<DiagnosticListResponse>> getAllDiagnostics(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) StatutDiagnostic statut) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(diagnosticService.getAll(pageRequest, search, statut));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer un diagnostic par son identifiant")
    public ResponseEntity<?> getDiagnosticById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(diagnosticService.getById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/ordre-reparation/{ordreReparationId}")
    @Operation(summary = "Récupérer le diagnostic associé à un ordre de réparation")
    public ResponseEntity<?> getDiagnosticByOrdreReparationId(@PathVariable Long ordreReparationId) {
        try {
            return ResponseEntity.ok(diagnosticService.getByOrdreReparationId(ordreReparationId));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping
    @Operation(summary = "Créer un nouveau diagnostic")
    public ResponseEntity<?> createDiagnostic(@RequestBody DiagnosticRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(diagnosticService.create(request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour un diagnostic existant")
    public ResponseEntity<?> updateDiagnostic(@PathVariable Long id, @RequestBody DiagnosticRequest request) {
        try {
            return ResponseEntity.ok(diagnosticService.update(id, request));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PatchMapping("/{id}/statut")
    @Operation(summary = "Mettre à jour le statut d'un diagnostic")
    public ResponseEntity<?> updateStatut(@PathVariable Long id, @RequestParam StatutDiagnostic statut) {
        try {
            return ResponseEntity.ok(diagnosticService.updateStatut(id, statut));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un diagnostic")
    public ResponseEntity<?> deleteDiagnostic(@PathVariable Long id) {
        try {
            diagnosticService.delete(id);
            return ResponseEntity.ok(Map.of("message", "Diagnostic supprimé avec succès"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // ─── Intégration Stepper / Étape 2 ─────────────────────────────────────

    @PostMapping("/step")
    @Operation(summary = "Enregistrer ou mettre à jour le diagnostic depuis le formulaire de l'étape 2 (DiagnosticStepDto)")
    public ResponseEntity<?> saveFromStep(@RequestBody DiagnosticStepDto stepDto) {
        try {
            return ResponseEntity.ok(diagnosticService.saveFromStep(stepDto));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // ─── Pièces jointes ───────────────────────────────────────────────────

    @PostMapping("/{id}/pieces-jointes")
    @Operation(summary = "Ajouter une pièce jointe (photo ou PDF) à un diagnostic")
    public ResponseEntity<?> addPieceJointe(@PathVariable Long id, @RequestBody PieceJointeDiagnosticRequest request) {
        try {
            PieceJointeDiagnosticResponse pj = diagnosticService.addPieceJointe(id, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(pj);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/pieces-jointes/{pieceJointeId}")
    @Operation(summary = "Supprimer une pièce jointe de diagnostic")
    public ResponseEntity<?> deletePieceJointe(@PathVariable Long pieceJointeId) {
        try {
            diagnosticService.deletePieceJointe(pieceJointeId);
            return ResponseEntity.ok(Map.of("message", "Pièce jointe supprimée avec succès"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // ─── Remarques ───────────────────────────────────────────────────────

    @PostMapping("/{id}/remarques")
    @Operation(summary = "Ajouter une remarque à un diagnostic")
    public ResponseEntity<?> addRemarque(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        try {
            String contenu = (String) payload.get("contenu");
            Long technicienId = payload.get("technicienId") != null ? Long.valueOf(payload.get("technicienId").toString()) : null;
            RemarqueDiagnosticResponse r = diagnosticService.addRemarque(id, contenu, technicienId);
            return ResponseEntity.status(HttpStatus.CREATED).body(r);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/remarques/{remarqueId}")
    @Operation(summary = "Supprimer une remarque de diagnostic")
    public ResponseEntity<?> deleteRemarque(@PathVariable Long remarqueId) {
        try {
            diagnosticService.deleteRemarque(remarqueId);
            return ResponseEntity.ok(Map.of("message", "Remarque supprimée avec succès"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
