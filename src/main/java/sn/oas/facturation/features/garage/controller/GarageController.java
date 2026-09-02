package sn.oas.facturation.features.garage.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.oas.facturation.features.garage.dto.GarageRequest;
import sn.oas.facturation.features.garage.dto.GarageResponse;
import sn.oas.facturation.features.garage.service.GarageService;

import java.util.List;

@RestController
@RequestMapping("/api/admin/garages")
@RequiredArgsConstructor
@Tag(name = "Gestion des garages", description = "API pour la gestion des garages")
public class GarageController {

    private final GarageService garageService;

    @PostMapping
    @Operation(summary = "Créer un nouveau garage")
    public ResponseEntity<GarageResponse> createGarage(@RequestBody GarageRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(GarageResponse.from(garageService.createGarage(request)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour un garage existant")
    public ResponseEntity<GarageResponse> updateGarage(@PathVariable Long id, @RequestBody GarageRequest request) {
        return ResponseEntity.ok(GarageResponse.from(garageService.updateGarage(id, request)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtenir les détails d'un garage")
    public ResponseEntity<GarageResponse> getGarageById(@PathVariable Long id) {
        return ResponseEntity.ok(GarageResponse.from(garageService.getGarageById(id)));
    }

    @GetMapping
    @Operation(summary = "Lister tous les garages avec pagination")
    public ResponseEntity<?> getAllGarages(
            @RequestParam(defaultValue = "false") boolean includeArchived,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(garageService.getAllGarages(includeArchived, page, size).map(GarageResponse::from));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Archiver un garage")
    public ResponseEntity<Void> deleteGarage(@PathVariable Long id) {
        garageService.deleteGarage(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/restore")
    @Operation(summary = "Restaurer un garage archivé")
    public ResponseEntity<Void> restoreGarage(@PathVariable Long id) {
        garageService.restoreGarage(id);
        return ResponseEntity.ok().build();
    }
}
