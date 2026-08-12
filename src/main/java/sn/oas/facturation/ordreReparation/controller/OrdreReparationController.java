package sn.oas.facturation.ordreReparation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.oas.facturation.ordreReparation.data.entity.OrdreReparation;
import sn.oas.facturation.ordreReparation.dto.OrdreReparationRequest;
import sn.oas.facturation.ordreReparation.dto.OrdreReparationLightDTO;
import sn.oas.facturation.ordreReparation.service.OrdreReparationService;

import java.util.List;

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

    @PostMapping("/{ficheId}/mecaniciens/{mecanicienId}")
    @Operation(summary = "Assigner un mécanicien à une fiche atelier")
    public ResponseEntity<?> assignMecanicien(@PathVariable Long ficheId, @PathVariable Long mecanicienId) {
        try {
            ordreReparationService.assignMecanicien(ficheId, mecanicienId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("{\"message\": \"" + e.getMessage() + "\"}");
        }
    }

    @DeleteMapping("/{ficheId}/mecaniciens/{mecanicienId}")
    @Operation(summary = "Retirer un mécanicien d'une fiche atelier")
    public ResponseEntity<?> removeMecanicien(@PathVariable Long ficheId, @PathVariable Long mecanicienId) {
        try {
            ordreReparationService.removeMecanicien(ficheId, mecanicienId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("{\"message\": \"" + e.getMessage() + "\"}");
        }
    }

    @PostMapping("/{ficheId}/mecaniciens-reparation/{mecanicienId}")
    @Operation(summary = "Assigner un mécanicien pour la réparation")
    public ResponseEntity<?> assignMecanicienReparation(@PathVariable Long ficheId, @PathVariable Long mecanicienId) {
        try {
            ordreReparationService.assignMecanicienReparation(ficheId, mecanicienId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("{\"message\": \"" + e.getMessage() + "\"}");
        }
    }

    @DeleteMapping("/{ficheId}/mecaniciens-reparation/{mecanicienId}")
    @Operation(summary = "Retirer un mécanicien de la réparation")
    public ResponseEntity<?> removeMecanicienReparation(@PathVariable Long ficheId, @PathVariable Long mecanicienId) {
        try {
            ordreReparationService.removeMecanicienReparation(ficheId, mecanicienId);
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
}
