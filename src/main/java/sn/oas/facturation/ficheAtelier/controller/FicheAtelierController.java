package sn.oas.facturation.ficheAtelier.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.oas.facturation.ficheAtelier.data.entity.FicheAtelier;
import sn.oas.facturation.ficheAtelier.dto.FicheAtelierRequest;
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
    public ResponseEntity<List<FicheAtelier>> getAllFichesAtelier() {
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
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/create")
    @Operation(summary = "Créer une nouvelle fiche atelier")
    public ResponseEntity<?> createFicheAtelier(@RequestBody FicheAtelierRequest request) {
        try {
            FicheAtelier ficheAtelier = ficheAtelierService.createFicheAtelier(request);
            return ResponseEntity.ok(ficheAtelier);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour une fiche atelier")
    public ResponseEntity<?> updateFicheAtelier(@PathVariable Long id, @RequestBody FicheAtelierRequest request) {
        try {
            FicheAtelier ficheAtelier = ficheAtelierService.updateFicheAtelier(id, request);
            return ResponseEntity.ok(ficheAtelier);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une fiche atelier")
    public ResponseEntity<?> deleteFicheAtelier(@PathVariable Long id) {
        try {
            ficheAtelierService.deleteFicheAtelier(id);
            return ResponseEntity.ok("Fiche Atelier supprimée avec succès !");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
