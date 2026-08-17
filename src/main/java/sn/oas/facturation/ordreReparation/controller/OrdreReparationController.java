package sn.oas.facturation.ordreReparation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.oas.facturation.ordreReparation.data.entity.OrdreReparation;
import sn.oas.facturation.ordreReparation.data.enums.TypePieceJointe;
import sn.oas.facturation.ordreReparation.dto.OrdreReparationRequest;
import sn.oas.facturation.ordreReparation.dto.OrdreReparationLightDTO;
import sn.oas.facturation.ordreReparation.dto.PieceJointeDiagnosticRequest;
import sn.oas.facturation.ordreReparation.dto.PieceJointeDiagnosticResponse;
import sn.oas.facturation.ordreReparation.service.OrdreReparationService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ordres-reparation")
@RequiredArgsConstructor
@Tag(name = "Fiches Atelier", description = "API pour la gestion des fiches atelier")
public class OrdreReparationController {

    private final OrdreReparationService ordreReparationService;

    @GetMapping
    @Operation(summary = "Lister toutes les fiches atelier")
    public ResponseEntity<List<OrdreReparationLightDTO>> getAllOrdresReparation() {
        return ResponseEntity.ok(ordreReparationService.getAllOrdresReparation());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer une fiche atelier par son ID")
    public ResponseEntity<?> getOrdreReparationById(@PathVariable Long id) {
        try {
            OrdreReparation ordreReparation = ordreReparationService.getOrdreReparationById(id)
                    .orElseThrow(() -> new RuntimeException("Fiche Atelier non trouvée"));
            return ResponseEntity.ok(ordreReparation);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("{\"message\": \"" + e.getMessage() + "\"}");
        }
    }

    @PostMapping("/create")
    @Operation(summary = "Créer une nouvelle fiche atelier")
    public ResponseEntity<?> createOrdreReparation(@RequestBody OrdreReparationRequest request) {
        try {
            OrdreReparation ordreReparation = ordreReparationService.createOrdreReparation(request);
            return ResponseEntity.ok(ordreReparation);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("{\"message\": \"" + e.getMessage() + "\"}");
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour une fiche atelier")
    public ResponseEntity<?> updateOrdreReparation(@PathVariable Long id, @RequestBody OrdreReparationRequest request) {
        try {
            OrdreReparation ordreReparation = ordreReparationService.updateOrdreReparation(id, request);
            return ResponseEntity.ok(ordreReparation);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("{\"message\": \"" + e.getMessage() + "\"}");
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une fiche atelier")
    public ResponseEntity<?> deleteOrdreReparation(@PathVariable Long id) {
        try {
            ordreReparationService.deleteOrdreReparation(id);
            return ResponseEntity.ok("{\"message\": \"Fiche Atelier supprimée avec succès !\"}");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("{\"message\": \"" + e.getMessage() + "\"}");
        }
    }

    @PostMapping("/{ficheId}/techniciens/{technicienId}")
    @Operation(summary = "Assigner un technicien à une fiche atelier")
    public ResponseEntity<?> assignTechnicien(@PathVariable Long ficheId, @PathVariable Long technicienId) {
        try {
            ordreReparationService.assignTechnicien(ficheId, technicienId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("{\"message\": \"" + e.getMessage() + "\"}");
        }
    }

    @DeleteMapping("/{ficheId}/techniciens/{technicienId}")
    @Operation(summary = "Retirer un technicien d'une fiche atelier")
    public ResponseEntity<?> removeTechnicien(@PathVariable Long ficheId, @PathVariable Long technicienId) {
        try {
            ordreReparationService.removeTechnicien(ficheId, technicienId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("{\"message\": \"" + e.getMessage() + "\"}");
        }
    }

    @PostMapping("/{ficheId}/techniciens-reparation/{technicienId}")
    @Operation(summary = "Assigner un technicien pour la réparation")
    public ResponseEntity<?> assignTechnicienReparation(@PathVariable Long ficheId, @PathVariable Long technicienId) {
        try {
            ordreReparationService.assignTechnicienReparation(ficheId, technicienId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("{\"message\": \"" + e.getMessage() + "\"}");
        }
    }

    @DeleteMapping("/{ficheId}/techniciens-reparation/{technicienId}")
    @Operation(summary = "Retirer un technicien de la réparation")
    public ResponseEntity<?> removeTechnicienReparation(@PathVariable Long ficheId, @PathVariable Long technicienId) {
        try {
            ordreReparationService.removeTechnicienReparation(ficheId, technicienId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("{\"message\": \"" + e.getMessage() + "\"}");
        }
    }

    @PatchMapping("/{id}/statut")
    @Operation(summary = "Mettre à jour le statut d'une fiche atelier")
    public ResponseEntity<?> updateStatut(@PathVariable Long id, @RequestParam String statut) {
        try {
            OrdreReparation fiche = ordreReparationService.updateStatut(id, statut);
            return ResponseEntity.ok(fiche);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("{\"message\": \"" + e.getMessage() + "\"}");
        }
    }

    // ─── Pièces jointes de diagnostic ──────────────────────────────────

    @GetMapping("/{id}/diagnostic/pieces-jointes")
    @Operation(summary = "Lister (et filtrer par type) les pièces jointes de diagnostic d'un ordre de réparation")
    public ResponseEntity<?> getPiecesJointesDiagnostic(@PathVariable Long id,
            @RequestParam(required = false) TypePieceJointe type) {
        try {
            List<PieceJointeDiagnosticResponse> pieces = ordreReparationService.getPiecesJointesDiagnostic(id, type);
            return ResponseEntity.ok(pieces);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/{id}/diagnostic/pieces-jointes")
    @Operation(summary = "Ajouter une pièce jointe de diagnostic (photo ou PDF)")
    public ResponseEntity<?> addPieceJointeDiagnostic(@PathVariable Long id,
            @RequestBody PieceJointeDiagnosticRequest request) {
        try {
            PieceJointeDiagnosticResponse piece = ordreReparationService.addPieceJointeDiagnostic(id, request);
            return ResponseEntity.ok(piece);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}/diagnostic/pieces-jointes/{pieceJointeId}")
    @Operation(summary = "Supprimer une pièce jointe de diagnostic")
    public ResponseEntity<?> deletePieceJointeDiagnostic(@PathVariable Long id, @PathVariable Long pieceJointeId) {
        try {
            ordreReparationService.deletePieceJointeDiagnostic(id, pieceJointeId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // ─── Lien Fiche Atelier → Ordre de réparation ──────────────────────

    @PostMapping("/depuis-fiche-atelier/{ficheAtelierId}")
    @Operation(summary = "Créer un ordre de réparation à partir d'une fiche atelier existante")
    public ResponseEntity<?> createFromFicheAtelier(@PathVariable Long ficheAtelierId) {
        try {
            OrdreReparation ordreReparation = ordreReparationService.createFromFicheAtelier(ficheAtelierId);
            return ResponseEntity.ok(ordreReparation);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/exists-for-fiche-atelier/{ficheAtelierId}")
    @Operation(summary = "Vérifier si un ordre de réparation existe déjà pour une fiche atelier")
    public ResponseEntity<?> existsForFicheAtelier(@PathVariable Long ficheAtelierId) {
        return ResponseEntity.ok(Map.of("exists", ordreReparationService.existsByFicheAtelierId(ficheAtelierId)));
    }
}
