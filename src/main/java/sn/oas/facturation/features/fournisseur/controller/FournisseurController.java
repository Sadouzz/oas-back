package sn.oas.facturation.features.fournisseur.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.oas.facturation.features.fournisseur.data.entity.Fournisseur;
import sn.oas.facturation.features.fournisseur.dto.FournisseurListResponse;
import sn.oas.facturation.features.fournisseur.dto.FournisseurRequest;
import sn.oas.facturation.features.fournisseur.service.FournisseurService;

import java.util.List;

@RestController
@RequestMapping("/api/fournisseurs")
@RequiredArgsConstructor
@Tag(name = "Fournisseurs", description = "API pour la gestion des fournisseurs")
public class FournisseurController {
    private final FournisseurService fournisseurService;

    @GetMapping
    @Operation(summary = "Lister tous les fournisseurs ou rechercher par mot-clé")
    public ResponseEntity<Page<FournisseurListResponse>> getFournisseurs(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        if (keyword != null && !keyword.trim().isEmpty()){
            return ResponseEntity.ok(fournisseurService.searchFournisseur(keyword.trim(), page, size).map(FournisseurListResponse::from));
        }
        return ResponseEntity.ok(fournisseurService.getAllFournisseur(page, size).map(FournisseurListResponse::from));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer un fournisseur par son ID")
    public ResponseEntity<?> getFournisseurById(@PathVariable Long id){
        try {
            return ResponseEntity.ok(fournisseurService.getFournisseurById(id));
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/create")
    @Operation(summary = "Créer un nouveau fournisseur")
    public ResponseEntity<?> addFournisseur(@RequestBody FournisseurRequest request){
        try {
            Fournisseur fournisseur = fournisseurService.createFournisseur(request);
            return ResponseEntity.ok(fournisseur);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour un fournisseur")
    public ResponseEntity<?> updateFournisseur(@PathVariable Long id, @RequestBody FournisseurRequest request){
        try {
            Fournisseur fournisseur = fournisseurService.updateFournisseur(id,request);
            return ResponseEntity.ok(fournisseur);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un fournisseur")
    public ResponseEntity<?> deleteFournisseur(@PathVariable Long id){
        try {
            fournisseurService.deleteFournisseur(id);
            return ResponseEntity.ok("Fournisseur supprimé avec succes");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/archive")
    @Operation(summary = "Archiver un fournisseur")
    public ResponseEntity<?> archiveFournisseur(@PathVariable Long id){
        try {
            fournisseurService.archiveFournisseur(id);
            return ResponseEntity.ok("Fournisseur archivé avec succes");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/unarchive")
    @Operation(summary = "Désarchiver un fournisseur")
    public ResponseEntity<?> unarchiveFournisseur(@PathVariable Long id){
        try {
            fournisseurService.unarchiveFournisseur(id);
            return ResponseEntity.ok("Fournisseur désararchivé avec succes");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
