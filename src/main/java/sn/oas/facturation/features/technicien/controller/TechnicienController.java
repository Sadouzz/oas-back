package sn.oas.facturation.features.technicien.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.oas.facturation.features.auth.data.entity.Technicien;
import sn.oas.facturation.features.technicien.dto.TechnicienListResponse;
import sn.oas.facturation.features.technicien.dto.TechnicienRequest;
import sn.oas.facturation.features.technicien.service.TechnicienService;

import java.util.List;
import java.util.Map;

/**
 * CRUD staff des comptes Technicien (écran gestion/techniciens). Même niveau de protection
 * que l'ancien module mecanicien/ qu'il remplace : restreint uniquement par le garde de rôle
 * frontend (SUPER_AGENT, MASTER, CHEF_ATELIER) — pas de règle backend dédiée dans
 * WebSecurityConfig au-delà de "authentifié", comme c'était déjà le cas pour /api/mecaniciens.
 */
@RestController
@RequestMapping("/api/techniciens")
@RequiredArgsConstructor
@Tag(name = "Techniciens", description = "API pour la gestion des comptes technicien")
public class TechnicienController {

    private final TechnicienService technicienService;

    @GetMapping
    @Operation(summary = "Lister tous les techniciens ou rechercher par mot-clé")
    public ResponseEntity<Page<TechnicienListResponse>> getAllTechniciens(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            return ResponseEntity.ok(technicienService.searchTechniciens(keyword.trim(), page, size).map(TechnicienListResponse::from));
        }
        return ResponseEntity.ok(technicienService.getAllTechniciens(page, size).map(TechnicienListResponse::from));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer un technicien par son ID")
    public ResponseEntity<?> getTechnicienById(@PathVariable Long id) {
        try {
            Technicien technicien = technicienService.getTechnicienById(id)
                    .orElseThrow(() -> new RuntimeException("Technicien non trouvé"));
            return ResponseEntity.ok(technicien);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/create")
    @Operation(summary = "Créer un nouveau compte technicien")
    public ResponseEntity<?> createTechnicien(@RequestBody TechnicienRequest request) {
        try {
            technicienService.createTechnicien(request);
            return ResponseEntity.ok(Map.of("message", "Compte technicien créé avec succès !"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour un technicien")
    public ResponseEntity<?> updateTechnicien(@PathVariable Long id, @RequestBody TechnicienRequest request) {
        try {
            Technicien technicien = technicienService.updateTechnicien(id, request);
            return ResponseEntity.ok(technicien);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un technicien")
    public ResponseEntity<?> deleteTechnicien(@PathVariable Long id) {
        try {
            technicienService.deleteTechnicien(id);
            return ResponseEntity.ok(Map.of("message", "Technicien supprimé avec succès !"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
