package sn.oas.facturation.mecanicien.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.oas.facturation.mecanicien.data.entity.Mecanicien;
import sn.oas.facturation.mecanicien.dto.MecanicienRequest;
import sn.oas.facturation.mecanicien.service.MecanicienService;

import java.util.List;

@RestController
@RequestMapping("/api/mecaniciens")
@RequiredArgsConstructor
@Tag(name = "Mécaniciens", description = "API pour la gestion des mécaniciens")
public class MecanicienController {

    private final MecanicienService mecanicienService;

    @GetMapping
    @Operation(summary = "Lister tous les mécaniciens")
    public ResponseEntity<List<Mecanicien>> getAllMecaniciens() {
        return ResponseEntity.ok(mecanicienService.getAllMecaniciens());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer un mécanicien par son ID")
    public ResponseEntity<?> getMecanicienById(@PathVariable Long id) {
        try {
            Mecanicien mecanicien = mecanicienService.getMecanicienById(id)
                    .orElseThrow(() -> new RuntimeException("Mécanicien non trouvé"));
            return ResponseEntity.ok(mecanicien);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/create")
    @Operation(summary = "Créer un nouveau mécanicien")
    public ResponseEntity<?> createMecanicien(@RequestBody MecanicienRequest request) {
        try {
            Mecanicien mecanicien = mecanicienService.createMecanicien(request);
            return ResponseEntity.ok(mecanicien);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour un mécanicien")
    public ResponseEntity<?> updateMecanicien(@PathVariable Long id, @RequestBody MecanicienRequest request) {
        try {
            Mecanicien mecanicien = mecanicienService.updateMecanicien(id, request);
            return ResponseEntity.ok(mecanicien);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un mécanicien")
    public ResponseEntity<?> deleteMecanicien(@PathVariable Long id) {
        try {
            mecanicienService.deleteMecanicien(id);
            return ResponseEntity.ok("Mécanicien supprimé avec succès !");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
