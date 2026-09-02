package sn.oas.facturation.features.technicien.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;
import sn.oas.facturation.features.auth.data.entity.Technicien;
import sn.oas.facturation.features.ordreReparation.data.entity.OrdreReparation;
import sn.oas.facturation.features.ordreReparation.data.enums.TypePieceJointe;
import sn.oas.facturation.features.ordreReparation.dto.PieceJointeDiagnosticRequest;
import sn.oas.facturation.features.ordreReparation.dto.PieceJointeDiagnosticResponse;
import sn.oas.facturation.features.ordreReparation.service.OrdreReparationService;
import sn.oas.facturation.features.technicien.dto.PannesRequest;
import sn.oas.facturation.features.technicien.dto.TechnicienLigneMainDoeuvreRequest;
import sn.oas.facturation.features.technicien.dto.TechnicienLignePieceRequest;
import sn.oas.facturation.features.technicien.service.TechnicienPortalService;
import sn.oas.facturation.features.technicien.service.TechnicienService;

import java.util.List;
import java.util.Map;

/**
 * Portail technicien (self-service), sur le même modèle que ClientPortalController /
 * AdminPortalController. Accès restreint aux comptes technicien via
 * WebSecurityConfig (/api/technicien/**).
 */
@RestController
@RequestMapping("/api/technicien/portal")
@RequiredArgsConstructor
@Tag(name = "Portail Technicien", description = "API destinées au compte technicien connecté")
public class TechnicienPortalController {

    private final TechnicienService technicienService;
    private final TechnicienPortalService technicienPortalService;
    private final OrdreReparationService ordreReparationService;

    @GetMapping("/me")
    @Operation(summary = "Récupérer le profil du technicien connecté")
    public ResponseEntity<Technicien> getProfile() {
        return ResponseEntity.ok(technicienService.getTechnicienConnecte());
    }

    @GetMapping("/ordres-reparation")
    @Operation(summary = "Lister les ordres de réparation assignés au technicien connecté")
    public ResponseEntity<List<OrdreReparation>> getMesOrdresReparation() {
        Technicien technicien = technicienService.getTechnicienConnecte();
        return ResponseEntity.ok(technicienPortalService.getMesOrdresReparation(technicien));
    }

    @GetMapping("/ordres-reparation/{id}")
    @Operation(summary = "Détail d'un ordre de réparation assigné")
    public ResponseEntity<?> getOrdreReparationById(@PathVariable Long id) {
        try {
            Technicien technicien = technicienService.getTechnicienConnecte();
            return ResponseEntity.ok(technicienPortalService.getMonOrdreReparation(technicien, id));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // ─── Diagnostic : pièces jointes ────────────────────────────────────

    @GetMapping("/ordres-reparation/{id}/diagnostic/pieces-jointes")
    @Operation(summary = "Lister les pièces jointes de diagnostic d'un ordre assigné")
    public ResponseEntity<?> getPiecesJointesDiagnostic(@PathVariable Long id,
            @RequestParam(required = false) TypePieceJointe type) {
        try {
            Technicien technicien = technicienService.getTechnicienConnecte();
            return ResponseEntity.ok(technicienPortalService.getPiecesJointesDiagnostic(technicien, id, type));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/ordres-reparation/{id}/diagnostic/pieces-jointes")
    @Operation(summary = "Ajouter une pièce jointe de diagnostic (photo ou PDF)")
    public ResponseEntity<?> addPieceJointeDiagnostic(@PathVariable Long id, @RequestBody PieceJointeDiagnosticRequest request) {
        try {
            Technicien technicien = technicienService.getTechnicienConnecte();
            PieceJointeDiagnosticResponse piece = technicienPortalService.addPieceJointeDiagnostic(technicien, id, request);
            return ResponseEntity.ok(piece);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/ordres-reparation/{id}/diagnostic/pieces-jointes/{pieceJointeId}")
    @Operation(summary = "Supprimer une pièce jointe de diagnostic")
    public ResponseEntity<?> deletePieceJointeDiagnostic(@PathVariable Long id, @PathVariable Long pieceJointeId) {
        try {
            Technicien technicien = technicienService.getTechnicienConnecte();
            technicienPortalService.deletePieceJointeDiagnostic(technicien, id, pieceJointeId);
            return ResponseEntity.ok().build();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // ─── Pannes détectées ───────────────────────────────────────────────

    @PutMapping("/ordres-reparation/{id}/pannes")
    @Operation(summary = "Renseigner les pannes détectées (listeDefauts) sur un ordre assigné")
    public ResponseEntity<?> updatePannes(@PathVariable Long id, @RequestBody PannesRequest request) {
        try {
            Technicien technicien = technicienService.getTechnicienConnecte();
            return ResponseEntity.ok(technicienPortalService.updatePannesDetectees(technicien, id, request));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // ─── Pièces / main d'œuvre proposées, sans prix ────────────────────

    @PostMapping("/ordres-reparation/{id}/pieces")
    @Operation(summary = "Proposer une pièce (sans prix) sur un ordre assigné")
    public ResponseEntity<?> proposerPiece(@PathVariable Long id, @RequestBody TechnicienLignePieceRequest request) {
        try {
            Technicien technicien = technicienService.getTechnicienConnecte();
            technicienPortalService.proposerPiece(technicien, id, request);
            return ResponseEntity.ok().build();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/ordres-reparation/{id}/main-doeuvre")
    @Operation(summary = "Proposer une main d'œuvre (sans prix) sur un ordre assigné")
    public ResponseEntity<?> proposerMainDoeuvre(@PathVariable Long id, @RequestBody TechnicienLigneMainDoeuvreRequest request) {
        try {
            Technicien technicien = technicienService.getTechnicienConnecte();
            technicienPortalService.proposerMainDoeuvre(technicien, id, request);
            return ResponseEntity.ok().build();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // ─── Remarques de diagnostic ────────────────────────────────────────

    @GetMapping("/ordres-reparation/{id}/diagnostic/remarques")
    @Operation(summary = "Lister les remarques de diagnostic d'un ordre assigné")
    public ResponseEntity<?> getRemarquesDiagnostic(@PathVariable Long id) {
        try {
            Technicien technicien = technicienService.getTechnicienConnecte();
            // vérifie que le tech est assigné via le service existant
            technicienPortalService.getMonOrdreReparation(technicien, id);
            return ResponseEntity.ok(ordreReparationService.getRemarquesDiagnostic(id));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/ordres-reparation/{id}/diagnostic/remarques")
    @Operation(summary = "Ajouter une remarque de diagnostic (attribuée au technicien connecté)")
    public ResponseEntity<?> addRemarqueDiagnostic(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            Technicien technicien = technicienService.getTechnicienConnecte();
            // vérifie assignation
            technicienPortalService.getMonOrdreReparation(technicien, id);
            String contenu = body.get("contenu");
            return ResponseEntity.ok(ordreReparationService.addRemarqueDiagnostic(id, technicien, contenu));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/ordres-reparation/{id}/diagnostic/remarques/{remarqueId}")
    @Operation(summary = "Supprimer une de ses propres remarques de diagnostic")
    public ResponseEntity<?> deleteRemarqueDiagnostic(@PathVariable Long id, @PathVariable Long remarqueId) {
        try {
            Technicien technicien = technicienService.getTechnicienConnecte();
            technicienPortalService.getMonOrdreReparation(technicien, id);
            ordreReparationService.deleteRemarqueDiagnostic(id, remarqueId);
            return ResponseEntity.ok().build();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
