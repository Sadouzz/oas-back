package sn.oas.facturation.features.categorie_pieces.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.oas.facturation.features.categorie_pieces.data.entity.Categorie;
import sn.oas.facturation.features.categorie_pieces.dto.request.CategorieRequest;
import sn.oas.facturation.features.categorie_pieces.dto.response.CategorieResponse;
import sn.oas.facturation.features.categorie_pieces.service.CategorieService;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@CrossOrigin("*")
@Tag(name = "Catégories de pièces", description = "API de gestion des catégories de pièces détachées")
public class CategorieController {

    private final CategorieService categorieService;

    @GetMapping
    @Operation(summary = "Récupérer la liste des catégories (paginée ou complète)")
    public ResponseEntity<?> getAll(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        if (page != null && size != null) {
            if (keyword != null && !keyword.trim().isEmpty()) {
                Page<CategorieResponse> result = categorieService.searchCategories(keyword.trim(), page, size)
                        .map(CategorieResponse::from);
                return ResponseEntity.ok(result);
            }
            Page<CategorieResponse> result = categorieService.getAllCategories(page, size)
                    .map(CategorieResponse::from);
            return ResponseEntity.ok(result);
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            List<CategorieResponse> list = categorieService.searchCategories(keyword.trim()).stream()
                    .map(CategorieResponse::from)
                    .toList();
            return ResponseEntity.ok(list);
        }
        List<CategorieResponse> list = categorieService.getAllCategories().stream()
                .map(CategorieResponse::from)
                .toList();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer une catégorie par son ID")
    public ResponseEntity<CategorieResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(CategorieResponse.from(categorieService.getCategorieById(id)));
    }

    @GetMapping("/depot/{depotId}")
    @Operation(summary = "Récupérer les catégories d'un dépôt spécifique (paginée ou complète)")
    public ResponseEntity<?> getByDepotId(
            @PathVariable Long depotId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        if (page != null && size != null) {
            Page<CategorieResponse> result = categorieService.getCategoriesByDepotId(depotId, page, size)
                    .map(CategorieResponse::from);
            return ResponseEntity.ok(result);
        }
        List<CategorieResponse> list = categorieService.getCategoriesByDepotId(depotId).stream()
                .map(CategorieResponse::from)
                .toList();
        return ResponseEntity.ok(list);
    }

    @PostMapping
    @Operation(summary = "Créer une nouvelle catégorie")
    public ResponseEntity<CategorieResponse> create(@Valid @RequestBody CategorieRequest request) {
        Categorie created = categorieService.createCategorie(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(CategorieResponse.from(created));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour une catégorie")
    public ResponseEntity<CategorieResponse> update(@PathVariable Long id, @Valid @RequestBody CategorieRequest request) {
        Categorie updated = categorieService.updateCategorie(id, request);
        return ResponseEntity.ok(CategorieResponse.from(updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une catégorie")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        categorieService.deleteCategorie(id);
        return ResponseEntity.noContent().build();
    }
}
