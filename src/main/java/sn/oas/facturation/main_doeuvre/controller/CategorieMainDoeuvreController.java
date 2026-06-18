package sn.oas.facturation.main_doeuvre.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.oas.facturation.main_doeuvre.data.entity.CategorieMainDoeuvre;
import sn.oas.facturation.main_doeuvre.dto.CategorieMainDoeuvreRequest;
import sn.oas.facturation.main_doeuvre.service.CategorieMainDoeuvreService;

import java.util.List;

@RestController
@RequestMapping("/api/categorie-main-doeuvres")
@RequiredArgsConstructor
@CrossOrigin("*") // Or follow your CORS policy
public class CategorieMainDoeuvreController {

    private final CategorieMainDoeuvreService categorieService;

    @GetMapping
    public ResponseEntity<List<CategorieMainDoeuvre>> getAll() {
        return ResponseEntity.ok(categorieService.getAllCategories());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategorieMainDoeuvre> getById(@PathVariable Long id) {
        return ResponseEntity.ok(categorieService.getCategorieById(id));
    }

    @PostMapping
    public ResponseEntity<CategorieMainDoeuvre> create(@Valid @RequestBody CategorieMainDoeuvreRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categorieService.createCategorie(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategorieMainDoeuvre> update(@PathVariable Long id, @Valid @RequestBody CategorieMainDoeuvreRequest request) {
        return ResponseEntity.ok(categorieService.updateCategorie(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        categorieService.deleteCategorie(id);
        return ResponseEntity.noContent().build();
    }
}
