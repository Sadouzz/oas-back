package sn.oas.facturation.features.ordreReparation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.oas.facturation.features.auth.data.entity.Client;
import sn.oas.facturation.features.client.service.ClientService;
import sn.oas.facturation.features.ordreReparation.data.entity.OrdreReparation;
import sn.oas.facturation.features.ordreReparation.data.enums.TypePieceJointe;
import sn.oas.facturation.features.ordreReparation.dto.OrdreReparationRequest;
import sn.oas.facturation.features.ordreReparation.dto.OrdreReparationLightDTO;
import sn.oas.facturation.features.ordreReparation.dto.PieceJointeDiagnosticRequest;
import sn.oas.facturation.features.ordreReparation.dto.PieceJointeDiagnosticResponse;
import sn.oas.facturation.features.ordreReparation.dto.RemarqueDiagnosticResponse;
import sn.oas.facturation.features.ordreReparation.repository.OrdreReparationRepository;
import sn.oas.facturation.features.ordreReparation.service.OrdreReparationService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ordres-reparation")
@RequiredArgsConstructor
@Tag(name = "Fiches Atelier", description = "API pour la gestion des fiches atelier")
public class OrdreReparationController {

    private final OrdreReparationService ordreReparationService;
    private final ClientService clientService;
    private final OrdreReparationRepository ordreReparationRepository;

    @GetMapping
    @Operation(summary = "Lister toutes les fiches atelier avec pagination")
    public ResponseEntity<?> getAllOrdresReparation(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ordreReparationService.getAllOrdresReparation(page, size));
    }

    @GetMapping("/me")
    @Operation(summary = "Lister l'historique des interventions/réparations du client connecté")
    public ResponseEntity<List<OrdreReparation>> getMyInterventions() {
        Client client = clientService.getClientConnecte();
        return ResponseEntity.ok(ordreReparationRepository.findByVehiculeClientIdOrderByDateCreationDesc(client.getId()));
    }

    @GetMapping("/client/{clientId}")
    @Operation(summary = "Lister l'historique des réparations d'un client")
    public ResponseEntity<List<OrdreReparation>> getInterventionsByClient(@PathVariable Long clientId) {
        return ResponseEntity.ok(ordreReparationRepository.findByVehiculeClientIdOrderByDateCreationDesc(clientId));
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
            e.printStackTrace();
            java.io.StringWriter sw = new java.io.StringWriter();
            e.printStackTrace(new java.io.PrintWriter(sw));
            return ResponseEntity.badRequest().body(java.util.Map.of("message", e.getMessage() != null ? e.getMessage() : "null", "trace", sw.toString()));
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

    // ─── Remarques de diagnostic ────────────────────────────────────────

    @GetMapping("/{id}/diagnostic/remarques")
    @Operation(summary = "Lister les remarques de diagnostic d'un ordre")
    public ResponseEntity<?> getRemarquesDiagnostic(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(ordreReparationService.getRemarquesDiagnostic(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/{id}/diagnostic/remarques")
    @Operation(summary = "Ajouter une remarque de diagnostic (depuis le portail chef atelier)")
    public ResponseEntity<?> addRemarqueDiagnostic(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            String contenu = body.get("contenu");
            RemarqueDiagnosticResponse r = ordreReparationService.addRemarqueDiagnostic(id, null, contenu);
            return ResponseEntity.ok(r);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}/diagnostic/remarques/{remarqueId}")
    @Operation(summary = "Supprimer une remarque de diagnostic")
    public ResponseEntity<?> deleteRemarqueDiagnostic(@PathVariable Long id, @PathVariable Long remarqueId) {
        try {
            ordreReparationService.deleteRemarqueDiagnostic(id, remarqueId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
