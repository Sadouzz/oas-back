package sn.oas.facturation.features.ordreReparation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.*;

import sn.oas.facturation.features.client.data.entity.Client;
import sn.oas.facturation.features.client.service.ClientService;
import sn.oas.facturation.features.ordreReparation.data.entity.OrdreReparation;
import sn.oas.facturation.features.diagnostic.data.enums.TypePieceJointe;
import sn.oas.facturation.features.diagnostic.dto.PieceJointeDiagnosticRequest;
import sn.oas.facturation.features.diagnostic.dto.PieceJointeDiagnosticResponse;
import sn.oas.facturation.features.diagnostic.dto.RemarqueDiagnosticResponse;
import sn.oas.facturation.features.ordreReparation.dto.OrdreReparationRequest;
import sn.oas.facturation.features.ordreReparation.dto.RestitutionRequest;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import sn.oas.facturation.features.ordreReparation.dto.OrdreReparationListDTO;
import sn.oas.facturation.features.ordreReparation.dto.OrdreReparationResponseDTO;
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
    @Operation(summary = "Lister toutes les  atelier avec pagination")
    public ResponseEntity<Page<OrdreReparationListDTO>> getAllOrdresReparation(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity
                .ok(ordreReparationService.getAllOrdresReparation(page, size).map(OrdreReparationListDTO::from));
    }

    @GetMapping({"/usage", "/rubriques-usage", "/ordres-usage"})
    @Operation(summary = "Vérifier l'utilisation des rubriques dans les ordres de réparation")
    public ResponseEntity<Map<String, Object>> getUsage() {
        return ResponseEntity.ok(Map.of(
                "totalOrdres", ordreReparationRepository.count(),
                "used", java.util.Collections.emptyList()
        ));
    }

    @GetMapping("/me")
    @Operation(summary = "Lister l'historique des interventions/réparations du client connecté")
    public ResponseEntity<List<OrdreReparation>> getMyInterventions() {
        Client client = clientService.getClientConnecte();
        return ResponseEntity
                .ok(ordreReparationRepository.findClientHistory(client.getId()));
    }

    @GetMapping("/client/{clientId}")
    @Operation(summary = "Lister l'historique des réparations d'un client")
    public ResponseEntity<List<OrdreReparation>> getInterventionsByClient(@PathVariable Long clientId) {
        return ResponseEntity.ok(ordreReparationRepository.findClientHistory(clientId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer une fiche atelier par son ID")
    public ResponseEntity<?> getOrdreReparationById(@PathVariable Long id) {
        try {
            OrdreReparationResponseDTO dto = ordreReparationService.getOrdreReparationResponseById(id);
            return ResponseEntity.ok(dto);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("{\"message\": \"" + e.getMessage() + "\"}");
        }
    }

    @GetMapping("/{id}/summary")
    @Operation(summary = "Récupérer le résumé minimal d'une fiche atelier (pour le header)")
    public ResponseEntity<?> getOrdreReparationSummary(@PathVariable Long id) {
        try {
            sn.oas.facturation.features.ordreReparation.dto.responses.OrdreReparationSummaryDto summary = ordreReparationService.getOrdreReparationSummary(id);
            return ResponseEntity.ok(sn.oas.facturation.shared.dto.ApiResponse.success("Opération effectuée avec succès", summary));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("{\"message\": \"" + e.getMessage() + "\"}");
        }
    }

    @PostMapping({"", "/create"})
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

    @GetMapping("/{id}/step-reception")
    @Operation(summary = "Obtenir l'étape Réception")
    public ResponseEntity<?> getStepReception(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(ordreReparationService.getStepReception(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("{\"message\": \"" + e.getMessage() + "\"}");
        }
    }

    @GetMapping("/{id}/step-diagnostic")
    @Operation(summary = "Obtenir l'étape Diagnostic")
    public ResponseEntity<?> getStepDiagnostic(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(ordreReparationService.getStepDiagnostic(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("{\"message\": \"" + e.getMessage() + "\"}");
        }
    }

    @GetMapping("/{id}/step-pieces-mo")
    @Operation(summary = "Obtenir l'étape Pièces et Main d'Oeuvre")
    public ResponseEntity<?> getStepPiecesMo(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(ordreReparationService.getStepPiecesMo(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("{\"message\": \"" + e.getMessage() + "\"}");
        }
    }

    @GetMapping("/{id}/step-assignation")
    @Operation(summary = "Obtenir l'étape Assignation")
    public ResponseEntity<?> getStepAssignation(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(ordreReparationService.getStepAssignation(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("{\"message\": \"" + e.getMessage() + "\"}");
        }
    }

    @GetMapping("/{id}/step-proforma")
    @Operation(summary = "Obtenir l'étape Proforma")
    public ResponseEntity<?> getStepProforma(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(ordreReparationService.getStepProforma(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("{\"message\": \"" + e.getMessage() + "\"}");
        }
    }

    @GetMapping("/{id}/step-approvisionnement")
    @Operation(summary = "Obtenir l'étape Approvisionnement")
    public ResponseEntity<?> getStepApprovisionnement(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(ordreReparationService.getStepApprovisionnement(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("{\"message\": \"" + e.getMessage() + "\"}");
        }
    }

    @GetMapping("/{id}/step-bon-sortie")
    @Operation(summary = "Obtenir l'étape Bon de Sortie")
    public ResponseEntity<?> getStepBonSortie(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(ordreReparationService.getStepBonSortie(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("{\"message\": \"" + e.getMessage() + "\"}");
        }
    }

    @GetMapping("/{id}/step-paiement")
    @Operation(summary = "Obtenir l'étape Paiement")
    public ResponseEntity<?> getStepPaiement(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(ordreReparationService.getStepPaiement(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("{\"message\": \"" + e.getMessage() + "\"}");
        }
    }

    @GetMapping("/{id}/step-pret-a-livrer")
    @Operation(summary = "Obtenir l'étape Prêt à Livrer")
    public ResponseEntity<?> getStepPretALivrer(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(ordreReparationService.getStepPretALivrer(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("{\"message\": \"" + e.getMessage() + "\"}");
        }
    }

    @GetMapping("/{id}/step-reparation")
    @Operation(summary = "Obtenir l'étape Réparation")
    public ResponseEntity<?> getStepReparation(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(ordreReparationService.getStepReparation(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("{\"message\": \"" + e.getMessage() + "\"}");
        }
    }

    @GetMapping("/{id}/step-livraison")
    @Operation(summary = "Obtenir l'étape Livraison")
    public ResponseEntity<?> getStepLivraison(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(ordreReparationService.getStepLivraison(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("{\"message\": \"" + e.getMessage() + "\"}");
        }
    }

    @PutMapping("/{id}/step-reception")
    @Operation(summary = "Mettre à jour l'étape Réception")
    public ResponseEntity<?> updateStepReception(@PathVariable Long id, @RequestBody sn.oas.facturation.features.ordreReparation.dto.steps.StepReceptionDto request) {
        try {
            return ResponseEntity.ok(ordreReparationService.updateStepReception(id, request));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("{\"message\": \"" + e.getMessage() + "\"}");
        }
    }

    @PutMapping("/{id}/step-diagnostic")
    @Operation(summary = "Mettre à jour l'étape Diagnostic")
    public ResponseEntity<?> updateStepDiagnostic(@PathVariable Long id, @RequestBody sn.oas.facturation.features.ordreReparation.dto.steps.StepDiagnosticDto request) {
        try {
            return ResponseEntity.ok(ordreReparationService.updateStepDiagnostic(id, request));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("{\"message\": \"" + e.getMessage() + "\"}");
        }
    }

    @PutMapping("/{id}/step-pieces-mo")
    @Operation(summary = "Mettre à jour l'étape Pièces et Main d'Oeuvre")
    public ResponseEntity<?> updateStepPiecesMo(@PathVariable Long id, @RequestBody sn.oas.facturation.features.ordreReparation.dto.steps.StepPiecesMoDto request) {
        try {
            return ResponseEntity.ok(ordreReparationService.updateStepPiecesMo(id, request));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("{\"message\": \"" + e.getMessage() + "\"}");
        }
    }

    @PutMapping("/{id}/step-assignation")
    @Operation(summary = "Mettre à jour l'étape Assignation")
    public ResponseEntity<?> updateStepAssignation(@PathVariable Long id, @RequestBody sn.oas.facturation.features.ordreReparation.dto.steps.StepAssignationDto request) {
        try {
            return ResponseEntity.ok(ordreReparationService.updateStepAssignation(id, request));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("{\"message\": \"" + e.getMessage() + "\"}");
        }
    }

    @PutMapping("/{id}/step-proforma")
    @Operation(summary = "Mettre à jour l'étape Proforma")
    public ResponseEntity<?> updateStepProforma(@PathVariable Long id, @RequestBody sn.oas.facturation.features.ordreReparation.dto.steps.StepProformaDto request) {
        try {
            return ResponseEntity.ok(ordreReparationService.updateStepProforma(id, request));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("{\"message\": \"" + e.getMessage() + "\"}");
        }
    }

    @PutMapping("/{id}/step-approvisionnement")
    @Operation(summary = "Mettre à jour l'étape Approvisionnement")
    public ResponseEntity<?> updateStepApprovisionnement(@PathVariable Long id, @RequestBody sn.oas.facturation.features.ordreReparation.dto.steps.StepApprovisionnementDto request) {
        try {
            return ResponseEntity.ok(ordreReparationService.updateStepApprovisionnement(id, request));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("{\"message\": \"" + e.getMessage() + "\"}");
        }
    }

    @PutMapping("/{id}/step-bon-sortie")
    @Operation(summary = "Mettre à jour l'étape Bon de Sortie")
    public ResponseEntity<?> updateStepBonSortie(@PathVariable Long id, @RequestBody sn.oas.facturation.features.ordreReparation.dto.steps.StepBonSortieDto request) {
        try {
            return ResponseEntity.ok(ordreReparationService.updateStepBonSortie(id, request));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("{\"message\": \"" + e.getMessage() + "\"}");
        }
    }

    @PutMapping("/{id}/step-paiement")
    @Operation(summary = "Mettre à jour l'étape Paiement")
    public ResponseEntity<?> updateStepPaiement(@PathVariable Long id, @RequestBody sn.oas.facturation.features.ordreReparation.dto.steps.StepPaiementDto request) {
        try {
            return ResponseEntity.ok(ordreReparationService.updateStepPaiement(id, request));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("{\"message\": \"" + e.getMessage() + "\"}");
        }
    }

    @PutMapping("/{id}/step-pret-a-livrer")
    @Operation(summary = "Mettre à jour l'étape Prêt à Livrer")
    public ResponseEntity<?> updateStepPretALivrer(@PathVariable Long id, @RequestBody sn.oas.facturation.features.ordreReparation.dto.steps.StepPretALivrerDto request) {
        try {
            return ResponseEntity.ok(ordreReparationService.updateStepPretALivrer(id, request));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("{\"message\": \"" + e.getMessage() + "\"}");
        }
    }

    @PutMapping("/{id}/step-reparation")
    @Operation(summary = "Mettre à jour l'étape Réparation")
    public ResponseEntity<?> updateStepReparation(@PathVariable Long id, @RequestBody sn.oas.facturation.features.ordreReparation.dto.steps.StepReparationDto request) {
        try {
            return ResponseEntity.ok(ordreReparationService.updateStepReparation(id, request));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("{\"message\": \"" + e.getMessage() + "\"}");
        }
    }

    @PutMapping("/{id}/step-livraison")
    @Operation(summary = "Mettre à jour l'étape Livraison")
    public ResponseEntity<?> updateStepLivraison(@PathVariable Long id, @RequestBody sn.oas.facturation.features.ordreReparation.dto.steps.StepLivraisonDto request) {
        try {
            return ResponseEntity.ok(ordreReparationService.updateStepLivraison(id, request));
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
            return ResponseEntity.badRequest().body(java.util.Map.of("message",
                    e.getMessage() != null ? e.getMessage() : "null", "trace", sw.toString()));
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
            return ResponseEntity.ok(Map.of(
                    "message", "Statut mis à jour avec succès",
                    "id", fiche.getId(),
                    "statut", fiche.getStatut() != null ? fiche.getStatut().name() : statut
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/{id}/restitution")
    @PreAuthorize("hasAnyRole('AGENT', 'SUPER_AGENT', 'MASTER', 'CHEF_ATELIER')")
    @Operation(summary = "Enregistrer la signature, la garantie et la restitution du véhicule")
    public ResponseEntity<?> restituerVehicule(@PathVariable Long id, @Valid @RequestBody RestitutionRequest request) {
        try {
            ordreReparationService.restituerVehicule(id, request.signature(), request.garantieMois());
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
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
    public ResponseEntity<?> addRemarqueDiagnostic(@PathVariable Long id, @RequestBody(required = false) Map<String, Object> body) {
        try {
            String contenu = null;
            if (body != null) {
                if (body.get("contenu") != null) {
                    contenu = String.valueOf(body.get("contenu"));
                } else if (body.get("remarque") != null) {
                    contenu = String.valueOf(body.get("remarque"));
                } else if (body.get("message") != null) {
                    contenu = String.valueOf(body.get("message"));
                }
            }
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
