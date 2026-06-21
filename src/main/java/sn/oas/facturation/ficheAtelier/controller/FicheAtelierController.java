package sn.oas.facturation.ficheAtelier.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.oas.facturation.ficheAtelier.data.entity.FicheAtelier;
import sn.oas.facturation.ficheAtelier.dto.FicheAtelierRequest;
import sn.oas.facturation.ficheAtelier.dto.FicheAtelierLightDTO;
import sn.oas.facturation.ficheAtelier.service.FicheAtelierService;

import java.util.List;

@RestController
@RequestMapping("/api/fiches-atelier")
@RequiredArgsConstructor
@Tag(name = "Fiches Atelier", description = "API pour la gestion des fiches atelier")
public class FicheAtelierController {

    private final FicheAtelierService ficheAtelierService;

    @GetMapping
    @Operation(summary = "Lister toutes les fiches atelier")
    public ResponseEntity<List<FicheAtelierLightDTO>> getAllFichesAtelier() {
        return ResponseEntity.ok(ficheAtelierService.getAllFichesAtelier());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer une fiche atelier par son ID")
    public ResponseEntity<?> getFicheAtelierById(@PathVariable Long id) {
        try {
            FicheAtelier ficheAtelier = ficheAtelierService.getFicheAtelierById(id)
                    .orElseThrow(() -> new RuntimeException("Fiche Atelier non trouvée"));
            return ResponseEntity.ok(ficheAtelier);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("{\"message\": \"" + e.getMessage() + "\"}");
        }
    }

    @PostMapping("/create")
    @Operation(summary = "Créer une nouvelle fiche atelier")
    public ResponseEntity<?> createFicheAtelier(@RequestBody FicheAtelierRequest request) {
        try {
            FicheAtelier ficheAtelier = ficheAtelierService.createFicheAtelier(request);
            return ResponseEntity.ok(ficheAtelier);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("{\"message\": \"" + e.getMessage() + "\"}");
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour une fiche atelier")
    public ResponseEntity<?> updateFicheAtelier(@PathVariable Long id, @RequestBody FicheAtelierRequest request) {
        try {
            FicheAtelier ficheAtelier = ficheAtelierService.updateFicheAtelier(id, request);
            return ResponseEntity.ok(ficheAtelier);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("{\"message\": \"" + e.getMessage() + "\"}");
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une fiche atelier")
    public ResponseEntity<?> deleteFicheAtelier(@PathVariable Long id) {
        try {
            ficheAtelierService.deleteFicheAtelier(id);
            return ResponseEntity.ok("{\"message\": \"Fiche Atelier supprimée avec succès !\"}");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("{\"message\": \"" + e.getMessage() + "\"}");
        }
    }

    @PostMapping("/{ficheId}/mecaniciens/{mecanicienId}")
    @Operation(summary = "Assigner un mécanicien à une fiche atelier")
    public ResponseEntity<?> assignMecanicien(@PathVariable Long ficheId, @PathVariable Long mecanicienId) {
        try {
            ficheAtelierService.assignMecanicien(ficheId, mecanicienId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("{\"message\": \"" + e.getMessage() + "\"}");
        }
    }

    @DeleteMapping("/{ficheId}/mecaniciens/{mecanicienId}")
    @Operation(summary = "Retirer un mécanicien d'une fiche atelier")
    public ResponseEntity<?> removeMecanicien(@PathVariable Long ficheId, @PathVariable Long mecanicienId) {
        try {
            ficheAtelierService.removeMecanicien(ficheId, mecanicienId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("{\"message\": \"" + e.getMessage() + "\"}");
        }
    }

    @PostMapping("/{ficheId}/mecaniciens-reparation/{mecanicienId}")
    @Operation(summary = "Assigner un mécanicien pour la réparation")
    public ResponseEntity<?> assignMecanicienReparation(@PathVariable Long ficheId, @PathVariable Long mecanicienId) {
        try {
            ficheAtelierService.assignMecanicienReparation(ficheId, mecanicienId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("{\"message\": \"" + e.getMessage() + "\"}");
        }
    }

    @DeleteMapping("/{ficheId}/mecaniciens-reparation/{mecanicienId}")
    @Operation(summary = "Retirer un mécanicien de la réparation")
    public ResponseEntity<?> removeMecanicienReparation(@PathVariable Long ficheId, @PathVariable Long mecanicienId) {
        try {
            ficheAtelierService.removeMecanicienReparation(ficheId, mecanicienId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("{\"message\": \"" + e.getMessage() + "\"}");
        }
    }

    @PatchMapping("/{id}/statut")
    @Operation(summary = "Mettre à jour le statut d'une fiche atelier")
    public ResponseEntity<?> updateStatut(@PathVariable Long id, @RequestParam String statut) {
        try {
            FicheAtelier fiche = ficheAtelierService.updateStatut(id, statut);
            return ResponseEntity.ok(fiche);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("{\"message\": \"" + e.getMessage() + "\"}");
        }
    }
}
