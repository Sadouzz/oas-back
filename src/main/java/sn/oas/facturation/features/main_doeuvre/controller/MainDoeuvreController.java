package sn.oas.facturation.features.main_doeuvre.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.oas.facturation.features.main_doeuvre.data.entity.MainDoeuvre;
import sn.oas.facturation.features.main_doeuvre.dto.MainDoeuvreRequest;
import sn.oas.facturation.features.main_doeuvre.service.MainDoeuvreService;
import java.util.List;

@RestController
@RequestMapping("/api/main-doeuvres")
@RequiredArgsConstructor
@Tag(name = "Main d'œuvre", description = "API pour la gestion de la main d'œuvre")
public class MainDoeuvreController {

    private final MainDoeuvreService mainDoeuvreService;

    @GetMapping
    @Operation(summary = "Lister toutes les main d'œuvres avec pagination ou rechercher par mot-clé")
    public ResponseEntity<?> getAllMainDoeuvres(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            return ResponseEntity.ok(mainDoeuvreService.searchMainDoeuvres(keyword.trim(), page, size));
        }
        return ResponseEntity.ok(mainDoeuvreService.getAllMainDoeuvres(page, size));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer une main d'œuvre par son ID")
    public ResponseEntity<MainDoeuvre> getMainDoeuvreById(@PathVariable Long id) {
        return ResponseEntity.ok(mainDoeuvreService.getMainDoeuvreById(id));
    }

    @PostMapping
    @Operation(summary = "Créer une nouvelle main d'œuvre")
    public ResponseEntity<MainDoeuvre> createMainDoeuvre(@RequestBody MainDoeuvreRequest request) {
        MainDoeuvre created = mainDoeuvreService.createMainDoeuvre(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour une main d'œuvre")
    public ResponseEntity<MainDoeuvre> updateMainDoeuvre(@PathVariable Long id,
            @RequestBody MainDoeuvreRequest request) {
        return ResponseEntity.ok(mainDoeuvreService.updateMainDoeuvre(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une main d'œuvre")
    public ResponseEntity<Void> deleteMainDoeuvre(@PathVariable Long id) {
        mainDoeuvreService.deleteMainDoeuvre(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/archive")
    @Operation(summary = "Archiver ou désarchiver une main d'œuvre")
    public ResponseEntity<MainDoeuvre> archiveMainDoeuvre(@PathVariable Long id,
        @RequestParam boolean archived) {
        return ResponseEntity.ok(mainDoeuvreService.archiveMainDoeuvre(id, archived));
    }
}