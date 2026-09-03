package sn.oas.facturation.features.marketplace.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.oas.facturation.features.marketplace.data.entity.Produit;
import sn.oas.facturation.features.marketplace.dto.ProduitListResponse;
import sn.oas.facturation.features.marketplace.dto.ProduitRequest;
import sn.oas.facturation.features.marketplace.service.ProduitService;

import java.util.List;

@RestController
@RequestMapping("/api/admin/marketplace/produits")
@RequiredArgsConstructor
@Tag(name = "Marketplace Admin", description = "Gestion admin du marketplace")
public class ProduitAdminController {

    private final ProduitService produitService;

    @PostMapping
    @Operation(summary = "Admin: ajouter un produit")
    public ResponseEntity<Produit> create(@RequestBody ProduitRequest request) {
        return new ResponseEntity<>(produitService.create(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Admin: modifier un produit")
    public ResponseEntity<Produit> update(@PathVariable Long id, @RequestBody ProduitRequest request) {
        return ResponseEntity.ok(produitService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Admin: supprimer un produit")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        produitService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(summary = "Admin: lister tous les produits")
    public ResponseEntity<List<ProduitListResponse>> getAll() {
        return ResponseEntity.ok(produitService.getAll().stream().map(ProduitListResponse::from).toList());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Admin: voir les détails d'un produit")
    public ResponseEntity<Produit> getById(@PathVariable Long id) {
        return ResponseEntity.ok(produitService.getById(id));
    }

    @GetMapping("/disponibles")
    @Operation(summary = "Admin: voir les produits disponibles")
    public ResponseEntity<List<Produit>> getDisponibles() {
        return ResponseEntity.ok(produitService.getDisponibles());
    }

    @GetMapping("/search")
    @Operation(summary = "Admin: rechercher un produit")
    public ResponseEntity<?> search(
            @RequestParam String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        if (page != null && size != null) {
            return ResponseEntity.ok(produitService.search(keyword, page, size));
        }
        return ResponseEntity.ok(produitService.search(keyword));
    }

    @GetMapping("/archives")
    @Operation(summary = "Admin: voir les produits archivés")
    public ResponseEntity<List<Produit>> getArchives() {
        return ResponseEntity.ok(produitService.getArchives());
    }

    @PatchMapping("/{id}/disponibilite")
    @Operation(summary = "Admin: activer/désactiver un produit")
    public ResponseEntity<Produit> toggleDisponibilite(@PathVariable Long id, @RequestParam Boolean disponible) {
        return ResponseEntity.ok(produitService.toggleDisponibilite(id, disponible));
    }

    @PatchMapping("/{id}/archiver")
    @Operation(summary = "Admin: archiver un produit")
    public ResponseEntity<Produit> archiver(@PathVariable Long id) {
        return ResponseEntity.ok(produitService.archiver(id));
    }

    @PatchMapping("/{id}/desarchiver")
    @Operation(summary = "Admin: désarchiver un produit")
    public ResponseEntity<Produit> desarchiver(@PathVariable Long id) {
        return ResponseEntity.ok(produitService.desarchiver(id));
    }
}
